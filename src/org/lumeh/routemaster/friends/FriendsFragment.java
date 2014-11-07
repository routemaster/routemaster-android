package org.lumeh.routemaster.friends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import org.lumeh.routemaster.R;

public class FriendsFragment extends Fragment {
    private static final String TAG = "RouteMaster";
    private static final String TAG_FB_FRAGMENT = "fbFragment";

    private UiLifecycleHelper uiHelper;

    public void onSessionStateChange(Session session, SessionState state,
                                     Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in");
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle state) {
        super.onCreateView(inflater, container, state);
        View view = inflater.inflate(R.layout.friends, container, false);
        LoginButton authButton =
            (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        return view;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        uiHelper = new UiLifecycleHelper(
                getActivity(),
                new Session.StatusCallback() {
                    @Override
                    public void call(Session session, SessionState state,
                                     Exception exception) {
                        onSessionStateChange(session, state, exception);
                    }
                }
            );
        uiHelper.onCreate(state);
    }

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
