package com.yunti.clickread.widget;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunti.clickread.R;
import com.yunti.util.YTDisplayHelper;
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
        YTDisplayHelper.setProgressTint(mProgressBar, R.color.color_purple);
    }

    public void showTrackLoading(RectF rectF) {
        RelativeLayout.LayoutParams params
                = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.leftMargin = (int) (rectF.left + rectF.width() / 2 - YTDisplayHelper.dpToPx(15));
        params.topMargin = (int) (rectF.top + rectF.height() / 2 - YTDisplayHelper.dpToPx(15));
        setLayoutParams(params);
        setLoading();
    }

    public void showLoading() {
        RelativeLayout.LayoutParams params
                = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(params);
        setLoading();
    }

    public void showError() {
        setVisibility(View.VISIBLE);
        setEnabled(true);
        mLoadTips.setText("图片加载失败");
        mProgressBar.setVisibility(View.INVISIBLE);
        mLoadTips.setVisibility(View.VISIBLE);
    }

    public void hide() {
        if (getVisibility() != View.GONE) {
            setVisibility(View.GONE);
            setEnabled(false);
        }
    }

    private void setLoading() {
        setVisibility(View.VISIBLE);
        setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mLoadTips.setVisibility(View.INVISIBLE);
    }
}
