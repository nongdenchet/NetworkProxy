package com.rain.networkproxy;

import android.support.annotation.Nullable;
import android.util.Log;

import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.NPState;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

final class NPProcess implements StateProvider<NPState>, Dispatcher<NPCommand> {
    private static final String TAG = "NPProcess";
    private final BehaviorSubject<NPState> state = BehaviorSubject.create();
    private final PublishSubject<NPCommand> commands = PublishSubject.create();

    @Nullable
    private Disposable disposable;

    void startProcess() {
        stopProcess();
        disposable = commands.scan(NPState.DEFAULT, new NPStateReducer())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<NPState>() {
                    @Override
                    public void accept(NPState npState) {
                        state.onNext(npState);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e(Constants.TAG, TAG, throwable);
                    }
                });
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
