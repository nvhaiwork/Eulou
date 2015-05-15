/**
 *
 */
package com.ezzet.eulou.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.ezzet.eulou.R;

/**
 * @author nvhaiwork
 */
public class CustomFontClickTextView extends CustomFontTextView {

	private boolean isDefaultBg;
	public CustomFontClickTextView(Context paramContext) {
		super(paramContext);
		// setFont();
		init();
	}

	public CustomFontClickTextView(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		// setFont();
		init();
	}

	public CustomFontClickTextView(Context paramContext,
			AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		// setFont();
		init();
	}

	/**
	 * Init
	 * */
	private void init() {

		if (!isEnabled()) {

			setAlpha(0.7f);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#setPressed(boolean)
	 */
	@Override
	public void setPressed(boolean pressed) {
		// TODO Auto-generated method stub
		if (pressed) {

			if (getBackground() == null || isDefaultBg) {

				isDefaultBg = true;
				setBackgroundColor(getContext().getResources().getColor(
						R.color.button_bg_clicked));
			}

			setAlpha(0.7f);
		} else {

			if (isDefaultBg) {

				setBackgroundColor(getContext().getResources().getColor(
						android.R.color.transparent));
			}

			setAlpha(1f);
		}

		super.setPressed(pressed);
	}

	public void setFont() {

		if (isInEditMode()) {

			return;
		}

		int style = 0;
		Typeface typeFace = getTypeface();
		if (typeFace != null) {

			style = typeFace.getStyle();
		}

		switch (style) {
			case 1 :

				typeFace = Typeface.createFromAsset(getContext().getAssets(),
						"Roboto-Bold.ttf");
				break;
			case 2 :

				typeFace = Typeface.createFromAsset(getContext().getAssets(),
						"Roboto-LightItalic.ttf");
				break;
			case 3 :

				typeFace = Typeface.createFromAsset(getContext().getAssets(),
						"Roboto-BoldItalic.ttf");
				break;
			default :

				typeFace = Typeface.createFromAsset(getContext().getAssets(),
						"Roboto-Light.ttf");
				break;
		}

		setTypeface(typeFace);
	}

}
