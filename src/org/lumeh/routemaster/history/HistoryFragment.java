package org.lumeh.routemaster.history;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.collect.ImmutableList;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import org.lumeh.routemaster.R;
import org.lumeh.routemaster.RouteMasterFragment;
import org.lumeh.routemaster.net.RecentJourneysEvent;
import org.lumeh.routemaster.models.Journey;

public class HistoryFragment extends RouteMasterFragment {
    @Inject HistoryCardAdapter cardAdapter;
    private RecyclerView recyclerView;
    @Inject Bus bus;

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
        recyclerView.setAdapter(cardAdapter);

        bus.register(this);
        bus.post(new RecentJourneysEvent.Get("test"));
    }

    @Subscribe
    public void onEvent(RecentJourneysEvent.Response ev) {
        cardAdapter.setJourneys((Collection<Journey>)ev.get());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }
}
