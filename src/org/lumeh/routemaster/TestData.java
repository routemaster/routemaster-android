package org.lumeh.routemaster;

import java.io.StringReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.lumeh.routemaster.models.Journey;

public class TestData {
    public static Journey journey;

    static {
        String journeyJsonString =
            "{\"distanceM\": 100.0, \"efficiency\": 52, \"visibility\": " +
            "\"private\", \"startTimeUtc\": \"2014-10-30T13:59:37.811000\", " +
            "\"waypoints\": [{\"accuracyM\": 43.5, \"heightM\": 0.0, " +
            "\"journeyId\": 15, \"latitude\": 29.6575151, \"timeUtc\": " +
            "\"2014-10-30T13:59:37.811000\", \"id\": 50, \"longitude\": " +
            "-82.3418487}, {\"accuracyM\": 12.0, \"heightM\": 27.0, " +
            "\"journeyId\": 15, \"latitude\": 29.6578416, \"timeUtc\": " +
            "\"2014-10-30T13:59:57.365000\", \"id\": 51, \"longitude\": " +
            "-82.3421509}, {\"accuracyM\": 12.0, \"heightM\": 7.0, " +
            "\"journeyId\": 15, \"latitude\": 29.657919, \"timeUtc\": " +
            "\"2014-10-30T14:00:00.316000\", \"id\": 52, \"longitude\": " +
            "-82.3422764}], \"accountId\": 1, \"stopTimeUtc\": " +
            "\"2014-10-30T14:00:00.316000\", \"startPlaceId\": null, \"id\": " +
            "15, \"stopPlaceId\": null}";
        try {
            journey = new Journey(new JSONObject(journeyJsonString));
        } catch (JSONException e) {
            throw new RuntimeException(
                "The test Journey's JSON couldn't be parsed",
                e
            );
        }
    }
}
