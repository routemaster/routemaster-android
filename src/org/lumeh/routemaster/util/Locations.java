package org.lumeh.routemaster.util;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class Locations {
    public static LatLng toLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }
}
