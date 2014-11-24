package org.lumeh.routemaster.net;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.inject.Inject;
import org.lumeh.routemaster.RouteMasterService;
import org.lumeh.routemaster.models.Journey;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NetworkService extends RouteMasterService {
    private static final String TAG = "RouteMaster";

    @Inject Bus bus;
    @Inject RouteMasterApi api;
    @Inject Gson gson;

    /**
     * Calls outstanding that *must* be fulfilled before we'll let the
     * application fully exit.
     */
    private int criticalOutstandingCalls = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        bus.register(this);
        // TODO: kill the service when we have nothing left to do or when the
        // application is backgrounded and we have no criticalOutstandingCalls.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    public void onEvent(JourneyEvent.Post ev) {
        api.postJourney(
            ev.get(),
            new ResponseEventCallback<Journey>(JourneyEvent.Response.class, ev)
        );
    }

    @Subscribe
    public void onEvent(JourneyEvent.Get ev) {
        api.getJourney(
            ev.get(),
            new ResponseEventCallback<Journey>(JourneyEvent.Response.class, ev)
        );
    }

    @Subscribe
    public void onEvent(JourneyEvent.Response<?> ev) {
        Log.i(TAG, "Got Journey back from server: " + ev.get());
    }

    @Subscribe
    public void onEvent(RecentJourneysEvent.Get ev) {
        api.getRecentJourneys(
            ev.get(),
            new ResponseEventCallback<List<Journey>>(
                RecentJourneysEvent.Response.class, ev
            )
        );
    }

    @Subscribe
    public void onNetworkEvent(NetworkEvent.Post<?> ev) {
        criticalOutstandingCalls++;
        Log.d(TAG, "HTTP POST:\n" + gson.toJson(ev.get()));
    }

    @Subscribe
    public void onNetworkEvent(NetworkEvent.Response ev) {
        if(ev.getRequest() instanceof NetworkEvent.Post) {
            criticalOutstandingCalls--;
        }
        if(criticalOutstandingCalls < 0) {
            throw new RuntimeException("outstanding calls should not be < 0 !");
        } else if(criticalOutstandingCalls == 0) {
            // TODO: Actually implement startForeground somewhere
            stopForeground(true);
        }
    }

    @Subscribe
    public void onEvent(RetrofitError err) {
        Log.e(TAG, "Got RetrofitError: " + err.getMessage());
        // TODO: Add retry logic
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
    }

    /**
     * Upon being {@link #call}ed, generates an event (via reflection) and posts
     * it to the event bus.
     *
     * <p>TODO: There's no way to cancel pending callbacks upon onDestroy. This
     * is probably not an issue because if the NetworkService is stopped,
     * probably the process is too, but we should still handle this better,
     * especially in a way that doesn't inhibit GC of NetworkService.
     */
    private class ResponseEventCallback<T> implements Callback<T> {
        private Class<?> responseEventType;
        private NetworkEvent.Request<?> request;

        public ResponseEventCallback(Class<?> responseEventType,
                                     NetworkEvent.Request<?> request) {
            this.responseEventType = responseEventType;
        }

        public void success(T response, Response httpResponse) {
            try {
                Object ev = responseEventType
                    .getConstructors()[0]
                    .newInstance(response, request);
                bus.post(ev);
            } catch(InstantiationException|
                    IllegalAccessException|
                    InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void failure(RetrofitError error) {
            bus.post(error);
        }
    }
}
