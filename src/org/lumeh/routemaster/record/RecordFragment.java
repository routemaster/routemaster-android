package org.lumeh.routemaster.record;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.lumeh.routemaster.R;
import org.lumeh.routemaster.models.Account;
import org.lumeh.routemaster.models.Journey;
import org.lumeh.routemaster.models.TrackingConfig;
import org.lumeh.routemaster.server.Uploader;
import org.lumeh.routemaster.service.ServiceBinder;
import org.lumeh.routemaster.service.TrackingListener.TrackingError;
import org.lumeh.routemaster.service.TrackingListener;
import org.lumeh.routemaster.service.TrackingService;

public class RecordFragment extends Fragment {
    private static final String TAG = "RouteMaster";

    private static final String TAG_MAP_FRAGMENT = "mapFragment";
    private static final String TAG_JOURNEY = "journey";
    private static final String TAG_JOURNEY_LATLNGS = "journeyLatLngs";

    private Journey journey;
    private TrackingConfig trackingConfig;
    private TrackingMapFragment mapFragment;
    private TrackingServiceConnection trackingServiceConnection =
        new TrackingServiceConnection();
    private TrackingListener trackingListener = new RecordTrackingListener();

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

        }

        Intent serviceIntent = new Intent(getActivity(), TrackingService.class)
            .putExtra(TrackingService.INTENT_TRACKING_CONFIG, trackingConfig);
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, trackingServiceConnection,
                                  getActivity().BIND_AUTO_CREATE);
    }

    public TrackingMapFragment getMapFragment() {
        return mapFragment;
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

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(TAG_JOURNEY, journey);
        state.putParcelableArrayList(TAG_JOURNEY_LATLNGS, journeyLatLngs);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(trackingServiceConnection.getService().isPresent()) {
            trackingServiceConnection.getService().get()
                .unregisterTrackingListener(trackingListener);
        }
        getActivity().unbindService(trackingServiceConnection);
    }

    protected class TrackingServiceConnection implements ServiceConnection {
        private Optional<TrackingService> service = Optional.absent();

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = Optional.of(
                ((ServiceBinder<TrackingService>) binder).getService()
            );
            service.get().registerTrackingListener(trackingListener);
        }

        public Optional<TrackingService> getService() {
            return service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This typically happens when the process hosting the service has
            // crashed or been killed.
            //
            // If this is the result of onStop, it's the job of onStop to call
            // unregisterTrackingListener for us.
        }
    }

    protected class RecordTrackingListener implements TrackingListener {
        @Override
        public void onStart(Journey journey) { }

        @Override
        public void onUpdate(Location loc, Journey journey) {
            // FIXME: When there's a low-accuracy reading followed by a
            // high-accuracy reading within 10m, the location never actually
            // gets read, because the LocationApi doesn't fill us on on better
            // accuracy readings. We need to make *another* LocationRequest when
            // this happens for a one-off reading.
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

            // draw the updated route
            getMapFragment().setRoutePoints(Lists.transform(
                journey.getWaypoints(),
                new Function<Location, LatLng>() {
                    @Override
                    public LatLng apply(Location loc) {
                        return new LatLng(loc.getLatitude(),
                                          loc.getLongitude());
                    }
                }
            ));

            // FIXME: Delete all of this:
            journey.setEndTimeUtc(loc.getTime());
            Uploader up = new Uploader();
            up.add(journey);
            up.uploadAll();
        }

        @Override
        public void onError(TrackingError err) {
            String msg = "Unknown error!";
            switch(err) {
                case GPS_OFF:
                    msg = "Please turn GPS on and try again";
                    break;
                case PLAY_SERVICES_FAILED:
                    msg = "Could not connect to Google Play Services";
                    break;
                case INVALID_JOURNEY:
                    msg = "Journey is no longer valid";
                    break;
            }

            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStop(Journey journey) { }
    }
}
