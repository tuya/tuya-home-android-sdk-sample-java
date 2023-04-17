package com.tuya.smart.android.demo.camera;

import android.util.Log;

import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.android.common.utils.log.ILogInterception;

public class IPCLogUtils {

    public static void init() {
        L.setLogInterception(2, new ILogInterception() {
            @Override
            public void log(int i, String tag, String msg) {
                customLog(tag, msg);
            }
        });
    }

    private static void customLog(String var1, String var2) {
        //use your log system to print/save log
        Log.i(var1, var2);
    }
}