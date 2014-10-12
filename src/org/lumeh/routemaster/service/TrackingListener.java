package org.lumeh.routemaster.service;

import android.location.Location;
import org.lumeh.routemaster.models.Journey;

/**
 * Listen for events while tracking the users. This allows the TrackingService
 * to communicate updates to the RecordFragment.
 */
public interface TrackingListener {
    /**
     * Called when a route first starts being tracked, given an empty journey.
     * This is the same object that will be passed to onLocationChanged and
     * onStop.
     */
    public void onStart(Journey journey);

    /**
     * Called every time a new GPS location is detected, changing the journey.
     */
    public void onUpdate(Location loc, Journey journey);

    /**
     * Called when an unrecoverable error happened. Depending on the exact
     * error, the application should likely respond differently.
     * <p>
     * When this happens, tracking will be stopped.
     */
    public void onError(TrackingError err);

    /**
     * Called when tracking is stopped. If an error occured, this will be called
     * immediately after onError.
     */
    public void onStop(Journey journey);

    /**
     * What could go wrong?
     */
    public enum TrackingError {
        GPS_OFF,
        PLAY_SERVICES_FAILED,
        INVALID_JOURNEY
    };
}
