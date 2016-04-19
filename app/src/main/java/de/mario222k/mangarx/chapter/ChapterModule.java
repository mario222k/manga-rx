package de.mario222k.mangarx.chapter;

import android.app.Application;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;
import de.mario222k.mangarx.storage.ChapterStorage;
import de.mario222k.mangarx.storage.ChapterStorageImpl;

/**
 * MangaRx AppModule
 * Created by Mario.Sorge on 13/12/15.
 */
@Module
public class ChapterModule {

    @Inject
    public ChapterModule () { }

    @Provides
    ChapterStorage providesChapterStorage ( Application application ) {
        return new ChapterStorageImpl(application);
    }
}
