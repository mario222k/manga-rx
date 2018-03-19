package de.mario222k.mangarx.plugin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import de.mario222k.mangarx.R
import java.util.*

class PluginProvider(context: Context) {
    private val pluginFilter = context.getString(R.string.provider_name)
    var plugins = ArrayList<PluginDetail>()

    var activePlugin: PluginDetail? = null

    init {
        // package manager is used to retrieve the system's packages
        val packageManager = context.packageManager
        // we need an intent that will be used to load the packages
        val intent = Intent(pluginFilter)
        val availableActivities = packageManager.queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER);
        // for each one we create a custom list view item
        for (resolveInfo in availableActivities) {
            val applicationDetail = PluginDetail(resolveInfo)
            plugins.add(applicationDetail)
        }
    }
}
