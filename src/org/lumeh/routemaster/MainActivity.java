package org.lumeh.routemaster;

import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.os.Bundle;
import com.google.common.collect.ImmutableList;

public class MainActivity extends Activity {

    final TabListener dummyTabListener = new TabListener() {
        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Tab recordTab = actionBar.newTab()
                                 .setText("Record")
                                 .setTabListener(dummyTabListener);
        Tab historyTab = actionBar.newTab()
                                  .setText("History")
                                  .setTabListener(dummyTabListener);
        Tab friendsTab = actionBar.newTab()
                                  .setText("Friends")
                                  .setTabListener(dummyTabListener);
        actionBar.addTab(recordTab);
        actionBar.addTab(historyTab);
        actionBar.addTab(friendsTab);
        setContentView(R.layout.activity_main);

        // Something to test our dependencies
        ImmutableList.of(1, 2, 3);
    }
}
