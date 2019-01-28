package com.rain.networkproxy;

import com.rain.networkproxy.model.InternalResponse;
import com.rain.networkproxy.model.RequestFilter;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import java.util.List;

public final class DesktopState {
    private final BehaviorSubject<List<InternalResponse>> responses = BehaviorSubject.create();
    private final BehaviorSubject<RequestFilter> filter = BehaviorSubject.create();

    public void setResponses(List<InternalResponse> responses) {
        this.responses.onNext(responses);
    }

    public void setFilter(RequestFilter filter) {
        this.filter.onNext(filter);
    }

    public Observable<List<InternalResponse>> getResponses() {
        return responses.hide().serialize()
                .distinctUntilChanged();
    }

    public Observable<RequestFilter> getFilter() {
        return filter.hide().serialize()
                .distinctUntilChanged();
    }
}
