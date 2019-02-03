package com.rain.networkproxy.socket;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.rain.networkproxy.DesktopState;
import com.rain.networkproxy.model.FilterItem;
import com.rain.networkproxy.model.SocketMessage;
import com.rain.networkproxy.model.SocketMessageParser;
import com.rain.networkproxy.socket.handler.SocketHandler;
import com.rain.networkproxy.storage.FilterStorage;
import com.rain.networkproxy.utils.RxUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public final class SocketClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;

    private final Gson gson;
    private final FilterStorage filterStorage;
    private final SocketHandlerAdapter socketAdapter;
    private final SocketMessageParser socketParser;
    private final BehaviorSubject<SocketConnectionStatus> status;
    private final PublishSubject<Object> connectSignal;
    private final PublishSubject<String> writeMessage;

    @Nullable
    private Disposable signalDisposable;
    @Nullable
    private Disposable writeDisposable;
    @Nullable
    private Disposable syncFilterDisposable;

    public SocketClient(DesktopState state, FilterStorage filterStorage) {
        this.gson = new Gson();
        this.filterStorage = filterStorage;
        this.socketAdapter = new SocketHandlerAdapter(state);
        this.socketParser = new SocketMessageParser(gson);
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
                .switchMapCompletable(ignored -> makeConnection().onErrorComplete())
                .subscribe(Functions.EMPTY_ACTION);
    }

    public void stop() {
        RxUtils.dispose(signalDisposable);
        RxUtils.dispose(writeDisposable);
        RxUtils.dispose(syncFilterDisposable);
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
                .subscribe(dataOutputStream::writeUTF);
    }

    private void startSyncFilters() {
        RxUtils.dispose(syncFilterDisposable);
        syncFilterDisposable = filterStorage.getFilters()
                .switchMapSingle(filterItems -> Observable.fromIterable(filterItems)
                        .filter(FilterItem::isActive)
                        .map(FilterItem::getRule)
                        .toList())
                .map(rules -> new SocketMessage<>(SocketMessage.FILTER, rules))
                .distinctUntilChanged()
                .observeOn(Schedulers.io())
                .subscribe(message -> writeMessage(gson.toJson(message)));
    }

    @SuppressWarnings("unchecked")
    private Completable makeConnection() {
        final Completable completable = Completable.fromAction(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(HOST, PORT));
                status.onNext(SocketConnectionStatus.CONNECTED);
                System.out.println("Server connected");

                try (DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                     DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                    startListenToWriteMessage(outputStream);
                    startSyncFilters();

                    while (!socket.isClosed()) {
                        final String message = inputStream.readUTF();
                        System.out.println("Message: " + message);

                        final SocketMessage socketMessage = socketParser.parseMessage(message);
                        final SocketHandler handler = socketAdapter.getSocketHandler(socketMessage.getType());
                        handler.execute(socketMessage.getPayload());
                    }
                }
            }
        });

        return completable.doOnSubscribe(disposable -> status.onNext(SocketConnectionStatus.CONNECTING))
                .doOnTerminate(() -> {
                    System.out.println("Connection closed");
                    status.onNext(SocketConnectionStatus.DISCONNECTED);
                    RxUtils.dispose(writeDisposable);
                    RxUtils.dispose(syncFilterDisposable);
                });
    }
}
