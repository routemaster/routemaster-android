package org.lumeh.routemaster.record;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.lumeh.routemaster.R;
import org.lumeh.routemaster.RouteMasterFragment;
import org.lumeh.routemaster.models.Journey;
import org.lumeh.routemaster.models.TrackingConfig;
import org.lumeh.routemaster.service.ServiceBinder;
import org.lumeh.routemaster.service.TrackingListener.TrackingError;
import org.lumeh.routemaster.service.TrackingListener;
import org.lumeh.routemaster.service.TrackingService;

public class RecordFragment extends RouteMasterFragment {
    private static final String TAG = "RouteMaster";

    private static final String TAG_MAP_FRAGMENT = "mapFragment";

    private Button startStopButton;
    private TrackingConfig trackingConfig;
    private TrackingMapFragment mapFragment;
    private TrackingServiceConnection trackingServiceConnection =
        new TrackingServiceConnection();
    private TrackingListener trackingListener = new RecordTrackingListener();

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
                .replace(R.id.map, mapFragment, TAG_MAP_FRAGMENT)
                .commit();
        }

        // grab the start/stop button for later. Functionality is attached
        // in TrackingServiceConnection.onServiceConnected()
        startStopButton = (Button) getView().findViewById(R.id.startstop);
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

    @Override
    public void onStop() {
        super.onStop();
        if(trackingServiceConnection.getService().isPresent()) {
            trackingServiceConnection.getService().get()
                .unregisterTrackingListener(trackingListener);
        }
        getActivity().unbindService(trackingServiceConnection);
    }

    protected void updateMap(Journey journey) {
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
    }

    protected class TrackingServiceConnection implements ServiceConnection {
        private Optional<TrackingService> service = Optional.absent();

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = Optional.of(
                ((ServiceBinder<TrackingService>) binder).getService()
            );
            service.get().registerTrackingListener(trackingListener);

            // Initialize the startstop button
            startStopButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if(service.get().getIsTracking()) {
                        service.get().stopTracking();
                        startStopButton.setText("Start");
                    } else {
                        service.get().startTracking();
                        startStopButton.setText("Stop");
                    }
                }
            });
            startStopButton.setText(
                service.get().getIsTracking() ? "Stop" : "Start"
            );
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
        public void onStart(Journey journey) {
            updateMap(journey);
        }

        @Override
        public void onUpdate(Location loc, Journey journey) {
            updateMap(journey);
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
