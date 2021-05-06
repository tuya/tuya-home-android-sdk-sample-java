package com.tuya.smart.android.demo.camera.utils;

import android.os.Message;

/**
 * Created by lee on 16/5/12.
 */
public class MessageUtil {

    public static Message getMessage(int msgWhat, int arg){
        Message msg = new Message();
        msg.what = msgWhat;
        msg.arg1 = arg;
        return msg;
    }
}
