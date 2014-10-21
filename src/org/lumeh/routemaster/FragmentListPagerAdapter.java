package org.lumeh.routemaster;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * A {@link FragmentPagerAdapter} backed by an {@link ImmutableList} of
 * {@link TabEntry}s.
 */
public class FragmentListPagerAdapter extends FragmentPagerAdapter {
    final ImmutableList<TabEntry> tabs;

    public FragmentListPagerAdapter(FragmentManager fragmentManager,
                                    TabEntry... tabs) {
        this(fragmentManager, ImmutableList.copyOf(tabs));
    }

    public FragmentListPagerAdapter(FragmentManager fragmentManager,
                                    List<TabEntry> tabs) {
        super(fragmentManager);
        this.tabs = ImmutableList.copyOf(tabs);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).getTitle();
    }

    @Override
    public Fragment getItem(int position) {
        // This is introspection-based (slow), but won't be called very often
        try {
            return tabs.get(position).getFragmentClass().newInstance();
        } catch(InstantiationException|IllegalAccessException ex) {
            throw new RuntimeException(
                "This should never happen -- All fragments should have " +
                "default constructors",
                ex
            );
        }
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    public static class TabEntry {
        final CharSequence title;
        final Class<? extends Fragment> fragment;

        public TabEntry(CharSequence title,
                        Class<? extends Fragment> fragment) {
            this.title = title;
            this.fragment = fragment;
        }

        public CharSequence getTitle() {
            return title;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return fragment;
        }
    }
}
