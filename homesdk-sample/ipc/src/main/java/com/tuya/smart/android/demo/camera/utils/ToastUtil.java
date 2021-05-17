package com.tuya.smart.android.demo.camera.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    public static void shortToast(Context context, String tips) {
        Toast.makeText(context, tips, Toast.LENGTH_SHORT).show();
    }
}
