// IProviderInterface.aidl
package de.mario222k.mangarxinterface.provider;

import de.mario222k.mangarxinterface.model.Chapter;
import de.mario222k.mangarxinterface.model.Manga;
import de.mario222k.mangarxinterface.model.Page;

interface IProviderInterface {
    /**
     * Return an Array of supported MangaRX versions.
     * for example: ["1.1", "1.0"]
     */
    List<String> getSupportedVersions();

    /**
     * Return a description of this Provider.
     */
    String getDescription();

    /**
     * Fetch a list of {@link Chapter} that were recently released.
     *
     * @param page page to load, {@code <=0} for reset
     * @return List of {@link Manga}s, should never be {@code null}
     */
    List<Manga> getLatestMangas(int page);

    /**
     * Fetch the {@link Chapter} pages.
     *
     * @param chapter current chapter
     * @return new instance from {@link Chapter} with all {@link Page}s
     */
    Chapter getCompleteChapter(in Chapter chapter);

    /**
     * Fetch the {@link Page} from {@link Chapter} with given page number.
     *
     * @param chapter current chapter
     * @param page    number of page
     * @return {@link Page} or {@code null}
     */
    Page getPage(in Chapter chapter, int page);
}
