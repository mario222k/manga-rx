package de.mario222k.mangarx.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.mario222k.mangarx.model.MangaBuilder;
import de.mario222k.mangarxinterface.model.Chapter;
import de.mario222k.mangarxinterface.model.Manga;
import de.mario222k.mangarxinterface.model.Page;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * MangaProvider Implementation for mangareader.net.
 * <p/>
 * Created by Mario.Sorge on 13/12/15.
 */
public class MangaReader {

    private static final String BASE_URL = "http://www.mangareader.net";
    private static final String IMAGE_URL = "http://s%d.mangareader.net/cover";
    private static final String LATEST_URL = BASE_URL + "/latest";

    private int mNextPage = -1;
    private int mInitPage = -1;

    /**
     * Fetch a list of {@link Manga} that were recently released.
     *
     * @param page page to load, {@code <=0} for reset
     * @return Observable for list of {@link Chapter}s
     */
    @NonNull
    public Observable<List<Manga>> getLatestMangas ( int page ) {
        if (page <= 0) {
            mNextPage = -1;
            mInitPage = -1;
        }

        if (mInitPage > 0) {
            mNextPage = Math.max(0, mInitPage - page);
        }

        return Observable.create(new Observable.OnSubscribe<List<Manga>>() {
            @Override
            public void call ( Subscriber<? super List<Manga>> subscriber ) {
                String url = LATEST_URL;
                if (mNextPage >= 0) {
                    url += "/" + String.valueOf(mNextPage) + "00";
                }

                Document document = getDocument(url);
                if (document == null) {
                    subscriber.onError(new RuntimeException("Error fetching from: " + url));
                    subscriber.onCompleted();
                    return;
                }

                updateNextId(document);

                List<Manga> mangas = grepMangas(document);
                subscriber.onNext(mangas);
                subscriber.onCompleted();
            }
        });
    }

    /**
     * Fetch the {@link Chapter} pages.
     *
     * @param chapter current chapter
     * @return Observable that emits a new instance from {@link Chapter} with all {@link Page}s
     */
    @NonNull
    public static Observable<Chapter> getCompleteChapter ( @NonNull final Chapter chapter ) {

        return Observable.create(new Observable.OnSubscribe<Chapter>() {
            @Override
            public void call ( final Subscriber<? super Chapter> subscriber ) {
                final Chapter chapterCopy = new Chapter(chapter.getName(), chapter.getUrl());
                String url = chapterCopy.getUrl();
                Document document = getDocument(url);
                if (document == null) {
                    subscriber.onError(new RuntimeException("Error fetching from: " + url));
                    subscriber.onCompleted();
                    return;
                }

                Element pageMenu = document.getElementById("pageMenu");
                if (pageMenu == null) {
                    subscriber.onError(new RuntimeException("Error select id:pageMenu in: " + document.body()));
                    subscriber.onCompleted();
                    return;
                }

                final Elements options = pageMenu.getElementsByTag("option");
                if (options == null || options.isEmpty()) {
                    subscriber.onError(new RuntimeException("Error fetch page count"));
                    subscriber.onCompleted();
                    return;
                }

                chapterCopy.setPageCount(options.size());
                final SparseArrayCompat<Page> pages = chapterCopy.getPages();
                if (pages == null) {
                    subscriber.onError(new RuntimeException("Error pages is null"));
                    subscriber.onCompleted();
                    return;
                }

                final int[] done = {0};
                for (Element option : options) {
                    getPage(BASE_URL + option.attr("value"), Integer.parseInt(option.html()))
                            .filter(new Func1<Page, Boolean>() {
                                @Override
                                public Boolean call ( Page page ) {
                                    return page != null && !TextUtils.isEmpty(page.getUrl());
                                }
                            })
                            .subscribe(
                                    new Action1<Page>() {
                                        @Override
                                        public void call ( Page page ) {
                                            pages.put(page.getPage() - 1, page);
                                        }
                                    },
                                    new Action1<Throwable>() {
                                        @Override
                                        public void call ( Throwable throwable ) {
                                            throwable.printStackTrace();
                                        }
                                    },
                                    new Action0() {
                                        @Override
                                        public void call () {
                                            done[0]++;
                                            if (done[0] >= options.size()) {
                                                subscriber.onNext(chapterCopy);
                                                subscriber.onCompleted();
                                            }
                                        }
                                    });
                }
            }
        });
    }

