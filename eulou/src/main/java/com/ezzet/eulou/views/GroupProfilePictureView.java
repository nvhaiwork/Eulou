package com.example.tiennt.androidtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.ezzet.eulou.utilities.BitmapUtil;

/**
 * Created by tiennt on 5/19/15.
 */
public class GroupProfilePictureView extends View {

    private static final String TAG = "ProfileView";
    int size;
    int horizontalGap = 10;
    int verticalGap = 10;
    Paint mBitmapPaint = new Paint();

    Bitmap mAxBitmap;
    Bitmap mAxSocialIcon;

    Rect mAxProfileRect;
    RectF mAxProfileDrawRect;
    Rect mAxSocialRect;
    RectF mAxSocialDrawRect;

    Paint mWhiteStrokePaint;

    public GroupProfilePictureView(Context context) {
        super(context);
        init();
    }

    public GroupProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GroupProfilePictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mAxProfileDrawRect = new RectF();
        mAxProfileRect = new Rect();
        mAxSocialRect = new Rect();
        mAxSocialDrawRect = new RectF();

        mWhiteStrokePaint = new Paint();
        mWhiteStrokePaint.setStyle(Paint.Style.STROKE);
        mWhiteStrokePaint.setColor(Color.WHITE);
        mWhiteStrokePaint.setStrokeWidth(7);
        mWhiteStrokePaint.setAntiAlias(true);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setStyle(Paint.Style.FILL);
        mBitmapPaint.setColor(Color.WHITE);
    }

    public void setBitmap(Bitmap profileImage) {
        mAxBitmap = BitmapUtil.getRoundedBitmap(profileImage);
        mAxProfileRect.set(0, 0, profileImage.getWidth(), profileImage.getHeight());
        invalidate();
    }

    public void setSocialBitmap(Bitmap socialBitmap) {
        mAxSocialIcon = BitmapUtil.getRoundedBitmap(socialBitmap);
        mAxSocialRect.set(0, 0, mAxSocialIcon.getWidth(), mAxSocialIcon.getHeight());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        drawOne(canvas, null, width, height);
    }

    private void drawOne(Canvas canvas, Rect output, int w, int h) {

        // Padding.
        int pl = getPaddingLeft();
        int pr = getPaddingRight();
        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        Log.v(TAG, String.format("%d %d %d %d", pl, pr, pt, pb));

        int realWidth = w - pl - pr;
        int realHeight = h - pt - pb;
        int min = realWidth < realHeight ? realWidth : realHeight;
        Log.v(TAG, "Min: " + min);

        mAxProfileDrawRect.set(pl, pt, pl + min, pt + min);
        if (mAxBitmap != null) {
            canvas.drawBitmap(mAxBitmap, mAxProfileRect, mAxProfileDrawRect, mBitmapPaint);
        }

        float profileSize = mAxProfileDrawRect.width();
        float iconSize = profileSize / 3;
        float icLeft = pl + profileSize + 20 - iconSize;
        float icTop = pt + profileSize - iconSize + 10;

        mAxSocialDrawRect.set(icLeft, icTop, icLeft + iconSize, icTop + iconSize);
        canvas.drawRoundRect(mAxSocialDrawRect, iconSize, iconSize, mWhiteStrokePaint);
        if (mAxSocialIcon != null) {
            mAxSocialDrawRect.set(icLeft + 2, icTop + 2, icLeft + iconSize - 2, icTop + iconSize - 2);
            canvas.drawBitmap(mAxSocialIcon, mAxSocialRect, mAxSocialDrawRect, mBitmapPaint);
        }
    }

    private void drawThree(Canvas canvas, int w, int h) {
        int cx = w / 2;
        int cy = h / 2;


    }


    private void drawMany() {

    }
}
