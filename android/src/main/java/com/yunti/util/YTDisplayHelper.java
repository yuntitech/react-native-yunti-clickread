package com.yunti.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import com.yunti.clickread.R;

public class YTDisplayHelper {

    /**
     * 屏幕密度,系统源码注释不推荐使用
     */
    private static final float DENSITY = Resources.getSystem()
            .getDisplayMetrics().density;

    public static int getColor(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

    /**
     * 把以 dp 为单位的值，转化为以 px 为单位的值
     *
     * @param dpValue 以 dp 为单位的值
     * @return px value
     */
    public static int dpToPx(float dpValue) {
        return Math.round(dpValue * DENSITY + 0.5f);
    }

    /**
     * 把以 px 为单位的值，转化为以 dp 为单位的值
     *
     * @param pxValue 以 px 为单位的值
     * @return dp值
     */
    public static int pxToDp(float pxValue) {
        return (int) (pxValue / DENSITY + 0.5f);
    }

    public static void setProgressTint(ProgressBar progressBar, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setIndeterminateTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(progressBar.getContext(), color)));
        }
    }
}
