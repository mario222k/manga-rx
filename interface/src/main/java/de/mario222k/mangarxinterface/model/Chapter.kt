package de.mario222k.mangarxinterface.model

import android.os.Parcel
import android.os.Parcelable
import android.support.v4.util.SparseArrayCompat

class Chapter : Parcelable {
    var name: String? = null
        private set
    var url: String? = null
        private set
    var pages: SparseArrayCompat<Page>? = null
        private set

    constructor(name: String, url: String) {
        this.name = name
        this.url = url
        pages = null
    }

    /**
     * Hold information about loaded pages. Should be equal with
     * Pages size or `-1` for not loaded.

     * @return chapter page count or `-1`
     */
    var pageCount: Int
        get() = if (pages == null) -1 else pages?.size()
        set(pageCount) {
            if (pages != null) {
                pages?.clear()
            }

            if (pageCount >= 0) {
                pages = SparseArrayCompat<Page>(pageCount)

            } else {
                pages = null
            }
        }

    private constructor(`in`: Parcel) {
        name = `in`.readString()
        url = `in`.readString()
        @Suppress("UNCHECKED_CAST")
        setPagesFromArray(`in`.readParcelableArray(Page::class.java.getClassLoader()) as Array<Page>?)
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(url)
        dest.writeParcelableArray(pagesAsArray, flags)
    }

    private val pagesAsArray: Array<Page>?
        get() {
            if (pages == null) {
                return null
            }

            val length = pages?.size() ?: 0
            return Array(length, { i -> pages?.get(i) ?: Page(i, "") })
        }

    private fun setPagesFromArray(array: Array<Page>?) {
        if (array == null) {
            pageCount = -1
            return
        }

        pageCount = array.size

        for (i in 0..pageCount - 1) {
            pages?.put(i, array[i])
        }
    }

    companion object {
        @JvmField // requested by android to keep static field
        val CREATOR: Parcelable.Creator<Chapter> = object : Parcelable.Creator<Chapter> {
            override fun createFromParcel(`in`: Parcel) = Chapter(`in`)
            override fun newArray(size: Int) = Array(size, { i -> Chapter("", "") })
        }
    }
}
