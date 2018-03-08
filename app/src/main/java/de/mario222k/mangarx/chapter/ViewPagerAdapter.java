package de.mario222k.mangarx.chapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import de.mario222k.mangarx.R;

/**
 * Created by mariokreussel on 08.05.14.
 * <p>
 * PagerAdapter that can handle Views with caching
 */
public abstract class ViewPagerAdapter<T extends View> extends PagerAdapter {

    private Context mContext;
    private T mCurrentView;

    @Nullable
    private T mRecycleView;

    /**
     * @param context context
     */
    public ViewPagerAdapter (@NonNull Context context) {
        mContext = context;
    }

    /**
     * this method should initialize convertView
     *
     * @param container the parent
     * @param item      a recycled Item that can be used or {@code null}
     * @param position  the item position from controller
     * @return new instance from {@code T}
     */
    public abstract T createView (@NonNull ViewGroup container, @Nullable T item, int position);

    /**
     * Position is initialized by {@code createView()}.
     * Each call will add view to container, if view has no parent.
     *
     * @param container current view group
     * @param position  adapter position
     * @return updated view from class type
     */
    @NonNull
    @Override
    public Object instantiateItem (@NonNull ViewGroup container, int position) {
        final T updatedView;

        updatedView = createView(container, mRecycleView, position);

        if (mRecycleView == updatedView) {
            mRecycleView = null;
        }

        updatedView.setTag(R.id.viewpager_adapter_item_dirty_key, false);

        if (updatedView.getParent() == null) {
            //add view to parent
            container.addView(updatedView);
        }

        return updatedView;
    }

    /**
     * Each call will remove view from container
     *
     * @param container current view group
     * @param position  adapter position
     * @param object    view from class type
     */
    @Override
    public void destroyItem (@NonNull ViewGroup container, int position, @NonNull Object object) {
        //noinspection unchecked
        final T view = (T) object;

        // remove view from parent
        container.removeView(view);

        mRecycleView = view;
    }


    @Override
    public void setPrimaryItem (@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        //noinspection unchecked
        mCurrentView = (T) object;
    }

    @Override
    public boolean isViewFromObject (@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public int getItemPosition (@NonNull Object object) {
        if (object instanceof View && (Boolean) ((View) object).getTag(R.id.viewpager_adapter_item_dirty_key)) {
            return POSITION_NONE; // force recreation
        }
        return super.getItemPosition(object);
    }

    @NonNull
    protected Context getContext () {
        return mContext;
    }

    @Nullable
    public T getCurrentView () {
        return mCurrentView;
    }
}
