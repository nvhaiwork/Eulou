/**
 *
 */
package com.ezzet.eulou.views;

import com.ezzet.eulou.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author ubuntu
 */
public class CustomAlertDialog extends Dialog {

    /**
     * View on click positive
     */
    View.OnClickListener positiveClickListener = new android.view.View.OnClickListener() {

        /*
         * (non-Javadoc)
         *
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            mPositiveClick.onButtonClick(v);
        }
    };
    /**
     * View on click negative
     */
    View.OnClickListener negativeClickListener = new View.OnClickListener() {

        /*
         * (non-Javadoc)
         *
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            mNegativeClick.onButtonClick(v);
        }
    };
    private String mTitle, mMessage, mPositive, mNegative;
    private TextView mPositiveBtn, mNegativeBtn, mTitleTxt, mMessageTxt;
    private OnPositiveButtonClick mPositiveClick;
    private OnNegativeButtonClick mNegativeClick;
    private boolean isCancelable;

    /**
     * @param context
     */
    public CustomAlertDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.mTitle = context.getString(R.string.app_name);
        this.isCancelable = true;
    }

    /**
     * @param context
     * @param mContext
     * @param title
     * @param message
     */
    public CustomAlertDialog(Context context, Context mContext, String title, String message) {
        super(context);
        this.mTitle = title;
        this.mMessage = message;
        this.isCancelable = true;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public boolean isCancelableFlag() {
        return isCancelable;
    }

    public void setCancelableFlag(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Dialog#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_alert);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        mTitleTxt = (TextView) findViewById(R.id.alert_title);
        mMessageTxt = (TextView) findViewById(R.id.alert_content);
        mPositiveBtn = (TextView) findViewById(R.id.alert_button_right);
        mNegativeBtn = (TextView) findViewById(R.id.alert_button_left);

        if (mTitle.equals("")) {

            mTitleTxt.setVisibility(View.GONE);
        }

        mTitleTxt.setText(mTitle);
        mMessageTxt.setText(mMessage);
        setCancelable(isCancelable);

        if (isCancelable) {

            RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.parent);
            rootLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    dismiss();
                }
            });
        }

        if (mPositiveClick != null) {

            mPositiveBtn.setVisibility(View.VISIBLE);
            mPositiveBtn.setText(mPositive);
            mPositiveBtn.setOnClickListener(positiveClickListener);
        }

        if (mNegativeClick != null) {

            mNegativeBtn.setVisibility(View.VISIBLE);
            mNegativeBtn.setText(mNegative);
            mNegativeBtn.setOnClickListener(negativeClickListener);
        }

        if (mPositiveClick != null && mNegativeClick != null) {

            View seperator = (View) findViewById(R.id.alert_button_sep);
            seperator.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set positive button
     *
     * @param displayText   Text for display
     * @param positiveClick
     */
    public void setPositiveButton(String displayText, OnPositiveButtonClick positiveClick) {

        this.mPositiveClick = positiveClick;
        this.mPositive = displayText;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    /**
     * Set negative button
     *
     * @param displayText   Text for display
     * @param negativeClick
     */
    public void setNegativeButton(String displayText, OnNegativeButtonClick negativeClick) {

        this.mNegativeClick = negativeClick;
        this.mNegative = displayText;
    }

    public interface OnPositiveButtonClick {

        public void onButtonClick(View view);
    }

    public interface OnNegativeButtonClick {

        public void onButtonClick(View view);
    }
}
