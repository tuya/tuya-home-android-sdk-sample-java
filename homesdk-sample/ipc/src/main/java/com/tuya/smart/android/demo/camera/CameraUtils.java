package com.tuya.smart.android.demo.camera;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.thingclips.smart.android.camera.sdk.ThingIPCSdk;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCCore;
import com.tuya.smart.android.demo.camera.utils.CameraDoorbellManager;
import com.tuya.smart.android.demo.camera.utils.FrescoManager;

import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_DEV_ID;

public final class CameraUtils {

    private CameraUtils() {
    }

    public static void init(Application application) {
        FrescoManager.initFresco(application);
        CameraDoorbellManager.getInstance().init(application);
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