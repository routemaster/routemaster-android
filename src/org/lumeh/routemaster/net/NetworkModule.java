package org.lumeh.routemaster.net;

import android.location.Location;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.inject.Singleton;
import org.lumeh.routemaster.net.RouteMasterApi;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

@Module(library = true)
public class NetworkModule {
    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder()
            .registerTypeAdapter(Optional.class,
                                 new JsonAdapters.OptionalAdapter())
            .registerTypeAdapter(Location.class,
                                 new JsonAdapters.LocationAdapter())
            .registerTypeAdapter(Date.class, new JsonAdapters.DateAdapter())
            .setPrettyPrinting()
            .create();
    }

    @Provides
    @Singleton
    Client provideHttpClient() {
        return new OkClient();
    }

    @Provides
    @Singleton
    Converter provideConverter(Gson gson) {
        return new GsonConverter(gson);
    }

    @Provides
    @Singleton
    RouteMasterApi provideRouteMasterApi(Client client, Converter converter) {
        return new RestAdapter.Builder()
            .setEndpoint("http://routemaster.lumeh.org")
            .setClient(client)
            .setConverter(converter)
            .build()
            .create(RouteMasterApi.class);
    }
}
