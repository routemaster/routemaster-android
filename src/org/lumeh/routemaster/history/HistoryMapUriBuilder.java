package org.lumeh.routemaster.history;

import android.location.Location;
import android.net.Uri;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lumeh.routemaster.models.Journey;

public class HistoryMapUriBuilder {
    private static final Uri BASE_URI =
        Uri.parse("https://maps.googleapis.com/maps/api/staticmap");
    private final Uri.Builder builder = BASE_URI.buildUpon();

    public Uri build() {
        return builder.build();
    }

    public HistoryMapUriBuilder path(Journey path) {
        return this.path(null, path);
    }

    public HistoryMapUriBuilder path(String style, Journey path) {
        return this.path(style, path.getWaypoints());
    }

    public HistoryMapUriBuilder path(List<?> path) {
        return this.path(null, path);
    }

    /**
     * Defines a single path of two or more connected points to overlay on the
     * image at specified locations. You may supply additional paths by calling
     * this method multiple times. Note that if you supply a path for a map, you
     * do not need to specify the (normally required) center and zoom
     * parameters.
     */
    public HistoryMapUriBuilder path(String style, List<?> path) {
        String param = style != null && style != "" ? style + "|" : "";
        param += format(path);
        builder.appendQueryParameter("path", param);
        return this;
    }

    /**
     * Affects the number of pixels that are returned. scale=2 returns twice as
     * many pixels as scale=1 while retaining the same coverage area and level
     * of detail (i.e. the contents of the map don't change). This is useful
     * when developing for high-resolution displays, or when generating a map
     * for printing. The default value is 1. Accepted values are 2 and 4 (4 is
     * only available to Google Maps API for Work customers.)
     */
    public HistoryMapUriBuilder scale(int scale) {
        builder.appendQueryParameter("scale", "" + scale);
        return this;
    }

    /**
     * Defines the rectangular dimensions of the map image. This parameter takes
     * a string of the form {horizontal_value}x{vertical_value}. For example,
     * 500x400 defines a map 500 pixels wide by 400 pixels high. Maps smaller
     * than 180 pixels in width will display a reduced-size Google logo. This
     * parameter is affected by the scale parameter, described below; the final
     * output size is the product of the size and scale values.
     */
    public HistoryMapUriBuilder size(int width, int height) {
        builder.appendQueryParameter("size", width + "x" + height);
        return this;
    }

    /**
     * Defines the center of the map, equidistant from all edges of the map.
     */
    public HistoryMapUriBuilder center(Location loc) {
        return center(format(loc));
    }

    /**
     * Defines the center of the map, equidistant from all edges of the map.
     */
    public HistoryMapUriBuilder center(LatLng loc) {
        return center(format(loc));
    }

    /**
     * Defines the center of the map, equidistant from all edges of the map.
     * @param loc Address (e.g. "city hall, new york, ny") identifying a unique
     *            location on the face of the earth
     */
    public HistoryMapUriBuilder center(String loc) {
        builder.appendQueryParameter("center", loc);
        return this;
    }

    /**
     * The resolution of the current view. Zoom levels between 0 (the lowest
     * zoom level, in which the entire world can be seen on one map) and 21+
     * (down to streets and individual buildings) are possible within the
     * default roadmap view. Building outlines, where available, appear on the
     * map around zoom level 17. This value differs from area to area and can
     * change over time as the data evolves.
     */
    public HistoryMapUriBuilder zoom(int level) {
        builder.appendQueryParameter("zoom", "" + level);
        return this;
    }

    public HistoryMapUriBuilder marker(Object loc) {
        return marker(null, loc);
    }

    public HistoryMapUriBuilder marker(String style, Object loc) {
        return markers(style, ImmutableList.of(loc));
    }

    public HistoryMapUriBuilder markers(List<?> loc) {
        return markers(null, loc);
    }

    public HistoryMapUriBuilder markers(String style, List<?> loc) {
        String param = style != null && style != "" ? style + "|" : "";
        param += format(loc);
        builder.appendQueryParameter("markers", param);
        return this;
    }

    public static StyleBuilder style() {
        return new StyleBuilder();
    }

    private static String format(List<?> in) {
        ArrayList<String> toJoin = new ArrayList<>(in.size());
        for(Object loc : in) {
            if(loc instanceof String) {
                toJoin.add((String) loc);
            } else if(loc instanceof Location) {
                toJoin.add(format((Location) loc));
            } else if(loc instanceof LatLng) {
                toJoin.add(format((LatLng) loc));
            } else {
                throw new ClassCastException("Given a list with elements " +
                                             "that aren't LatLng or Location.");
            }
        }
        return Joiner.on("|").join(toJoin);
    }

    private static String format(Location loc) {
        return format(new LatLng(loc.getLatitude(), loc.getLongitude()));
    }

    /**
     * Format a LatLng pair with 6 places of accuracy past the decimal. Any
     * further is ignored by the maps API.
     */
    private static String format(LatLng loc) {
        return new Formatter()
            .format("%.6f,%.6f", loc.latitude, loc.longitude)
            .toString();
    }

    public static final class StyleBuilder {
        private final HashMap<String, String> map = new HashMap<>();

        private StyleBuilder() { }

        public StyleBuilder put(String key, float value) {
            map.put(key, "" + value);
            return this;
        }

        public StyleBuilder put(String key, double value) {
            map.put(key, "" + value);
            return this;
        }

        public StyleBuilder put(String key, int value) {
            map.put(key, "" + value);
            return this;
        }

        public StyleBuilder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        public String build() {
            return Joiner.on("|").join(
                Lists.transform(ImmutableList.copyOf(map.entrySet()),
                    new Function<Map.Entry<String, String>, String>() {
                        @Override
                        public String apply(Map.Entry<String, String> val) {
                            return val.getKey() + ":" + val.getValue();
                        }
                    }
                )
            );
        }
    }
}
