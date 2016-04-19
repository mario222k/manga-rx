package de.mario222k.mangarx.plugin

import android.graphics.drawable.Drawable

data class PluginDetail(
        val label: String,
        val name: String,
        val version: String,
        val icon: Drawable? = null) {

    override fun toString() = "$label ($name): $version"
}