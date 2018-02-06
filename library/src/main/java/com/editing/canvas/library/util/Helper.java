package com.editing.canvas.library.util;

import android.content.Context;
import android.content.CursorLoader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hemangi.vekaria on 31-05-2017.
 */

public class Helper {
    public static int PORTRAIT = 1;
    public static int LANDSCAPE = 2;
    public static int CENTER = 0;

    public static boolean isLandscapeScreen(Context context) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return false;
        else
            return true;
    }

    public static int getImageOrientation(Context mcontext, Uri imageURI) {
        InputStream imageStream = null;
        Bitmap yourSelectedImage = null;
        try {
            imageStream = mcontext.getContentResolver().openInputStream(imageURI);
            yourSelectedImage = BitmapFactory.decodeStream(imageStream);
        } catch (FileNotFoundException e) {
            URL url = null;
            try {
                url = new URL(imageURI.toString());
                yourSelectedImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

        int fHeight = yourSelectedImage.getHeight();
        int fWidth = yourSelectedImage.getWidth();
        if (fWidth == fHeight)
            return CENTER;
        else {
            if (fHeight > fWidth)
                return PORTRAIT;
            else
                return LANDSCAPE;
        }
    }

    public static String getRealPathFromURI(Context mContext, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        if (contentUri != null) {
            CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
            Cursor cursor = loader.loadInBackground();

            if (cursor == null)
                return contentUri.getPath();
            if (cursor.getCount() == 0)
                return contentUri.getPath();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch(Exception e){e.printStackTrace();}finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.KITKAT;
        Log.i("URI", uri + "");
        String result = uri + "";
        if (isKitKat && (result.contains("media.documents"))) {

            String[] ary = result.split("/");
            int length = ary.length;
            String imgary = ary[length - 1];
            final String[] dat = imgary.split("%3A");

            final String docId = dat[1];
            final String type = dat[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {

            } else if ("audio".equals(type)) {
            }
            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    dat[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public static int getVideoOrientation(String videoFilePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap bmp = null;
        try {
            retriever.setDataSource(videoFilePath);
            bmp = retriever.getFrameAtTime();
            int fHeight = bmp.getHeight();
            int fWidth = bmp.getWidth();
            if (fWidth == fHeight)
                return CENTER;
            else {
                if (fHeight > fWidth)
                    return PORTRAIT;
                else
                    return LANDSCAPE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return PORTRAIT;
    }

    public static Bitmap getImageBitmap(Context mcontext, Uri imageURI) {
        InputStream imageStream = null;
        try {
            imageStream = mcontext.getContentResolver().openInputStream(imageURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
        return yourSelectedImage;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        // GET CURRENT SIZE
        int width = bm.getWidth();
        int height = bm.getHeight();
        // GET SCALE SIZE
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public float dpToPixels(Context mcontext, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mcontext.getResources().getDisplayMetrics());
    }

    public static long getVideoFileLength(Context context, String path) {
        File videoFile = new File(path);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.fromFile(videoFile));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(time);
    }
}
