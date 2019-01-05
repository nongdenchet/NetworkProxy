package com.rain.networkproxy.helper;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public final class EventBus {
    private final PublishSubject<Event> events = PublishSubject.create();

    // Interface represent event of EventBus
    public interface Event {}

    public void dispatch(Event event) {
        events.onNext(event);
    }

    Observable<Event> observeEvents() {
        return events.serialize();
    }
}
