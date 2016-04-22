package de.mario222k.mangarx.provider

import android.text.TextUtils
import de.mario222k.mangarx.model.setChapters
import de.mario222k.mangarx.model.setCoverUrl
import de.mario222k.mangarx.model.setUrl
import de.mario222k.mangarxinterface.model.Chapter
import de.mario222k.mangarxinterface.model.Manga
import de.mario222k.mangarxinterface.model.Page
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rx.Observable
import java.io.IOException
import java.util.*

/**
 * MangaProvider Implementation for mangareader.net.
 *
 *
 * Created by Mario.Sorge on 13/12/15.
 */
class MangaReader {

    private var nextPage = -1
    private var initPage = -1

    /**
     * Fetch a list of [Manga] that were recently released.

     * @param page page to load, `&lt;=0` for reset
     * *
     * @return Observable for list of [Chapter]s
     */
    fun getLatestMangas(page: Int): Observable<List<Manga>> {
        if (page <= 0) {
            nextPage = -1
            initPage = -1
        }

        if (initPage > 0) {
            nextPage = Math.max(0, initPage - page)
        }

        return Observable.create(Observable.OnSubscribe<List<Manga>> { subscriber ->
            var url = LATEST_URL
            if (nextPage >= 0) {
                url += "/${nextPage.toString()}00"
            }

            val document = getDocument(url)
            if (document == null) {
                subscriber.onError(RuntimeException("Error fetching from: $url"))
                subscriber.onCompleted()
                return@OnSubscribe
            }

            updateNextId(document)

            val mangas = grepMangas(document)
            subscriber.onNext(mangas)
            subscriber.onCompleted()
        })
    }

    private fun updateNextId(document: Document) {
        if (initPage < 0) {
            initPage = getPageFromTitle(document) - 1
            nextPage = initPage
        }

        nextPage = Math.max(0, nextPage - 1)
    }

    private fun getPageFromTitle(document: Document): Int {
        val title = document.title()
        if (TextUtils.isEmpty(title)) {
            return -1
        }

        val lastSpace = title.lastIndexOf(' ')
        if (lastSpace < 0) {
            return -1
        }

        try {
            return Integer.parseInt(title.substring(lastSpace + 1))

        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return -1
        }

    }

    @Synchronized private fun grepMangas(document: Document): List<Manga> {
        val mangaList = ArrayList<Manga>()
        val chapterMap = HashMap<String, ArrayList<Chapter>>()

        val table = document.select("table.updates").first() ?: return mangaList

        val chapters = table.select("a.chaptersrec")
        for (element in chapters) {
            val url = element.attr("href")
            if (TextUtils.isEmpty(url)) {
                continue
            }

            val key = url.substring(0, url.lastIndexOf('/'))
            val chapter = Chapter(element.text(), BASE_URL + url)

            var chapterList = chapterMap[key]
            if (chapterList == null) {
                chapterList = ArrayList<Chapter>()
                chapterMap.put(key, chapterList)
            }

            chapterList.add(chapter)
        }

        val mangas = table.select("a.chapter")
        for (element in mangas) {
            val url = element.attr("href")
            if (TextUtils.isEmpty(url)) {
                continue
            }

            val manga = Manga(element.text())
                    .setUrl(BASE_URL + url)
                    .setChapters(chapterMap[url])
                    .setCoverUrl(String.format(IMAGE_URL, (Math.random() * 5).toInt()) + url + url + "-l0.jpg")
            mangaList.add(manga)
            chapterMap.remove(url)
        }

        return mangaList
    }

    companion object {
        private val BASE_URL = "http://www.mangareader.net"
        private val IMAGE_URL = "http://s%d.mangareader.net/cover"
        private val LATEST_URL = BASE_URL + "/latest"

        /**
         * Fetch the [Chapter] pages.

         * @param chapter current chapter
         * *
         * @return Observable that emits a new instance from [Chapter] with all [Page]s
         */
        fun getCompleteChapter(chapter: Chapter): Observable<Chapter> {

            return Observable.create(Observable.OnSubscribe<de.mario222k.mangarxinterface.model.Chapter> { subscriber ->
                val chapterCopy = Chapter(chapter.name ?: "", chapter.url ?: "")
                val url = chapterCopy.url ?: ""
                val document = getDocument(url)
                if (document == null) {
                    subscriber.onError(RuntimeException("Error fetching from: " + url))
                    subscriber.onCompleted()
                    return@OnSubscribe
                }

                val pageMenu = document.getElementById("pageMenu")
                if (pageMenu == null) {
                    subscriber.onError(RuntimeException("Error select id:pageMenu in: " + document.body()))
                    subscriber.onCompleted()
                    return@OnSubscribe
                }

                val options = pageMenu.getElementsByTag("option")
                if (options == null || options.isEmpty()) {
                    subscriber.onError(RuntimeException("Error fetch page count"))
                    subscriber.onCompleted()
                    return@OnSubscribe
                }

                chapterCopy.pageCount = options.size
                val pages = chapterCopy.pages
                if (pages == null) {
                    subscriber.onError(RuntimeException("Error pages is null"))
                    subscriber.onCompleted()
                    return@OnSubscribe
                }

                val done = intArrayOf(0)
                for (option in options) {
                    getPage(BASE_URL + option.attr("value"), Integer.parseInt(option.html()))
                            .filter (
                                    { page -> page != null && !TextUtils.isEmpty(page.url) })

                            .subscribe(
                                    { page -> pages.put(page.page - 1, page) },
                                    { throwable -> throwable.printStackTrace() })

                            {
                                done[0]++
                                if (done[0] >= options.size) {
                                    subscriber.onNext(chapterCopy)
                                    subscriber.onCompleted()
                                }
                            }
                }
            })
        }

        /**
         * Fetch the [Page] from [Chapter] with given page number.
         * Observable will complete after fetching entry.

         * @param chapter current chapter
         * *
         * @param page    number of page
         * *
         * @return Observable for [Page] or `error`
         */
        fun getPage(chapter: Chapter, page: Int): Observable<Page> {
            val pages = chapter.pages

            if (pages != null && pages.get(page - 1) != null) {
                return Observable.just<Page>(pages.get(page - 1))

            } else {
                return getPage("${chapter.url}/$page", page)
            }
        }

        /**
         * Fetch the [Page] from [Chapter] with given page number.
         * Observable will complete after fetching entry.

         * @param pageUrl url for chapter page
         * *
         * @param page    number of page
         * *
         * @return Observable for [Page] or `null`
         */
        fun getPage(pageUrl: String, page: Int): Observable<Page> {

            return Observable.create(Observable.OnSubscribe<de.mario222k.mangarxinterface.model.Page> { subscriber ->
                val document = getDocument(pageUrl)
                if (document == null) {
                    subscriber.onError(RuntimeException("Error fetching from: " + pageUrl))
                    subscriber.onCompleted()
                    return@OnSubscribe
                }

                val img = document.getElementById("img")
                if (img == null) {
                    subscriber.onError(RuntimeException("Error select id:img in: " + document.body()))
                    subscriber.onCompleted()
                    return@OnSubscribe
                }

                subscriber.onNext(Page(page, img.attr("src")))
                subscriber.onCompleted()
            })
        }

        private fun getDocument(url: String): Document? {
            try {
                return Jsoup.connect(url)
                        .header("Cache-control", "no-cache")
                        .header("Cache-store", "no-store")
                        .header("Pragma", "no-cache")
                        .header("Expires", "0").get()

            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

        }
    }
}
