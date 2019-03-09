package com.rain.networkproxy;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.helper.RxUtils;
import com.rain.networkproxy.helper.StreamUtils;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.InternalResponse;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.model.SocketMessage;
import com.rain.networkproxy.model.SocketMessageParser;
import com.rain.networkproxy.ui.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * ServerThread to sync data with Desktop app
 */
final class ServerThread extends Thread {
    static final int NO_PORT = -1;

    private final int port;
    private final Gson gson;
    private final Dispatcher<NPCommand> dispatcher;
    private final StateProvider<NPState> stateProvider;
    private final SocketMessageParser socketParser;

    @Nullable
    private ServerSocket serverSocket;
    @Nullable
    private Disposable disposable;

    ServerThread(int port, Dispatcher<NPCommand> dispatcher, StateProvider<NPState> stateProvider) {
        this.port = port;
        this.dispatcher = dispatcher;
        this.stateProvider = stateProvider;
        this.gson = new Gson();
        this.socketParser = new SocketMessageParser(gson);
    }

    private void listenToPendingResponses(final DataOutputStream dataOutputStream) {
        RxUtils.dispose(disposable);
        disposable = stateProvider.state()
                .serialize()
                .observeOn(Schedulers.io())
                .map(NPState::getResponses)
                .distinctUntilChanged()
                .map(this::toInternalResponse)
                .map(internalResponses -> new SocketMessage<>(SocketMessage.INTERNAL_RESPONSES, internalResponses))
                .map(message -> gson.toJson(message, SocketMessage.class))
                .subscribe(data -> sendMessage(dataOutputStream, data), throwable ->
                        NPLogger.logError("NPProcess#listenToPendingResponses", throwable));
    }

    private List<InternalResponse> toInternalResponse(List<PendingResponse> pendingResponses) {
        return Observable.fromIterable(pendingResponses)
                .map(this::toInternalResponse)
                .toList()
                .blockingGet();
    }

    private InternalResponse toInternalResponse(PendingResponse pendingResponse) throws IOException {
        final Response response = pendingResponse.getResponse();
        final ResponseBody responseBody = response.body();

        return new InternalResponse(
                pendingResponse.getId(),
                response.request().url().url().toString(),
                responseBody == null ? null : Utils.readFromBuffer(response.headers(), responseBody),
                response.code()
        );
    }

    private void sendMessage(DataOutputStream outputStream, String message) throws IOException {
        NPLogger.log("Sending message: " + message);
        outputStream.writeUTF(message);
    }

    void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                NPLogger.logError("ServerThread#stopServer", e);
            } finally {
                NPLogger.log("Stop server");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleMessage(String message) {
        final SocketMessage socketMessage = socketParser.parseMessage(message);
        final Object payload = socketMessage.getPayload();

        if (payload instanceof Instruction) {
            dispatcher.dispatch(new NPCommand.ApplyInstructions(Collections.singletonList((Instruction) payload)));
        } else if (payload instanceof List<?>) {
            dispatcher.dispatch(new NPCommand.ApplyFilter((List<String>) payload));
        } else {
            throw new IllegalArgumentException("No support message");
        }
    }

    private void handleDisconnectClient() {
        NPLogger.log("Disconnecting Desktop App");
        dispatcher.dispatch(new NPCommand.ApplyFilter(Collections.emptyList()));
        dispatcher.dispatch(new NPCommand.SkipAllPendingResponse());
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            NPLogger.log("Server started");

            while (!serverSocket.isClosed()) {
                final Socket socket = serverSocket.accept();
                NPLogger.log("Client connected");
                DataOutputStream outputStream = null;
                DataInputStream inputStream = null;

                try {
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    inputStream = new DataInputStream(socket.getInputStream());
                    listenToPendingResponses(outputStream);

                    while (!socket.isClosed()) {
                        final String message = inputStream.readUTF();
                        NPLogger.log("Message: " + message);
                        handleMessage(message);
                    }
                } catch (EOFException e) {
                    NPLogger.logError("Connection", e);
                } finally {
                    StreamUtils.close(outputStream);
                    StreamUtils.close(inputStream);
                    NPLogger.log("Stop client");
                    handleDisconnectClient();
                }
            }
        } catch (IOException e) {
            NPLogger.logError("ServerThread", e);
        } finally {
            stopServer();
        }
    }
}
