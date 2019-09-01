package com.yunti.clickread.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yunti.clickread.widget.ClickReadPageView;
import com.yunti.clickread.widget.JazzyViewPager;
import com.yunti.clickread.widget.OutlineContainer;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/8/19 0019.
 * 点读界面viewpager adapter
 */

public class ClickReadPagerAdapter extends PagerAdapter {

    private JazzyViewPager mJazzy;
    private List<ClickReadPage> mPages;
    private Context mContext;
    private View.OnClickListener mOnBuyClickListener;
    private ClickReadPageView.ClickReadPageViewDelegate mPageViewDelegate;
    public final static String TAG_VIEW = "pager_view_";
    private String mBuyViewTag;

    public ClickReadPagerAdapter(Context context, JazzyViewPager jazzy) {
        this.mContext = context;
        this.mJazzy = jazzy;
    }

    public void setData(List<ClickReadPage> pages) {
        if (mPages != null) {
            mPages.clear();
        }
        this.mPages = pages;
        this.notifyDataSetChanged();
    }

    public void addPage(ClickReadPage page) {
        this.mPages.add(page);
        this.notifyDataSetChanged();
    }

    public void addPages(List<ClickReadPage> pages) {
        this.mPages.addAll(pages);
        notifyDataSetChanged();
    }

    public void removePage(ClickReadPage page) {
        if (mPages != null) {
            mPages.remove(page);
        }
    }

    public int getPosition(ClickReadPage page) {
        return mPages.indexOf(page);
    }

    public void setOnBuyClickListener(View.OnClickListener listener) {
        this.mOnBuyClickListener = listener;
    }

    public void setPageViewDelegate(ClickReadPageView.ClickReadPageViewDelegate pageViewDelegate) {
        mPageViewDelegate = pageViewDelegate;
    }

    public ClickReadPage getItem(int position) {
        if (CollectionUtils.isEmpty(mPages) || position < 0) {
            return null;
        }
        return mPages.get(Math.min(position, mPages.size() - 1));
    }

    public List<ClickReadTrackinfo> getTracks(int position) {
        ClickReadPage clickReadPage = getItem(position);
        return clickReadPage != null ? clickReadPage.getTracks() : null;
    }

    @Override
    public int getCount() {
        return mPages != null ? mPages.size() : 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ClickReadPage clickReadPage = getItem(position);
        ClickReadPageView pageView = new ClickReadPageView(mContext);
        if (mPageViewDelegate != null) {
            pageView.setDelegate(mPageViewDelegate);
        }
        container.addView(pageView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mJazzy.setObjectForPosition(pageView, position);
        pageView.setTag(TAG_VIEW + position);
        //-1购买页
        if (Long.valueOf(-1L).equals(clickReadPage.getId())) {
            mBuyViewTag = TAG_VIEW + position;
            pageView.renderBuyBookTips(clickReadPage.getAuthVal(),
                    mOnBuyClickListener);
        } else {
            pageView.setBookPage(clickReadPage, position);
        }
        return pageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object obj) {
        ClickReadPageView pageView = (ClickReadPageView) obj;
        pageView.clearBitmap();
        container.removeView(pageView);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        if (view instanceof OutlineContainer) {
            return ((OutlineContainer) view).getChildAt(0) == object;
        } else {
            return view == object;
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        ClickReadPageView pageView = (ClickReadPageView) object;
        if ((TAG_VIEW + 0).equals(pageView.getTag())
                || (mBuyViewTag != null && mBuyViewTag.equals(pageView.getTag()))
        ) {
            return PagerAdapter.POSITION_NONE;
        }
        return PagerAdapter.POSITION_UNCHANGED;
    }

}
