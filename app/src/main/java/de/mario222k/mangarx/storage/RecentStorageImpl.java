package de.mario222k.mangarx.storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.mario222k.mangarxinterface.model.Chapter;

/**
 *
 * Created by Mario.Sorge on 02/01/16.
 */
public class RecentStorageImpl implements RecentStorage {
    private final String CHAPTER_STORAGE_NAME = "recent_chapter";

    Application mApplication;

    public RecentStorageImpl ( @NonNull Application application ) {
        mApplication = application;
    }

    @Override
    public boolean setLastChapter ( @Nullable Chapter chapter ) {
        SharedPreferences.Editor editor = mApplication.getSharedPreferences(CHAPTER_STORAGE_NAME, Context.MODE_PRIVATE).edit();

        if(chapter == null) {
            return editor
                    .remove("last_name")
                    .remove("last_url")
                    .commit();
        }

        return editor
                .putString("last_name", chapter.getName())
                .putString("last_url", chapter.getUrl())
                .commit();
    }

    @Override
    @Nullable
    public Chapter getLastChapter () {
        SharedPreferences preferences = mApplication.getSharedPreferences(CHAPTER_STORAGE_NAME, Context.MODE_PRIVATE);
        String name = preferences.getString("last_name", null);
        String url = preferences.getString("last_url", null);

        if(name == null || url == null) {
            return  null;
        }

        return new Chapter(name, url);
    }
}
