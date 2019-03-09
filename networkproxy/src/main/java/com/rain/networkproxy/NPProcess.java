package com.rain.networkproxy;

import android.support.annotation.Nullable;

import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.helper.RxUtils;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.NPState;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.rain.networkproxy.ServerThread.NO_PORT;

final class NPProcess implements StateProvider<NPState>, Dispatcher<NPCommand> {
    private final BehaviorSubject<NPState> state = BehaviorSubject.create();
    private final PublishSubject<NPCommand> commands = PublishSubject.create();
    private final AtomicInteger currentId = new AtomicInteger(0);

    @Nullable
    private Disposable disposable;
    @Nullable
    private ServerThread serverThread;

    void startProcess(int port) {
        stopProcess();
        startServer(port);
        disposable = bindCommands();
    }

    private void startServer(int port) {
        if (port != NO_PORT) {
            serverThread = new ServerThread(port, this, this);
            serverThread.start();
        }
    }

    private Disposable bindCommands() {
        return commands.serialize()
                .scan(NPState.DEFAULT, new NPStateReducer())
                .subscribe(state::onNext, throwable ->
                        NPLogger.logError("NPProcess#bindCommands", throwable));
    }

    int getNextId() {
        return currentId.getAndIncrement();
    }

    boolean isRunning() {
        return disposable != null && !disposable.isDisposed();
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
        if (serverThread != null) {
            serverThread.stopServer();
            serverThread = null;
        }
        RxUtils.dispose(disposable);
    }
}
