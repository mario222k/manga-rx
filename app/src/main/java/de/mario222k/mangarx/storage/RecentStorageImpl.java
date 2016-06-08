package de.mario222k.mangarx.storage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import de.mario222k.mangarx.plugin.PluginDetail;
import de.mario222k.mangarx.plugin.PluginProvider;
import de.mario222k.mangarxinterface.model.Chapter;

/**
 *
 * Created by Mario.Sorge on 02/01/16.
 */
public class RecentStorageImpl implements RecentStorage {
    private final String CHAPTER_STORAGE_NAME = "recent_chapter";
    private final String PLUGIN_STORAGE_NAME = "recent_plugin";

    Application mApplication;
    PluginProvider mPluginProvider;

    public RecentStorageImpl ( @NonNull Application application, @NonNull PluginProvider pluginProvider ) {
        mApplication = application;
        mPluginProvider = pluginProvider;
    }

    @Override
    public boolean setLastChapter ( @Nullable Chapter chapter ) {
        SharedPreferences.Editor editor = mApplication.getSharedPreferences(CHAPTER_STORAGE_NAME, Context.MODE_PRIVATE).edit();

        if(chapter == null) {
            return editor
                    .remove("last_name")
                    .remove("last_url")
                    .commit();
        }

        return editor
                .putString("last_name", chapter.getName())
                .putString("last_url", chapter.getUrl())
                .commit();
    }

    @Override
    @Nullable
    public Chapter getLastChapter () {
        SharedPreferences preferences = mApplication.getSharedPreferences(CHAPTER_STORAGE_NAME, Context.MODE_PRIVATE);
        String name = preferences.getString("last_name", null);
        String url = preferences.getString("last_url", null);

        if(name == null || url == null) {
            return  null;
        }

        return new Chapter(name, url);
    }

    @Override
    public boolean setLastPlugin ( @Nullable PluginDetail plugin ) {
        SharedPreferences.Editor editor = mApplication.getSharedPreferences(PLUGIN_STORAGE_NAME, Context.MODE_PRIVATE).edit();

        if(plugin == null || TextUtils.isEmpty(plugin.getPackage())) {
            return editor.remove("last_plugin").commit();
        }

        return editor.putString("last_plugin", plugin.getPackage()).commit();
    }

    @Nullable
    @Override
    public PluginDetail getLastPlugin () {
        SharedPreferences preferences = mApplication.getSharedPreferences(PLUGIN_STORAGE_NAME, Context.MODE_PRIVATE);
        String pluginPackage = preferences.getString("last_plugin", null);

        if(TextUtils.isEmpty(pluginPackage)) {
            return  null;
        }

        for(PluginDetail plugin : mPluginProvider.getPlugins()) {
            if(pluginPackage.equals(plugin.getPackage())) {
                return plugin;
            }
        }

        return null;
    }
}
