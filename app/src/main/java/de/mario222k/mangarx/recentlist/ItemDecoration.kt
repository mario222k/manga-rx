package de.mario222k.mangarx.recentlist

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import de.mario222k.mangarx.R

class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    val left = context.resources.getDimensionPixelOffset(R.dimen.recent_item_margin_left)
    val top = context.resources.getDimensionPixelOffset(R.dimen.recent_item_margin_top)
    val right = context.resources.getDimensionPixelOffset(R.dimen.recent_item_margin_right)
    val bottom = context.resources.getDimensionPixelOffset(R.dimen.recent_item_margin_bottom)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        outRect.set(left, top, right, bottom)
    }
}
