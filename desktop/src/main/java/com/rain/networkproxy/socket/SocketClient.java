package com.rain.networkproxy.socket;

import com.rain.networkproxy.DesktopState;
import com.rain.networkproxy.model.SocketMessage;
import com.rain.networkproxy.socket.handler.SocketHandler;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
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
import java.net.Socket;

public final class SocketClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;

    private final SocketHandlerAdapter socketAdapter;
    private final SocketMessageParser socketParser;
    private final BehaviorSubject<Boolean> connected;
    private final PublishSubject<Object> connectSignal;
    private final PublishSubject<String> writeMessage;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public SocketClient(DesktopState state) {
        this.socketAdapter = new SocketHandlerAdapter(state);
        this.socketParser = new SocketMessageParser();
        this.connected = BehaviorSubject.createDefault(false);
        this.connectSignal = PublishSubject.create();
        this.writeMessage = PublishSubject.create();
    }

    final Observable<Boolean> isConnected() {
        return connected.hide().serialize();
    }

    public void start() {
        stop();
        listenToSignal();
        connect();
    }

    private void listenToSignal() {
        disposables.add(connectSignal.serialize()
                .observeOn(Schedulers.io())
                .switchMapCompletable(new Function<Object, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Object ignored) {
                        return makeConnection();
                    }
                })
                .subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

    public void stop() {
        disposables.clear();
    }

    public void connect() {
        if (connected.getValue()) {
            return;
        }

        connectSignal.onNext(new Object());
    }

    public void writeMessage(String message) {
        writeMessage.onNext(message);
    }

    private void startListenToWriteMessage(final DataOutputStream dataOutputStream) {
        disposables.add(writeMessage.serialize()
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
                }));
    }

    @SuppressWarnings("unchecked")
    private Completable makeConnection() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                try (Socket socket = new Socket(HOST, PORT)) {
                    System.out.println("Server connected");
                    connected.onNext(true);

                    final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    startListenToWriteMessage(dataOutputStream);

                    while (!socket.isClosed()) {
                        final String message = inputStream.readUTF();
                        System.out.println("Message: " + message);

                        final SocketMessage socketMessage = socketParser.parseMessage(message);
                        final SocketHandler handler = socketAdapter.getSocketHandler(socketMessage.getType());

                        handler.execute(socketMessage.getPayload());
                    }

                    inputStream.close();
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Connection closed");
                    connected.onNext(false);
                }
            }
        });
    }
}
