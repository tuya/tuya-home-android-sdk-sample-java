package com.tuya.lock.demo.ble.utils;

import android.content.Context;

import com.tuya.lock.demo.R;
import com.thingclips.smart.optimus.lock.api.ThingUnlockType;

public class OpModeUtils {

    public static String getTypeName(Context context, String dpCode) {
        String name = dpCode;
        if (dpCode.equals(ThingUnlockType.FINGERPRINT)) {
            name = context.getString(R.string.mode_fingerprint);
        } else if (dpCode.equals(ThingUnlockType.CARD)) {
            name = context.getString(R.string.mode_card);
        } else if (dpCode.equals(ThingUnlockType.PASSWORD)) {
            name = context.getString(R.string.mode_password);
        } else if (dpCode.equals(ThingUnlockType.VOICE_REMOTE)) {
            name = context.getString(R.string.mode_voice_password);
        }
        return name;
    }
}