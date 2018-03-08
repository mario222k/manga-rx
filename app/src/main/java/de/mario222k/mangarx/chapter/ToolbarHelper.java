package de.mario222k.mangarx.chapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Helper for all Toolbar Stuff.
 * Created by Mario.Sorge on 27/12/15.
 */
public class ToolbarHelper {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.getMainLooper());

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run () {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run () {
            // Delayed display of UI elements
            ActionBar actionBar = mActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            if (mControlsView != null) {
                mControlsView.setVisibility(View.VISIBLE);
            }
        }
    };

    @NonNull
    private AppCompatActivity mActivity;
    @Nullable
    private View mControlsView;
    private View mContentView;
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run () {
            hide();
        }
    };

    public ToolbarHelper (@NonNull AppCompatActivity activity,
                          @Nullable View controlsView,
                          @NonNull View contentView) {
        mActivity = activity;
        mControlsView = controlsView;
        mContentView = contentView;
    }

    public void setVisible (boolean isVisible) {
        mVisible = isVisible;
    }

    public void toggle () {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    public void hide () {
        // Hide UI first
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (mControlsView != null) {
            mControlsView.setVisibility(View.GONE);
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    public void show () {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    public void delayedHide (int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Get visible state.
     *
     * @return {@code true} if visible
     */
    public boolean isVisible () {
        return mVisible;
    }

    @NonNull
    public AppCompatActivity getActivity () {
        return mActivity;
    }
}
