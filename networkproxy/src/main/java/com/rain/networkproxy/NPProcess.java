package com.rain.networkproxy;

import android.support.annotation.Nullable;

import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.NPState;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

final class NPProcess implements StateProvider<NPState>, Dispatcher<NPCommand> {
    private static final String TAG = "NPProcess";

    private final BehaviorSubject<NPState> state = BehaviorSubject.create();
    private final PublishSubject<NPCommand> commands = PublishSubject.create();
    private final AtomicInteger currentId = new AtomicInteger(0);

    @Nullable
    private Disposable disposable;

    void startProcess() {
        stopProcess();
        disposable = commands.scan(NPState.DEFAULT, new NPStateReducer())
                .subscribe(new Consumer<NPState>() {
                    @Override
                    public void accept(NPState npState) {
                        state.onNext(npState);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError(TAG, throwable);
                    }
                });
    }

    int getNextId() {
        return currentId.getAndIncrement();
    }

    boolean isRunning() {
        if (disposable == null) {
            return false;
        }

        return !disposable.isDisposed();
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
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }
}
