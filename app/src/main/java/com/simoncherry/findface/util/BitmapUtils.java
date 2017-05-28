package com.simoncherry.findface.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Simon on 2017/5/28.
 */

public class BitmapUtils {

    public static Bitmap getEvenWidthBitmap(String imgUrl) {
        Bitmap srcImg = BitmapFactory.decodeFile(imgUrl);
        if (srcImg != null) {
            Bitmap srcFace = srcImg.copy(Bitmap.Config.RGB_565, true);
            srcImg = null;
            int w = srcFace.getWidth();
            int h = srcFace.getHeight();
            if (w % 2 == 1) {
                w++;
                srcFace = Bitmap.createScaledBitmap(srcFace,
                        srcFace.getWidth() + 1, srcFace.getHeight(), false);
            }
            if (h % 2 == 1) {
                h++;
                srcFace = Bitmap.createScaledBitmap(srcFace,
                        srcFace.getWidth(), srcFace.getHeight() + 1, false);
            }
            return srcFace;
        }
        return null;
    }
}
