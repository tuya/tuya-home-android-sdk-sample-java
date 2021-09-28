package com.tuya.smart.android.demo.camera.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

/**
 * huangdaju
 * 2018/12/17
 **/

public class Constants {

    public static final String INTENT_MSGID = "msgid";
    public static final String INTENT_DEV_ID = "intent_devId";
    public static final String INTENT_P2P_TYPE = "intent_p2p_type";
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;
    public static final int EXTERNAL_AUDIO_REQ_CODE = 11;

    public static final int ARG1_OPERATE_SUCCESS = 0;
    public static final int ARG1_OPERATE_FAIL = 1;

    public static final int MSG_CONNECT = 2033;
    public static final int MSG_CREATE_DEVICE = 2099;
    public static final int MSG_SET_CLARITY = 2054;

    public static final int MSG_TALK_BACK_FAIL = 2021;
    public static final int MSG_TALK_BACK_BEGIN = 2022;
    public static final int MSG_TALK_BACK_OVER = 2023;
    public static final int MSG_DATA_DATE = 2035;

    public static final int MSG_MUTE = 2024;
    public static final int MSG_SCREENSHOT = 2017;

    public static final int MSG_VIDEO_RECORD_FAIL = 2018;
    public static final int MSG_VIDEO_RECORD_BEGIN = 2019;
    public static final int MSG_VIDEO_RECORD_OVER = 2020;


    public static final int MSG_DATA_DATE_BY_DAY_SUCC = 2045;
    public static final int MSG_DATA_DATE_BY_DAY_FAIL = 2046;

    public static final int ALARM_DETECTION_DATE_MONTH_FAILED = 2047;
    public static final int ALARM_DETECTION_DATE_MONTH_SUCCESS = 2048;
    public static final int MSG_GET_ALARM_DETECTION = 2049;
    public static final int MOTION_CLASSIFY_FAILED = 2050;
    public static final int MOTION_CLASSIFY_SUCCESS = 2051;
    public static final int MSG_DELETE_ALARM_DETECTION = 2052;
    public static final int MSG_GET_VIDEO_CLARITY = 2053;

    public synchronized static boolean requestPermission(Context context, String permission, int requestCode, String tip) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    permission)) {
                ToastUtil.shortToast(context, tip);
            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{permission},
                        requestCode);
                return false;
            }

            return false;
        } else {
            return true;
        }
    }

    @SuppressLint("all")
    public static boolean hasRecordPermission() {
        int minBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSizeInBytes = 640;
        byte[] audioData = new byte[bufferSizeInBytes];
        int readSize = 0;
        AudioRecord audioRecord = null;
        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 8000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
            // start recording
            audioRecord.startRecording();
        } catch (Exception e) {
            if (audioRecord != null) {
                audioRecord.release();
                audioRecord = null;
            }
            return false;
        }
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            return false;
        } else {
            readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            // Check whether the recording result can be obtained
            if (readSize <= 0) {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                Log.e("ss", "No recording permission");
                return false;
            } else {
                //Have permission, start recording normally and have data
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                return true;
            }
        }
    }
}
