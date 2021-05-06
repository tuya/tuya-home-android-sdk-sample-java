package com.tuya.smart.android.demo.camera.utils;

import android.icu.util.TimeZone;
import android.os.Build;

import java.util.Locale;
import java.util.SimpleTimeZone;

/**
 * huangdaju
 * 2019-11-19
 **/

public class TimeZoneUtils {
    public static String getTimezoneGCMById(String timezoneId) {
        int timeZoneByRawOffset;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TimeZone timeZone = TimeZone.getTimeZone(timezoneId);
            timeZoneByRawOffset = timeZone.getRawOffset() + timeZone.getDSTSavings();
        } else {
            java.util.TimeZone timeZone = SimpleTimeZone.getTimeZone(timezoneId);
            timeZoneByRawOffset = timeZone.getRawOffset() + timeZone.getDSTSavings();
        }
        return getTimeZoneByRawOffset(timeZoneByRawOffset);
    }


    private static String getTimeZoneByRawOffset(int rawOffset) {
        String timeDisplay = rawOffset >= 0 ? "+" : "";
        int hour = rawOffset / 1000 / 3600;
        int minute = (rawOffset - hour * 1000 * 3600) / 1000 / 60;
        timeDisplay += String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        return timeDisplay;
    }
}
