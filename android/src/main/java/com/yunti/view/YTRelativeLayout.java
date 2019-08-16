package com.yunti.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class YTRelativeLayout extends RelativeLayout {

    public YTRelativeLayout(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public YTRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public YTRelativeLayout(Context context) {
        super(context);
        init(context, null);
    }

    protected void init(Context context, AttributeSet attrs) {
    }

}
