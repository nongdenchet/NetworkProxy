package com.rain.networkproxy;

import android.content.Context;
import android.support.annotation.NonNull;

import com.rain.networkproxy.helper.BroadcastReceiverProcess;
import com.rain.networkproxy.helper.EventBus;
import com.rain.networkproxy.helper.NotificationHandler;
import com.rain.networkproxy.helper.RequestFilterProcess;
import com.rain.networkproxy.helper.ResourceProvider;
import com.rain.networkproxy.helper.ResourceProviderImpl;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.storage.FilterStorage;
import com.rain.networkproxy.storage.FilterStorageImpl;

import static com.rain.networkproxy.Constants.STORAGE;

public final class InstanceProvider {
    private static volatile InstanceProvider instance;

    private final NPProcess process = new NPProcess();
    private final EventBus eventBus = new EventBus();

    private volatile FilterStorage filterStorage;

    @NonNull
    public static InstanceProvider instance() {
        if (instance == null) {
            synchronized (InstanceProvider.class) {
                if (instance == null) {
                    instance = new InstanceProvider();
                }
            }
        }

        return instance;
    }

    RequestFilterProcess provideRequestFilterProcess(@NonNull Context context) {
        return new RequestFilterProcess(provideFilterStorage(context), provideDispatcher());
    }

    NotificationHandler provideNotificationHandler(@NonNull Context context) {
        return new NotificationHandler(context);
    }

    BroadcastReceiverProcess provideBroadcastReceiverProcess() {
        return new BroadcastReceiverProcess(provideDispatcher(), provideEventBus());
    }

    NPProcess provideProcess() {
        return process;
    }

    public FilterStorage provideFilterStorage(@NonNull Context context) {
        if (filterStorage == null) {
            synchronized (InstanceProvider.class) {
                if (filterStorage == null) {
                    filterStorage = new FilterStorageImpl(context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE));
                }
            }
        }

        return filterStorage;
    }

    EventBus provideEventBus() {
        return eventBus;
    }

    public ResourceProvider provideResourceProvider(@NonNull Context context) {
        return new ResourceProviderImpl(context);
    }

    public StateProvider<NPState> provideStateProvider() {
        return process;
    }

    public Dispatcher<NPCommand> provideDispatcher() {
        return process;
    }
}
