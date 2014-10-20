package org.lumeh.routemaster;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;

/**
 * Attach and detach fragments based on tab selections. The tab's state is still
 * managed in the view hierarchy, but is simply not visible. This survives
 * configuration changes.
 */
public class FragmentTabListener implements TabListener {
    private final Fragment fragment;

    public FragmentTabListener(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        ft.attach(fragment);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        ft.detach(fragment);
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
}
