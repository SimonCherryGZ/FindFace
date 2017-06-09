package com.simoncherry.findface.util;

import com.simoncherry.findface.model.CVFace;

/**
 * Created by Simon on 2017/1/21.
 */

public class JNIUtils {

    static {
        System.loadLibrary("JNI_APP");
    }

    private long cPtr;

    public static native int[] doGrayScale(int[] buf, int w, int h);

    public static native int[] doEdgeDetection(int[] buf, int w, int h);

    public static native int[] doBinaryzation(int[] buf, int w, int h);

    public static native long setClassifier(String path);

    private static native void deleteCassifier(long cPtr);

    public static native CVFace[] detectFace(int[] buf, int w, int h);

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        deleteCassifier(cPtr);
    }
}
