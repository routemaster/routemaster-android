package org.lumeh.routemaster.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dates {
    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final SimpleDateFormat formatter =
        new SimpleDateFormat(FORMAT);

    public static String toIsoString(long millisSinceEpoch) {
        return formatter.format(new Date(millisSinceEpoch));
    }

    public static long toMillisSinceEpoch(String isoString)
            throws ParseException {
        return formatter.parse(isoString).getTime();
    }
}
