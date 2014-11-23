package org.lumeh.routemaster;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import javax.inject.Singleton;
import org.lumeh.routemaster.history.HistoryFragment;
import org.lumeh.routemaster.net.NetworkModule;
import org.lumeh.routemaster.net.NetworkService;
import org.lumeh.routemaster.record.RecordFragment;
import org.lumeh.routemaster.service.TrackingService;

public class MainApplication extends Application {
    private static final String TAG = "RouteMaster";
    ObjectGraph objectGraph = ObjectGraph.create(new MainModule());

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Welcome to RouteMaster!");
        objectGraph.inject(this);
        startService(new Intent(this, NetworkService.class));
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }

    public void inject(Object obj) {
        getObjectGraph().inject(obj);
    }

    @Module(
        includes = {
            NetworkModule.class
        },
        injects = {
            HistoryFragment.class,
            MainActivity.class,
            MainApplication.class,
            NetworkService.class,
            RecordFragment.class,
            RouteMasterActivity.class,
            RouteMasterFragment.class,
            RouteMasterService.class,
            TrackingService.class
        }
    )
    public class MainModule {
        @Provides
        @Singleton
        Bus provideBus() {
            return new Bus();
        }

        @Provides
        MainApplication provideMainApplication() {
            return MainApplication.this;
        }

        @Provides
        Context provideContext(MainApplication app) {
            return app;
        }

        @Provides
        @Singleton
        Picasso providePicasso(Context context) {
            return new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(context))
                .build();
        }
    }
}
