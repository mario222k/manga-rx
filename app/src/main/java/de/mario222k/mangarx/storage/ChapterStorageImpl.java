package de.mario222k.mangarx.storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import de.mario222k.mangarxinterface.model.Chapter;

/**
 *
 * Created by Mario.Sorge on 02/01/16.
 */
public class ChapterStorageImpl implements ChapterStorage {

    private final String PAGE_STORAGE_NAME = "chapter_page";

    Application mApplication;

    public ChapterStorageImpl ( @NonNull Application application ) {
        mApplication = application;
    }

    @Override
    public boolean setChapterPageReaded ( @NonNull Chapter chapter, int page ) {
        SharedPreferences.Editor editor = mApplication.getSharedPreferences(PAGE_STORAGE_NAME, Context.MODE_PRIVATE).edit();
        return editor.putInt(chapter.getUrl(), page).commit();
    }

    @Override
    public boolean setChapterCompleted ( @NonNull Chapter chapter ) {
        return setChapterPageReaded(chapter, Integer.MAX_VALUE);
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean setChapterBookmark ( @NonNull Chapter chapter, boolean isBookmarked ) {
        SharedPreferences preferences = mApplication.getSharedPreferences(PAGE_STORAGE_NAME, Context.MODE_PRIVATE);

        if(isBookmarked) {
            return setChapterPageReaded(chapter, 0);

        } else if(preferences.contains(chapter.getUrl())) {
            return removeEntries(chapter);

        } else {
            return true;
        }
    }

    @Override
    public int getLastChapterPageReaded ( @NonNull Chapter chapter ) {
        SharedPreferences preferences = mApplication.getSharedPreferences(PAGE_STORAGE_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(chapter.getUrl(), -1);
    }

    @Override
    public boolean isChapterCompleteReaded ( @NonNull Chapter chapter ) {
        return getLastChapterPageReaded(chapter) == Integer.MAX_VALUE;
    }

    @Override
    public boolean isChapterBookmarked ( @NonNull Chapter chapter ) {
        return getLastChapterPageReaded(chapter) == 0;
    }

    @Override
    public boolean removeEntries ( @NonNull Chapter chapter ) {
        SharedPreferences.Editor editor = mApplication.getSharedPreferences(PAGE_STORAGE_NAME, Context.MODE_PRIVATE).edit();
        return editor.remove(chapter.getUrl()).commit();
    }
}
