package org.lumeh.routemaster.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import org.lumeh.routemaster.MainActivity;
import org.lumeh.routemaster.NotificationIds;
import org.lumeh.routemaster.R;
import org.lumeh.routemaster.models.Account;
import org.lumeh.routemaster.models.Journey;
import org.lumeh.routemaster.models.TrackingConfig;

/**
 * http://www.rahuljiresal.com/2014/02/user-location-on-android/
 */
public class TrackingService extends Service implements LocationListener {

    private static final String TAG = "RouteMaster";
    public static final String INTENT_TRACKING_CONFIG =
        TrackingConfig.class.getName();

    private final ServiceBinder<TrackingService> binder =
        new ServiceBinder<>(this);

    private final LocationRequest locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    /**
     * Absent if we haven't been started yet.
     */
    private Optional<TrackingConfig> trackingConfig = Optional.absent();

    /**
     * Absent if we're not currently tracking any route.
     */
    private Optional<Journey> journey = Optional.absent();

    private Optional<GoogleApiClient> apiClient = Optional.absent();
    private final ArrayList<TrackingListener> listeners = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(apiClient.isPresent()) {
            return START_NOT_STICKY;
        }

        // extract intent arguments
        trackingConfig = Optional.of((TrackingConfig)
            intent.getExtras().getParcelable(INTENT_TRACKING_CONFIG)
        );
        // TODO: Account is not parcelable
        journey = Optional.of(new Journey(new Account()));

        apiClient = Optional.of(
            new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()
        );

        startTracking();

        // add logging callbacks, eventually we'll add
        GoogleApiClientCallbacks callbacks = new GoogleApiClientCallbacks();
        apiClient.get().registerConnectionCallbacks(callbacks);
        apiClient.get().registerConnectionFailedListener(callbacks);

        startForeground();

        apiClient.get().connect();

        // TODO: use START_REDELIVER_INTENT and restore the journey somehow
        return START_NOT_STICKY;
    }

    protected void startTracking() {
        // update locationRequest with the trackingConfig
        locationRequest
            .setInterval(trackingConfig.get().getPollingIntervalMs())
            .setSmallestDisplacement(
                trackingConfig.get().getGeofencingDistanceM());

        DelayedLocationUpdatesRequest locationCallbacks =
            new DelayedLocationUpdatesRequest(
                apiClient.get(), locationRequest, this
            );
        apiClient.get().registerConnectionCallbacks(locationCallbacks);
        apiClient.get().registerConnectionFailedListener(locationCallbacks);
    }

    /**
     * Start as a foreground service to avoid being killed.
     */
    protected void startForeground() {
        Intent intent = new Intent(this, MainActivity.class);

        // don't create a new activity if one already exists
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 0
        );

        Notification notification = new Notification.Builder(this)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setTicker(getText(R.string.tracking_service_ticker))
            .setContentTitle(getText(R.string.tracking_service_title))
            .setContentText(getText(R.string.tracking_service_text))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
        startForeground(NotificationIds.TRACKING_SERVICE, notification);
    }

    @Override
    public ServiceBinder<TrackingService> onBind(Intent intent) {
        return binder;
    }

    /**
     * If tracking has already started, onStart is called immediately. If there
     * has already been at least one location update, onUpdate is called
     * immediately.
     */
    public void registerTrackingListener(TrackingListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));

        // If tracking has already started...
        if(journey.isPresent()) {
            for(TrackingListener l : listeners) {
                // ...onStart is called immediately.
                l.onStart(journey.get());
                // If there's already been at least one location update...
                if(journey.get().getLastWaypoint().isPresent()) {
                    l.onUpdate(journey.get().getLastWaypoint().get(),
                               journey.get());
                }
            }
        }
    }

    /**
     * Removes listeners from this TrackingService. Removing does not generate
     * any callbacks.
     * <p>
     * If the specified listeners are not registered, nothing happens.
     */
    public void unregisterTrackingListener(TrackingListener... listeners) {
        this.listeners.removeAll(Arrays.asList(listeners));
    }

    /**
     * The current journey, or Optional.absent() if not currently tracking.
     */
    public Optional<Journey> getJourney() {
        return journey;
    }

    /**
     * Intercepts the location update from LocationListener, adds it to the
     * Journey, and dispatches it to our TrackingListeners.
     */
    @Override
    public void onLocationChanged(Location loc) {
        // FIXME: When there's a low-accuracy reading followed by a
        // high-accuracy reading within 10m, the location never actually
        // gets read, because the LocationApi doesn't fill us on on better
        // accuracy readings. We need to make *another* LocationRequest when
        // this happens for a one-off reading.
        //
        // Another person with the same problem:
        // http://stackoverflow.com/q/22365188/130598
        if(loc.getAccuracy() > trackingConfig.get().getMinAccuracyM()) {
            Log.d(TAG, "ignoring inaccurate location");
            return;
        }

        ImmutableList<Location> waypoints = journey.get().getWaypoints();
        if(waypoints.size() > 0) {
            Location prevLoc = waypoints.get(waypoints.size() - 1);
            float distanceM = prevLoc.distanceTo(loc);
            if(distanceM > trackingConfig.get().getMaxDistanceM()) {
                // TODO: actually discard
                Log.w(TAG, "route has a large jump, should be discarded");
            }
        }

        journey.get().addWaypoint(loc);
        for(TrackingListener l : listeners) {
            l.onUpdate(loc, journey.get());
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        apiClient.get().disconnect();
        apiClient = Optional.absent();
    }

    /**
     * A stub that exposes TrackingService to bound Activities.
     */
    public class TrackingServiceBinder extends Binder {
        TrackingService getService() {
            return TrackingService.this;
        }
    }

    /**
     * Dummy logging callbacks for the GoogleApiClient. TODO: Error handling.
     */
    private class GoogleApiClientCallbacks
                         implements GoogleApiClient.ConnectionCallbacks,
                                    GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.i(TAG, "connected to google play services");
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.w(TAG, "connected suspended from google play services");
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.w(TAG, "failed to connect to google play services");
        }
    }
}
