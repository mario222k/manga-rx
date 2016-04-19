package de.mario222k.mangarx.storage;

import android.support.annotation.Nullable;

import de.mario222k.mangarxinterface.model.Chapter;

/**
 *
 * Created by Mario.Sorge on 02/01/16.
 */
public interface RecentStorage {
    boolean setLastChapter ( @Nullable Chapter chapter );
    @Nullable
    Chapter getLastChapter ();
}
