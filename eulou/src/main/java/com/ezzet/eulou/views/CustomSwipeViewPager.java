package com.ezzet.eulou.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Created by hainguyen on 12/18/14.
 */
public class CustomSwipeViewPager extends ViewPager {

	private boolean canSwipe;

	public void setCanSwipe(boolean canSwipe) {

		this.canSwipe = canSwipe;
	}

	public CustomSwipeViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		canSwipe = true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (canSwipe) {

			return super.onInterceptTouchEvent(ev);
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (canSwipe) {

			return super.onTouchEvent(ev);
		}

		return false;

	}
}
