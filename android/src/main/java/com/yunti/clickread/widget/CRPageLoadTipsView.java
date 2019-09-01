package com.yunti.clickread.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yunti.clickread.R;
import com.yunti.view.YTFrameLayout;

/*
 * @Author: kangqiang
 * @Date: 2019-09-01 14:43
 * @Last Modified by: kangqiang
 * @Last Modified time: 2019-09-01 14:43
 */
public class CRPageLoadTipsView extends YTFrameLayout {

    private TextView mLoadTips;
    private ProgressBar mProgressBar;

    public CRPageLoadTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CRPageLoadTipsView(Context context) {
        super(context);
    }

    @Override
    protected void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.view_cr_page_load_tips, this);
        mLoadTips = findViewById(R.id.tv_load_tips);
        mProgressBar = findViewById(R.id.view_progress);
    }

    public void showLoading() {
        setVisibility(View.VISIBLE);
        setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mLoadTips.setVisibility(View.INVISIBLE);
    }

    public void showError() {
        setVisibility(View.VISIBLE);
        setEnabled(true);
        mLoadTips.setText("图片加载失败");
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
        setEnabled(false);
    }
}
