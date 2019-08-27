package com.yunti.clickread.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yunti.clickread.R;
import com.yunti.clickread.Utils;

import okhttp3.internal.Util;

/**
 * Created by Administrator on 2016/10/12 0012.
 * 试读结束收费提示
 */

public class ClickReadBookBuyTipsView extends LinearLayout {

    private Context mContext;
    private TextView mTvBookPrice;

    public ClickReadBookBuyTipsView(Context context) {
        super(context);
        initView(context);
    }

    public ClickReadBookBuyTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }


    public void initView(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_click_read_buy_tips, this, true);
        mTvBookPrice = (TextView) findViewById(R.id.tv_book_price);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        findViewById(R.id.layout_buy_clickread_book).setOnClickListener(l);
    }

    public void refreshBuyBtnText(String authVal) {
        this.mTvBookPrice.setText(Utils.fen2Yuan(authVal));
    }

}
