package de.mario222k.mangarxinterface.model

import android.os.Parcel
import android.os.Parcelable

class Page : Parcelable {
    var page: Int = 0
        private set
    var url: String = ""
        private set

    constructor(page: Int, url: String) {
        this.page = page
        this.url = url
    }

    constructor(`in`: Parcel) {
        page = `in`.readInt()
        url = `in`.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(page)
        dest.writeString(url)
    }

    companion object {
        @JvmField // requested by android to keep static field
        val CREATOR: Parcelable.Creator<Page> = object : Parcelable.Creator<Page> {
            override fun createFromParcel(`in`: Parcel) = Page(`in`)
            override fun newArray(size: Int) = Array(size, {i -> Page(-1, "")})
        }
    }
}
