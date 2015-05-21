package com.ezzet.eulou.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by tiennt on 5/22/15.
 */
public class ScrollDetectingListView extends ListView implements AbsListView.OnScrollListener {
    private int mInitialScroll = 0;
    private OnScrollDirectionChangedListener mDirectionChangedListener;

    public ScrollDetectingListView(Context context) {
        super(context);
        init();
    }

    public ScrollDetectingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollDetectingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int scrolledOffset = computeVerticalScrollOffset();

        boolean scrollUp = scrolledOffset > mInitialScroll;
        mInitialScroll = scrolledOffset;
        if (mDirectionChangedListener != null) {
            if (scrollUp) {
                mDirectionChangedListener.onScrollUp();
            } else {
                mDirectionChangedListener.onScrollDown();
            }
        }
    }

    public void setDirectionChangeListener(OnScrollDirectionChangedListener directionChangedListener) {
        this.mDirectionChangedListener = directionChangedListener;
    }

    public interface OnScrollDirectionChangedListener {
        void onScrollUp();
        void onScrollDown();
    }
}
