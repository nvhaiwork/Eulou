/**
 *
 */
package com.ezzet.eulou.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author ubuntu
 */
public class CircleImageView extends ImageView {

	public CircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
	// Bitmap sbmp;
	// if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
	// sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
	// } else {
	// sbmp = bmp;
	// }
	//
	// Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(),
	// Config.ARGB_8888);
	// Canvas canvas = new Canvas(output);
	// final Paint paint = new Paint();
	// final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());
	// paint.setAntiAlias(true);
	// paint.setFilterBitmap(true);
	// paint.setDither(true);
	// canvas.drawARGB(0, 0, 0, 0);
	// paint.setColor(Color.parseColor("#BAB399"));
	// canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 +
	// 0.7f, sbmp.getWidth() / 2 + 0.1f, paint);
	// paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	// canvas.drawBitmap(sbmp, rect, rect, paint);
	// return output;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}

		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}

		Bitmap b = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
		Bitmap newBitmap = getRoundedBitmap(bitmap);
		// Bitmap roundBitmap = getCroppedBitmap(newBitmap, w);
		canvas.drawBitmap(newBitmap, 0, 0, null);
	}

	private Bitmap getRoundedBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	// private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int
	// newWidth) {
	// int sourceWidth = source.getWidth();
	// int sourceHeight = source.getHeight();
	// float xScale = (float) newWidth / sourceWidth;
	// float yScale = (float) newHeight / sourceHeight;
	// float scale = Math.max(xScale, yScale);
	//
	// // Now get the size of the source bitmap when scaled
	// float scaledWidth = scale * sourceWidth;
	// float scaledHeight = scale * sourceHeight;
	// float left = (newWidth - scaledWidth) / 2;
	// float top = (newHeight - scaledHeight) / 2;
	//
	// // The target rectangle for the new, scaled version of the source bitmap
	// // will now be
	// RectF targetRect = new RectF(left, top, left + scaledWidth, top +
	// scaledHeight);
	// Bitmap dest = Bitmap.createBitmap(newWidth, newHeight,
	// source.getConfig());
	// Canvas canvas = new Canvas(dest);
	// canvas.drawBitmap(source, null, targetRect, null);
	// return dest;
	// }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Drawable d = getDrawable();

		if (d != null) {
			// ceil not round - avoid thin vertical gaps along the left/right
			// edges
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = width;
			setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
