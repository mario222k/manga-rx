package de.mario222k.mangarx.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * ViewPager wrap for PhotoView support.
 * Original ViewPager will crash with java.lang.IllegalArgumentException: pointerIndex out of range.
 * <p>
 * Created by Mario.Sorge on 29/12/15.
 */
public class PhotoViewPager extends android.support.v4.view.ViewPager {

    public PhotoViewPager (Context context) {
        super(context);
    }

    public PhotoViewPager (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent (MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}