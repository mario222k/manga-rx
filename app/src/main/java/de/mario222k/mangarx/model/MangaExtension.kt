package de.mario222k.mangarx.model

import de.mario222k.mangarxinterface.model.Chapter
import de.mario222k.mangarxinterface.model.Manga

fun Manga.setCoverUrl(coverUrl: String?): Manga {
    cover = coverUrl
    return this
}

fun Manga.setUrl(url: String?): Manga {
    this.url = url
    return this
}

fun Manga.setChapters(chapters: List<Chapter>?): Manga {
    this.chapters.clear()
    if (chapters != null) {
        this.chapters.addAll(chapters)
    }
    return this
}
