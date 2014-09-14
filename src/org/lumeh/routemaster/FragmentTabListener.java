package org.lumeh.routemaster;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentTransaction;

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
