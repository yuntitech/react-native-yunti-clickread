package com.yunti.clickread.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yunti.clickread.widget.ClickReadPageView;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/8/19 0019.
 * 点读界面viewpager adapter
 */

public class ClickReadPagerAdapter extends PagerAdapter {

    private List<ClickReadPage> mPages;
    private Context mContext;
    private View.OnClickListener mOnBuyClickListener;
    private ClickReadPageView.ClickReadPageViewDelegate mPageViewDelegate;
    private ClickReadPageView.ImageLoadListener mImageLoadListener;
    public final static String TAG_VIEW = "pager_view_";
    public final static String TAG_BUY_VIEW = "pager_buy_view";

    public ClickReadPagerAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<ClickReadPage> pages) {
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

    public void setOnBuyClickListener(View.OnClickListener listener) {
        this.mOnBuyClickListener = listener;
    }

    public void setPageViewDelegate(ClickReadPageView.ClickReadPageViewDelegate pageViewDelegate) {
        mPageViewDelegate = pageViewDelegate;
    }

    public void setImageLoadListener(ClickReadPageView.ImageLoadListener listener) {
        this.mImageLoadListener = listener;
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
        if (mImageLoadListener != null) {
            pageView.setImageLoadListener(mImageLoadListener);
        }
        container.addView(pageView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pageView.setTag(TAG_VIEW + position);
        //-1购买页
        if (Long.valueOf(-1L).equals(clickReadPage.getId())) {
            pageView.renderBuyBookTips(clickReadPage.getAuthVal(),
                    mOnBuyClickListener);
        } else {
            pageView.setBookPage(clickReadPage, position);
        }
        return pageView;
    }


    public void refreshBookBuyTipsView(ViewPager viewPager, int position) {
        ClickReadPageView pageView = viewPager.findViewWithTag(TAG_VIEW + position);
        if (pageView != null) {
            pageView.refreshBookBuyTipsView();
            pageView.setBookPage(getItem(position), position);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object obj) {
        container.removeView((View) obj);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
