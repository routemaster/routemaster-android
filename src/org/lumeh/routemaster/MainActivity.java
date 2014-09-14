package org.lumeh.routemaster;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import com.google.common.collect.ImmutableList;
import org.lumeh.routemaster.history.HistoryFragment;
import org.lumeh.routemaster.record.RecordFragment;

public class MainActivity extends Activity {
    private static final String TAG = "RouteMaster";
    private static final String STATE_SELECTED_TAB_ID = "selectedTabId";
    private static final String TAG_RECORD_FRAGMENT = "recordFragment";
    private static final String TAG_HISTORY_FRAGMENT = "historyFragment";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        FragmentManager fm = getFragmentManager();

        setContentView(R.layout.main);
        if(state == null) {
            Log.i(TAG, "Welcome to RouteMaster!");
            Fragment recordFragment = new RecordFragment(),
                     historyFragment = new HistoryFragment();
            fm.beginTransaction()
                .add(R.id.main, recordFragment, TAG_RECORD_FRAGMENT)
                .add(R.id.main, historyFragment, TAG_HISTORY_FRAGMENT)
                .commit();
        } else {
            Log.i(TAG, "device configuration changed - recreating");
        }

        addTabs();

        // select the previous tab, if there was one
        if(state != null) {
            int tabId = state.getInt(STATE_SELECTED_TAB_ID);
            getActionBar().setSelectedNavigationItem(tabId);
        }

        // something to test our dependencies
        ImmutableList.of(1, 2, 3);
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
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        FragmentManager fm = getFragmentManager();
        int tabId = getActionBar().getSelectedNavigationIndex();
        state.putInt(STATE_SELECTED_TAB_ID, tabId);

        /*fm.putFragment(state, STATE_RECORD_FRAGMENT, recordFragment);
        fm.putFragment(state, STATE_HISTORY_FRAGMENT, historyFragment);*/
    }
}
