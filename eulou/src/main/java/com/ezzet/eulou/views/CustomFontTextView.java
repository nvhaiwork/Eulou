/**
 * 
 */
package com.ezzet.eulou.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author nvhaiwork
 *
 */
public class CustomFontTextView extends TextView {

	public CustomFontTextView(Context paramContext) {
		super(paramContext);
		// setFont();
	}

	public CustomFontTextView(Context paramContext,
			AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		// setFont();
	}

	public CustomFontTextView(Context paramContext,
			AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		// setFont();
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

			setAlpha(0.7f);
		} else {

			setAlpha(1f);
		}

		super.setPressed(pressed);
	}

	public void setFont() {

		int style = 0;
		Typeface typeFace = getTypeface();
		if (typeFace != null) {

			style = typeFace.getStyle();
		}

		typeFace = null;
		switch (style) {
		case 1:

			typeFace = Typeface.createFromAsset(getContext().getAssets(),
					"Roboto-Bold.ttf");
			break;
		case 2:

			typeFace = Typeface.createFromAsset(getContext().getAssets(),
					"Roboto-LightItalic.ttf");
			break;
		case 3:

			typeFace = Typeface.createFromAsset(getContext().getAssets(),
					"Roboto-BoldItalic.ttf");
			break;
		default:

			typeFace = Typeface.createFromAsset(getContext().getAssets(),
					"Roboto-Light.ttf");
			break;
		}

		setTypeface(typeFace);
	}

}
