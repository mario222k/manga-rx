package de.mario222k.mangarx.chapter;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import de.mario222k.mangarx.BuildConfig;
import de.mario222k.mangarx.R;
import de.mario222k.mangarx.application.MyApp;
import de.mario222k.mangarx.plugin.PluginConnection;
import de.mario222k.mangarx.plugin.PluginDetail;
import de.mario222k.mangarx.storage.ChapterStorage;
import de.mario222k.mangarx.utils.ColorUtils;
import de.mario222k.mangarxinterface.model.Chapter;
import de.mario222k.mangarxinterface.model.Page;
import de.mario222k.mangarxinterface.provider.IProviderInterface;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ChapterActivity extends AppCompatActivity {

    private final String TAG = ChapterActivity.class.getSimpleName();

    @Inject
    PluginConnection mPluginConnection;

    @Inject
    ChapterStorage mStorage;

    private ToolbarHelper mToolbarHelper;
    private ViewPagerAdapter<PhotoView> mViewAdapter;

    private ViewPager mViewPager;
    private TextView mCrouton;
    private Runnable mCroutonHideRunnable = new Runnable() {
        @Override
        public void run () {
            mCrouton.setVisibility(View.GONE);
        }
    };

    private int mCurrentPosition;

    @NonNull
    private Chapter mChapter;
    @Nullable
    private PluginDetail mSelectedPlugin;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApp) getApplication()).getChapterComponent().inject(this);

        setContentView(R.layout.activity_chapter);
        mChapter = getIntent().getParcelableExtra("chapter");
        mSelectedPlugin = getIntent().getParcelableExtra("plugin");

        Log.i(TAG, "init chapter activity");

        //noinspection ConstantConditions
        if (mChapter == null || mSelectedPlugin == null) {
            // cancel if not set
            if (BuildConfig.DEBUG) {
                showText("chapter: " + mChapter + ", plugin: " + mSelectedPlugin);
            }
            finish();
            return;
        }

        mPluginConnection.setListener(new PluginConnection.Listener() {
            @Override
            public void onConnected () {
                if (mSelectedPlugin == null) {
                    return;
                }
                if (BuildConfig.DEBUG) {
                    showText("connected: " + mSelectedPlugin.getName(getApplicationContext()));
                }

                IProviderInterface providerInterface = mPluginConnection.getBinder();
                if (providerInterface != null) {
                    onPluginConnected(providerInterface);
                }
            }

            @Override
            public void onDisconnected () {
                if (mSelectedPlugin == null) {
                    return;
                }
                if (BuildConfig.DEBUG) {
                    showText("disconnected: " + mSelectedPlugin.getName(getApplicationContext()));
                }
            }
        });

        // set title to chapter name until user starts paging
        setTitle(mChapter.getName());

        mViewAdapter = new ViewPagerAdapter<PhotoView>(this) {
            @Override
            public PhotoView createView (@NonNull ViewGroup container, @Nullable PhotoView item, int position) {
                return createOrUpdateImageView(item, position);
            }

            @Override
            public int getCount () {
                int pageCount = getPageCount();
                // allow paging even when MangaReader.getCompleteChapter() is not finished yet
                return pageCount < 0 ? Integer.MAX_VALUE : pageCount;
            }
        };

        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setEnabled(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected (int position) {
                onPositionChanged(position);
            }

            @Override
            public void onPageScrollStateChanged (int state) {
            }
        });

        mCrouton = findViewById(R.id.crouton_text);

        mToolbarHelper = new ToolbarHelper(this, null, mViewPager);
        mToolbarHelper.setVisible(true);
    }

    @Override
    protected void onPostCreate (Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // hide toolbar after user has seen this
        mToolbarHelper.delayedHide(100);
    }

    @Override
    protected void onResume () {
        super.onResume();
        if (mSelectedPlugin != null) {
            mPluginConnection.connect(this, mSelectedPlugin.getPackage());
        }
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        /*
         * Update ScaleType for all ImageViews that are added to the ViewPager.
         */
        ImageView.ScaleType type;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            type = ImageView.ScaleType.CENTER_CROP;
        } else {
            type = ImageView.ScaleType.CENTER_INSIDE;
        }

        for (int i = 0; i < mViewPager.getChildCount(); i++) {
            View view = mViewPager.getChildAt(i);
            if (view instanceof PhotoView) {
                ((PhotoView) view).setScaleType(type);
                view.scrollTo(0, 0);
            }
        }
    }

    @Override
    protected void onPause () {
        super.onPause();
        mPluginConnection.disconnect(this);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        // avoid errors when activity is finished
        mCrouton.removeCallbacks(mCroutonHideRunnable);
    }

    /**
     * Will be triggered as soon as the plugin is connected. This will load async the requested chapter.
     *
     * @param providerInterface connected interface
     */
    private void onPluginConnected (@NonNull final IProviderInterface providerInterface) {
        // update adapter to start loading for visible pages
        replaceAdapter();

        // fetch all pages from chapter for faster paging, not needed but cool :)
        Observable.create(new Observable.OnSubscribe<Chapter>() {
            @Override
            public void call (Subscriber<? super Chapter> subscriber) {
                try {
                    if (getPageCount() <= 0) {
                        subscriber.onNext(providerInterface.getCompleteChapter(mChapter));
                    } else {
                        subscriber.onNext(mChapter);
                    }

                } catch (RemoteException e) {
                    subscriber.onError(e.getCause());
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Chapter, Boolean>() {
                    @Override
                    public Boolean call (Chapter chapter) {
                        return chapter != null && !TextUtils.isEmpty(chapter.getUrl());
                    }
                })
                .subscribe(new Action1<Chapter>() {
                    @Override
                    public void call (@NonNull Chapter chapter) {
                        onChapterLoaded(chapter);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call (Throwable throwable) {
                        showText(throwable.getMessage());
                        finish();
                    }
                });
    }

    private int getPageCount() {
        List<Page> pages = mChapter.getPages();
        return (pages != null) ? pages.size() : 0;
    }

    /**
     * Will be triggered as soon as chapter was loaded. This will update the Adapter of the ViewPager.
     *
     * @param chapter loaded chapter
     */
    private void onChapterLoaded (@NonNull Chapter chapter) {
        if (isFinishing()) {
            return;
        }

        int page = mCurrentPosition;
        mChapter = chapter;
        replaceAdapter();

        if (!restoreLastPage()) {
            mViewPager.setCurrentItem(page, false);
        }
    }

    private void replaceAdapter () {
        mViewPager.setEnabled(true);
        mViewPager.setAdapter(null);
        Log.d(TAG, "replace chapter");
        mViewPager.setAdapter(mViewAdapter);
    }

    /**
     * Select last ridden page if possible.
     *
     * @return {@code true} if last ridden chapter page was restored
     */
    private boolean restoreLastPage () {
        if (isFinishing()) {
            return false;
        }

        int storedPage = mStorage.getLastChapterPageReaded(mChapter);
        if (mCurrentPosition + 1 == storedPage) {
            return false;
        }

        mViewPager.setCurrentItem(storedPage - 1);
        return true;
    }

    /**
     * Update the state of the PhotoView: photoTapListener, tag, background, scale type and image bitmap (async).
     *
     * @param imageView target view
     * @param position  within chapter
     * @return updated instance as given or new instance if was {@code null}
     */
    private PhotoView createOrUpdateImageView (@Nullable PhotoView imageView, final int position) {
        Log.d(TAG, "create page " + position + ", " + imageView);

        if (imageView == null) {
            // create new if not given
            imageView = new PhotoView(ChapterActivity.this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        // always scroll to image top --> doesn't work
        imageView.scrollTo(0, 0);
        imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap (View view, float x, float y) {
                mToolbarHelper.toggle();
            }
        });
        // store adapter position for async image loading
        imageView.setTag(R.id.viewpager_item_position, position);

        // determine background color from page number
        String s = String.valueOf(position);
        int character = s.charAt(s.length() - 1);
        if (s.length() > 1) {
            int d = s.charAt(s.length() - 2);
            if (d % 2 > 0) {
                character = 57 - character + 48;
            }
        }
        int color = ColorUtils.getColorFromChar(imageView.getContext(), character);
        imageView.setBackgroundColor(color);

        // reset imageview bitmap
        imageView.setImageBitmap(null);

        // set different scale type for each orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // full screen with scrolling, mostly match with manga row height
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // full image on screen
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        final IProviderInterface providerInterface = mPluginConnection.getBinder();
        if (providerInterface == null) {
            return imageView;
        }

        final ImageView finalImageView = imageView;
        Observable.create(new Observable.OnSubscribe<Page>() {
            @Override
            public void call (Subscriber<? super Page> subscriber) {
                try {
                    subscriber.onNext(providerInterface.getPage(mChapter, position + 1));

                } catch (RemoteException e) {
                    subscriber.onError(e.getCause());
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Page, Boolean>() {
                    @Override
                    public Boolean call (Page page) {
                        return page != null && !TextUtils.isEmpty(page.getUrl());
                    }
                })
                .subscribe(new Action1<Page>() {
                    @Override
                    public void call (@NonNull Page page) {
                        int ivPosition = (int) finalImageView.getTag(R.id.viewpager_item_position);
                        if (ivPosition != position) {
                            Log.w(TAG, "skip image loading for " + position);
                            return;
                        }
                        Log.d(TAG, "load image for " + position);
                        // load async image from url
                        Picasso.with(ChapterActivity.this).load(page.getUrl()).into(finalImageView);
                    }
                });

        return imageView;
    }

    /**
     * Update member and activity title. If Toolbar is not visible this will show a crouton.
     *
     * @param position new position
     */
    private void onPositionChanged (int position) {
        mCurrentPosition = position;
        int count = getPageCount();
        int page = position + 1;
        String title = getString(R.string.chapter_title, page, count);
        setTitle(title);

        if (!mToolbarHelper.isVisible()) {
            showCrouton(title);
        }

        if (page < count) {
            mStorage.setChapterPageReaded(mChapter, page);
        } else if (count >= 0) {
            mStorage.setChapterCompleted(mChapter);
        }
    }

    private void showText (String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show or update visible Crouton for 3 seconds.
     *
     * @param text content for crouton
     */
    private void showCrouton (@NonNull String text) {
        mCrouton.removeCallbacks(mCroutonHideRunnable);

        mCrouton.setText(text);
        mCrouton.setVisibility(View.VISIBLE);
        mCrouton.postDelayed(mCroutonHideRunnable, 3000);
    }
}
