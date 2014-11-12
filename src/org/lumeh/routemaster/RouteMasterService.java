package org.lumeh.routemaster;

import android.app.Service;

/**
 * An extended version of {@link Service} that sets up dagger injection during
 * {@link RouteMasterService#onCreate}.
 *
 * <p>Remember that if you extend this, you need to add yourself to
 * {@code MainApplication.MainModule}'s {@code injects} list.
 */
public abstract class RouteMasterService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        ((MainApplication) getApplication()).inject(this);
    }
}
