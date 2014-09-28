package org.lumeh.routemaster.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import static org.lumeh.routemaster.util.Dates.toIsoString;

/**
 * A particular instance of walking from one Place to another.
 */
public class Journey implements Parcelable {
    private Optional<Integer> id = Optional.absent();
    private Visibility visibility;
    private Optional<Long> startTimeUtc = Optional.absent();
    private Optional<Long> endTimeUtc = Optional.absent();
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
        this.endTimeUtc = (Optional<Long>) source.readSerializable();
        this.distanceM = source.readDouble();
        this.efficiency = source.readInt();
        this.waypoints = new ArrayList<Location>();
        source.readTypedList(this.waypoints, Location.CREATOR);
    }

    public void addWaypoint(Location loc) {
        // TODO: update distance and efficiency
        this.waypoints.add(Preconditions.checkNotNull(loc));
    }

    public ImmutableList<Location> getWaypoints() {
        return ImmutableList.copyOf(this.waypoints);
    }

    public void setStartTimeUtc(long t) {
        this.startTimeUtc = Optional.of(t);
    }

    public void setEndTimeUtc(long t) {
        this.endTimeUtc = Optional.of(t);
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
        dest.writeSerializable(this.endTimeUtc);
        dest.writeDouble(this.distanceM);
        dest.writeInt(this.efficiency);
        dest.writeTypedList(this.waypoints);
    }

    public JsonObject toJson() {
        JsonArrayBuilder waypointBuilder = Json.createArrayBuilder();
        for (Location loc : this.waypoints) {
            waypointBuilder.add(Json.createObjectBuilder()
                    .add("time", toIsoString(loc.getTime()))
                    .add("accuracyM", loc.getAccuracy())
                    .add("latitude", loc.getLatitude())
                    .add("longitude", loc.getLongitude())
                    .add("heightM", loc.getAltitude())
                    .build());
        }
        JsonObjectBuilder b = Json.createObjectBuilder();
        b.add("visibility", this.visibility.toString());
        b.add("waypoints", waypointBuilder.build());
        if (this.id.isPresent()) {
            b.add("id", this.id.get());
        }
        if (this.startTimeUtc.isPresent()) {
            b.add("startTimeUtc", toIsoString(this.startTimeUtc.get()));
        }
        if (this.endTimeUtc.isPresent()) {
            b.add("endTimeUtc", toIsoString(this.endTimeUtc.get()));
        }
        return b.build();
    }
}
