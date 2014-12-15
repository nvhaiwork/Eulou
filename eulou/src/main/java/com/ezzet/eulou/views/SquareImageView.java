/**
 *
 */
package com.ezzet.eulou.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author ubuntu
 * 
 */
public class SquareImageView extends ImageView {

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable d = getDrawable();

		if (d != null) {

			setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
		} else {

			super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		}
	}
}
