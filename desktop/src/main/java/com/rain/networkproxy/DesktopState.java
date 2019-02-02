package com.rain.networkproxy;

import com.rain.networkproxy.model.InternalResponse;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class DesktopState {
    private final BehaviorSubject<List<InternalResponse>> responses = BehaviorSubject.create();

    public void setResponses(List<InternalResponse> responses) {
        this.responses.onNext(responses);
    }

    Observable<List<InternalResponse>> getResponses() {
        return responses.hide().serialize()
                .distinctUntilChanged();
    }
}
