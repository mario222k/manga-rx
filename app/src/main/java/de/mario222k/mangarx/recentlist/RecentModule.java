package de.mario222k.mangarx.recentlist;

import android.app.Application;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import de.mario222k.mangarx.storage.ChapterStorageImpl;
import de.mario222k.mangarx.storage.RecentStorage;
import de.mario222k.mangarx.storage.RecentStorageImpl;

/**
 * MangaRx AppModule
 * Created by Mario.Sorge on 13/12/15.
 */
@Module
public class RecentModule {

    @Inject
    public RecentModule () {

    }

    @Provides
    RecentAdapter providesRecentAdapter ( Application application ) {
        return new RecentAdapter(application, new ChapterStorageImpl(application));
    }


    @Provides
    RecentStorage providesRecentStorage ( Application application ) {
        return new RecentStorageImpl(application);
    }
}
