package org.lumeh.routemaster.history;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import org.lumeh.routemaster.R;
import org.lumeh.routemaster.RouteMasterFragment;
import org.lumeh.routemaster.models.Journey;

public class HistoryFragment extends RouteMasterFragment {
    @Inject @Named("test data") Journey testJourney;
    @Inject HistoryCardAdapter cardAdapter;
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
        cardAdapter.setJourneys(ImmutableList.of(
            testJourney, testJourney, testJourney, testJourney
        ));

        // configure the recyclerView
        LinearLayoutManager layoutManager =
            new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(cardAdapter);
    }
}
