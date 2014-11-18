package org.lumeh.routemaster;

import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import com.astuetz.PagerSlidingTabStrip;
import com.google.common.collect.ImmutableList;
import org.lumeh.routemaster.FragmentListPagerAdapter.TabEntry;
import org.lumeh.routemaster.friends.FriendsFragment;
import org.lumeh.routemaster.history.HistoryFragment;
import org.lumeh.routemaster.record.RecordFragment;

public class MainActivity extends RouteMasterActivity {
    private static final String TAG = "RouteMaster";
    private static final String STATE_SELECTED_TAB_ID = "selectedTabId";
    private static final String TAG_RECORD_FRAGMENT = "recordFragment";
    private static final String TAG_HISTORY_FRAGMENT = "historyFragment";
    private static final String TAG_FRIENDS_FRAGMENT = "friendsFragment";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.main);
        getSupportFragmentManager().executePendingTransactions();

        if(state == null) {
            onInitialCreate();
        } else {
            Log.i(TAG, "device configuration changed - recreating");
        }

        addTabs();
    }

    /**
     * Called when onCreate is called for the first time. This may show
     * warnings, errors, and create the initial fragments for the activity.
     */
    protected void onInitialCreate() {
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

    /**
     * Add a record and history tab, with their correct labels.
     */
    protected void addTabs() {
        FragmentListPagerAdapter adapter = new FragmentListPagerAdapter(
            getSupportFragmentManager(),
            new TabEntry("Record", RecordFragment.class),
            new TabEntry("History", HistoryFragment.class),
            new TabEntry("Friends", FriendsFragment.class)
        );

        ViewPager pager = (ViewPager) findViewById(R.id.main);
        pager.setAdapter(adapter);

        PagerSlidingTabStrip tabs =
            (PagerSlidingTabStrip) findViewById(R.id.main_tabs);
        tabs.setViewPager(pager);
    }
}
