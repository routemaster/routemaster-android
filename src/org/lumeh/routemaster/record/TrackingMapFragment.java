package org.lumeh.routemaster.record;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.Collections;
import java.util.List;

public class TrackingMapFragment extends MapFragment {
    private static final String TAG = "RouteMaster";
    private static PolylineOptions ROUTE_OPTIONS = new PolylineOptions()
        .color(0xff33b5e5) // light holo blue
        .width(7); // screen pixels

    private boolean isConfigured = false;
    private Polyline route = null;
    private List<LatLng> routePointsCache = Collections.emptyList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle state) {
        View superView = super.onCreateView(inflater, container, state);

        // The internal GoogleMap object should now be instantiated
        if(getMap() == null) {
            Log.e(TAG, "getMap() is returning null. Are Google Play Services " +
                       "unavailable?");
            return superView;
        }

        // don't reconfigure the map if onCreateView is called multiple times
        if(isConfigured) {
            return superView;
        }

        // customize the map ui
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setMyLocationButtonEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(false);

        // add the route
        route = getMap().addPolyline(ROUTE_OPTIONS);
        route.setPoints(routePointsCache);
        routePointsCache = null;

        isConfigured = true;
        return superView;
    }

    /**
     * Sets the points on the route, if the route exists, otherwise it
     * caches the points until the route is constructed.
     */
    public void setRoutePoints(List<LatLng> points) {
        if(route != null) {
            route.setPoints(points);
        } else {
            routePointsCache = points;
        }
    }
}
