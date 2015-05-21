package com.ezzet.eulou.extra;

import android.os.Build;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;

import com.larswerkman.quickreturnlistview.QuickReturnListView;

/**
 * Created by tiennt on 5/19/15.
 */
public abstract class ScrollDirectionDetector implements AbsListView.OnScrollListener {

    protected abstract int getQuickReturnHeight();
    protected abstract QuickReturnListView getListView();
    protected abstract View getQuickReturnView();
    protected abstract View getPlaceHolderView();

    private static final int STATE_ONSCREEN = 0;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;
    private int mState = STATE_ONSCREEN;
    private int mScrollY;
    private int mMinRawY = 0;
    private int mCachedVerticalScrollRange;

    private TranslateAnimation anim;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            QuickReturnListView mListView = getListView();
            mScrollY = 0;
            int translationY = 0;

            if (mListView.scrollYIsComputed()) {
                mScrollY = mListView.getComputedScrollY();
            }

        int rawY = getPlaceHolderView().getTop()
                    - Math.min(
                    mCachedVerticalScrollRange
                            - mListView.getHeight(), mScrollY);

            switch (mState) {
                case STATE_OFFSCREEN:
                    if (rawY <= mMinRawY) {
                        mMinRawY = rawY;
                    } else {
                        mState = STATE_RETURNING;
                    }
                    translationY = rawY;
                    break;

                case STATE_ONSCREEN:
                    if (rawY < - getQuickReturnHeight()) {
                        mState = STATE_OFFSCREEN;
                        mMinRawY = rawY;
                    }
                    translationY = rawY;
                    break;

                case STATE_RETURNING:
                    translationY = (rawY - mMinRawY) - getQuickReturnHeight();
                    if (translationY > 0) {
                        translationY = 0;
                        mMinRawY = rawY - getQuickReturnHeight();
                    }

                    if (rawY > 0) {
                        mState = STATE_ONSCREEN;
                        translationY = rawY;
                    }

                    if (translationY < -getQuickReturnHeight()) {
                        mState = STATE_OFFSCREEN;
                        mMinRawY = rawY;
                    }
                    break;
            }

            /** this can be used if the build is below honeycomb **/
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                anim = new TranslateAnimation(0, 0, translationY,
                        translationY);
                anim.setFillAfter(true);
                anim.setDuration(0);
                getQuickReturnView().startAnimation(anim);
            } else {
                getQuickReturnView().setTranslationY(translationY);
            }
    }
}
