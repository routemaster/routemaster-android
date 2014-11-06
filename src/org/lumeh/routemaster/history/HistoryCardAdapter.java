package org.lumeh.routemaster.history;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.common.collect.ImmutableList;
import com.squareup.picasso.Picasso;
import java.util.Collection;
import org.lumeh.routemaster.R;
import com.google.android.gms.maps.model.LatLng;
import org.lumeh.routemaster.models.Journey;
import org.lumeh.routemaster.TestData;

public class HistoryCardAdapter
                     extends Adapter<HistoryCardAdapter.HistoryCardViewHolder> {
    private static final String TAG = "RouteMaster";
    private static final String START_STYLE = HistoryMapUriBuilder.style()
        .put("color", "green")
        .put("size", "med")
        .build();
    private static final String END_STYLE = HistoryMapUriBuilder.style()
        .put("color", "red")
        .put("size", "med")
        .build();

    private ImmutableList<Journey> journeys;

    public HistoryCardAdapter(Collection<Journey> journeys) {
        this.journeys = ImmutableList.copyOf(journeys);
    }

    @Override
    public int getItemCount() {
        return 1000;
    }

    @Override
    public HistoryCardViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.history_card, parent, false);
        return new HistoryCardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(HistoryCardViewHolder holder, int position) {
        holder.getStartTimeView().setText("st " + position);
        holder.getDistanceView().setText("dist " + position);
        holder.getEfficiencyView().setText("eff " + position);
        holder.getLayout().post(new InjectMapRunnable(holder));
    }

    /**
     * Injects a map into the given HistoryCardViewHolder when run. This must be
     * a Runnable, so it can be called by `holder.getLayout().post()`, after the
     * element's layout executes. Before the layout executes, the map's width
     * and height may be either incorrect or undefined as zero.
     */
    private static class InjectMapRunnable implements Runnable {
        private final HistoryCardViewHolder holder;

        public InjectMapRunnable(HistoryCardViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) holder
                .getCardView()
                .getContext()
                .getSystemService(Context.WINDOW_SERVICE);
            windowManager
                .getDefaultDisplay()
                .getMetrics(metrics);

            int scale = 1; // low-res devices
            if(metrics.densityDpi > DisplayMetrics.DENSITY_HIGH) {
                scale = 2; // high-res devices
            }
            // google maps for work (not free) offers `scale = 4`

            // requested width and height is multipled by the scale for the
            // actual pixel size.
            float width = (float) holder.getMapView().getWidth() / scale;
            float height = (float) holder.getMapView().getHeight() / scale;

            // the free static maps api limits the width and height values to
            // 640x640, so we may need to scale those values preserving the
            // aspect ratio
            if(width > 640) {
                height *= 640 / width;
                width = 640;
            } else if(height > 640) {
                width *= 640 / height;
                height = 640;
            }

            Uri uri = new HistoryMapUriBuilder()
                .scale(scale)
                .path(TestData.journey)
                .marker(START_STYLE, new LatLng(29.6574919, -82.3418686))
                .marker(END_STYLE, new LatLng(29.6578225, -82.3421188))
                .size((int) Math.ceil(width), (int) Math.ceil(height))
                .build();

            // Picasso.with(holder.getCardView().getContext())
            //     .setIndicatorsEnabled(true);
            Picasso.with(holder.getCardView().getContext())
                .load(uri)
                .into(holder.getMapView());
        }
    }

    public static class HistoryCardViewHolder extends ViewHolder {
        private final RelativeLayout layout;
        private final TextView startTimeView, distanceView, efficiencyView;
        private final ImageView mapView;

        public HistoryCardViewHolder(CardView cardView) {
            super(cardView);
            layout = (RelativeLayout) cardView.findViewById(R.id.layout);
            startTimeView = (TextView) cardView.findViewById(R.id.startTime);
            distanceView = (TextView) cardView.findViewById(R.id.distance);
            efficiencyView = (TextView) cardView.findViewById(R.id.efficiency);
            mapView = (ImageView) cardView.findViewById(R.id.map);
        }

        public CardView getCardView() {
            return (CardView) itemView;
        }

        public RelativeLayout getLayout() {
            return layout;
        }

        public TextView getStartTimeView() {
            return startTimeView;
        }

        public TextView getDistanceView() {
            return distanceView;
        }

        public TextView getEfficiencyView() {
            return efficiencyView;
        }

        public ImageView getMapView() {
            return mapView;
        }
    }
}
