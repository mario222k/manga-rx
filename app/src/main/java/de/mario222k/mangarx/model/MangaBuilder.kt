package de.mario222k.mangarx.model

import de.mario222k.mangarxinterface.model.Chapter
import de.mario222k.mangarxinterface.model.Manga

/**
 * Builder class for [Manga].
 * Created by Mario.Sorge on 15/12/15.
 */
class MangaBuilder {

    private val manga: Manga

    constructor(name: String) {
        manga = Manga(name)
    }

    fun setCoverUrl(coverUrl: String?): MangaBuilder {
        manga.cover = coverUrl
        return this
    }

    fun setUrl(url: String?): MangaBuilder {
        manga.url = url
        return this
    }

    fun setChapters(chapters: List<Chapter>?): MangaBuilder {
        manga.chapters.clear()
        if (chapters != null) {
            manga.chapters.addAll(chapters)
        }
        return this
    }

    fun build() = manga
}
