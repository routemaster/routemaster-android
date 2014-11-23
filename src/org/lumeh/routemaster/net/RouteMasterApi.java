package org.lumeh.routemaster.net;

import java.util.List;
import org.lumeh.routemaster.models.Journey;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * This provides an interface that Retrofit can use to provide an async REST
 * client. This rest client is used by the {@link NetworkService}. It <em>should
 * not be called by any Activity or other Service</em>, as other activities or
 * services may be destroyed before the request completes.
 */
public interface RouteMasterApi {
    @POST("/journey")
    public void postJourney(@Body Journey journey, Callback<Journey> cb);

    @GET("/journey/{id}")
    public void getJourney(@Path("id") String id, Callback<Journey> cb);

    @GET("/account/{id}/recent")
    public void getRecentJourneys(@Path("id") String id,
                                  Callback<List<Journey>> cb);
}
