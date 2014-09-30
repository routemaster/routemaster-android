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
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import org.lumeh.routemaster.R;
import org.lumeh.routemaster.models.Account;
import org.lumeh.routemaster.models.Journey;
import org.lumeh.routemaster.models.TrackingConfig;
import org.lumeh.routemaster.server.Uploader;

public class RecordFragment extends Fragment implements LocationListener {
    private static final String TAG = "RouteMaster";

    private static final String TAG_MAP_FRAGMENT = "mapFragment";
    private static final String TAG_JOURNEY = "journey";
    private static final String TAG_JOURNEY_LATLNGS = "journeyLatLngs";

    private TrackingConfig trackingConfig;
    private final LocationRequest locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private GoogleApiClient googleApiClient;
    private TrackingMapFragment mapFragment;
    private LocationGoogleApiClientCallbacks locationCallbacks;

    private Journey journey = new Journey(new Account());

    /**
     * Store visited locations as LatLng, the data format Polyline requires,
     * avoiding converting the Locations on every update.
     */
    private ArrayList<LatLng> journeyLatLngs = new ArrayList<>();

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
            journey = state.getParcelable(TAG_JOURNEY);
            journeyLatLngs = state.getParcelableArrayList(TAG_JOURNEY_LATLNGS);
            getMapFragment().setRoutePoints(journeyLatLngs);
        }
    }

    public void onStart() {
        super.onStart();

        // get the TrackingConfig (TODO: should pull from server)
        if(trackingConfig == null) {
            trackingConfig = new TrackingConfig(1000,   // pollingIntervalMs
                                                10.0f,  // geofencingDistanceM
                                                20.0f,  // minAccuracyM
                                                40.0f); // maxDistanceM

            // update locationRequest
            locationRequest
                .setInterval(trackingConfig.getPollingIntervalMs())
                .setSmallestDisplacement(
                    trackingConfig.getGeofencingDistanceM()
                );
        }

        // asynchronously request location updates upon connecting to Google
        // Play Services
        if(locationCallbacks == null) {
            locationCallbacks = new LocationGoogleApiClientCallbacks(
                googleApiClient, locationRequest, this
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
        if(loc.getAccuracy() > trackingConfig.getMinAccuracyM()) {
            Log.d(TAG, "ignoring inaccurate location");
            return;
        }

        ImmutableList<Location> waypoints = journey.getWaypoints();
        if(waypoints.size() > 0) {
            Location prevLoc = waypoints.get(waypoints.size() - 1);
            if(prevLoc.distanceTo(loc) > trackingConfig.getMaxDistanceM()) {
                // TODO: actually discard
                Log.w(TAG, "route has a large jump, should be discarded");
            }
        }

        journey.addWaypoint(loc);
        journeyLatLngs.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        getMapFragment().setRoutePoints(journeyLatLngs);

        // FIXME: Delete all of this:
        Uploader up = new Uploader();
        up.add(journey);
        up.uploadAll();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(TAG_JOURNEY, journey);
        state.putParcelableArrayList(TAG_JOURNEY_LATLNGS, journeyLatLngs);
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
