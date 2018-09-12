package com.chart.chartline.utils;

import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.Field;

public class Utils {

    public static int getScreenHeight(Context context) {
        Resources resource = context.getResources();
        return resource.getDisplayMetrics().heightPixels;
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public static int parseInt(String s, int defaultValue) {
        int value;
        try {
            value = Integer.parseInt(s);
        } catch (Exception e) {
            value = defaultValue;
        }
        return value;
    }
}
