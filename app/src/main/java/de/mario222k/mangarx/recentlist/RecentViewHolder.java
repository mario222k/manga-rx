package de.mario222k.mangarx.recentlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.mario222k.mangarx.R;
import de.mario222k.mangarx.storage.ChapterStorage;
import de.mario222k.mangarx.utils.ColorUtils;
import de.mario222k.mangarxinterface.model.Chapter;
import de.mario222k.mangarxinterface.model.Manga;

/**
 * ViewHolder for {@link RecentAdapter}
 * Created by Mario.Sorge on 15/12/15.
 */
class RecentViewHolder extends RecyclerView.ViewHolder {

    public static RecentViewHolder create (@NonNull Context context, @NonNull ChapterStorage storage) {
        @SuppressLint("InflateParams")
        CardView cardView = (CardView) LayoutInflater.from(context).inflate(R.layout.layout_recent_item, null);
        return new RecentViewHolder(cardView, storage);
    }

    private ChapterStorage mStorage;

    private CardView mCardView;
    private ImageView mCoverImage;
    private TextView mTitleText;
    private LinearLayout mChapterContainer;

    private RecentViewHolder (@NonNull CardView cardView, @NonNull ChapterStorage storage) {
        super(cardView);

        cardView.setClickable(true);
        cardView.setForeground(cardView.getContext().getDrawable(R.drawable.transparent_ripple));

        mStorage = storage;
        mCardView = cardView;
        mCoverImage = cardView.findViewById(R.id.cover_image);
        mTitleText = cardView.findViewById(R.id.title_text);
        mChapterContainer = cardView.findViewById(R.id.chapter_container);
    }

    public void update (@Nullable final Manga manga, @Nullable final RecentAdapter.OnClickListener listener) {
        if (manga == null || manga.getChapters().isEmpty()) {
            mCardView.setVisibility(View.GONE);
            return;
        }

        mCardView.setVisibility(View.VISIBLE);
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (listener == null) {
                    return;
                }
                listener.onMangaClick(v, manga);
            }
        });

        char character = (manga.getName().length() > 0) ? manga.getName().toUpperCase(Locale.US).charAt(0) : ' ';
        int color = ColorUtils.getColorFromChar(mCoverImage.getContext(), character);
        mCoverImage.setBackgroundColor(color);

        if (!TextUtils.isEmpty(manga.getCover())) {
            Picasso.with(mCoverImage.getContext()).load(manga.getCover()).into(mCoverImage);
        } else {
            mCoverImage.setImageBitmap(null);
        }

        mTitleText.setText(manga.getName());

        mChapterContainer.removeAllViews();
        if (!manga.getChapters().isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(mChapterContainer.getContext());

            for (final Chapter chapter : manga.getChapters()) {
                if (TextUtils.isEmpty(chapter.getName())) {
                    continue;
                }

                if (mChapterContainer.getChildCount() > 0) {
                    inflater.inflate(R.layout.view_seperator, mChapterContainer);
                }

                @SuppressLint("InflateParams") final TextView chapterText = (TextView) inflater.inflate(R.layout.layout_recent_chapter_item, null);
                chapterText.setText(chapter.getName());
                chapterText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick (View v) {
                        Drawable drawable = chapterText.getCompoundDrawables()[2];
                        int level = drawable.getLevel();
                        switch (level) {
                            case 3: // ignore
                                break;
                            case 2: // ignore
                                break;
                            case 1:
                                mStorage.setChapterBookmark(chapter, false);
                                drawable.setLevel(0);
                                v.invalidate();
                                break;
                            case 0:
                            default:
                                mStorage.setChapterBookmark(chapter, true);
                                drawable.setLevel(1);
                                v.invalidate();
                                break;
                        }

                        return true;
                    }
                });

                chapterText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View v) {
                        if (listener == null) {
                            return;
                        }
                        listener.onChapterClick(v, chapter);
                    }
                });

                int level;
                if (mStorage.isChapterCompleteReaded(chapter)) {
                    level = 3;
                } else if (mStorage.getLastChapterPageReaded(chapter) > 0) {
                    level = 2;
                } else if (mStorage.isChapterBookmarked(chapter)) {
                    level = 1;
                } else {
                    level = 0;
                }

                Drawable drawable = chapterText.getResources().getDrawable(R.drawable.chapter_state);
                drawable.setLevel(level);
                drawable.setColorFilter(new PorterDuffColorFilter(chapterText.getCurrentTextColor(), PorterDuff.Mode.SRC_IN));
                chapterText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

                mChapterContainer.addView(chapterText);
            }
            mChapterContainer.setVisibility(View.VISIBLE);
        } else {
            mChapterContainer.setVisibility(View.GONE);
        }
    }
}
