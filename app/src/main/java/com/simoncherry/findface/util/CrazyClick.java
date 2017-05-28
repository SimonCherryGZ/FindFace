package com.simoncherry.findface.util;

import android.os.SystemClock;

/**
 * Created by Simon on 2017/5/28.
 */

public class CrazyClick {
    public static final int MIN_CLICK_DELAY_TIME = 300;
    private static long mLastClickTime = 0;

    public static boolean isCrazy() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < MIN_CLICK_DELAY_TIME) {
            return true;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return false;
    }
}
