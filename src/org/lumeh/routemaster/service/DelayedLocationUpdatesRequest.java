package org.lumeh.routemaster.service;

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Upon connecting to Google Play Services, submits a request for location
 * updates.
 * <p>
 * Uses some tricks described on:
 * http://www.rahuljiresal.com/2014/02/user-location-on-android/
 */
public class DelayedLocationUpdatesRequest
                     implements GoogleApiClient.ConnectionCallbacks,
                                GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "RouteMaster";
    private final GoogleApiClient apiClient;
    private final LocationRequest request;
    private final LocationListener listener;

    public DelayedLocationUpdatesRequest(GoogleApiClient apiClient,
                                         LocationRequest request,
                                         LocationListener listener) {
        this.apiClient = apiClient;
        this.request = request;
        this.listener = listener;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
            apiClient, request, listener
        );
        Log.d(TAG, "requested location updates");
    }

    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO: fall back to LocationManager
    }

    public void disconnect() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
            apiClient, listener
        );
    }
}
