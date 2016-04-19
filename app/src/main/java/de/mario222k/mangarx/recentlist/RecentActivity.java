package de.mario222k.mangarx.recentlist;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import javax.inject.Inject;

import de.mario222k.mangarx.R;
import de.mario222k.mangarx.application.MyApp;
import de.mario222k.mangarx.application.PluginDialogFragment;
import de.mario222k.mangarx.application.PluginSelectListener;
import de.mario222k.mangarx.chapter.ChapterActivity;
import de.mario222k.mangarx.plugin.PluginDetail;
import de.mario222k.mangarx.storage.RecentStorage;
import de.mario222k.mangarxinterface.model.Chapter;
import de.mario222k.mangarxinterface.model.Manga;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RecentActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 3835;

    @Inject
    RecentAdapter mRecentAdapter;

    @Inject
    RecentStorage mStorage;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        ((MyApp) getApplication()).getRecentComponent().inject(this);

        setContentView(R.layout.activity_recent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(android.R.color.white);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh () {
                mRecentAdapter.refresh();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets ( Rect outRect, View view, RecyclerView parent, RecyclerView.State state ) {
                int left = getResources().getDimensionPixelOffset(R.dimen.recent_item_margin_left);
                int top = getResources().getDimensionPixelOffset(R.dimen.recent_item_margin_top);
                int right = getResources().getDimensionPixelOffset(R.dimen.recent_item_margin_right);
                int bottom = getResources().getDimensionPixelOffset(R.dimen.recent_item_margin_bottom);
                outRect.set(left, top, right, bottom);
            }
        });

        mRecentAdapter.setOnClickListener(new RecentAdapter.OnClickListener() {
            @Override
            public void onMangaClick ( @NonNull View view, @NonNull Manga item ) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(item.getUrl()));
                startActivity(i);
            }

            @Override
            public void onChapterClick ( @NonNull View view, @NonNull Chapter item ) {
                Intent i = new Intent(RecentActivity.this, ChapterActivity.class);
                i.putExtra("chapter", item);
                startActivityForResult(i, REQUEST_CODE);

                mStorage.setLastChapter(item);
            }
        });

        mRecentAdapter.getLoadingObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call ( Boolean isLoading ) {
                        refreshLayout.setRefreshing(isLoading);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call ( Throwable throwable ) {
                        throwable.printStackTrace();
                        refreshLayout.setRefreshing(false);
                    }
                });
        recyclerView.setAdapter(mRecentAdapter);
    }

    @Override
    protected void onResume () {
        super.onResume();

        Chapter lastChapter = mStorage.getLastChapter();
        if (lastChapter != null) {
            Intent i = new Intent(RecentActivity.this, ChapterActivity.class);
            i.putExtra("chapter", lastChapter);
            startActivityForResult(i, REQUEST_CODE);
            return;
        }

        if (mRecentAdapter.getItemCount() == 0) {
            mRecentAdapter.fetchMore();
        }
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data ) {
        if(requestCode == REQUEST_CODE) {
            mStorage.setLastChapter(null);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_recent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected ( MenuItem item ) {
        if (item.getItemId() != R.id.plugin_menu_item) {
            return super.onOptionsItemSelected(item);
        }
        showPlugins();
        return true;
    }

    private void showPlugins() {
        final PluginDialogFragment dialogFragment = new PluginDialogFragment();
        dialogFragment.setPluginSelectListener(new PluginSelectListener() {
            @Override
            public void onPluginSelect ( @Nullable PluginDetail plugin ) {
                dialogFragment.dismiss();
                if(plugin == null) {
                    return;
                }

                Toast.makeText(RecentActivity.this, plugin.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        dialogFragment.show(getSupportFragmentManager(), "pluginDialog");
    }
}
