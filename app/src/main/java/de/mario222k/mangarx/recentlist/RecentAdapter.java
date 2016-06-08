package de.mario222k.mangarx.recentlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.mario222k.mangarx.storage.ChapterStorage;
import de.mario222k.mangarxinterface.model.Chapter;
import de.mario222k.mangarxinterface.model.Manga;
import de.mario222k.mangarxinterface.provider.IProviderInterface;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Adapter for recent Manga Chapters.
 * Created by Mario.Sorge on 13/12/15.
 */
public class RecentAdapter extends RecyclerView.Adapter {

    private ChapterStorage mStorage;
    private Context mContext;

    @Nullable
    private IProviderInterface mInterface;

    @Nullable
    private OnClickListener mClickListener;

    @NonNull
    private BehaviorSubject<Boolean> mIsLoadingSubject;

    @NonNull
    private List<Manga> mRecentMangas;
    private int mNextPage = 0;

    public RecentAdapter ( @NonNull Context context, @NonNull ChapterStorage storage ) {
        mContext = context;
        mStorage = storage;
        mRecentMangas = new ArrayList<>();

        mIsLoadingSubject = BehaviorSubject.create(false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder ( ViewGroup parent, int viewType ) {
        return RecentViewHolder.create(mContext, mStorage);
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void onBindViewHolder ( RecyclerView.ViewHolder holder, int position ) {
        if (position > mRecentMangas.size() - 5) {
            fetchMore();
        }

        RecentViewHolder recentViewHolder = (RecentViewHolder) holder;

        if (mRecentMangas.size() > position) {
            final Manga manga = mRecentMangas.get(position);
            recentViewHolder.update(manga, mClickListener);

        } else {
            recentViewHolder.update(null, null);
        }
    }

    @Override
    public int getItemCount () {
        return mRecentMangas.size();
    }

    public void setOnClickListener ( @Nullable OnClickListener listener ) {
        mClickListener = listener;
    }

    public void setInterface ( IProviderInterface providerInterface ) {
        mInterface = providerInterface;
        mIsLoadingSubject.onNext(false);
    }

    public void refresh () {
        if (mIsLoadingSubject.getValue()) {
            return;
        }

        mRecentMangas.clear();
        notifyDataSetChanged();

        mNextPage = 0;
        fetchMore();
    }

    public Observable<Boolean> getLoadingObservable () {
        return mIsLoadingSubject;
    }

    public void fetchMore () {
        if (mIsLoadingSubject.getValue() || mInterface == null) {
            return;
        }

        mIsLoadingSubject.onNext(true);
        try {
            List<Manga> mangas = mInterface.getLatestMangas(mNextPage);
            mRecentMangas.addAll(mangas);
            notifyDataSetChanged();
            mNextPage++;
        } catch (RemoteException e) {
            e.printStackTrace();

        } finally {
            mIsLoadingSubject.onNext(false);
        }
    }

    public interface OnClickListener {
        void onMangaClick ( @NonNull View view, @NonNull Manga item );

        void onChapterClick ( @NonNull View view, @NonNull Chapter item );
    }
}
