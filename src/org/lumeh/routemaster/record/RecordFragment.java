package org.lumeh.routemaster.record;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import org.lumeh.routemaster.R;

public class RecordFragment extends Fragment implements LocationListener {
    private static final String TAG = "RouteMaster";

    private static final String TAG_MAP_FRAGMENT = "mapFragment";
    private static final String TAG_ROUTE_LOCATIONS = "routeLocations";
    private static final String TAG_ROUTE_POINTS = "routePoints";

    private static final LocationRequest LOCATION_REQUEST =
        LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(10.0f) // meters, saves power
            .setInterval(1000); // ms, actual interval may differ

    /**
     * If a location has a worse accuracy than this value, it's completely
     * discarded, because it's of no use to us. This is useful when the app is
     * first starting, so it doesn't use a bad value before the gps/wifi is
     * ready.
     */
    private static final float MIN_ACCURACY = 20.0f; // meters

    /**
     * If the distance between the current and last locations are larger than
     * this our recording is bad, and should be discarded. (TODO)
     * <p>
     * Santity check: 60 miles per hour is about 26.82 meters per second
     */
    private static final float MAX_DISTANCE = 40.0f; // meters

    private GoogleApiClient googleApiClient;
    private TrackingMapFragment mapFragment;
    private LocationGoogleApiClientCallbacks locationCallbacks;

    /**
     * Store the raw Location objects received from onLocationChanged, allowing
     * further processing later.
     */
    private ArrayList<Location> routeLocations = new ArrayList<>();

    /**
     * Store visited locations as LatLng, the data format Polyline requires,
     * avoiding converting the routeLocations structure on every update.
     */
    private ArrayList<LatLng> routePoints = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle state) {
        super.onCreateView(inflater, container, state);
        return inflater.inflate(R.layout.record, container, false);
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    /**
     * Created after the activity is created so we can call getFragment().
     * <p>
     * http://stackoverflow.com/q/19112641/130598
     */
    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        // find mapFragment in the view hierarchy
        if(mapFragment == null) {
            mapFragment = (TrackingMapFragment) getChildFragmentManager()
                .findFragmentByTag(TAG_MAP_FRAGMENT);
        }

        // mapFragment wasn't in the view hierarchy, so we need to create it
        if(mapFragment == null) {
            mapFragment = new TrackingMapFragment();
            getChildFragmentManager().beginTransaction()
                .add(R.id.record, mapFragment, TAG_MAP_FRAGMENT)
                .commit();
        }

        // restore from state
        if(state != null) {
            routeLocations = state.getParcelableArrayList(TAG_ROUTE_LOCATIONS);
            routePoints = state.getParcelableArrayList(TAG_ROUTE_POINTS);
            getMapFragment().setRoutePoints(routePoints);
        }
    }

    public void onStart() {
        super.onStart();
        // Asynchronously request location updates upon connecting to Google
        // Play Services
        if(locationCallbacks == null) {
            locationCallbacks = new LocationGoogleApiClientCallbacks(
                googleApiClient, LOCATION_REQUEST, this
            );
            googleApiClient.registerConnectionCallbacks(locationCallbacks);
            googleApiClient.registerConnectionFailedListener(locationCallbacks);
        }
    }

    public TrackingMapFragment getMapFragment() {
        return mapFragment;
    }

    @Override
    public void onLocationChanged(Location loc) {
        Log.d(TAG, "location update");

        // FIXME: When there's a low-accuracy reading followed by a
        // high-accuracy reading within 10m, the location never actually gets
        // read, because the LocationApi doesn't fill us on on better accuracy
        // readings. We need to make *another* LocationRequest when this happens
        // for a one-off reading.
        //
        // Another person with the same problem:
        // http://stackoverflow.com/q/22365188/130598
        if(loc.getAccuracy() > MIN_ACCURACY) {
            Log.d(TAG, "ignoring inaccurate location");
            return;
        }

        if(routeLocations.size() > 0) {
            Location prevLoc = routeLocations.get(routeLocations.size() - 1);
            if(prevLoc.distanceTo(loc) > MAX_DISTANCE) {
                // TODO: actually discard
                Log.w(TAG, "route has a large jump, should be discarded");
            }
        }

        routeLocations.add(loc);
        routePoints.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        getMapFragment().setRoutePoints(routePoints);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putParcelableArrayList(TAG_ROUTE_LOCATIONS, routeLocations);
        state.putParcelableArrayList(TAG_ROUTE_POINTS, routePoints);
    }

    /**
     * This is to work around a bug in Android:
     * http://stackoverflow.com/a/15656428/130598
     */
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            java.lang.reflect.Field childFragmentManager =
                Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
