package com.yunti.clickread.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;

import com.yunti.clickread.R;
import com.yunti.util.YTDisplayHelper;

/**
 * 点读点击选中框
 */
public class FrameMask extends View {
    private Paint mPaint;
    private RectF mCurRect;

    public FrameMask(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FrameMask(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrameMask(Context context) {
        super(context);
        init();
    }

    public void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mCurRect = new RectF(0, 0, 0, 0);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPaint.setShadowLayer(YTDisplayHelper.dpToPx(2), 1, 1, getResources().getColor(R.color.black_alpha_percent_35));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(getResources().getColor(R.color.color_purple));
        mPaint.setStrokeWidth(YTDisplayHelper.dpToPx(1.5f));
        canvas.drawRect(mCurRect, mPaint);
    }

    public void switchFrame(RectF frame) {
        FrameSwitchAnimation mAnim = new FrameSwitchAnimation(mCurRect, frame);
        this.clearAnimation();
        mAnim.setFillAfter(true);
        mAnim.setDuration(1000);
        mAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        this.startAnimation(mAnim);

    }

    public void showFrame(float top, float left, float right, float bottom) {
        mCurRect.set(left, top, right, bottom);
        postInvalidate();
    }

    class FrameSwitchAnimation extends Animation {

        private RectF from;
        private RectF to;


        public FrameSwitchAnimation(RectF from, RectF to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, android.view.animation.Transformation t) {
            float top = from.top + (to.top - from.top) * interpolatedTime;
            float left = from.left + (to.left - from.left) * interpolatedTime;
            float right = from.right + (to.right - from.right) * interpolatedTime;
            float bottom = from.bottom + (to.bottom - from.bottom) * interpolatedTime;
            showFrame(top, left, right, bottom);
        }
    }

}
