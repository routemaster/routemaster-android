package org.lumeh.routemaster.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.lumeh.routemaster.util.Dates;

/**
 * A particular instance of walking from one Place to another.
 */
public class Journey implements Parcelable {

    private String id = UUID.randomUUID().toString();
    private Visibility visibility;
    private Optional<Date> startTimeUtc = Optional.absent();
    private Optional<Date> stopTimeUtc = Optional.absent();
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
        this.id = source.readString();
        this.visibility = (Visibility) source.readSerializable();
        this.startTimeUtc = (Optional<Date>) source.readSerializable();
        this.stopTimeUtc = (Optional<Date>) source.readSerializable();
        this.distanceM = source.readDouble();
        this.efficiency = source.readInt();
        this.waypoints = new ArrayList<Location>();
        source.readTypedList(this.waypoints, Location.CREATOR);
    }

    public double getDistanceM() {
        return distanceM;
    }

    public int getEfficiency() {
        return efficiency;
    }

    public void addWaypoint(Location loc) {
        if (this.waypoints.isEmpty()) {
            this.setStartTimeUtc(new Date(loc.getTime()));
        }
        this.waypoints.add(Preconditions.checkNotNull(loc));
        // TODO: update distance and efficiency
    }

    public ImmutableList<Location> getWaypoints() {
        return ImmutableList.copyOf(this.waypoints);
    }

    public Optional<Location> getFirstWaypoint() {
        if(waypoints.size() > 0) {
            return Optional.of(waypoints.get(0));
        }
        return Optional.absent();
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

    public void setStartTimeUtc(Date t) {
        this.startTimeUtc = Optional.of(t);
    }

    public Optional<Date> getStartTimeUtc() {
        return startTimeUtc;
    }

    public void setStopTimeUtc(Date t) {
        this.stopTimeUtc = Optional.of(t);
    }

    public Optional<Date> getStopTimeUtc() {
        return stopTimeUtc;
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
}
