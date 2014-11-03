package org.lumeh.routemaster.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lumeh.routemaster.util.Dates;

/**
 * A particular instance of walking from one Place to another.
 */
public class Journey implements Parcelable, Uploadable {
    private static final String UPLOAD_PATH = "/journey";

    private Optional<Integer> id = Optional.absent();
    private Visibility visibility;
    private Optional<Long> startTimeUtc = Optional.absent();
    private Optional<Long> stopTimeUtc = Optional.absent();
    private double distanceM = 0;
    private int efficiency = 100;
    private List<Location> waypoints = new ArrayList<>();

    public static final Parcelable.Creator<Journey> CREATOR =
            new Parcelable.Creator<Journey>() {
        @Override
        public Journey createFromParcel(Parcel source) {
            return new Journey(source);
        }

        @Override
        public Journey[] newArray(int size) {
            return new Journey[size];
        }
    };

    public Journey(Account currentAccount) {
        this.visibility = currentAccount.getDefaultVisibility();
    }

    public Journey(Parcel source) {
        this.id = (Optional<Integer>) source.readSerializable();
        this.visibility = (Visibility) source.readSerializable();
        this.startTimeUtc = (Optional<Long>) source.readSerializable();
        this.stopTimeUtc = (Optional<Long>) source.readSerializable();
        this.distanceM = source.readDouble();
        this.efficiency = source.readInt();
        this.waypoints = new ArrayList<Location>();
        source.readTypedList(this.waypoints, Location.CREATOR);
    }

    public Journey(JSONObject j) {
        try {
            this.id = Optional.of(j.getInt("id"));
            this.visibility = Visibility.get(j.getString("visibility"));
            try {
                this.startTimeUtc = Optional.of(
                        Dates.toMillisSinceEpoch(j.getString("startTimeUtc")));
                this.stopTimeUtc = Optional.of(
                        Dates.toMillisSinceEpoch(j.getString("stopTimeUtc")));
            } catch (ParseException e) {
                throw new RuntimeException("Couldn't parse date in JSON", e);
            }
            this.distanceM = j.getDouble("distanceM");
            this.efficiency = j.getInt("efficiency");
            this.waypoints = new ArrayList<Location>();
            JSONArray waypoints = j.getJSONArray("waypoints");
            for (int i = 0; i < waypoints.length(); i++) {
                JSONObject w = waypoints.getJSONObject(i);
                Location loc = new Location("RouteMaster");
                try {
                    loc.setTime(Dates.toMillisSinceEpoch(w.getString("timeUtc")));
                } catch (ParseException e) {
                    throw new RuntimeException("Couldn't parse date in JSON", e);
                }
                loc.setAccuracy((float) w.getDouble("accuracyM"));
                loc.setLatitude(w.getDouble("latitude"));
                loc.setLongitude(w.getDouble("longitude"));
                loc.setAltitude(w.getDouble("heightM"));
                this.waypoints.add(loc);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void addWaypoint(Location loc) {
        if (this.waypoints.isEmpty()) {
            this.setStartTimeUtc(loc.getTime());
        }
        this.waypoints.add(Preconditions.checkNotNull(loc));
        // TODO: update distance and efficiency
    }

    public ImmutableList<Location> getWaypoints() {
        return ImmutableList.copyOf(this.waypoints);
    }

    /**
     * When tracking, this is the current position.
     */
    public Optional<Location> getLastWaypoint() {
        if(waypoints.size() > 0) {
            return Optional.of(waypoints.get(waypoints.size() - 1));
        }
        return Optional.absent();
    }

    public void setStartTimeUtc(long t) {
        this.startTimeUtc = Optional.of(t);
    }

    public void setStopTimeUtc(long t) {
        this.stopTimeUtc = Optional.of(t);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.id);
        dest.writeSerializable(this.visibility);
        dest.writeSerializable(this.startTimeUtc);
        dest.writeSerializable(this.stopTimeUtc);
        dest.writeDouble(this.distanceM);
        dest.writeInt(this.efficiency);
        dest.writeTypedList(this.waypoints);
    }

    @Override
    public String getUploadPath() {
        return UPLOAD_PATH;
    }

    @Override
    public JSONObject toJson() {
        JSONObject b;
        try {
            b = new JSONObject();
            JSONArray waypoints = new JSONArray();
            for (Location loc : this.waypoints) {
                waypoints.put(new JSONObject()
                        .put("timeUtc", Dates.toIsoString(loc.getTime()))
                        .put("accuracyM", loc.getAccuracy())
                        .put("latitude", loc.getLatitude())
                        .put("longitude", loc.getLongitude())
                        .put("heightM", loc.getAltitude()));
            }
            b.put("visibility", this.visibility.toString());
            b.put("waypoints", waypoints);
            if (this.id.isPresent()) {
                b.put("id", this.id.get());
            }
            if (this.startTimeUtc.isPresent()) {
                b.put("startTimeUtc", Dates.toIsoString(this.startTimeUtc.get()));
            }
            if (this.stopTimeUtc.isPresent()) {
                b.put("stopTimeUtc", Dates.toIsoString(this.stopTimeUtc.get()));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return b;
    }
}
