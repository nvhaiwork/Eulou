package com.ezzet.eulou.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tiennt on 5/20/15.
 */
public class BitmapUtil {
    public static Bitmap decodeWithSampleSize(InputStream source, int width, int height) throws IOException {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(source, null, opts);

        int width_tmp = opts.outWidth, height_tmp = opts.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < width || height_tmp / 2 < height) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;

        // Reset stream
        source.reset();

        Bitmap bitmap = BitmapFactory.decodeStream(source, null, opts);
        source.close();

        return bitmap;
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
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
}
