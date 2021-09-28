package com.tuya.smart.android.demo.camera.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    private BitmapUtils() {
    }

    public static boolean savePhotoToSDCard(Bitmap photoBitmap, String path, String name) {
        boolean isSave = false;
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "savePhotoToSDCard create file fail, path: " + path);
            }
        }

        File photoFile = new File(path, name);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(photoFile);
            if (photoBitmap != null) {
                if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
                    fileOutputStream.flush();
                }
                isSave = true;
            }
        } catch (FileNotFoundException e) {
            if (!photoFile.delete()) {
                Log.e(TAG, "savePhotoToSDCard delete photoFile fail, path: " + path);
            }
            e.printStackTrace();
            isSave = false;
        } catch (IOException e) {
            if (!photoFile.delete()) {
                Log.e(TAG, "savePhotoToSDCard try catch delete file fail, path: " + path);
            }
            e.printStackTrace();
            isSave = false;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isSave;
    }

    public static boolean savePhotoToSDCard(Bitmap photoBitmap, String path) {
        return savePhotoToSDCard(photoBitmap, path, String.valueOf(System.currentTimeMillis()) + ".png");
    }

}
