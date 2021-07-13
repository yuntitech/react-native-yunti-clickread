package com.yunti.clickread.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yunti.clickread.R;
import com.yunti.view.YTLinearLayout;

public class ClickReadTitleBar extends YTLinearLayout {


    private Button mClickArea;
    private Button mBuy;
    private Button mSpokenTest;

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
        mClickArea = findViewById(R.id.btn_click_area);
        mBuy = findViewById(R.id.btn_buy);
        mSpokenTest = findViewById(R.id.btn_spoken_test);
        mClickArea.setEnabled(false);
        setVisibility(View.INVISIBLE);
    }


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        findViewById(R.id.btn_catalog).setOnClickListener(l);
        mBuy.setOnClickListener(l);
        mClickArea.setOnClickListener(l);
        mSpokenTest.setOnClickListener(l);
        findViewById(R.id.img_back).setOnClickListener(l);
    }

    public void setClickAreaEnabled(boolean enabled) {
        mClickArea.setEnabled(enabled);
    }

    public void setClickAreaShow(boolean isShow) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), isShow ?
                R.drawable.selector_click_area : R.drawable.selector_click_area_default);
        mClickArea.setCompoundDrawablesWithIntrinsicBounds(null, drawable,
                null, null);
    }

    @Override
    public void setVisibility(int visibility) {
        findViewById(R.id.ll_buttons).setVisibility(visibility);
        findViewById(R.id.view_divider).setVisibility(visibility);
    }

    public void setBuyButtonVisible(boolean visible) {
        mBuy.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setSpokenTestVisible(Boolean visible) {
        mSpokenTest.setVisibility(Boolean.TRUE.equals(visible) ? View.VISIBLE : View.GONE);
    }
}
