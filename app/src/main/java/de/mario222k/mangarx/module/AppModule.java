package de.mario222k.mangarx.module;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.mario222k.mangarx.plugin.PluginProvider;

/**
 * MangaRx AppModule
 * Created by Mario.Sorge on 13/12/15.
 */
@Module
public class AppModule {

    private Application mApplication;

    public AppModule (Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application providesApplication () {
        return mApplication;
    }

    @Provides
    @Singleton
    PluginProvider providesPluginProvider (Application application) {
        return new PluginProvider(application.getApplicationContext());
    }
}
