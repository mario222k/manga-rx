package de.mario222k.mangarxinterface.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Manga : Parcelable {
    var name: String
    var url: String? = null
    var cover: String? = null
    val chapters: ArrayList<Chapter>

    constructor(name: String) {
        this.name = name
        chapters = ArrayList<Chapter>()
    }

    private constructor(`in`: Parcel) {
        name = `in`.readString()
        url = `in`.readString()
        cover = `in`.readString()
        chapters = ArrayList()
        @Suppress("UNCHECKED_CAST")
        setChaptersFromArray(`in`.readParcelableArray(Chapter::class.java.getClassLoader()) as Array<Chapter>?)
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(url)
        dest.writeString(cover)
        val array = Array(chapters.size, { i -> Chapter("", "") })
        chapters.toArray(array)
        dest.writeParcelableArray(array, flags)
    }

    private fun setChaptersFromArray(array: Array<Chapter>?) {
        val count = array?.size ?: -1

        for (i in 0..count - 1) {
            chapters.add(i, array!![i])
        }
    }

    companion object {
        @JvmField // requested by android to keep static field
        val CREATOR: Parcelable.Creator<Manga> = object : Parcelable.Creator<Manga> {
            override fun createFromParcel(`in`: Parcel) = Manga(`in`)
            override fun newArray(size: Int) = Array(size, { i -> Manga("") })
        }
    }
}
