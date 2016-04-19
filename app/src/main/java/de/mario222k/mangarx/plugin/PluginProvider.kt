package de.mario222k.mangarx.plugin

import android.content.Context
import android.content.Intent
import java.util.*

class PluginProvider(context: Context) {
    val pluginFilter = "de.mario222k.mangarx.PICK_PLUGIN"
    var plugins = ArrayList<PluginDetail>()

    var activePlugin: PluginDetail? = null

    init {
        // package manager is used to retrieve the system's packages
        val packageManager = context.packageManager
        // we need an intent that will be used to load the packages
        val intent = Intent(Intent.ACTION_MAIN, null);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // in this case we want to load all packages available with our filter
        intent.addCategory(pluginFilter);
        val availableActivities = packageManager.queryIntentActivities(intent, 0);
        // for each one we create a custom list view item
        for (resolveInfo in availableActivities) {
            val packageName = resolveInfo.activityInfo.packageName
            val info = packageManager.getPackageInfo(packageName, 0)
            val applicationDetail = PluginDetail(
                    resolveInfo.loadLabel(packageManager).toString(),
                    packageName,
                    info.versionName,
                    resolveInfo.activityInfo.loadIcon(packageManager));
            plugins.add(applicationDetail);
        }
    }
}
