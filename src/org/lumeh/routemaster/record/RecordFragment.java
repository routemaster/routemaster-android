package org.lumeh.routemaster.record;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import org.lumeh.routemaster.R;

public class RecordFragment extends Fragment {
    private static final String TAG_MAP_FRAGMENT = "mapFragment";

    private MapFragment mapFragment;
    private boolean mapIsConfigured;

    public void onCreate(Bundle state) {
        super.onCreate(state);
    }

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

        // ignore if we're called on re-attach
        if(getMapFragment() != null) {
            return;
        }

        if(state == null) {
            MapFragment mapFragment = MapFragment.newInstance(
                new GoogleMapOptions().zoomControlsEnabled(false)
            );
            getChildFragmentManager().beginTransaction()
                .add(R.id.record, mapFragment, TAG_MAP_FRAGMENT)
                .commit();
        }
        mapIsConfigured = false;
    }

    public MapFragment getMapFragment() {
        return (MapFragment) getChildFragmentManager()
            .findFragmentByTag(TAG_MAP_FRAGMENT);
    }

    public GoogleMap getMap() {
        return getMapFragment().getMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        // we have to wait until start, because the MapFragment creates the
        // underlying map in its onCreateView method.
        if(!mapIsConfigured) {
            configureMap();
            mapIsConfigured = true;
        }
    }

    /**
     * Gets called *after* the map fragment's underlying GoogleMap instance is
     * contructed. This happens when the fragment transaction is executed.
     * {@code map} should not be null at this point.
     * <p>
     * We can use {@link GoogleMapOptions} for some things, but it's incomplete.
     */
    private void configureMap() {
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setMyLocationButtonEnabled(true);
        // TODO: Figure out some way of calling setRetainInstance(true). This
        // would allow us to handle rotation without any flashing of the map.
        // Android doesn't support retaining nested fragments.
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
