package com.rain.networkproxy;

import android.support.annotation.Nullable;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.model.PendingResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class NPProcess implements StateProvider<NPState>, Dispatcher<NPCommand> {
    static final int NO_PORT = -1;

    private final BehaviorSubject<NPState> state = BehaviorSubject.create();
    private final PublishSubject<NPCommand> commands = PublishSubject.create();
    private final AtomicInteger currentId = new AtomicInteger(0);
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    private ServerThread serverThread;

    void startProcess(int port) {
        stopProcess();
        if (port != NO_PORT) {
            serverThread = new ServerThread(port);
            serverThread.start();
        }
        disposables.add(bindCommands());
        disposables.add(bindState());
    }

    private Disposable bindState() {
        return state.serialize()
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
                        if (serverThread != null) {
                            serverThread.sendMessage(pendingResponses.toString());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("NPProcess#bindState", throwable);
                    }
                });
    }

    private Disposable bindCommands() {
        return commands.serialize()
                .scan(NPState.DEFAULT, new NPStateReducer())
                .subscribe(new Consumer<NPState>() {
                    @Override
                    public void accept(NPState npState) {
                        state.onNext(npState);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("NPProcess#bindCommands", throwable);
                    }
                });
    }

    int getNextId() {
        return currentId.getAndIncrement();
    }

    boolean isRunning() {
        return disposables.size() != 0;
    }

    @Override
    public Observable<NPState> state() {
        return state.hide();
    }

    @Override
    public NPState currentState() {
        final NPState state = this.state.getValue();
        return state != null ? state : NPState.DEFAULT;
    }

    @Override
    public void dispatch(NPCommand command) {
        commands.onNext(command);
    }

    private void stopProcess() {
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.stopServer();
            serverThread = null;
        }
        disposables.clear();
    }

    private class ServerThread extends Thread {
        private final int port;

        @Nullable
        private ServerSocket serverSocket;
        @Nullable
        private Socket socket;

        private ServerThread(int port) {
            this.port = port;
        }

        void sendMessage(String message) throws IOException {
            if (socket == null) {
                NPLogger.logError("ServerThread#sendMessage", new Throwable("connection not started"));
                return;
            }

            if (socket.isClosed()) {
                NPLogger.logError("ServerThread#sendMessage", new Throwable("connection is closed"));
                return;
            }

            final DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(message);
        }

        void stopServer() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    NPLogger.logError("ServerThread#stopServer", e);
                } finally {
                    NPLogger.log("Stop connection");
                }
            }
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

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                NPLogger.log("Server started");

                while (!serverSocket.isClosed()) {
                    final Socket socket = serverSocket.accept();
                    this.socket = socket;
                    NPLogger.log("Client connected");

                    try {
                        while (!socket.isClosed()) {
                            final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                            final String message = inputStream.readUTF();
                            NPLogger.log("Message: " + message);

                            if ("start".equals(message)) {
                                sendMessage(currentState().getResponses().toString());
                            } else {
                                final List<Instruction> instructions = Collections.singletonList(new Instruction(message, new Instruction.Input()));
                                commands.onNext(new NPCommand.ApplyInstructions(instructions));
                            }
                        }
                    } catch (EOFException e) {
                        NPLogger.logError("Connection", e);
                    } finally {
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
}
