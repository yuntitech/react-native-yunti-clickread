package com.yunti.clickread.fragment;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.badoo.mobile.util.WeakHandler;
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
import com.yunti.clickread.widget.JazzyViewPager;
import com.yunti.clickread.widget.JoinBookShelfButton;
import com.yunti.clickread.widget.YTLoadTipsView;
import com.yunti.view.SnappingRecyclerView;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.util.ArrayList;
import java.util.List;


public class ClickReadFragment extends Fragment implements
        ClickReadPageView.ClickReadPageViewDelegate, PlayerManager.EventListener,
        ViewPager.OnPageChangeListener, SnappingRecyclerView.OnViewSelectedListener,
        View.OnClickListener, ClickReadThumbnailList.ClickReadThumbnailListDelegate,
        JoinBookShelfButton.JoinBookShelfButtonDelegate {

    private ClickReadThumbnailList mClickReadThumbnailList;
    private ImageButton mPlayTracks;
    private TextView mPage;
    private JazzyViewPager mViewPager;
    private ClickReadPagerAdapter mPagerAdapter;
    private PlayerManager mPlayerManager;
    private YTLoadTipsView mCRLoadTipsView;
    private ClickReadTitleBar mTitleBar;
    private boolean isScrollByThumbnail = false;
    private boolean isShowClickArea = false;
    private ClickReadFragmentDelegate mDelegate;
    private ClickReadDTO mClickReadDTO;
    private JoinBookShelfButton mJoinBookShelfButton;
    private int prevPosition;
    private boolean isBought = false;
    private boolean fromStudyPlan = false;
    private int mFreeEndPageIndex = -1;
    private boolean mIsInBookShelf = false;
    private List<ClickReadPage> mClickReadPages;
    private boolean mPlayTracksPageChanged = false;
    private boolean[] mRestoreCompleted = new boolean[]{true, true};
    private int mRestorePageIndex = -1;
    private static int AUDIO_LOAD_MSG = 1;
    private WeakHandler mAudioLoadingHandler = new WeakHandler(msg -> {
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            pageView.showTrackLoading();
        }
        return false;
    });
    private Long sectionIdFromDefaultChapterId;

    public int getRestorePageIndex() {
        return mRestorePageIndex;
    }

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
    public void onStop() {
        super.onStop();
        storePageIndex();
    }

    @Override
    public void onDestroy() {
        if (mPlayerManager != null) {
            mPlayerManager.release();
        }
        mAudioLoadingHandler.removeMessages(AUDIO_LOAD_MSG);
        resetPageViews();
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FetchInfo.setHostAndApiCommonParameters(getArguments());
        if (getArguments() != null) {
            mRestorePageIndex = getArguments().getInt("restorePageIndex", -1);
        }
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
        mCRLoadTipsView = rootView.findViewById(R.id.view_load_tips);
        mCRLoadTipsView.setOnClickListener(this);
        mViewPager = (JazzyViewPager) rootView.findViewById(R.id.view_paper);
        mClickReadThumbnailList = rootView.findViewById(R.id.layout_bottom_bar);
        mClickReadThumbnailList.setDelegate(this);
        mJoinBookShelfButton = rootView.findViewById(R.id.layout_add_to_book_shelf);
        mJoinBookShelfButton.setDelegate(this);
        mPage = rootView.findViewById(R.id.tv_page_index);
        mPlayTracks = rootView.findViewById(R.id.btn_play_tracks);
        mPlayTracks.setOnClickListener(this);
        mPagerAdapter = new ClickReadPagerAdapter(getContext(), mViewPager);
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


    public void userHasChanged(boolean isInBookShelf) {
        mIsInBookShelf = isInBookShelf;
        renderJoinBookShelfButton();
        if (mClickReadDTO != null) {
            fetchIsBuy(mClickReadDTO.getId(), false, null);
        }
    }

    private void fetchData() {
        mCRLoadTipsView.showLoading();
        mIsInBookShelf = isInBookShelf();
        fromStudyPlan = fromStudyPlan();
        isBought = fromStudyPlan;
        FetchInfo.FetchInfoParams fetchInfoParams = FetchInfo.queryByBookId(getBookId());
        YTApi.loadCache(fetchInfoParams, new YTApi.Callback<ClickReadDTO>() {

            @Override
            public boolean runOnUiThread() {
                return false;
            }

            @Override
            public void onFailure(int code, String errorMsg) {
                switch (code) {
                    case YTApi.API_CODE_CACHE:
                        YTApi.fetch(FetchInfo.queryByBookId(getBookId()), this, ClickReadFragment.this);
                        break;
                    case YTApi.API_CODE_NET:
                        ClickReadFragment.this.runOnUiThread(() -> {
                            if (mClickReadDTO == null) {
                                mCRLoadTipsView.showError("数据加载失败");
                                mClickReadThumbnailList.hide();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onResponse(int code, ClickReadDTO response) {
                mClickReadDTO = response;
                if (mPlayerManager != null) {
                    mPlayerManager.setClickReadId(response.getId());
                }
                mClickReadPages = getClickReadPages(response);
                restorePageIndex(new RNYtClickreadModule.Callback() {

                    @Override
                    public void resolve(String result) {
                        Integer initPageIndex = getInitPageIndex();
                        if (initPageIndex != null) {
                            result = String.valueOf(initPageIndex);
                        }
                        if (!TextUtils.isEmpty(result)) {
                            mRestorePageIndex = Integer.parseInt(result);
                            mRestoreCompleted[0] = false;
                            mRestoreCompleted[1] = false;
                        }
                        ClickReadFragment.this.runOnUiThread(() -> {
                            mTitleBar.setSpokenTestVisible(response.getSpoken());
                            if (mDelegate != null) {
                                mDelegate.onResponse(response);
                            }
                            renderMaybeKnowIsBuy(response);
                        });

                    }
                });
                if (YTApi.API_CODE_CACHE == code) {
                    YTApi.fetch(fetchInfoParams, null, ClickReadFragment.this);
                }
            }
        }, this);
    }


    private void renderMaybeKnowIsBuy(ClickReadDTO clickReadDTO) {
        if (fromStudyPlan) {
            render();
        } else {
            fetchIsBuy(clickReadDTO.getId(), true, new YTApi.Callback<BuyResultDTO>() {

                @Override
                public void onFailure(int code, String errorMsg) {
                    if (YTApi.API_CODE_CACHE == code) {
                        render();
                        fetchIsBuy(clickReadDTO.getId(), false, this);
                    }
                }

                @Override
                public void onResponse(int code, BuyResultDTO response) {
                    switch (code) {
                        case YTApi.API_CODE_CACHE:
                            isBought = response.isBuySuccess();
                            render();
                            fetchIsBuy(clickReadDTO.getId(), false, this);
                            break;
                        case YTApi.API_CODE_NET:
                            onIsBuyResponse(response);
                            break;
                    }
                }
            });
        }
    }

    private void fetchIsBuy(long crId, boolean loadCache, YTApi.Callback<BuyResultDTO> callback) {
        YTApi.Callback<BuyResultDTO> isBuyCallback = new YTApi.Callback<BuyResultDTO>() {
            @Override
            public void onFailure(int code, String errorMsg) {
                if (callback != null) {
                    callback.onFailure(code, errorMsg);
                }
            }

            @Override
            public void onResponse(int code, BuyResultDTO response) {
                if (callback != null) {
                    callback.onResponse(code, response);
                    return;
                }
                onIsBuyResponse(response);
            }
        };
        FetchInfo.FetchInfoParams fetchInfoParams
                = FetchInfo.isBuy(crId, UserOrderDTO.USERORDER_ORDERTYPE_BUY_CLICKREAD);
        if (loadCache) {
            YTApi.loadCache(fetchInfoParams, isBuyCallback, this);
        } else {
            YTApi.fetch(fetchInfoParams, isBuyCallback, this);
        }
    }

    private void onIsBuyResponse(BuyResultDTO netResponse) {
        boolean prevIsBought = isBought;
        if (prevIsBought && netResponse.isBuySuccess()) {
            return;
        }
        isBought = netResponse.isBuySuccess();
        if (isBought) {
            buySuccess();
        }
        //退款的情况
        else if (prevIsBought) {
            mPagerAdapter.setBuyViewTag(Math.max(mFreeEndPageIndex, 0));
            mRestoreCompleted[0] = true;
            mRestoreCompleted[1] = true;
            mRestorePageIndex = Math.max(mFreeEndPageIndex - 1, 0);
            mViewPager.setCurrentItem(mRestorePageIndex);
            renderFreePages();
            if (mDelegate != null) {
                mDelegate.onBuyResult(isBought);
            }
            mTitleBar.setBuyButtonVisible(!isBought);
        }
    }

    private boolean isInBookShelf() {
        return getArguments() != null
                && getArguments().getBoolean("isInBookShelf", false);
    }

    private boolean fromStudyPlan() {
        return getArguments() != null
                && getArguments().getBoolean("fromStudyPlan", false);
    }

    private Long getBookId() {
        Object bookId = getArguments() != null ? getArguments().get("bookId") : null;
        if (bookId != null) {
            if (bookId instanceof Double) {
                return ((Double) bookId).longValue();
            } else if (bookId instanceof String) {
                return Long.parseLong(bookId.toString());
            }
        }
        return 0L;
    }

    private Long getDefaultChapterId() {
        Double defaultChapterId = getArguments() != null ? getArguments().getDouble("defaultChapterId") : null;
        return defaultChapterId != null ? defaultChapterId.longValue() : 0L;
    }

    private Long getDefaultSectionId() {
        Double defaultSectionId = getArguments() != null ? getArguments().getDouble("defaultSectionId") : null;
        return defaultSectionId != null ? defaultSectionId.longValue() : null;
    }

    private List<ClickReadPage> getClickReadPages(ClickReadDTO clickReadDTO) {
        List<ClickReadPage> clickReadPages = null;
        if (clickReadDTO != null && clickReadDTO.getChapters() != null) {
            Long defaultChapterId = getDefaultChapterId();
            clickReadPages = new ArrayList<>();
            for (ClickReadCatalogDTO chapter : clickReadDTO.getChapters()) {
                //试读结束位置
                if (mFreeEndPageIndex == -1
                        && !ClickReadCatalogDTO.CRCODE_AUTH_CODE_BOOK_SHIDUCRCODE.equals(chapter.getAuthType())
                        && !ClickReadDTO.CLICKREAD_AUTHTYPE_NEED_NO.equals(clickReadDTO.getAuthType())
                ) {
                    mFreeEndPageIndex = clickReadPages.size();
                }
                if (chapter.getSections() != null && chapter.getSections().size() > 0) {
                    if (defaultChapterId.equals(chapter.getId())) {
                        this.sectionIdFromDefaultChapterId = chapter.getSections().get(0).getId();
                    }
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
            stopPlayTracksIfNeeded();
            mPlayerManager.play(track);
        }
    }

    @Override
    public void onClickSameArea() {
        if (mPlayerManager != null) {
            stopPlayTracksIfNeeded();
            mPlayerManager.playAgain();
        }
    }

    @Override
    public void onClickOtherArea() {
        mClickReadThumbnailList.toggle();
    }

    @Override
    public Long getClickReadId() {
        return mClickReadDTO != null ? mClickReadDTO.getId() : 0;
    }

    @Override
    public void onImageLoadSuccess(int position) {
        if (position == mViewPager.getCurrentItem()) {
            renderButtonEnable(true);
        }
    }

    @Override
    public void onImageLoadFail(int position) {
        if (position == mViewPager.getCurrentItem()) {
            renderButtonEnable(false);
        }
    }

    @Override
    public void onTrackEnd() {
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            pageView.hideFrame();
        }
    }

    public void onVideoClose(boolean fromUser) {
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            pageView.hideFrame();
        }
        if (fromUser) {
            stopPlayTracksIfNeeded();
        }
    }

    public void onVideoTrackEnd() {
        if (mPlayerManager != null) {
            mPlayerManager.onTrackEnd();
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
    public void onLoading() {
        if (mAudioLoadingHandler.hasMessages(AUDIO_LOAD_MSG)) {
            mAudioLoadingHandler.removeMessages(AUDIO_LOAD_MSG);
        }
        mAudioLoadingHandler.sendEmptyMessageDelayed(AUDIO_LOAD_MSG, 300);
    }

    @Override
    public void onLoadEnd() {
        mAudioLoadingHandler.removeMessages(AUDIO_LOAD_MSG);
        ClickReadPageView pageView = getCurrentPageView();
        if (pageView != null) {
            pageView.hideTrackLoading();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mJoinBookShelfButton.collapseAddShelfButton();
    }

    @Override
    public void onPageSelected(int position) {
        renderPage();
        if (!isBought && position == mPagerAdapter.getCount() - 1) {
            renderButtonEnable(false);
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
                renderButtonEnable(curPageView.isLoadImageSuccess());
                if (isShowClickArea) {
                    curPageView.showClickArea();
                } else {
                    curPageView.splashClickArea();
                }
                if (!isScrollByThumbnail && isRestoreCompleted()) {
                    scrollToPosition(position);
                }
                mRestoreCompleted[0] = true;
                isScrollByThumbnail = false;
                setButtonsVisible(true);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSelected(View view, int position, int selectedPosition) {
        if (mRestoreCompleted[1] && position != selectedPosition) {
            isScrollByThumbnail = true;
            mViewPager.setCurrentItem(position, false);
        } else if (!mRestoreCompleted[1]) {
            if (!isBought) {
                mRestoreCompleted[1] = true;
                return;
            }
            if (position != mRestorePageIndex) {
                mViewPager.postDelayed(() -> scrollToPosition(mRestorePageIndex), 200);
            }
            if (position == mRestorePageIndex) {
                mRestoreCompleted[1] = true;
            }

        }
    }

    @Override
    public void onScrolled() {
        mJoinBookShelfButton.collapseAddShelfButton();
        mClickReadThumbnailList.hideDelay();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_catalog && mClickReadDTO != null) {
            mDelegate.onCatalogClick();
        } else if (viewId == R.id.btn_buy
                || viewId == R.id.layout_buy_clickread_book) {
            if (FetchInfo.isGuest()) {
                RNYtClickreadModule.guestAlert(this);
            } else {
                RNYtClickreadModule.pushOrderHomeScreen(mClickReadDTO, getActivity());
            }
        } else if (viewId == R.id.img_back) {
            mDelegate.onBackClick();
        } else if (viewId == R.id.btn_click_area) {
            toggleClickArea();
        } else if (viewId == R.id.btn_play_tracks) {
            onPressPlayTracks();
        } else if (viewId == R.id.view_load_tips) {
            fetchData();
        } else if (viewId == R.id.btn_spoken_test) {
            if (FetchInfo.isGuest()) {
                RNYtClickreadModule.guestAlert(this);
            } else if (isBought) {
                RNYtClickreadModule.pushSpeechEvaluationSentenceListScreen(getBookId(),
                        getCurrentPage(), getActivity());
            } else {
                RNYtClickreadModule.buyAlert(this, mClickReadDTO);
            }
        }
    }


    public void buySuccess() {
        isBought = true;
        mTitleBar.setBuyButtonVisible(false);
        if (CollectionUtils.isNotEmpty(mClickReadPages)) {
            setButtonsVisible(true);
            if (mFreeEndPageIndex >= 0) {
                List<ClickReadPage> chargePages = Utils.subList(mClickReadPages, mFreeEndPageIndex,
                        mClickReadPages.size());
                mPagerAdapter.setData(chargePages);
                mClickReadThumbnailList.setData(chargePages, mClickReadDTO.getId());
            } else {
                mPagerAdapter.setData(mClickReadPages);
                mClickReadThumbnailList.setData(mClickReadPages, mClickReadDTO.getId());
            }
            renderPage();
        }
        if (mDelegate != null) {
            mDelegate.onBuyResult(isBought);
        }
    }

    public void scrollToPage(ClickReadPage page) {
        int position = mPagerAdapter.getPosition(page);
        if (position != -1) {
            mViewPager.setCurrentItem(position, false);
        }
    }

    public ClickReadPage getCurrentPage() {
        return mPagerAdapter.getItem(mViewPager.getCurrentItem());
    }

    private void renderButtonEnable(boolean enable) {
        if (CollectionUtils.isEmpty(mPagerAdapter.getTracks(mViewPager.getCurrentItem()))) {
            mTitleBar.setClickAreaEnabled(false);
            mPlayTracks.setEnabled(false);
        } else {
            mTitleBar.setClickAreaEnabled(enable);
            mPlayTracks.setEnabled(enable);
        }
    }

    private void render() {
        mCRLoadTipsView.hide();
        mTitleBar.setVisibility(View.VISIBLE);
        if (isBought) {
            buySuccess();
        } else {
            renderFreePages();
        }
        mClickReadThumbnailList.hideDelay();
        mViewPager.setCurrentItem(mRestorePageIndex, false);
        mViewPager.postDelayed(() -> {
                    scrollToPosition(mRestorePageIndex);
                },
                300);
    }

    private void renderFreePages() {
        int useFreeEndPageIndex = Math.max(mFreeEndPageIndex, 0);
        List<ClickReadPage> freePageList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mClickReadPages)) {
            for (int n = 0; n < useFreeEndPageIndex; n++) {
                freePageList.add(mClickReadPages.get(n));
            }
        }
        ClickReadPage fakePage = new ClickReadPage();
        fakePage.setAuthVal(mClickReadDTO.getAuthVal());
        fakePage.setId(-1L);
        freePageList.add(fakePage);
        mPagerAdapter.setData(freePageList);
        mClickReadThumbnailList.setData(Utils.subList(freePageList, 0, freePageList.size() - 1),
                mClickReadDTO.getId());
        scrollToPosition(mViewPager.getCurrentItem());
        if (useFreeEndPageIndex == 0) {
            setButtonsVisible(false);
        }
        renderPage();
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
        mJoinBookShelfButton.setVisibility(mIsInBookShelf || fromStudyPlan ? View.GONE : View.VISIBLE);
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
            renderPlayTracks(R.drawable.selector_play_tracks_play);
            RNYtClickreadModule.showToast(getContext(), R.string.read_has_stopped);
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
            stopPlayTracksIfNeeded();
            mPlayerManager.dismissVideoLightBoxIfNeeded(null);
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

    private void stopPlayTracksIfNeeded() {
        if (mPlayerManager != null && mPlayerManager.isPlayTracks()) {
            mPlayerManager.stopTracks();
            renderPlayTracks(R.drawable.selector_play_tracks_play);
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
                stopPlayTracksIfNeeded();
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
        if (FetchInfo.isGuest()) {
            RNYtClickreadModule.guestAlert(this);
            return;
        }
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


    private void storePageIndex() {
        if (mClickReadDTO != null && getContext() != null) {
            RNYtClickreadModule.setStorageItem(getContext(), getPageIndexKey(),
                    String.valueOf(mViewPager.getCurrentItem()));
        }
    }

    private void restorePageIndex(RNYtClickreadModule.Callback callback) {
        if (mRestorePageIndex != -1) {
            mRestoreCompleted[0] = false;
            mRestoreCompleted[1] = false;
            callback.resolve(String.valueOf(mRestorePageIndex));
            return;
        }
        if (mClickReadDTO != null) {
            RNYtClickreadModule.getStorageItem(getContext(), getPageIndexKey(),
                    new RNYtClickreadModule.Callback() {

                        @Override
                        public boolean runAsync() {
                            return false;
                        }

                        @Override
                        public void reject(String error) {
                            callback.resolve(null);
                        }

                        @Override
                        public void resolve(String result) {
                            callback.resolve(result);
                        }
                    }, this);
        }
    }

    private boolean isRestoreCompleted() {
        return mRestoreCompleted[0] && mRestoreCompleted[1];
    }

    private void scrollToPosition(int position) {
        if (mClickReadThumbnailList.getCount() >= 1) {
            mClickReadThumbnailList.scrollToPosition(Math.min(position, mClickReadThumbnailList.getCount() - 1));
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(runnable);
        }
    }

    private String getPageIndexKey() {
        return Utils.format("CLICK_READ_CUR_PAGE_KEY_%d_%d",
                FetchInfo.USER_ID, mClickReadDTO.getId());
    }

    private ClickReadPageView getPageView(int position) {
        String itemTag = Utils.format("%s%d",
                ClickReadPagerAdapter.TAG_VIEW, position);
        return mViewPager.findViewWithTag(itemTag);
    }

    private ClickReadPageView getCurrentPageView() {
        return getPageView(mViewPager.getCurrentItem());
    }

    private void resetPageViews() {
        for (int i = 0; i < mViewPager.getChildCount(); i++) {
            ClickReadPageView pageView = (ClickReadPageView) mViewPager.getChildAt(i);
            if (pageView != null) {
                pageView.reset();
            }
        }
    }

    private Integer getInitPageIndex() {
        Long defaultSectionId = getDefaultSectionId();
        if (defaultSectionId == null) {
            defaultSectionId = this.sectionIdFromDefaultChapterId;
        }
        if (defaultSectionId != null) {
            Long finalDefaultSectionId = defaultSectionId;
            ClickReadPage currentPage = CollectionUtils.find(mClickReadPages, clickReadPage
                    -> clickReadPage.getSectionId() != null
                    && clickReadPage.getSectionId().equals(finalDefaultSectionId));
            if (currentPage != null) {
                return mClickReadPages.indexOf(currentPage);
            }
        }
        return null;
    }

    public interface ClickReadFragmentDelegate {
        void onResponse(ClickReadDTO clickReadDTO);

        void onBuyResult(boolean isBought);

        void onCatalogClick();

        void onBackClick();

    }
}
