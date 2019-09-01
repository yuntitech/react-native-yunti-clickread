package com.yunti.clickread.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yunti.clickread.R;
import com.yunti.view.YTFrameLayout;

/*
 * @Author: kangqiang
 * @Date: 2019-09-01 13:08
 * @Last Modified by: kangqiang
 * @Last Modified time: 2019-09-01 13:08
 */
public class YTLoadTipsView extends YTFrameLayout {

    private TextView mLoadTips;
    private ProgressBar mProgressBar;
    private ViewGroup mTipsContainer;

    public YTLoadTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public YTLoadTipsView(Context context) {
        super(context);
    }

    @Override
    protected void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.view_yt_load_tips, this);
        mLoadTips = findViewById(R.id.tv_load_tips);
        mProgressBar = findViewById(R.id.view_progress);
        mTipsContainer = findViewById(R.id.ll_tips_container);
    }

    public void showLoading() {
        setVisibility(View.VISIBLE);
        setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mTipsContainer.setVisibility(View.INVISIBLE);
    }

    public void showError(String error) {
        setVisibility(View.VISIBLE);
        setEnabled(true);
        mLoadTips.setText(error);
        mProgressBar.setVisibility(View.INVISIBLE);
        mTipsContainer.setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
        setEnabled(false);
    }

}
