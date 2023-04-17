package com.tuya.lock.demo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {


    public static long getStampTime(String time, String pattern) {
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != date) {
            return date.getTime();
        } else {
            return 0;
        }
    }

    public static long getStampTime(String time) {
        return getStampTime(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDateDay(long stamp) {
        return getDateDay(stamp, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDateDay(long stamp, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return simpleDateFormat.format(new Date(stamp));
    }

    public static void showImageUrl(String imageUrl, ImageView imageView) {
        try {
            URL url = new URL(imageUrl);
            new Thread(() -> {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                    imageView.post(() -> imageView.setImageBitmap(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStringTime(int year, int month, int dayOfMonth) {
        String day;
        if (dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        } else {
            day = String.valueOf(dayOfMonth);
        }
        String monthStr;
        if (month < 9) {
            monthStr = "0" + (month + 1);
        } else {
            monthStr = String.valueOf(month + 1);
        }
        return String.format("%s-%s-%s", year, monthStr, day);
    }
}