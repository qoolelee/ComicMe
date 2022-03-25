package com.uuballgame.comicme;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static String TOKEN = "";
    public static AllComicFiltersFragment.NewUUID NEW_UUID;
    public static Boolean PRO_USER = false;

    public static final String SERVER_IP = "https://kooler.com.tw";
    public static final String GET_NEW_UUID_URL = SERVER_IP + "/ComicMe/Api/getNewUUID.php";
    public static final String LOGIN_URL = SERVER_IP + "/ComicMe/Api/login.php";
    public static final String GET_FILTERS_DATA_LIST_URL = SERVER_IP + "/ComicMe/Api/getComicFiltersList.php";
    public static final String IMAGE_UPLOAD_PHP_URL = SERVER_IP + "/ComicMe/Api/uploadImage.php";
    public static final String START_PICTURE_PROCESS_URL = SERVER_IP + "/ComicMe/Api/startImageProcess.php";
    public static final String RETRIEVE_ID_PASSWORD_URL = SERVER_IP + "/ComicMe/Api/retrieveIdPassword.php";

    public static List<ComicFilter> COMIC_FILTERS_LIST = new ArrayList<>();
    public static List<ComicFilter> COMIC_FILTERS_HISTORICAL = new ArrayList<>();
    public static List<ComicSourceImage> COMIC_SOURCE_IMAGES_LIST = new ArrayList<>();

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }

    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",")  + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap scaleBitmap(Bitmap source, int targetW, int targetH) {
        return Bitmap.createScaledBitmap(source, targetW, targetH, false);
    }

    public static Bitmap scaleBitmap(Bitmap source, float scale) {
        return Bitmap.createScaledBitmap(source, (int)(source.getWidth() * scale), (int)(source.getHeight() * scale), false);
    }

    public static Bitmap getScaledBitmap(String currentPhotoPath, int targetW, int targetH) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        return  bitmap;
    }

    public static Bitmap enlargeBmap(Bitmap bitmap, float scale) {
        Bitmap result = Bitmap.createBitmap((int)(bitmap.getWidth()*scale), (int)(bitmap.getHeight()*scale), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        Rect rect = new Rect(0,0, result.getWidth(), result.getHeight());
        canvas.drawRect(rect, paint);

        float wShift = (float) result.getWidth()/2.0f - (float)bitmap.getWidth()/2.0f;
        float hShift = (float) result.getHeight()/2.0f - (float)bitmap.getHeight()/2.0f;
        canvas.drawBitmap(bitmap, wShift, hShift, null);

        return result;
    }

    public static Bitmap getCircularCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public static Uri bitmapToUri(Context context, Bitmap bitmap) { // File name like "image.png"
        //create a file to write bitmap data
        File file = null;
        try {
            file = new File(Environment.getExternalStorageDirectory() + File.separator + "FromComicMe.png");
            file.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 , bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return Uri.fromFile(file);
        }catch (Exception e){
            e.printStackTrace();
            return null; // it will return null
        }
    }

}
