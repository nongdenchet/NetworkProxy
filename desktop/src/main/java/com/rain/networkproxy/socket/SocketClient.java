package com.rain.networkproxy.socket;

import android.support.annotation.Nullable;
import com.rain.networkproxy.DesktopState;
import com.rain.networkproxy.model.SocketMessage;
import com.rain.networkproxy.socket.handler.SocketHandler;
import com.rain.networkproxy.utils.RxUtils;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class SocketClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;

    private final SocketHandlerAdapter socketAdapter;
    private final SocketMessageParser socketParser;
    private final BehaviorSubject<SocketConnectionStatus> status;
    private final PublishSubject<Object> connectSignal;
    private final PublishSubject<String> writeMessage;

    @Nullable
    private Disposable signalDisposable;
    @Nullable
    private Disposable writeDisposable;

    public SocketClient(DesktopState state) {
        this.socketAdapter = new SocketHandlerAdapter(state);
        this.socketParser = new SocketMessageParser();
        this.status = BehaviorSubject.createDefault(SocketConnectionStatus.DISCONNECTED);
        this.connectSignal = PublishSubject.create();
        this.writeMessage = PublishSubject.create();
    }

    public final Observable<SocketConnectionStatus> getStatus() {
        return status.hide()
                .serialize()
                .distinctUntilChanged();
    }

    public void start() {
        stop();
        listenToSignal();
        connect();
    }

    private void listenToSignal() {
        RxUtils.dispose(signalDisposable);
        signalDisposable = connectSignal.serialize()
                .observeOn(Schedulers.io())
                .switchMapCompletable(new Function<Object, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Object ignored) {
                        return makeConnection().onErrorComplete();
                    }
                })
                .subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    public void stop() {
        RxUtils.dispose(signalDisposable);
        RxUtils.dispose(writeDisposable);
    }

    public void connect() {
        if (status.getValue() == SocketConnectionStatus.CONNECTED) {
            return;
        }

        connectSignal.onNext(new Object());
    }

    public void writeMessage(String message) {
        writeMessage.onNext(message);
    }

    private void startListenToWriteMessage(final DataOutputStream dataOutputStream) {
        RxUtils.dispose(writeDisposable);
        writeDisposable = writeMessage.serialize()
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String message) throws Exception {
                        dataOutputStream.writeUTF(message);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private Completable makeConnection() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                 DataInputStream inputStream = null;
                 DataOutputStream outputStream = null;

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(HOST, PORT));
                    status.onNext(SocketConnectionStatus.CONNECTED);
                    System.out.println("Server connected");

                    inputStream = new DataInputStream(socket.getInputStream());
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    startListenToWriteMessage(outputStream);

                    while (!socket.isClosed()) {
                        final String message = inputStream.readUTF();
                        System.out.println("Message: " + message);

                        final SocketMessage socketMessage = socketParser.parseMessage(message);
                        final SocketHandler handler = socketAdapter.getSocketHandler(socketMessage.getType());
                        handler.execute(socketMessage.getPayload());
                    }
                } catch (IOException e) {
                    // No-op
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        // No-op
                    }
                    System.out.println("Connection closed");
                    status.onNext(SocketConnectionStatus.DISCONNECTED);
                }
            }
        }).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) {
                status.onNext(SocketConnectionStatus.CONNECTING);
            }
        }).doOnComplete(new Action() {
            @Override
            public void run() {
                RxUtils.dispose(writeDisposable);
            }
        });
    }
}
