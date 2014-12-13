package com.ezzet.eulou.views;

import com.ezzet.eulou.R;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class CustomLoadingDialog extends Dialog {

    public CustomLoadingDialog(Context context) {
        super(context, R.style.new_dialog);
    }

    public static CustomLoadingDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static CustomLoadingDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static CustomLoadingDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static CustomLoadingDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        CustomLoadingDialog dialog = new CustomLoadingDialog(context);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        dialog.show();

        return dialog;
    }
}
