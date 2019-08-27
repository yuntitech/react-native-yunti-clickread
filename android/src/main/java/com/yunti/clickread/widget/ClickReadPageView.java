package com.yunti.clickread.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nineoldandroids.animation.ObjectAnimator;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yunti.clickread.FetchInfo;
import com.yunti.clickread.R;
import com.yunti.util.ResourceUtils;
import com.yunti.util.YTDisplayHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/19 0019.
 * 点读单页view
 */

public class ClickReadPageView extends RelativeLayout {

    public static final int BITMAP_WIDTH = 720;
    public static final int BITMAP_HEIGHT = 1280;

    private ImageView mPageBg;
    private FrameMask mFrameMask;
    private ClickArea mClickArea;
    private ClickReadPage mClickPage;
    private RelativeLayout mLayoutLoad;
    private TextView mNetWorkErrorTips;
    private TextView mBuyNoticeView;
    private SimpleTarget<Bitmap> mImgTarget;
    private Context mContext;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mItemHeight;
    private int mItemWidth;
    private int mOriginalIteHeight;
    private int mOriginalIteWidth;
    private List<RectF> mOriginalFrames;
    private ObjectAnimator mAnimator;
    private PhotoViewAttacher mAttacher;
    private RectF mCurFrame;
    private ClickReadPageViewDelegate mDelegate;
    private ImageLoadListener mImageLoadListener;
    private final static int TITLE_BAR_HEIGHT = 64;
    private final static int MENU_BOTTOM_HEIGHT = 62;
    private int offsetTop;
    private int offsetLeft;
    private int statusBarHeight;
    private boolean isLoadImageSuccess = false;
    private int position;
    private boolean isShowClickArea = false;

    public ClickReadPageView(Context context) {
        super(context);
        init(context);
    }

    public ClickReadPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ClickReadPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setDelegate(ClickReadPageViewDelegate delegate) {
        mDelegate = delegate;
    }

    public void setImageLoadListener(ImageLoadListener listener) {
        this.mImageLoadListener = listener;
    }

    public void refreshPage() {
        setBookPage(mClickPage, position);
    }

