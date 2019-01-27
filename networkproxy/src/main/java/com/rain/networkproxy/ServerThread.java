package com.rain.networkproxy;

import android.support.annotation.Nullable;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.helper.RxUtils;
import com.rain.networkproxy.helper.StreamUtils;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.model.PendingResponse;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

final class ServerThread extends Thread {
    static final int NO_PORT = -1;
    private static final String START = "start";

    private final int port;
    private final Dispatcher<NPCommand> dispatcher;
    private final StateProvider<NPState> stateProvider;

    @Nullable
    private ServerSocket serverSocket;
    @Nullable
    private Disposable disposable;

    ServerThread(int port, Dispatcher<NPCommand> dispatcher, StateProvider<NPState> stateProvider) {
        this.port = port;
        this.dispatcher = dispatcher;
        this.stateProvider = stateProvider;
    }

    private void listenToPendingResponses(final DataOutputStream dataOutputStream) {
        RxUtils.dispose(disposable);
        disposable = stateProvider.state()
                .serialize()
                .map(new Function<NPState, List<PendingResponse>>() {
                    @Override
                    public List<PendingResponse> apply(NPState npState) {
                        return npState.getResponses();
                    }
                })
                .distinctUntilChanged()
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<PendingResponse>>() {
                    @Override
                    public void accept(List<PendingResponse> pendingResponses) throws Exception {
                        sendMessage(dataOutputStream, pendingResponses.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("NPProcess#listenToPendingResponses", throwable);
                    }
                });
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

    private void handleMessage(DataOutputStream dataOutputStream, String message) {
        if (START.equals(message)) {
            listenToPendingResponses(dataOutputStream);
        } else {
            final List<Instruction> instructions = Collections.singletonList(new Instruction(message, new Instruction.Input()));
            dispatcher.dispatch(new NPCommand.ApplyInstructions(instructions));
        }
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

                    while (!socket.isClosed()) {
                        final String message = inputStream.readUTF();
                        NPLogger.log("Message: " + message);
                        handleMessage(outputStream, message);
                    }
                } catch (EOFException e) {
                    NPLogger.logError("Connection", e);
                } finally {
                    StreamUtils.close(outputStream);
                    StreamUtils.close(inputStream);
                    NPLogger.log("Stop client");
                }
            }
        } catch (IOException e) {
            NPLogger.logError("ServerThread", e);
        } finally {
            stopServer();
        }
    }
}
