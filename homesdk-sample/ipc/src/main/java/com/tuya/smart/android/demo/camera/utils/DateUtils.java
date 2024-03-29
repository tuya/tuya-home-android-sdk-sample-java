package com.tuya.smart.android.demo.camera.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Created by lee on 2017/4/18.
 */

public class DateUtils {

    /**
     * Get the start of the day（00:00:00）
     *
     * @param currentTime
     * @return
     */
    public static int getTodayStart(long currentTime) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long value = calendar.getTimeInMillis() / 1000L;
        return (int) value;
    }

    /**
     * Get the end of the day（00:00:00）
     *
     * @param currentTime
     * @return
     */
    public static int getTodayEnd(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTime));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long value =  calendar.getTimeInMillis() / 1000L;
        return (int)value;
    }

    /**
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static long getCurrentTime(int year, int month, int day) {
        String monthStr;
        if (month < 10) {
            monthStr = "0" + month;
        } else {
            monthStr = "" + month;
        }
        String dayStr;
        if (day < 10) {
            dayStr = "0" + day;
        } else {
            dayStr = "" + day;
        }
        String currentDate = year + monthStr + dayStr;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            Date date = simpleDateFormat.parse(currentDate);
            return date.getTime();
        } catch (ParseException px) {
            px.printStackTrace();
        }
        return 0;
    }


}