package org.lumeh.routemaster.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Dates {
    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final SimpleDateFormat formatter =
        new SimpleDateFormat(FORMAT);

    static {
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Date fromIsoString(String d) throws ParseException {
        return formatter.parse(d);
    }

    public static String toIsoString(Date d) {
        return formatter.format(d);
    }

    public static String toIsoString(long millisSinceEpoch) {
        return toIsoString(new Date(millisSinceEpoch));
    }

    public static long toMillisSinceEpoch(String isoString)
                       throws ParseException {
        return fromIsoString(isoString).getTime();
    }
}
