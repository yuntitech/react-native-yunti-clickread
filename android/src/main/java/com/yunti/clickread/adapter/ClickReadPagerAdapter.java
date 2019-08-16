package com.yunti.clickread.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yunti.clickread.widget.ClickReadPageView;

import java.util.List;

/**
 * Created by Administrator on 2016/8/19 0019.
 * 点读界面viewpager adapter
 */

public class ClickReadPagerAdapter extends PagerAdapter {

    private ViewPager mJazzy;
    private List<ClickReadPage> mPages;
    private Context mContext;
    private ClickReadPageView.OnClickAreaListener mClickListener;
    private ClickReadPageView.ImageLoadListener mImageLoadListener;
    public final static String VIEW_TAG = "pager_view_";

    public ClickReadPagerAdapter(Context context, ViewPager jazz) {
        this.mContext = context;
        this.mJazzy = jazz;
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
        this.notifyDataSetChanged();
    }

    public void setOnClickAreaListener(ClickReadPageView.OnClickAreaListener listener) {
        this.mClickListener = listener;
    }

    public void setImageLoadListener(ClickReadPageView.ImageLoadListener listener) {
        this.mImageLoadListener = listener;
    }


    public ClickReadPage getItem(int position) {
        if (position >= mPages.size()) {
            position = mPages.size() - 1;
        }
        return mPages.get(position);
    }

    @Override
    public int getCount() {
        return mPages != null ? mPages.size() : 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        ClickReadPageView pageView = new ClickReadPageView(mContext);
        pageView.setBookPage(mPages.get(position), position);
        if (mClickListener != null) {
            pageView.setOnClickAreaListener(mClickListener);
        }
        if (mImageLoadListener != null) {
            pageView.setImageLoadListener(mImageLoadListener);
        }
        container.addView(pageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pageView.setTag(VIEW_TAG + position);
        return pageView;
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
