package de.mario222k.mangarx.application;

import android.app.Application;
import android.support.annotation.Nullable;

import de.mario222k.mangarx.chapter.ChapterComponent;
import de.mario222k.mangarx.chapter.ChapterModule;
import de.mario222k.mangarx.chapter.DaggerChapterComponent;
import de.mario222k.mangarx.module.AppModule;
import de.mario222k.mangarx.plugin.DaggerPluginComponent;
import de.mario222k.mangarx.plugin.PluginComponent;
import de.mario222k.mangarx.recentlist.DaggerRecentComponent;
import de.mario222k.mangarx.recentlist.RecentComponent;
import de.mario222k.mangarx.recentlist.RecentModule;

/**
 * Created by Mario.Sorge on 13/12/15.
 */
public class MyApp extends Application {
    @Nullable
    private PluginComponent mPluginComponent;

    @Nullable
    private RecentComponent mRecentComponent;

    @Nullable
    private ChapterComponent mChapterComponent;

    @Override
    public void onCreate () {
        super.onCreate();
    }

    public PluginComponent getPluginComponent () {
        if (mPluginComponent == null) {
            mPluginComponent = DaggerPluginComponent.builder()
                    .appModule(new AppModule(this))
                    .build();
        }
        return mPluginComponent;
    }

    public RecentComponent getRecentComponent () {
        if (mRecentComponent == null) {
            mRecentComponent = DaggerRecentComponent.builder()
                    .appModule(new AppModule(this))
                    .recentModule(new RecentModule())
                    .build();
        }
        return mRecentComponent;
    }

    public ChapterComponent getChapterComponent () {
        if (mChapterComponent == null) {
            mChapterComponent = DaggerChapterComponent.builder()
                    .appModule(new AppModule(this))
                    .chapterModule(new ChapterModule())
                    .build();
        }
        return mChapterComponent;
    }
}
