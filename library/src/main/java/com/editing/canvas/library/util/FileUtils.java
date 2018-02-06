package com.editing.canvas.library.util;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

/**
 * Created by hemangi.vekaria on 19-05-2017.
 */

public class FileUtils {
    Context mContext;
    static FileUtils fileUtils;

    public FileUtils(Context context) {
        mContext = context;
    }

    public static synchronized FileUtils Instance(Context context) {
        if (fileUtils == null)
            fileUtils = new FileUtils(context);
        return fileUtils;
    }

    public File getCameraImageSavePath(String fileName) {
        File file = new File(mContext.getExternalFilesDir(null), "CameraImages");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath() + File.separator + fileName;

        return new File(filePath);
    }

    public File getSavedImagePath(String fileName) {
        File file = new File(mContext.getExternalFilesDir(null), "SavedImages");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath() + File.separator + fileName;

        return new File(filePath);
    }

    public File getCameraVideoSavePath(String fileName) {
        File file = new File(mContext.getExternalFilesDir(null), "CameraVideo");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath() + File.separator + fileName;
        return new File(filePath);
    }

    public File getFinalSavedImagePath(String fileName) {
        File file = new File(mContext.getExternalFilesDir(null), "FinalSavedImage");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath() + File.separator + fileName;

        return new File(filePath);
    }
    public File getSavedMergerVideoPath(String fileName) {
        File file = new File(mContext.getExternalFilesDir(null), "MergedVideo");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath() + File.separator + fileName;

        return new File(filePath);
    }
    public File getHistoryPath(String fileName) {
        File file = new File(mContext.getExternalFilesDir(null), "HistoryImgVideo");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath() + File.separator + fileName;

        return new File(filePath);
    }
    public File getHistoryDirectoryPath() {
        File file = new File(mContext.getExternalFilesDir(null), "HistoryImgVideo");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath();

        return new File(filePath);
    }
    public File getDownloadImagePath() {
        File file = new File(mContext.getExternalFilesDir(null), "DownloadedImages");
        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(mContext, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
        }
        String filePath = file.getPath();

        return new File(filePath);
    }

}
