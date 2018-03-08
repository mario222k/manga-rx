package de.mario222k.mangarx.storage;

import android.support.annotation.NonNull;

import de.mario222k.mangarxinterface.model.Chapter;

/**
 * Created by Mario.Sorge on 02/01/16.
 */
public interface ChapterStorage {
    boolean setChapterPageReaded (@NonNull Chapter chapter, int page);

    boolean setChapterCompleted (@NonNull Chapter chapter);

    boolean setChapterBookmark (@NonNull Chapter chapter, boolean isBookmarked);

    int getLastChapterPageReaded (@NonNull Chapter chapter);

    boolean isChapterCompleteReaded (@NonNull Chapter chapter);

    boolean isChapterBookmarked (@NonNull Chapter chapter);

    boolean removeEntries (@NonNull Chapter chapter);
}
