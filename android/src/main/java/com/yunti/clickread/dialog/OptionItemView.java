package com.yunti.clickread.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yunti.clickread.R;


public class OptionItemView extends RelativeLayout {

    private Context context;
    private TextView mDescription;
    private View mDividerTop;
    private View mDividerBottom;
    private String description;

    public OptionItemView(Context context) {
        super(context);
    }

    public OptionItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OptionItemView);
        description = typedArray.getString(R.styleable.OptionItemView_option);
        typedArray.recycle();
        init();
    }

    public OptionItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OptionItemView);
        description = typedArray.getString(R.styleable.OptionItemView_option);
        typedArray.recycle();
        init();
    }

    private void init() {
        View.inflate(context, R.layout.view_option_item, this);
        mDescription = (TextView) findViewById(R.id.tv_desc);
        mDividerTop = findViewById(R.id.divider_top);
        mDividerBottom = findViewById(R.id.divider_bottom);
        if (!TextUtils.isEmpty(description)) {
            mDescription.setText(description);
        }
    }

    public View getDividerTop() {
        return mDividerTop;
    }

    public View getDividerBottom() {
        return mDividerBottom;
    }

    public void showDividerTop() {
        mDividerTop.setVisibility(View.VISIBLE);
    }

    public void showDividerBottom() {
        mDividerBottom.setVisibility(View.VISIBLE);
    }

    public void hideDividerTop() {
        mDividerTop.setVisibility(View.GONE);
    }

    public void hideDividerBottom() {
        mDividerBottom.setVisibility(View.GONE);
    }

    public void setDescription(String description) {
        mDescription.setText(description);
    }

    public TextView getDescription() {
        return mDescription;
    }

    public void setPressed(boolean flag) {
        mDescription.setPressed(flag);
    }
}
