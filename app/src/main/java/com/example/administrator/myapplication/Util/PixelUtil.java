package com.example.administrator.myapplication.Util;

import android.content.Context;

//Created by Administrator on 2018/1/25.

public class PixelUtil {
    public static int Dp2Px(Context context, int dpValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
}
