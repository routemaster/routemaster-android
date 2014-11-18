package org.lumeh.routemaster;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * An extended version of {@link ActionBarActivity} that sets up dagger
 * injection during {@link RouteMasterActivity#onCreate}.
 *
 * <p>Remember that if you extend this, you need to add yourself to
 * {@code MainApplication.MainModule}'s {@code injects} list.
 */
public class RouteMasterActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        ((MainApplication) getApplication()).inject(this);
    }
}
