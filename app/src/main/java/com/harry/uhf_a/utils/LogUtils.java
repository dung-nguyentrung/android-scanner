package com.harry.uhf_a.utils;

import android.util.Log;

public class LogUtils {
    private static boolean isDebug = true ;

    public static void logout(String msg) {
        if (isDebug) {
            Log.e("pang", msg);
        }
    }

    public static void logout(String tag ,String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }
}