    /**
     * Fetch the {@link Page} from {@link Chapter} with given page number.
     * Observable will complete after fetching entry.
     *
     * @param chapter current chapter
     * @param page    number of page
     * @return Observable for {@link Page} or {@code error}
     */
    @NonNull
    public static Observable<Page> getPage ( @NonNull final Chapter chapter, int page ) {
        SparseArrayCompat<Page> pages = chapter.getPages();

        if (pages != null && pages.get(page - 1) != null) {
            return Observable.just(pages.get(page - 1));

        } else {
            return getPage(chapter.getUrl() + "/" + String.valueOf(page), page);
        }
    }

    /**
     * Fetch the {@link Page} from {@link Chapter} with given page number.
     * Observable will complete after fetching entry.
     *
     * @param pageUrl url for chapter page
     * @param page    number of page
     * @return Observable for {@link Page} or {@code null}
     */
    @NonNull
    public static Observable<Page> getPage ( @NonNull final String pageUrl, final int page ) {

        return Observable.create(new Observable.OnSubscribe<Page>() {
            @Override
            public void call ( Subscriber<? super Page> subscriber ) {
                Document document = getDocument(pageUrl);
                if (document == null) {
                    subscriber.onError(new RuntimeException("Error fetching from: " + pageUrl));
                    subscriber.onCompleted();
                    return;
                }

                Element img = document.getElementById("img");
                if (img == null) {
                    subscriber.onError(new RuntimeException("Error select id:img in: " + document.body()));
                    subscriber.onCompleted();
                    return;
                }

                subscriber.onNext(new Page(page, img.attr("src")));
                subscriber.onCompleted();
            }
        });
    }

    @Nullable
    private static Document getDocument ( @NonNull String url ) {
        try {
            return Jsoup.connect(url)
                    .header("Cache-control", "no-cache")
                    .header("Cache-store", "no-store")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateNextId ( @NonNull Document document ) {
        if (mInitPage < 0) {
            mInitPage = getPageFromTitle(document) - 1;
            mNextPage = mInitPage;
        }

        mNextPage = Math.max(0, mNextPage - 1);
    }

    private int getPageFromTitle ( @NonNull Document document ) {
        String title = document.title();
        if (TextUtils.isEmpty(title)) {
            return -1;
        }

        int lastSpace = title.lastIndexOf(' ');
        if (lastSpace < 0) {
            return -1;
        }

        try {
            return Integer.parseInt(title.substring(lastSpace + 1));

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private synchronized List<Manga> grepMangas ( @NonNull Document document ) {
        List<Manga> mangaList = new ArrayList<>();
        HashMap<String, List<Chapter>> chapterMap = new HashMap<>();

        Element table = document.select("table.updates").first();
        if (table == null) {
            return mangaList;
        }

        Elements chapters = table.select("a.chaptersrec");
        for (Element element : chapters) {
            String url = element.attr("href");
            if (TextUtils.isEmpty(url)) {
                continue;
            }

            String key = url.substring(0, url.lastIndexOf('/'));
            Chapter chapter = new Chapter(element.text(), BASE_URL + url);

            List<Chapter> chapterList = chapterMap.get(key);
            if (chapterList == null) {
                chapterList = new ArrayList<>();
                chapterMap.put(key, chapterList);
            }

            chapterList.add(chapter);
        }

        Elements mangas = table.select("a.chapter");
        for (Element element : mangas) {
            String url = element.attr("href");
            if (TextUtils.isEmpty(url)) {
                continue;
            }

            mangaList.add(new MangaBuilder(element.text())
                    .setUrl(BASE_URL + url)
                    .setChapters(chapterMap.get(url))
                    .setCoverUrl(String.format(IMAGE_URL, (int) (Math.random() * 5)) + url + url + "-l0.jpg")
                    .build());

            chapterMap.remove(url);
        }

        return mangaList;
    }
}
