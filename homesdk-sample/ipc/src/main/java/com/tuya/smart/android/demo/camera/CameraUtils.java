package com.tuya.smart.android.demo.camera;

import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_DEV_ID;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.thingclips.loguploader.TLogSDK;
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCCore;
import com.tuya.smart.android.demo.camera.utils.CameraDoorbellManager;
import com.tuya.smart.android.demo.camera.utils.FrescoManager;

public final class CameraUtils {

    private CameraUtils() {
    }

    public static void init(Application application) {
        FrescoManager.initFresco(application);
        CameraDoorbellManager.getInstance().init(application);

        /*
         * 表示单个日志文件最大10M,同种类日志文件数最大为5个
         * 当日志数量达到最大时，每创建一个新日志文件，时间上最早创建的日志文件会被删除
         */
        TLogSDK.init(application);
    }

    public static boolean ipcProcess(Context context, String devId) {
        IThingIPCCore cameraInstance = ThingIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            if (cameraInstance.isIPCDevice(devId)) {
                Intent intent = new Intent(context, CameraPanelActivity.class);
                intent.putExtra(INTENT_DEV_ID, devId);
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }
}