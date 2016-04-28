package de.mario222k.mangarx.plugin

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable

class PluginDetail: Parcelable {

    private lateinit var info: ResolveInfo

    constructor(resolveInfo: ResolveInfo) {
        info = resolveInfo
    }

    constructor(source: Parcel) {
        info = source.readParcelable(ResolveInfo::class.java.classLoader)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(info, flags)
    }

    override fun describeContents() = 0

    override fun toString() = "$info"

    fun getIcon(context: Context): Drawable {
        return info.serviceInfo.loadIcon(context.packageManager)
    }

    fun getName(context: Context): String {
        return info.loadLabel(context.packageManager).toString()
    }

    fun getVersion(context: Context): String {
        return context.packageManager.getPackageInfo(getPackage(), 0).versionName
    }

    fun getPackage(): String {
        return info.serviceInfo.packageName;
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PluginDetail) {
            return false
        }

        return info.equals(other.info)
    }

    override fun hashCode(): Int {
        return info.hashCode()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PluginDetail> = object : Parcelable.Creator<PluginDetail> {
            override fun createFromParcel(`in`: Parcel) = PluginDetail(`in`)
            override fun newArray(size: Int) = Array(size, { i -> PluginDetail(ResolveInfo()) })
        }
    }
}