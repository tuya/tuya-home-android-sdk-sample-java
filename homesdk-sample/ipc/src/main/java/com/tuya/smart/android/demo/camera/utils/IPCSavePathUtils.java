package com.tuya.smart.android.demo.camera.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class IPCSavePathUtils {

    private static String ROOT_PATH;
    public static String DOWNLOAD_PATH;
    public static String DOWNLOAD_PATH_Q;

    public IPCSavePathUtils(Context context) {
        //初始化外部存储根目录
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //android11及以上设备
            if (null == context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            } else {
                ROOT_PATH = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
            }
        } else {
            //android11以下设备
            ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        DOWNLOAD_PATH = ROOT_PATH + "/Camera/Thumbnail/";
        DOWNLOAD_PATH_Q = ROOT_PATH + "/Camera/" + Environment.DIRECTORY_DCIM + "/Thumbnail/";
    }

    public String recordPathSupportQ(String devId) {
        String videoPath;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //分区存储
            videoPath = DOWNLOAD_PATH_Q + devId + "/";
        } else {
            //非分区存储
            videoPath = DOWNLOAD_PATH + devId + "/";
        }
        File file = new File(videoPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                // L.e(TAG, "recordPathQ create the directory fail, videoPath is " + videoPath);
                return "";
            }
        }
        return videoPath;
    }
}
