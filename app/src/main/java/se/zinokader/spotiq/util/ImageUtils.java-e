package se.zinokader.spotiq.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap byteArrayToBitmap(byte[] bytearray) {
       return BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
    }

    public static byte[] bitmapToByteArrayCompressed(Bitmap bitmap, int quality) {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, bytearrayoutputstream);
        return bytearrayoutputstream.toByteArray();
    }

    public static Bitmap compressBitmap(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        BitmapFactory.Options compressoptions = new BitmapFactory.Options();
        ByteArrayOutputStream byteoutputstream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteoutputstream);
        byte[] bytearray = byteoutputstream.toByteArray();
        return BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length, compressoptions);
    }

}
