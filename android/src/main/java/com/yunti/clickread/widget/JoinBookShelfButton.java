package com.yunti.clickread.widget;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.yunti.clickread.R;
import com.yunti.util.YTDisplayHelper;
import com.yunti.view.YTLinearLayout;


/*
 * @Author: kangqiang
 * @Date: 2019-08-16 17:01
 * @Last Modified by: kangqiang
 * @Last Modified time: 2019-08-16 17:01
 */
public class JoinBookShelfButton extends YTLinearLayout implements View.OnClickListener {

    private boolean mIsShowFullAddShelfButton = false;
    private JoinBookShelfButtonDelegate mDelegate;

    public void setDelegate(JoinBookShelfButtonDelegate delegate) {
        mDelegate = delegate;
    }

    public JoinBookShelfButton(Context context) {
        super(context);
    }


    public JoinBookShelfButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.btn_join_bookshelf, this);
        setOnClickListener(this);
    }


    /**
     * 展开加入书架按钮
     */
    private void expandAddShelfButton() {
        if (!mIsShowFullAddShelfButton) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    android.animation.ObjectAnimator.ofFloat(this, "translationX",
                            0, -YTDisplayHelper.dpToPx(68))
            );
            set.setDuration(300).start();
        }

        mIsShowFullAddShelfButton = true;
    }


    /**
     * 收起加入书架按钮
     */
    public void collapseAddShelfButton() {
        if (mIsShowFullAddShelfButton) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    android.animation.ObjectAnimator.ofFloat(this, "translationX",
                            -YTDisplayHelper.dpToPx(68), 0)
            );
            set.setDuration(300).start();
        }
        mIsShowFullAddShelfButton = false;
    }

    @Override
    public void onClick(View v) {
        if (mIsShowFullAddShelfButton) {
            if (mDelegate != null) {
                mDelegate.joinBookShelf();
            }
        } else {
            expandAddShelfButton();
        }
    }

    public interface JoinBookShelfButtonDelegate {
        void joinBookShelf();
    }
}
