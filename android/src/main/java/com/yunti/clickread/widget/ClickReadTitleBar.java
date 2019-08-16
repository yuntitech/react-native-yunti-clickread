package com.yunti.clickread.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.yunti.clickread.R;
import com.yunti.view.YTLinearLayout;

public class ClickReadTitleBar extends YTLinearLayout {

    public ClickReadTitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ClickReadTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickReadTitleBar(Context context) {
        super(context);
    }

    @Override
    protected void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        View.inflate(context, R.layout.view_click_read_title, this);
    }

    public void setOnCatalogClickListener(OnClickListener l) {
        findViewById(R.id.tv_catalog).setOnClickListener(l);
    }

    public void setOnBuyClickListener(OnClickListener l) {
        findViewById(R.id.tv_buy).setOnClickListener(l);
    }

    public void setOnBackClickListener(OnClickListener l) {
        findViewById(R.id.img_back).setOnClickListener(l);
    }


}
