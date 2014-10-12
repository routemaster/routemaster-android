package org.lumeh.routemaster.service;

import android.app.Service;
import android.os.Binder;

public class ServiceBinder<E extends Service> extends Binder {
    private final E service;

    public ServiceBinder(E service) {
        this.service = service;
    }

    public E getService() {
        return service;
    }
}
