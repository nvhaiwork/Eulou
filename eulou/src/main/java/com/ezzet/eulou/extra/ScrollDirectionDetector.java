package com.ezzet.eulou.extra;

import android.widget.AbsListView;

/**
 * Created by tiennt on 5/19/15.
 */
public abstract class ScrollDirectionDetector implements AbsListView.OnScrollListener {

    protected abstract void onScrollDown();
    protected abstract void onScrollUp();

    private int mLastFirstVisibleItem;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mLastFirstVisibleItem < firstVisibleItem) {
            onScrollDown();
        } else if (mLastFirstVisibleItem > firstVisibleItem) {
            onScrollUp();
        }
        mLastFirstVisibleItem = firstVisibleItem;
    }
}
