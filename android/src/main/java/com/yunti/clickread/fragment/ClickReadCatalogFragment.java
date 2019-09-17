package com.yunti.clickread.fragment;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.badoo.mobile.util.WeakHandler;
import com.yt.ytdeep.client.dto.ClickReadCatalogDTO;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yunti.clickread.FetchInfo;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.Utils;
import com.yunti.clickread.adapter.ClickReadCatalogAdapter;
import com.yunti.clickread.widget.ClickReadCatalogView;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/10/9 0009.
 * <p>
 * 点读目录列表
 */

public class ClickReadCatalogFragment extends Fragment implements ClickReadCatalogView.OperationCallback,
        View.OnClickListener, ClickReadCatalogAdapter.OnSectionItemClickListener {
    private OperationCallback mCallback;
    private ClickReadCatalogView mCatalogView;
    private Button mBtnCacheWholeBook;
    private ClickReadPage mCurGoPage;
    private TextView mDownloadLoadingProgress;
    private ClickReadDTO mClickReadDTO;
    private ClickReadCatalogFragmentDelegate mDelegate;
    private String mDownloadStatus;
    private WeakHandler mProgressHandler;
    private HandlerThread mHandlerThread;
    private long mTotalFileCount = 0;

    public void setDelegate(ClickReadCatalogFragmentDelegate delegate) {
        mDelegate = delegate;
    }

    public void setOperationCallback(OperationCallback callback) {
        this.mCallback = callback;
    }


    private Handler.Callback mProgressCallback = msg -> {
        long currentFileCount;
        if (msg.what == 2) {
            calculateTotalFileCount();
            currentFileCount = getCurrentFileCount();
            runOnUiThread(() -> renderProgress(currentFileCount));
            return false;
        }
        String clickReadDir = getClickReadDir();
        if (clickReadDir != null) {
            mProgressHandler.sendEmptyMessageDelayed(1, 1000);
            File clickReadFile = new File(clickReadDir);
            if (!clickReadFile.exists()) {
                return false;
            }
            currentFileCount = getCurrentFileCount();
            runOnUiThread(() -> renderProgress(currentFileCount));
        }
        return false;
    };

    public void setTotalFileCount(long totalFileCount) {
        mTotalFileCount = totalFileCount;
        renderProgress(getCurrentFileCount());
    }

    private String formatProgress(float progress) {
        StringBuffer sb = new StringBuffer();
        sb.append("（").append((int) (progress * 100)).append("%）");
        return sb.toString();
    }

    private String formatResourceLength(Long length) {
        StringBuffer sb = new StringBuffer();
        if (length != null && length > 0) {
            sb.append("（").append(Formatter.formatFileSize(getActivity(), length)).append("）");
        }
        return sb.toString();
    }

    public void buyResult(boolean isBought) {
        mCatalogView.buyResult(isBought);
    }

    public void refresh(ClickReadDTO clickReadDTO) {
        mClickReadDTO = clickReadDTO;
        mCatalogView.refresh(clickReadDTO.getChapters(),
                !ClickReadDTO.CLICKREAD_AUTHTYPE_NEED_BUY.equals(clickReadDTO.getAuthType()));
        renderDownloadStatus(null);
        getAndRenderDownloadStatus();
    }

    private void renderDownloadStatus(String status) {
        mDownloadStatus = status != null ? status : "";
        String text = "下载离线包";
        String length;
        switch (mDownloadStatus) {
            case "downloading":
                renderProgress(getCurrentFileCount());
                break;
            case "paused":
                text = "已暂停";
                break;
            case "downloaded":
                length = Utils.fileSize(mClickReadDTO.getLength(), this);
                text = Utils.format("删除离线包%s", length != null ? "（" + length + "）" : "");
                break;
            case "updateAvailable":
                text = "更新";
                break;
            case "failed":
                text = "下载失败";
                break;
            default:
                length = Utils.fileSize(mClickReadDTO.getLength(), this);
                text = Utils.format("下载离线包%s", length != null ? "（" + length + "）" : "");
                break;
        }
        mBtnCacheWholeBook.setText(text);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandlerThread = new HandlerThread("DownloadProgressThread");
        mHandlerThread.start();
        mProgressHandler = new WeakHandler(mHandlerThread.getLooper(), mProgressCallback);
    }

    @Override
    public void onDestroy() {
        mProgressHandler.removeMessages(1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_click_read_catalog, container, false);
        mCatalogView = (ClickReadCatalogView) rootView.findViewById(R.id.catalog_view);
        mCatalogView.setOperationCallback(this);
        mBtnCacheWholeBook = (Button) rootView.findViewById(R.id.btn_cache_whole_book);
        mDownloadLoadingProgress = (TextView) rootView.findViewById(R.id.download_loading_progress);
        mBtnCacheWholeBook.setOnClickListener(this);
        mCatalogView.setSectionItemClickListener(this);
        return rootView;
    }

    @Override
    public void goCatalogSectionPage(ClickReadPage page) {
        mCurGoPage = page;
        if (mCallback != null) {
            mCallback.goSectionPage(page);
        }
    }

    public void highLightPageSection(ClickReadPage clickReadPage) {
        mCatalogView.highLightCurSection(clickReadPage);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        //下载
        if (viewId == R.id.btn_cache_whole_book) {
            onPressDownload();
        }
    }

    private void onPressDownload() {
        if (mDelegate == null || mDownloadStatus == null) {
            return;
        }
        if (!mDelegate.isBought()) {
            buyAlert();
            return;
        }
        switch (mDownloadStatus) {
            case "downloading":
                RNYtClickreadModule.showBottomSheet(
                        new int[]{R.string.pause_download, R.string.cancel_download,
                                R.string.cancel},
                        this::pauseDownload,
                        this::removeDownload,
                        this);
                break;
            case "paused":
                RNYtClickreadModule.showBottomSheet(
                        new int[]{R.string.continue_download, R.string.cancel_download,
                                R.string.cancel},
                        this::startDownload,
                        this::removeDownload,
                        this);
                break;
            case "downloaded":
                RNYtClickreadModule.showBottomSheet(
                        new int[]{R.string.delete_offline_resource,
                                R.string.cancel},
                        this::removeDownload,
                        this);
                break;
            case "updateAvailable":
                RNYtClickreadModule.showBottomSheet(
                        new int[]{R.string.update_now, R.string.delete_offline_resource,
                                R.string.cancel},
                        this::startDownload,
                        this::removeDownload,
                        this);
                break;
            case "failed":
                RNYtClickreadModule.showBottomSheet(
                        new int[]{R.string.continue_download, R.string.cancel_download,
                                R.string.cancel},
                        this::startDownload,
                        this::removeDownload,
                        this);
                break;
            default:
                mDownloadStatus = "downloading";
                startDownload();
                break;
        }
    }

    private void buyAlert() {
        RNYtClickreadModule.alert(this, (dialog, which) -> {
            if (FetchInfo.isGuest()) {
                RNYtClickreadModule.guestAlert(this);
            } else {
                RNYtClickreadModule.pushOrderHomeScreen(mClickReadDTO, getActivity());
            }
        }, "购买后即可下载", "购买");
    }

    @Override
    public void onSectionClick(ClickReadCatalogDTO section) {
        if (mDelegate == null) {
            return;
        }
        if (mDelegate.isBought() ||
                ClickReadCatalogDTO.CRCODE_AUTH_CODE_BOOK_SHIDUCRCODE.equals(section.getAuthType())) {
            mCatalogView.highLightCurSection(section.getPages().get(0));
            goCatalogSectionPage(section.getPages().get(0));
        } else {
            if (FetchInfo.isGuest()) {
                RNYtClickreadModule.guestAlert(this);
            } else {
                RNYtClickreadModule.alert(this,
                        (dialog, which) ->
                                RNYtClickreadModule.pushOrderHomeScreen(mClickReadDTO, getActivity()),
                        R.string.tip_view_clickread_after_pay);
            }
        }
    }

    private void startDownload() {
        mProgressHandler.removeMessages(1);
        RNYtClickreadModule.download(getContext(), mClickReadDTO);
    }

    private void pauseDownload() {
        mProgressHandler.removeMessages(1);
        RNYtClickreadModule.pauseDownload(getContext(), mClickReadDTO.getId());
    }

    private void removeDownload() {
        mProgressHandler.removeMessages(1);
        RNYtClickreadModule.removeDownload(getContext(), mClickReadDTO.getId());
    }

    private void sendProgressMessage() {
        mProgressHandler.sendEmptyMessage(2);
        mProgressHandler.removeMessages(1);
        mProgressHandler.sendEmptyMessage(1);
    }

    public interface OperationCallback {
        void goSectionPage(ClickReadPage page);
    }

    private void showDownloadAllResourceLoading() {
        mBtnCacheWholeBook.setEnabled(false);
        mDownloadLoadingProgress.setVisibility(View.VISIBLE);
    }

    private void stopDownloadAllResourceLoading() {
        mBtnCacheWholeBook.setEnabled(true);
        mDownloadLoadingProgress.setVisibility(View.GONE);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            mCurGoPage = null;
        }
    }


    public void getAndRenderDownloadStatus() {
        if (FetchInfo.isGuest()) {
            return;
        }
        RNYtClickreadModule.getStorageItem(getContext(),
                getDownloadStatusKey(),
                new RNYtClickreadModule.Callback() {
                    @Override
                    public void reject(String error) {
                        mProgressHandler.removeMessages(1);
                    }

                    @Override
                    public void resolve(String result) {
                        if (result != null) {
                            Map<String, Object> resultMap = JSON.parseObject(result, Map.class);
                            mDownloadStatus = (String) resultMap.get("status");
                            if ("downloading".equals(mDownloadStatus)) {
                                sendProgressMessage();
                            } else {
                                mProgressHandler.removeMessages(1);
                            }
                            renderDownloadStatus(mDownloadStatus);
                        } else {
                            renderDownloadStatus(null);
                        }

                    }
                }, this);
    }

    private void calculateTotalFileCount() {
        if (mClickReadDTO != null && mClickReadDTO.getChapters() != null) {
            mTotalFileCount = 0;
            Set<String> urlSet = new HashSet<>();
            for (ClickReadCatalogDTO chapter : mClickReadDTO.getChapters()) {
                if (chapter.getSections() != null) {
                    for (ClickReadCatalogDTO section : chapter.getSections()) {
                        if (section.getPages() != null) {
                            for (ClickReadPage page : section.getPages()) {
                                String url = page.getImgUrl();
                                if (url != null && !urlSet.contains(url)) {
                                    mTotalFileCount++;
                                    urlSet.add(url);
                                }
                                url = page.getThumbnails();
                                if (url != null && !urlSet.contains(url)) {
                                    mTotalFileCount++;
                                    urlSet.add(url);
                                }
                                if (page.getTracks() != null) {
                                    String trackUrl;
                                    for (ClickReadTrackinfo trackinfo : page.getTracks()) {
                                        trackUrl = trackinfo.getUrl();
                                        if (trackUrl != null
                                                && !urlSet.contains(trackUrl)
                                                && trackUrl.startsWith("http")) {
                                            mTotalFileCount++;
                                            urlSet.add(trackUrl);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(runnable);
        }
    }

    private void renderProgress(long currentFileCount) {
        //下载完成
        if (mTotalFileCount > 0 && currentFileCount >= mTotalFileCount) {
            mProgressHandler.removeMessages(1);
            renderDownloadStatus("downloaded");
            return;
        }
        float progress = 0;
        if (mTotalFileCount > 0) {
            progress = currentFileCount * 1f / mTotalFileCount * 100;
        }
        mBtnCacheWholeBook.setText(String.format("下载中 (%s%s)", Utils.format("%.2f", progress), "%"));
    }

    private long getCurrentFileCount() {
        String dir = getClickReadDir();
        if (!TextUtils.isEmpty(dir) && new File(dir).exists()) {
            String[] resTypes = new String[]{"image", "audio", "video", "thumbnail"};
            long fileCount = 0;
            for (String type : resTypes) {
                fileCount += Utils.getDirFileCount(Utils.format("%s/%s", dir, type));
            }
            return fileCount;
        }
        return 0;
    }

    private String getClickReadDir() {
        Activity activity = getActivity();
        if (activity != null && activity.getExternalFilesDir(null) != null) {
            String externalDirectory = activity.getExternalFilesDir(null).getAbsolutePath();
            return String.format(Locale.CHINA, "%s/%s/clickRead/u%d/%d", externalDirectory,
                    activity.getPackageName(), FetchInfo.USER_ID, mClickReadDTO.getId());
        }
        return null;
    }

    private String getDownloadStatusKey() {
        long crId = mClickReadDTO != null ? mClickReadDTO.getId() : 0;
        return Utils.format("@clickReadDownloadStatus_user%d_%d", FetchInfo.USER_ID, crId);
    }

    public interface ClickReadCatalogFragmentDelegate {
        boolean isBought();
    }

}
