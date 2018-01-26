package com.example.administrator.myapplication.Util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Administrator on 2018/1/26.
 */

public class BitmapUtil {
    public static Bitmap ScaleBitmap(Bitmap bm, float scale) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }
}
