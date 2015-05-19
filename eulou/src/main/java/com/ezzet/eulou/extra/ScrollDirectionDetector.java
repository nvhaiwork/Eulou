package com.ezzet.eulou.extra;

import android.view.View;
import android.widget.AbsListView;

/**
 * Created by tiennt on 5/19/15.
 */
public abstract class ScrollDirectionDetector implements AbsListView.OnScrollListener {

    protected abstract void onScrollDown();
    protected abstract void onScrollUp();

    private int mLastFirstVisibleItem;
    private int mLastTop;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View firstChild = view.getChildAt(0);
        int top = firstChild == null ? 0 : firstChild.getTop();
        if (mLastFirstVisibleItem < firstVisibleItem) {
            onScrollDown();
        } else if (mLastFirstVisibleItem > firstVisibleItem) {
            onScrollUp();
        } else {
        }
        mLastFirstVisibleItem = firstVisibleItem;
    }
}
