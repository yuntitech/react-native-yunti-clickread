package com.yunti.clickread.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.badoo.mobile.util.WeakHandler;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yunti.clickread.R;
import com.yunti.clickread.adapter.ThumbnailAdapter;
import com.yunti.util.YTDisplayHelper;
import com.yunti.view.SnappingRecyclerView;
import com.yunti.view.YTRelativeLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * @Author: kangqiang
 * @Date: 2019-08-16 17:41
 * @Last Modified by: kangqiang
 * @Last Modified time: 2019-08-16 17:41
 */
public class ClickReadThumbnailList extends YTRelativeLayout {


    private SnappingRecyclerView mRvThumbnail;
    private ThumbnailAdapter mThumbnailAdapter;
    private boolean mIsShowMenuBar = true;
    private ClickReadThumbnailListDelegate mDelegate;
    private WeakHandler mHandler = new WeakHandler(msg -> {
        hide();
        return false;
    });

    public void setDelegate(ClickReadThumbnailListDelegate delegate) {
        mDelegate = delegate;
    }

    public ClickReadThumbnailList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickReadThumbnailList(Context context) {
        super(context);
    }

    public ThumbnailAdapter getThumbnailAdapter() {
        return mThumbnailAdapter;
    }

    @Override
    protected void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.view_click_read_thumbnail_list, this);
        mRvThumbnail = (SnappingRecyclerView) findViewById(R.id.rv_thumbnail);
        mThumbnailAdapter = new ThumbnailAdapter(getContext());
        mRvThumbnail.setAdapter(mThumbnailAdapter);
    }

    public void setData(List<ClickReadPage> pages, Long crId) {
        mThumbnailAdapter.setData(pages, crId);
    }

    public void scrollToPosition(int position) {
        mRvThumbnail.scrollToPosition(position);
    }

    public void scrollTo(int position) {
        mRvThumbnail.scrollTo(position);
    }

    public int getCount() {
        return mThumbnailAdapter.getItemCount();
    }


    public void toggle() {
        if (mIsShowMenuBar) {
            hide();
        } else {
            show();
        }
    }

    public void hide() {
        if (!mIsShowMenuBar) {
            return;
        }
        mHandler.removeMessages(1);
        mIsShowMenuBar = false;
        AnimatorSet set = new AnimatorSet();
        List<Animator> animators = new ArrayList<>();
        animators.add(android.animation.ObjectAnimator.ofFloat(this,
                "translationY", 0, YTDisplayHelper.dpToPx(62)));
        View playTracksView = getPlayTracksView();
        if (playTracksView != null) {
            animators.add(android.animation.ObjectAnimator.ofFloat(playTracksView,
                    "translationY", 0, YTDisplayHelper.dpToPx(62)));
        }
        set.playTogether(animators);
        set.setDuration(300).start();
    }

    public void show() {
        hideDelay();
        mIsShowMenuBar = true;
        AnimatorSet set = new AnimatorSet();
        List<Animator> animators = new ArrayList<>();
        animators.add(android.animation.ObjectAnimator.ofFloat(this,
                "translationY", YTDisplayHelper.dpToPx(62), 0));
        View playTracksView = getPlayTracksView();
        if (playTracksView != null) {
            animators.add(android.animation.ObjectAnimator.ofFloat(playTracksView,
                    "translationY", YTDisplayHelper.dpToPx(62), 0));
        }
        set.playTogether(animators);
        set.setDuration(300).start();
    }

    public void hideDelay() {
        mHandler.removeMessages(1);
        mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    private View getPlayTracksView() {
        return mDelegate != null ? mDelegate.getPlayTracksView() : null;
    }

    public void setOnViewSelectedListener(SnappingRecyclerView.OnViewSelectedListener listener) {
        mRvThumbnail.setOnViewSelectedListener(listener);
    }

    public interface ClickReadThumbnailListDelegate {
        View getPlayTracksView();

    }
}
