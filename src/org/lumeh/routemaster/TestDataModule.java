package org.lumeh.routemaster;

import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import java.io.StringReader;
import javax.inject.Named;
import org.lumeh.routemaster.models.Journey;
import org.lumeh.routemaster.net.NetworkModule;

@Module(includes = {NetworkModule.class}, library = true)
public class TestDataModule {
    private static final String JOURNEY_JSON_STRING =
        "{\"distanceM\": 100.0, \"efficiency\": 52, \"visibility\": " +
        "\"PRIVATE\", \"startTimeUtc\": \"2014-10-30T13:59:37.811000\", " +
        "\"waypoints\": [{\"accuracyM\": 43.5, \"heightM\": 0.0, " +
        "\"journeyId\": 15, \"latitude\": 29.6575151, \"timeUtc\": " +
        "\"2014-10-30T13:59:37.811000\", \"id\": 50, \"longitude\": " +
        "-82.3418487}, {\"accuracyM\": 12.0, \"heightM\": 27.0, " +
        "\"journeyId\": 15, \"latitude\": 29.6578416, \"timeUtc\": " +
        "\"2014-10-30T13:59:57.365000\", \"id\": 51, \"longitude\": " +
        "-82.3421509}, {\"accuracyM\": 12.0, \"heightM\": 7.0, " +
        "\"journeyId\": 15, \"latitude\": 29.657919, \"timeUtc\": " +
        "\"2014-10-30T14:00:00.316000\", \"id\": 52, \"longitude\": " +
        "-82.3422764}], \"accountId\": \"test\", \"stopTimeUtc\": " +
        "\"2014-10-30T14:00:00.316000\", \"startPlaceId\": null, \"id\": " +
        "15, \"stopPlaceId\": null}";

    @Provides
    @Named("test data")
    public Journey provideJourney(Gson gson) {
        return gson.fromJson(JOURNEY_JSON_STRING, Journey.class);
    }
}
