/**
 *
 */
package com.ezzet.eulou.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * @author nvhaiwork
 */
public class CustomImageButton extends ImageView {
    /**
     * @param context
     */
    public CustomImageButton(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param context
     * @param attrs
     */
    public CustomImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CustomImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
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

            ((View) this).setAlpha(0.6f);
        } else {

            ((View) this).setAlpha(1f);
        }

        super.setPressed(pressed);
    }
}
