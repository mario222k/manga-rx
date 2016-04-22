package de.mario222k.mangarx.chapter;

import android.content.res.Configuration;
import android.os.Bundle;
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

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import de.mario222k.mangarx.R;
import de.mario222k.mangarx.application.MyApp;
import de.mario222k.mangarxinterface.model.Chapter;
import de.mario222k.mangarxinterface.model.Page;
import de.mario222k.mangarx.provider.MangaReader;
import de.mario222k.mangarx.storage.ChapterStorage;
import de.mario222k.mangarx.utils.ColorUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ChapterActivity extends AppCompatActivity {
    
    private final String TAG = ChapterActivity.class.getSimpleName();

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
    private Chapter mChapter;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        ((MyApp) getApplication()).getChapterComponent().inject(this);

        setContentView(R.layout.activity_chapter);
        mChapter = getIntent().getParcelableExtra("chapter");
        
        Log.i(TAG, "init chapter activity");

        if (mChapter == null) {
            // cancel if not set
            finish();
            return;
        }

        // set title to chapter name until user starts paging
        setTitle(mChapter.getName());

        mViewAdapter = new ViewPagerAdapter<PhotoView>(this) {
            @Override
            public PhotoView createView ( @NonNull ViewGroup container, @Nullable PhotoView item, int position ) {
                return createOrUpdateImageView(item, position);
            }

            @Override
            public int getCount () {
                int pageCount = mChapter.getPageCount();
                // allow paging even when MangaReader.getCompleteChapter() is not finished yet
                return pageCount < 0 ? Integer.MAX_VALUE : pageCount;
            }
        };

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mViewAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled ( int position, float positionOffset, int positionOffsetPixels ) { }

            @Override
            public void onPageSelected ( int position ) {
                onPositionChanged(position);
            }

            @Override
            public void onPageScrollStateChanged ( int state ) { }
        });

        mCrouton = (TextView) findViewById(R.id.crouton_text);

        // fetch all pages from chapter for faster paging, not needed but cool :)
        MangaReader.Companion.getCompleteChapter(mChapter)
                .filter(new Func1<Chapter, Boolean>() {
                    @Override
                    public Boolean call ( Chapter chapter ) {
                        return chapter != null && chapter.getUrl().equals(mChapter.getUrl());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Chapter>() {
                    @Override
                    public void call ( Chapter chapter ) {
                        int page = mCurrentPosition;
                        mViewPager.setAdapter(null);
                        mChapter = chapter;
                        Log.d(TAG, "replace chapter");
                        mViewPager.setAdapter(mViewAdapter);
                        mViewPager.setCurrentItem(page, false);
                    }
                });

        mToolbarHelper = new ToolbarHelper(this, null, mViewPager);
        mToolbarHelper.setVisible(true);
    }

    @Override
    protected void onPostCreate ( Bundle savedInstanceState ) {
        super.onPostCreate(savedInstanceState);
        // hide toolbar after user has seen this
        mToolbarHelper.delayedHide(100);
    }

    @Override
    protected void onResume () {
        super.onResume();
        int storedPage = mStorage.getLastChapterPageReaded(mChapter);

        if(mCurrentPosition+1 != storedPage) {
            mViewPager.setCurrentItem(storedPage-1);
        }
    }

    @Override
    public void onConfigurationChanged ( Configuration newConfig ) {
        super.onConfigurationChanged(newConfig);

        /**
         * Update ScaleType for all ImageViews that are added to the ViewPager.
         */
        ImageView.ScaleType type;
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            type = ImageView.ScaleType.CENTER_CROP;
        } else {
            type = ImageView.ScaleType.CENTER_INSIDE;
        }

        for (int i = 0; i<mViewPager.getChildCount(); i++) {
            View view = mViewPager.getChildAt(i);
            if(view instanceof PhotoView) {
                ((PhotoView) view).setScaleType(type);
                view.scrollTo(0, 0);
            }
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        // avoid errors when activity is finished
        mCrouton.removeCallbacks(mCroutonHideRunnable);
    }

    private PhotoView createOrUpdateImageView ( @Nullable PhotoView imageView, final int position ) {
        Log.d(TAG, "create page " + position + ", " + imageView);

        if(imageView == null) {
            // create new if not given
            imageView = new PhotoView(ChapterActivity.this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        // always scroll to image top --> doesn't work
        imageView.scrollTo(0, 0);
        imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap ( View view, float x, float y ) {
                mToolbarHelper.toggle();
            }
        });
        // store adapter position for async image loading
        imageView.setTag(R.id.viewpager_item_position, position);

        // determine background color from page number
        String s = String.valueOf(position);
        int character = s.charAt(s.length()-1);
        if(s.length() > 1) {
            int d = s.charAt(s.length()-2);
            if(d % 2 > 0) {
                character = 57 - character + 48;
            }
        }
        int color = ColorUtils.getColorFromChar(imageView.getContext(), character);
        imageView.setBackgroundColor(color);

        // reset imageview bitmap
        imageView.setImageBitmap(null);

        // set different scale type for each orientation
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // full screen with scrolling, mostly match with manga row height
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // full image on screen
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        final PhotoView finalImageView = imageView;
        // fetch async image url
        MangaReader.Companion.getPage(mChapter, position + 1)
                .filter(new Func1<Page, Boolean>() {
                    @Override
                    public Boolean call ( Page page ) {
                        return page != null && !TextUtils.isEmpty(page.getUrl());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Page>() {
                    @Override
                    public void call ( Page page ) {
                        int ivPosition = (int) finalImageView.getTag(R.id.viewpager_item_position);
                        if (ivPosition != position) {
                            Log.w(TAG, "skip image loading for " + position);
                            return;
                        }
                        Log.d(TAG, "load image for " + position);
                        // load async image from url
                        Picasso.with(ChapterActivity.this).load(page.getUrl()).into(finalImageView);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call ( Throwable throwable ) {
                        throwable.printStackTrace();
                    }
                });

        return imageView;
    }

    /**
     * Update member and activity title. If Toolbar is not visible this will show a crouton.
     * @param position new position
     */
    private void onPositionChanged ( int position) {
        mCurrentPosition = position;
        int count = mChapter.getPageCount();
        int page = position + 1;
        String title = getString(R.string.chapter_title, page, count);
        setTitle(title);

        if(!mToolbarHelper.isVisible()) {
            showCrouton(title);
        }

        if(page < count) {
            mStorage.setChapterPageReaded(mChapter, page);
        } else if(count >= 0) {
            mStorage.setChapterCompleted(mChapter);
        }
    }

    /**
     * Show or update visible Crouton for 3 seconds.
     * @param text content for crouton
     */
    private void showCrouton ( @NonNull String text ) {
        mCrouton.removeCallbacks(mCroutonHideRunnable);

        mCrouton.setText(text);
        mCrouton.setVisibility(View.VISIBLE);
        mCrouton.postDelayed(mCroutonHideRunnable, 3000);
    }
}