    public void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_click_read_page, this, true);
        mPageBg = (ImageView) findViewById(R.id.page_bg);
        mFrameMask = (FrameMask) findViewById(R.id.frame_mask);
        mClickArea = (ClickArea) findViewById(R.id.pointing_area);
        mBuyNoticeView = (TextView) findViewById(R.id.buy_notice_view);
        mNetWorkErrorTips = (TextView) findViewById(R.id.network_error_tips);
        mLayoutLoad = (RelativeLayout) findViewById(R.id.layout_resource_load);
        mAttacher = new PhotoViewAttacher(mPageBg);
        mOriginalFrames = new ArrayList<>();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mItemWidth = mScreenWidth;
        mItemHeight = mScreenHeight - YTDisplayHelper.dpToPx(TITLE_BAR_HEIGHT) - YTDisplayHelper.dpToPx(MENU_BOTTOM_HEIGHT) - statusBarHeight;
        mClickArea.setOnClickPointingAreaListener(new ClickArea.OnClickPointingAreaListener() {
            @Override
            public void onClickHotArea(int position, RectF frame) {
                if (mCurFrame != null
                        && mCurFrame.left == frame.left
                        && mCurFrame.right == frame.right
                        && mCurFrame.top == frame.top
                        && mCurFrame.bottom == frame.bottom) {
                    if (mDelegate != null && position < mClickPage.getTracks().size()) {
                        mDelegate.onClickSameArea();
                        switchFrame(mCurFrame);
                    }
                } else {
                    mCurFrame = frame;
                    switchFrame(frame);
                    if (mDelegate != null && position < mClickPage.getTracks().size()) {
                        mDelegate.onClickHotArea(mClickPage.getTracks().get(position));
                    }
                }
            }

            @Override
            public void onClickOtherArea() {
                if (mDelegate != null) {
                    mDelegate.onClickOtherArea();
                }
            }
        });
        Rect frame = new Rect();
        ((Activity) mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;
        mNetWorkErrorTips.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                setBookPage(mClickPage, position);
            }
        });

        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v1) {
                mClickArea.doClickEvent(v, v1);
            }
        });

        mAttacher.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rectF) {
                offsetLeft = (int) rectF.left;
                offsetTop = (int) rectF.top;
                mItemWidth = (int) (rectF.right - rectF.left);
                mItemHeight = (int) (rectF.bottom - rectF.top);
                if (mClickPage.getTracks() != null) {
                    mClickArea.drawPointingArea(convertTrackToRectF(mClickPage));
                }
                if (mClickArea.getCurClickArea() != null) {
                    mFrameMask.clearAnimation();
                    mFrameMask.showFrame(mClickArea.getCurClickArea().top, mClickArea.getCurClickArea().left,
                            mClickArea.getCurClickArea().right, mClickArea.getCurClickArea().bottom);
                }
            }
        });
    }

    public void switchFrame(RectF frame) {
        mCurFrame = frame;
        mFrameMask.switchFrame(frame);
        mFrameMask.setVisibility(VISIBLE);
        mFrameMask.setAlpha(1);
    }

    public void hideFrame() {
        mFrameMask.setAlpha(0);
    }

    public void switchTrack(ClickReadTrackinfo track) {
        if (mFrameMask != null) {
            mCurFrame = new RectF(track.getL() * mItemWidth + offsetLeft, track.getT() * mItemHeight + offsetTop
                    , track.getR() * mItemWidth + offsetLeft, track.getB() * mItemHeight + offsetTop);
            boolean isTranslateLeft = false;
            boolean isTranslateTop = false;
            RectF originalFrame = null;
            if (mOriginalFrames != null && mClickArea.getCurClickAreaIndex() + 1 < mOriginalFrames.size()) {
                originalFrame = mOriginalFrames.get(mClickArea.getCurClickAreaIndex() + 1);
            } else {
                return;
            }
            //图片高度大于显示区域高度
            if (mCurFrame.top + mOriginalIteHeight - mCurFrame.bottom < 0) {
                float scale = (float) ((mOriginalIteHeight * 1.0) / (mCurFrame.bottom - mCurFrame.top));
                //进行缩放，放大到图片高度等于显示区域高度
                mAttacher.setScale(mOriginalIteHeight / (originalFrame.bottom - originalFrame.top), 0, 0, true);
                mCurFrame.bottom = mCurFrame.top + (mCurFrame.bottom - mCurFrame.top) * scale;
                mCurFrame.right = mCurFrame.left + (mCurFrame.right - mCurFrame.left) * scale;
                if (originalFrame != null) {
                    //水平方向上滚动到原来的位置
                    mAttacher.getSuppMatrix().postTranslate(originalFrame.left - mCurFrame.left, 0);
                    mCurFrame.right = mCurFrame.right - mCurFrame.left + originalFrame.left;
                    mCurFrame.left = originalFrame.left;
                }
                mAttacher.setImageViewMatrix(mAttacher.getDrawMatrix());
            }
            //图片宽度大于显示区域宽度
            if (mCurFrame.left + mOriginalIteWidth - mCurFrame.right < 0) {
                float scale = (float) ((mOriginalIteWidth * 1.0) / (mCurFrame.right - mCurFrame.left));
                //进行缩放，放大到图片宽度等于显示区域宽度
                mAttacher.setScale(mOriginalIteWidth / (originalFrame.right - originalFrame.left), 0, 0, true);
                mCurFrame.bottom = mCurFrame.top + (mCurFrame.bottom - mCurFrame.top) * scale;
                mCurFrame.right = mCurFrame.left + (mCurFrame.right - mCurFrame.left) * scale;
                if (originalFrame != null) {
                    //垂直方向上滚动到原来的位置
                    mAttacher.getSuppMatrix().postTranslate(0, originalFrame.top - mCurFrame.top);
                    mCurFrame.bottom = mCurFrame.bottom - mCurFrame.top + originalFrame.top;
                    mCurFrame.top = originalFrame.top;
                }
                mAttacher.setImageViewMatrix(mAttacher.getDrawMatrix());
            }
            //点读区域上方被遮挡
            if (mCurFrame.top < 0) {
                mAttacher.getSuppMatrix().postTranslate(0, -mCurFrame.top);
                mCurFrame.bottom = mCurFrame.bottom - mCurFrame.top;
                mCurFrame.top = 0;
                isTranslateTop = true;
                mAttacher.setImageViewMatrix(mAttacher.getDrawMatrix());
            }
            //点读区域左边被遮挡
            if (mCurFrame.left < 0) {
                mAttacher.getSuppMatrix().postTranslate(-mCurFrame.left, 0);
                mCurFrame.right = mCurFrame.right - mCurFrame.left;
                mCurFrame.left = 0;
                isTranslateLeft = true;
                mAttacher.setImageViewMatrix(mAttacher.getDrawMatrix());
            }
            //点读区域右边被遮挡
            if (mOriginalIteWidth - mCurFrame.right < 0 && !isTranslateLeft) {
                mAttacher.getSuppMatrix().postTranslate(mOriginalIteWidth - mCurFrame.right, 0);
                mCurFrame.left = mCurFrame.left + mOriginalIteWidth - mCurFrame.right;
                mCurFrame.right = mOriginalIteWidth;
                mAttacher.setImageViewMatrix(mAttacher.getDrawMatrix());
            }
            //点读区域下方被遮挡
            if (mOriginalIteHeight - mCurFrame.bottom - YTDisplayHelper.dpToPx(30) < 0 && !isTranslateTop) {
                mAttacher.getSuppMatrix().postTranslate(0, mOriginalIteHeight - mCurFrame.bottom - YTDisplayHelper.dpToPx(30));
                mCurFrame.top = mCurFrame.top + mOriginalIteHeight - mCurFrame.bottom - YTDisplayHelper.dpToPx(30);
                mCurFrame.bottom = mOriginalIteHeight - YTDisplayHelper.dpToPx(30);
                mAttacher.setImageViewMatrix(mAttacher.getDrawMatrix());
            }
            mClickArea.nextFrame();
            switchFrame(mCurFrame);
        }
    }

    public void resetClickArea() {
        mClickArea.resetFrame();
    }


    public void resetScale() {
        mAttacher.setZoomable(false);
        mAttacher.setZoomable(true);
    }

    public void splashClickArea() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mAnimator = ObjectAnimator.ofFloat(mClickArea, "alpha", 1, 0);
        mAnimator.setRepeatCount(2);
        mAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        mAnimator.setDuration(1000).start();
    }

    public void hideClickArea() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mClickArea.setAlpha(0);
    }

    public void showClickArea() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mClickArea.setAlpha(1);
    }

    public void showLoading() {
        mLayoutLoad.setVisibility(VISIBLE);
        mNetWorkErrorTips.setText("图片加载中...");
    }

    public void showLoadFail() {
        mLayoutLoad.setVisibility(VISIBLE);
        mNetWorkErrorTips.setText("图片加载失败");
        isLoadImageSuccess = false;
        if (mImageLoadListener != null) {
            mImageLoadListener.loadFail(position);
        }
    }

    public void showLoadSuccess() {
        mLayoutLoad.setVisibility(GONE);
        if (isShowClickArea) {
            showClickArea();
        } else {
            splashClickArea();
        }
        isLoadImageSuccess = true;
        if (mImageLoadListener != null) {
            mImageLoadListener.loadSuccess(position);
        }
    }

    public boolean isLoadImageSuccess() {
        return isLoadImageSuccess;
    }

    public void setBookPage(final ClickReadPage page, int position) {
        showLoading();
        mClickPage = page;
        offsetTop = 0;
        offsetLeft = 0;
        this.position = position;
        boolean smallResolution = mScreenHeight < BITMAP_HEIGHT;
        int overrideWidth = smallResolution ? mScreenWidth : BITMAP_WIDTH;
        int overrideHeight = smallResolution ? mScreenHeight : BITMAP_HEIGHT;
        mBuyNoticeView.setVisibility(GONE);
        String uri = ResourceUtils.getImageUri(mDelegate != null ? mDelegate.getClickReadId() : 0L,
                page.getImgResId(),
                page.getImgResSign(), FetchInfo.USER_ID, getContext());
        mImgTarget = new SimpleTarget<Bitmap>(mItemWidth, mItemHeight) {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                setPageBgSize();
                mPageBg.setImageBitmap(resource);
                showLoadSuccess();
            }

        };
        Glide.with(mContext)
                .asBitmap()
                .load(uri)
                .into(mImgTarget);

    }

    public void refreshBookBuyTipsView() {
        ClickReadBookBuyTipsView tipsView = findViewById(R.id.view_buy_book);
        if (tipsView != null) {
            removeView(tipsView);
        }
    }

    public void renderBuyBookTips(String authVal, OnClickListener listener) {
        ClickReadBookBuyTipsView tipsView = new ClickReadBookBuyTipsView(mContext);
        tipsView.setId(R.id.view_buy_book);
        tipsView.setOnClickListener(listener);
        tipsView.refreshBuyBtnText(authVal);
        addView(tipsView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    //设置背景图片大小
    public void setPageBgSize() {
        LayoutParams params = (LayoutParams) mPageBg.getLayoutParams();
        params.width = mItemWidth;
        params.height = mItemHeight;
        mPageBg.setLayoutParams(params);
        params = (LayoutParams) mFrameMask.getLayoutParams();
        params.width = mItemWidth;
        params.height = mItemHeight;
        mFrameMask.setLayoutParams(params);
        params = (LayoutParams) mClickArea.getLayoutParams();
        params.width = mItemWidth;
        params.height = mItemHeight;
        mClickArea.setLayoutParams(params);
        mOriginalIteHeight = mItemHeight;
        mOriginalIteWidth = mItemWidth;
        if (mClickPage.getTracks() != null) {
            List<RectF> frames = convertTrackToRectF(mClickPage);
            mOriginalFrames.addAll(frames);
            mClickArea.drawPointingArea(frames);
        }
    }

    public void reset() {
        hideFrame();
        resetScale();
        hideClickArea();
    }

    //转换成点读区域
    private List<RectF> convertTrackToRectF(ClickReadPage page) {
        List<RectF> mFrames = new ArrayList<>();
        for (ClickReadTrackinfo trackInfo : page.getTracks()) {
            RectF frame = new RectF(trackInfo.getL() * mItemWidth + offsetLeft, trackInfo.getT() * mItemHeight + offsetTop
                    , trackInfo.getR() * mItemWidth + offsetLeft, trackInfo.getB() * mItemHeight + offsetTop);
            mFrames.add(frame);
        }
        return mFrames;
    }

    public void clearBitmap() {
        if (mImgTarget == null) return;
    }

    public interface ClickReadPageViewDelegate {

        void onClickHotArea(ClickReadTrackinfo track);

        void onClickSameArea();

        void onClickOtherArea();

        long getClickReadId();
    }

    public interface ImageLoadListener {
        void loadSuccess(int position);

        void loadFail(int position);
    }

}
