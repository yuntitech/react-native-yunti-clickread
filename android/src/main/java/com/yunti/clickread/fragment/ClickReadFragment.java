package com.yunti.clickread.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.yt.ytdeep.client.dto.ClickReadCatalogDTO;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yunti.clickread.FetchInfo;
import com.yunti.clickread.PlayerManager;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.adapter.ClickReadPagerAdapter;
import com.yunti.clickread.adapter.ThumbnailAdapter;
import com.yunti.clickread.widget.ClickReadPageView;
import com.yunti.clickread.widget.ClickReadTitleBar;
import com.yunti.view.SnappingRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class ClickReadFragment extends Fragment implements Callback,
        ClickReadPageView.OnClickAreaListener, PlayerManager.EventListener,
        ViewPager.OnPageChangeListener, SnappingRecyclerView.OnViewSelectedListener, View.OnClickListener {

    private SnappingRecyclerView mRvThumbnail;
    private ThumbnailAdapter mThumbnailAdapter;
    private ViewPager mViewPager;
    private ClickReadPagerAdapter mPagerAdapter;
    private PlayerManager mPlayerManager;
    private TextView mLoading;
    private boolean isScrollByThumbnail = false;
    private ClickReadFragmentDelegate mDelegate;
    private ClickReadDTO mClickReadDTO;

    public void setDelegate(ClickReadFragmentDelegate delegate) {
        if (delegate != null) {
            mDelegate = delegate;
        }
    }

    public ClickReadDTO getClickReadDTO() {
        return mClickReadDTO;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FetchInfo.setHostAndApiCommonParameters(getArguments());
        mPlayerManager = new PlayerManager(getContext());
        mPlayerManager.setEventListener(this);
        fetchData(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_click_read, container, false);
        mLoading = (TextView) rootView.findViewById(R.id.tv_loading);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_paper);
        mRvThumbnail = (SnappingRecyclerView) rootView.findViewById(R.id.rv_thumbnail);
        mPagerAdapter = new ClickReadPagerAdapter(getContext(), mViewPager);
        mPagerAdapter.setOnClickAreaListener(this);
        mViewPager.setAdapter(mPagerAdapter);
        mThumbnailAdapter = new ThumbnailAdapter(getContext());
        mRvThumbnail.setAdapter(mThumbnailAdapter);
        mViewPager.addOnPageChangeListener(this);
        mRvThumbnail.setOnViewSelectedListener(this);
        ClickReadTitleBar titleBar = (ClickReadTitleBar) rootView.findViewById(R.id.view_title);
        titleBar.setOnCatalogClickListener(this);
        titleBar.setOnBuyClickListener(this);
        titleBar.setOnBackClickListener(this);
        return rootView;
    }

    private void fetchData(Callback callback) {
        mLoading.setText("数据加载中...");
        FetchInfo.FetchInfoParams fetchInfoParams = FetchInfo.queryByBookId(getBookId());
        Request request = new Request.Builder()
                .post(fetchInfoParams.getFormBody())
                .url(fetchInfoParams.getUrl())
                .build();
        OkHttpClientProvider.getOkHttpClient().newCall(request).enqueue(callback);
    }


    private Long getBookId() {
        Double bookId = getArguments() != null ? getArguments().getDouble("bookId") : null;
        return bookId != null ? bookId.longValue() : null;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoading.setText("数据加载失败...");
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        String responseData = response.body().string();
        JSONObject responseObject = JSON.parseObject(responseData);
        mClickReadDTO = JSON.parseObject(responseObject.getString("data"),
                ClickReadDTO.class);
        mPlayerManager.setClickReadId(mClickReadDTO.getId());
        if (mDelegate != null) {
            mDelegate.onResponse(mClickReadDTO);
        }
        final List<ClickReadPage> clickReadPages = getClickReadPages(mClickReadDTO);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoading.setText("");
                mPagerAdapter.setData(clickReadPages);
                mThumbnailAdapter.setData(clickReadPages);
            }
        });

    }

    private List<ClickReadPage> getClickReadPages(ClickReadDTO clickReadDTO) {
        List<ClickReadPage> clickReadPages = null;
        if (clickReadDTO != null && clickReadDTO.getChapters() != null) {
            clickReadPages = new ArrayList<>();
            for (ClickReadCatalogDTO chapter : clickReadDTO.getChapters()) {
                if (chapter.getSections() != null) {
                    for (ClickReadCatalogDTO section : chapter.getSections()) {
                        if (section.getPages() != null) {
                            clickReadPages.addAll(section.getPages());
                        }
                    }
                }
            }
        }
        return clickReadPages;
    }

    @Override
    public void onClickHotArea(ClickReadTrackinfo track) {

        if (mPlayerManager != null) {
            mPlayerManager.play(track);
        }


    }

    @Override
    public void onClickSameArea() {
        if (mPlayerManager != null) {
            mPlayerManager.playAgain();
        }
    }

    @Override
    public void onClickOtherArea() {

    }

    @Override
    public void onTrackEnd() {
        String itemTag = String.format(Locale.CHINA, "%s%d",
                ClickReadPagerAdapter.VIEW_TAG, mViewPager.getCurrentItem());
        ClickReadPageView clickReadPageView = (ClickReadPageView) mViewPager.findViewWithTag(itemTag);
        if (clickReadPageView != null) {
            clickReadPageView.hideFrame();
        }
    }

    @Override
    public void onSwitchTrack(ClickReadTrackinfo trackInfo) {

    }

    public void runOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(runnable);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (!isScrollByThumbnail) {
            mRvThumbnail.scrollToPosition(position);
        }
        isScrollByThumbnail = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSelected(View view, int position) {
        isScrollByThumbnail = true;
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onScrolled() {

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tv_catalog) {
            mDelegate.onCatalogClick();
        } else if (viewId == R.id.tv_buy) {
            onBuyClick();
        } else if (viewId == R.id.img_back) {
            mDelegate.onBackClick();
        }
    }

    private void onBuyClick() {
        if (mClickReadDTO != null && getContext() != null) {
            try {
                Long clickReadId = mClickReadDTO.getId();
                RNYtClickreadModule.push(getContext(),
                        "cn.bookln.ConfirmOrderHomeScreen", clickReadId, 8);
                Intent intent = new Intent();
                Class<?> clazz = Class.forName("com.reactnativenavigation.controllers.NavigationActivity");
                intent.setClass(getContext(), clazz);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public interface ClickReadFragmentDelegate {
        void onResponse(ClickReadDTO clickReadDTO);

        void onCatalogClick();

        void onBackClick();
    }
}
