package com.yunti.clickread.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.cqtouch.entity.BaseType;
import com.yt.ytdeep.client.dto.BuyResultDTO;
import com.yt.ytdeep.client.dto.ClickReadCatalogDTO;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yt.ytdeep.client.dto.UserOrderDTO;
import com.yunti.clickread.FetchInfo;
import com.yunti.clickread.PlayerManager;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.Utils;
import com.yunti.clickread.YTApi;
import com.yunti.clickread.adapter.ClickReadPagerAdapter;
import com.yunti.clickread.widget.ClickReadPageView;
import com.yunti.clickread.widget.ClickReadThumbnailList;
import com.yunti.clickread.widget.ClickReadTitleBar;
import com.yunti.clickread.widget.JoinBookShelfButton;
import com.yunti.view.SnappingRecyclerView;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


public class ClickReadFragment extends Fragment implements
        ClickReadPageView.ClickReadPageViewDelegate, PlayerManager.EventListener,
        ViewPager.OnPageChangeListener, SnappingRecyclerView.OnViewSelectedListener,
        View.OnClickListener, ClickReadThumbnailList.ClickReadThumbnailListDelegate,
        JoinBookShelfButton.JoinBookShelfButtonDelegate, YTApi.Callback<ClickReadDTO> {

    private ClickReadThumbnailList mClickReadThumbnailList;
    private ImageButton mPlayTracks;
    private TextView mPage;
    private ViewPager mViewPager;
    private ClickReadPagerAdapter mPagerAdapter;
    private PlayerManager mPlayerManager;
    private TextView mLoading;
    private ClickReadTitleBar mTitleBar;
    private boolean isScrollByThumbnail = false;
    private boolean isShowClickArea = false;
    private ClickReadFragmentDelegate mDelegate;
    private ClickReadDTO mClickReadDTO;
    private JoinBookShelfButton mJoinBookShelfButton;
    private int prevPosition;
    private boolean isBought = false;
    private int mFreeEndPageIndex = -1;
    private boolean mIsInBookShelf = false;
    private List<ClickReadPage> mClickReadPages;
    private boolean mPlayTracksPageChanged = false;

    public boolean isBought() {
        return isBought;
    }

    public void setDelegate(ClickReadFragmentDelegate delegate) {
        if (delegate != null) {
            mDelegate = delegate;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (mPlayerManager != null) {
            mPlayerManager.release();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FetchInfo.setHostAndApiCommonParameters(getArguments());
        if (getContext() != null && getContext().getApplicationContext() != null) {
            mPlayerManager = new PlayerManager(this, getContext().getApplicationContext());
            mPlayerManager.setEventListener(this);
            fetchData();
            renderJoinBookShelfButton();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_click_read, container, false);
        mLoading = (TextView) rootView.findViewById(R.id.tv_loading);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_paper);
        mClickReadThumbnailList = rootView.findViewById(R.id.layout_bottom_bar);
        mClickReadThumbnailList.setDelegate(this);
        mJoinBookShelfButton = rootView.findViewById(R.id.layout_add_to_book_shelf);
        mJoinBookShelfButton.setDelegate(this);
        mPage = rootView.findViewById(R.id.tv_page_index);
        mPlayTracks = rootView.findViewById(R.id.btn_play_tracks);
        mPlayTracks.setOnClickListener(this);
        mPagerAdapter = new ClickReadPagerAdapter(getContext());
        mPagerAdapter.setPageViewDelegate(this);
        mPagerAdapter.setOnBuyClickListener(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mClickReadThumbnailList.setOnViewSelectedListener(this);
        mTitleBar = (ClickReadTitleBar) rootView.findViewById(R.id.view_title);
        mTitleBar.setOnClickListener(this);
        mPlayTracks.setEnabled(false);
        return rootView;
    }

    private void fetchData() {
        mLoading.setText("数据加载中...");
        mIsInBookShelf = isInBookShelf();
        FetchInfo.FetchInfoParams fetchInfoParams = FetchInfo.queryByBookId(getBookId());
        YTApi.loadCache(fetchInfoParams, this, this);
    }


    @Override
    public void onFailure(int code, String errorMsg) {
        switch (code) {
            case YTApi.API_CODE_CACHE:
                YTApi.fetch(FetchInfo.queryByBookId(getBookId()), this, this);
                break;
            case YTApi.API_CODE_NET:
                mLoading.setText("数据加载失败...");
                break;
        }
    }

    @Override
    public void onResponse(int code, ClickReadDTO response) {
        fetchIsBuy(response.getId());
        mClickReadDTO = response;
        if (mPlayerManager != null) {
            mPlayerManager.setClickReadId(response.getId());
        }
        if (mDelegate != null) {
            mDelegate.onResponse(response);
        }
        mClickReadPages = getClickReadPages(response);
        render();
        if (YTApi.API_CODE_CACHE == code) {
            YTApi.fetch(FetchInfo.queryByBookId(getBookId()), null, null);
        }
    }

    private void fetchIsBuy(long crId) {
        YTApi.fetch(FetchInfo.isBuy(crId, UserOrderDTO.USERORDER_ORDERTYPE_BUY_CLICKREAD),
                new YTApi.Callback<BuyResultDTO>() {
                    @Override
                    public void onFailure(int code, String errorMsg) {

                    }

                    @Override
                    public void onResponse(int code, BuyResultDTO response) {
                        isBought = response.isBuySuccess();
                    }
                }, this);
    }

    private boolean isInBookShelf() {
        return getArguments() != null
                && getArguments().getBoolean("isInBookShelf", false);
    }

    private Long getBookId() {
        Double bookId = getArguments() != null ? getArguments().getDouble("bookId") : null;
        return bookId != null ? bookId.longValue() : 0;
    }

    private List<ClickReadPage> getClickReadPages(ClickReadDTO clickReadDTO) {
        List<ClickReadPage> clickReadPages = null;
        if (clickReadDTO != null && clickReadDTO.getChapters() != null) {
            clickReadPages = new ArrayList<>();
            for (ClickReadCatalogDTO chapter : clickReadDTO.getChapters()) {
                //试读结束位置
                if (mFreeEndPageIndex == -1
                        && !ClickReadCatalogDTO.CRCODE_AUTH_CODE_BOOK_SHIDUCRCODE.equals(chapter.getAuthType())
                        && !ClickReadDTO.CLICKREAD_AUTHTYPE_NEED_NO.equals(clickReadDTO.getAuthType())
                ) {
                    mFreeEndPageIndex = clickReadPages.size();
                }
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
        mClickReadThumbnailList.toggle();
    }

    @Override
    public long getClickReadId() {
        return mClickReadDTO != null ? mClickReadDTO.getId() : 0;
    }

    @Override
    public void onTrackEnd() {
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            pageView.hideFrame();
        }
    }

    @Override
    public void onTrackListEnd() {
        mPlayTracksPageChanged = true;
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void onSwitchTrack(ClickReadTrackinfo trackInfo) {
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            pageView.switchTrack(trackInfo);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mJoinBookShelfButton.collapseAddShelfButton();
    }

    @Override
    public void onPageSelected(int position) {
        refresh();
        if (!isBought && position == mPagerAdapter.getCount() - 1) {
            playOrPauseTracksIfPageChanged(true);
            setButtonsVisible(false);
        } else {
            playOrPauseTracksIfPageChanged(false);
            ClickReadPageView prevPageView = getPageView(prevPosition);
            if (prevPageView != null) {
                prevPageView.reset();
            }
            prevPosition = position;
            ClickReadPageView curPageView = getCurrentPageView();
            if (curPageView != null) {
                if (isShowClickArea) {
                    curPageView.showClickArea();
                } else {
                    curPageView.splashClickArea();
                }
                if (!isScrollByThumbnail) {
                    mClickReadThumbnailList.scrollToPosition(position);
                }
                isScrollByThumbnail = false;
                setButtonsVisible(true);
            }
        }
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
        mJoinBookShelfButton.collapseAddShelfButton();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_catalog) {
            mDelegate.onCatalogClick();
        } else if (viewId == R.id.btn_buy
                || viewId == R.id.layout_buy_clickread_book) {
            RNYtClickreadModule.openOrderHomeScreen(mClickReadDTO, getContext());
        } else if (viewId == R.id.img_back) {
            mDelegate.onBackClick();
        } else if (viewId == R.id.btn_click_area) {
            toggleClickArea();
        } else if (viewId == R.id.btn_play_tracks) {
            onPressPlayTracks();
        }
    }

    private void refresh() {
        renderPage();
        if (CollectionUtils.isEmpty(mPagerAdapter.getTracks(mViewPager.getCurrentItem()))) {
            mTitleBar.setClickAreaEnabled(false);
            mPlayTracks.setEnabled(false);
        } else {
            mTitleBar.setClickAreaEnabled(true);
            mPlayTracks.setEnabled(true);
        }
    }

    private void render() {
        mLoading.setText("");
        if (isBought) {
            mPagerAdapter.setData(mClickReadPages);
            mClickReadThumbnailList.setData(mClickReadPages);
        } else {
            int useFreeEndPageIndex = Math.max(mFreeEndPageIndex, 0);
            List<ClickReadPage> freePageList = new ArrayList<>();
            for (int n = 0; n < useFreeEndPageIndex; n++) {
                freePageList.add(mClickReadPages.get(n));
            }
            ClickReadPage fakePage = new ClickReadPage();
            fakePage.setAuthVal(mClickReadDTO.getAuthVal());
            fakePage.setId(-1L);
            freePageList.add(fakePage);
            mPagerAdapter.setData(freePageList);
            mClickReadThumbnailList.setData(freePageList.subList(0, freePageList.size() - 1));

            if (useFreeEndPageIndex == 0) {
                setButtonsVisible(false);
            }
        }
        refresh();
    }

    public void buySuccess() {
        isBought = true;
        mTitleBar.setBuyButtonVisible(false);
        if (CollectionUtils.isNotEmpty(mClickReadPages)) {
            renderPage();
            setButtonsVisible(true);
            ClickReadPage fakePage = mPagerAdapter.getItem(mFreeEndPageIndex);
            if (fakePage != null
                    && Long.valueOf(-1L).equals(fakePage.getId())) {
                mPagerAdapter.removePage(fakePage);
            }
            if (mFreeEndPageIndex >= 0) {
                List<ClickReadPage> chargePages = mClickReadPages.subList(mFreeEndPageIndex,
                        mClickReadPages.size());
                mPagerAdapter.addPages(chargePages);
                mClickReadThumbnailList.getThumbnailAdapter().addItems(chargePages);
                mPagerAdapter.refreshBookBuyTipsView(mViewPager, mFreeEndPageIndex);
                mClickReadThumbnailList.scrollToPosition(mViewPager.getCurrentItem());
            }
        }
    }


    private void renderPage() {
        int currentPage = mViewPager.getCurrentItem() + 1;
        if (isBought) {
            mPage.setText(Utils.format("%1$d/%2$d",
                    currentPage, mPagerAdapter.getCount()));
        } else {
            mPage.setText(Utils.format("试读%1$d/%2$d",
                    currentPage, mPagerAdapter.getCount() - 1));
        }
    }

    private void renderJoinBookShelfButton() {
        mJoinBookShelfButton.setVisibility(mIsInBookShelf ? View.GONE : View.VISIBLE);
    }

    private void onPressPlayTracks() {
        if (mPlayerManager == null || getContext() == null) {
            return;
        }
        mPlayerManager.togglePlayTracks();
        if (mPlayerManager.isPlayTracks()) {
            playTracks();
            RNYtClickreadModule.showToast(getContext(), R.string.start_read);
        } else {
            mPlayerManager.stopTracks();
            RNYtClickreadModule.showToast(getContext(), R.string.read_has_stopped);
            renderPlayTracks(R.drawable.selector_play_tracks_play);
        }
    }

    private void playTracks() {
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            pageView.resetClickArea();
        }
        List<ClickReadTrackinfo> tracks = mPagerAdapter.getTracks(mViewPager.getCurrentItem());
        if (CollectionUtils.isNotEmpty(tracks)) {
            mPlayTracks.setImageResource(R.drawable.ic_play_tracks);
            mPlayerManager.playTracks(tracks);
        } else {
            mPlayerManager.stopTracks();
            renderPlayTracks(R.drawable.selector_play_tracks_play);
        }
    }

    private void toggleClickArea() {
        isShowClickArea = !isShowClickArea;
        mTitleBar.setClickAreaShow(isShowClickArea);
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            if (isShowClickArea) {
                pageView.showClickArea();
            } else {
                pageView.hideClickArea();
            }

        }
    }

    private void setButtonsVisible(boolean visible) {
        if (visible) {
            if (mPlayTracks.getVisibility() == View.INVISIBLE) {
                mPlayTracks.setVisibility(View.VISIBLE);
                mTitleBar.setVisibility(View.VISIBLE);
                mPage.setVisibility(View.VISIBLE);
                mClickReadThumbnailList.show();
                if (!mIsInBookShelf) {
                    mJoinBookShelfButton.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (mPlayTracks.getVisibility() == View.VISIBLE) {
                mPlayTracks.setVisibility(View.INVISIBLE);
                mTitleBar.setVisibility(View.INVISIBLE);
                mPage.setVisibility(View.INVISIBLE);
                mClickReadThumbnailList.hide();
                if (!mIsInBookShelf) {
                    mJoinBookShelfButton.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


    @Override
    public View getPlayTracksView() {
        return mPlayTracks;
    }

    private void renderPlayTracks(int drawable) {
        if (getContext() == null) {
            return;
        }
        mPlayTracks.setImageDrawable(ContextCompat.getDrawable(getContext(), drawable));
    }

    private void playOrPauseTracksIfPageChanged(boolean stopPlay) {
        if (mPlayerManager == null) {
            return;
        }
        if (mPlayTracksPageChanged) {
            mPlayTracksPageChanged = false;
            if (stopPlay) {
                mPlayerManager.stopTracks();
                renderPlayTracks(R.drawable.selector_play_tracks_play);
            } else {
                playTracks();
            }

        } else {
            renderPlayTracks(R.drawable.selector_play_tracks_play);
            mPlayerManager.pause();
        }
    }

    @Override
    public void joinBookShelf() {
        YTApi.fetch(FetchInfo.joinBookShelf(getBookId()), new YTApi.Callback<BaseType>() {
            @Override
            public void onFailure(int code, String errorMsg) {
                RNYtClickreadModule.showToast(getContext(), errorMsg);
            }

            @Override
            public void onResponse(int code, BaseType response) {
                if (BaseType.BOOLEAN_TRUE.equals(response.getResult())) {
                    mIsInBookShelf = true;
                    RNYtClickreadModule.showToast(getContext(), "已加入学习");
                    renderJoinBookShelfButton();
                    RNYtClickreadModule.joinBookShelfSuccess(getContext());
                } else {
                    onFailure(YTApi.API_CODE_NET, "加入学习失败");
                }
            }
        }, this);

    }

    private ClickReadPageView getPageView(int position) {
        String itemTag = Utils.format("%s%d",
                ClickReadPagerAdapter.TAG_VIEW, position);
        return mViewPager.findViewWithTag(itemTag);
    }

    private ClickReadPageView getCurrentPageView() {
        return getPageView(mViewPager.getCurrentItem());
    }

    public interface ClickReadFragmentDelegate {
        void onResponse(ClickReadDTO clickReadDTO);

        void onCatalogClick();

        void onBackClick();
    }
}
