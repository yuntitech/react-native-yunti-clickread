package com.yunti.clickread.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.yunti.clickread.R;
import com.yunti.util.YTDisplayHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/19 0019.
 * 可点区域
 */

public class ClickArea extends View {

    private List<RectF> mFrames;
    private Paint mPaint;
    private OnClickPointingAreaListener mClickListener;
    private int mCurClickIndex = -1;

    public ClickArea(Context context) {
        super(context);
        init();
    }

    public ClickArea(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClickArea(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void init() {
        mFrames = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.color_purple));
        mPaint.setStrokeWidth(YTDisplayHelper.dpToPx(0.5f));
    }


    public void setOnClickPointingAreaListener(OnClickPointingAreaListener listener) {
        this.mClickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (RectF frame : mFrames) {
            canvas.drawRect(frame, mPaint);
        }

    }

    public void doClickEvent(float x, float y) {
        int clickIndex = getClickPointingAreaIndex(x, y);
        if (mClickListener != null) {
            if (clickIndex >= 0) {
                mCurClickIndex = clickIndex;
                mClickListener.onClickHotArea(clickIndex, mFrames.get(clickIndex));
            } else {
                mClickListener.onClickOtherArea();
            }
        }
    }

    private int getClickPointingAreaIndex(float x, float y) {
        int index = 0;
        int clickIndex = -1;
        for (RectF frame : mFrames) {
            RectF fixFrame = new RectF(frame.left - 10, frame.top - 10, frame.right + 10, frame.bottom + 10);
            if (x >= fixFrame.left && x < fixFrame.right && y > fixFrame.top && y < fixFrame.bottom) {
                clickIndex = index;
                return clickIndex;
            }
            index++;
        }
        return clickIndex;
    }

    public void drawPointingArea(List<RectF> frames) {
        this.mFrames = frames;
        postInvalidate();
    }

    public interface OnClickPointingAreaListener {

        void onClickHotArea(int postition, RectF frame);

        void onClickOtherArea();
    }

    public RectF getCurClickArea() {
        if (mCurClickIndex > -1 && mCurClickIndex < mFrames.size()) {
            return mFrames.get(mCurClickIndex);
        }
        return null;
    }

    public int getCurClickAreaIndex() {
        return mCurClickIndex;
    }

    public void nextFrame() {
        mCurClickIndex++;
    }

    public void resetFrame() {
        mCurClickIndex = -1;
    }
}
