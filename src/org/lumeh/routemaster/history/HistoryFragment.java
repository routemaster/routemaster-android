package org.lumeh.routemaster.history;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Collections;
import org.lumeh.routemaster.R;
import org.lumeh.routemaster.models.Journey;

public class HistoryFragment extends Fragment {
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle state) {
        super.onCreateView(inflater, container, state);
        return inflater.inflate(R.layout.history, container, false);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        recyclerView = (RecyclerView) getView().findViewById(R.id.list);

        // configure the recyclerView
        LinearLayoutManager layoutManager =
            new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(
            new HistoryCardAdapter(Collections.<Journey>emptyList())
        );
    }
}
