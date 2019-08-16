package com.yunti.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class YTFrameLayout extends FrameLayout {

    public YTFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public YTFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public YTFrameLayout(Context context) {
        super(context);
        init(context, null);
    }

    protected void init(Context context, AttributeSet attrs) {

    }

}
