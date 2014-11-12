package org.lumeh.routemaster;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * An extended version of {@link Fragment} that sets up dagger injection during
 * {@link RouteMasterFragment#onAttach}, and with a work-around for bugs in
 * detatching nested fragments.
 *
 * <p>Remember that if you extend this, you need to add yourself to
 * {@code MainApplication.MainModule}'s {@code injects} list.
 */
public class RouteMasterFragment extends Fragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainApplication) activity.getApplication()).inject(this);
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
