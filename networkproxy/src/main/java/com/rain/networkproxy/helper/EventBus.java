package com.rain.networkproxy.helper;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class EventBus {
    private final PublishSubject<Event> events = PublishSubject.create();

    public interface Event {}

    public void dispatch(Event event) {
        events.onNext(event);
    }

    public Observable<Event> observeEvents() {
        return events.serialize();
    }
}
