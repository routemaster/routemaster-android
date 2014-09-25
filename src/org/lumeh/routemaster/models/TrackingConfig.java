package org.lumeh.routemaster.models;

import android.os.Parcelable;
import android.os.Parcel;

/**
 * A configuration loaded from the server about what is and isn't a valid route
 * or waypoint. This allows us to perform some filtering on waypoints, and
 * notify users if their route is unacceptable before they waste their time
 * walking the entire route.
 */
public class TrackingConfig implements Parcelable {
    private int pollingIntervalMs;
    private float geofencingDistanceM;
    private float minAccuracyM;
    private float maxDistanceM;

    public static final Parcelable.Creator<TrackingConfig> CREATOR =
        new Parcelable.Creator<TrackingConfig>() {
            @Override
            public TrackingConfig createFromParcel(Parcel in) {
                return new TrackingConfig(in.readInt(),
                                          in.readFloat(),
                                          in.readFloat(),
                                          in.readFloat());
            }

            @Override
            public TrackingConfig[] newArray(int size) {
                return new TrackingConfig[size];
            }
        };

    public TrackingConfig(int pollingIntervalMs, float geofencingDistanceM,
                          float minAccuracyM, float maxDistanceM) {
        this.pollingIntervalMs = pollingIntervalMs;
        this.geofencingDistanceM = geofencingDistanceM;
        this.minAccuracyM = minAccuracyM;
        this.maxDistanceM = maxDistanceM;
    }

    /**
     * The requested time between location updates. Actual updates may happen
     * more or less frequently.
     */
    public int getPollingIntervalMs() {
        return pollingIntervalMs;
    }

    /**
     * The FusedLocationProviderApi can use geofencing to minimize the number of
     * location updates requested. It will avoid updating the location if it
     * doesn't think the device has moved at least this far.
     */
    public float getGeofencingDistanceM() {
        return geofencingDistanceM;
    }

    /**
     * If a location has a worse accuracy than this value, it's completely
     * discarded, because it's of no use to us. This is useful when the app is
     * first starting, so it doesn't use a bad value before the gps/wifi is
     * ready.
     */
    public float getMinAccuracyM() {
        return minAccuracyM;
    }

    /**
     * If the distance between the current and last locations are larger than
     * this our recording is bad, and should be discarded. (TODO)
     * <p>
     * Santity check: 60 miles per hour is about 26.82 meters per second
     */
    public float getMaxDistanceM() {
        return maxDistanceM;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(pollingIntervalMs);
        out.writeFloat(geofencingDistanceM);
        out.writeFloat(minAccuracyM);
        out.writeFloat(maxDistanceM);
    }
}
