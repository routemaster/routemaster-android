package org.lumeh.routemaster;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.ImmutableList;
import org.lumeh.routemaster.history.HistoryFragment;
import org.lumeh.routemaster.record.RecordFragment;

public class MainActivity extends Activity {
    private static final String TAG = "RouteMaster";
    private static final String STATE_SELECTED_TAB_ID = "selectedTabId";
    private static final String TAG_RECORD_FRAGMENT = "recordFragment";
    private static final String TAG_HISTORY_FRAGMENT = "historyFragment";

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        GoogleApiClientCallbacks cb = new GoogleApiClientCallbacks();
        googleApiClient = new GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(cb)
            .addOnConnectionFailedListener(cb)
            .build();

        setContentView(R.layout.main);
        if(state == null) {
            onInitialCreate();
        } else {
            Log.i(TAG, "device configuration changed - recreating");
        }

        addTabs();

        // select the previous tab, if there was one
        if(state != null) {
            int tabId = state.getInt(STATE_SELECTED_TAB_ID);
            getActionBar().setSelectedNavigationItem(tabId);
        }

        getRecordFragment().setGoogleApiClient(googleApiClient);
    }

    /**
     * Called when onCreate is called for the first time. This may show
     * warnings, errors, and create the initial fragments for the activity.
     */
    protected void onInitialCreate() {
        Log.i(TAG, "Welcome to RouteMaster!");

        // create fragments
        FragmentManager fm = getFragmentManager();
        Fragment recordFragment = new RecordFragment();
        Fragment historyFragment = new HistoryFragment();
        fm.beginTransaction()
            .add(R.id.main, recordFragment, TAG_RECORD_FRAGMENT)
            .add(R.id.main, historyFragment, TAG_HISTORY_FRAGMENT)
            .commit();

        // TODO: add better error handling
        LocationManager manager =
            (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(
                this,
                "GPS is disabled. Please re-enable it.",
                Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    public RecordFragment getRecordFragment() {
        return (RecordFragment) getFragmentManager()
            .findFragmentByTag(TAG_RECORD_FRAGMENT);
    }

    public HistoryFragment getHistoryFragment() {
        return (HistoryFragment) getFragmentManager()
            .findFragmentByTag(TAG_HISTORY_FRAGMENT);
    }

    /**
     * Add a record and history tab, with their correct labels.
     */
    protected void addTabs() {
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // fragment transactions are executed async in parallel with the main
        // thread. getRecordFragment() and getHistoryFragment() pull the
        // fragments from the view hierarchy.
        getFragmentManager().executePendingTransactions();

        addTab("Record", getRecordFragment()).select();
        addTab("History", getHistoryFragment());
    }

    protected Tab addTab(String label, Fragment fragment) {
        getFragmentManager()
            .beginTransaction()
            .detach(fragment)
            .commit();
        Tab tab = getActionBar().newTab()
            .setText(label)
            .setTabListener(new FragmentTabListener(fragment));
        getActionBar().addTab(tab);
        return tab;
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        FragmentManager fm = getFragmentManager();
        int tabId = getActionBar().getSelectedNavigationIndex();
        state.putInt(STATE_SELECTED_TAB_ID, tabId);
    }

    /**
     * Dummy logging callbacks for the GoogleApiClient. The activity doesn't
     * have to do anything to handle these events, although RecordFragment does.
     * <p>
     * Eventually, this should provide some nicer error-handling.
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
