package com.android.reloop.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeHelper
{
    public static String getDateTime() {
        SimpleDateFormat time1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        long date = System.currentTimeMillis();

        String timeString1 = time1.format(date);


        return timeString1;
    }

    public static String getUTCDateTime() {
        SimpleDateFormat time1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        time1.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeString1 = null;
        try {
            timeString1 = time1.format(new Date());
        } catch (Exception e) {
            return getDateTime();
        }
        return timeString1;
    }
}
