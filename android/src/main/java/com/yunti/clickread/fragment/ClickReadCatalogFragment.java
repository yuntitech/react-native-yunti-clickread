package com.yunti.clickread.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.yunti.clickread.FetchInfo;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.Utils;
import com.yunti.clickread.adapter.ClickReadCatalogAdapter;
import com.yunti.clickread.widget.ClickReadCatalogView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/9 0009.
 * <p>
 * 点读目录列表
 */

public class ClickReadCatalogFragment extends Fragment implements ClickReadCatalogView.OperationCallback,
        View.OnClickListener, ClickReadCatalogAdapter.OnSectionItemClickListener {
    private Long mBookId;
    private OperationCallback mCallback;
    private ClickReadCatalogView mCatalogView;
    private Button mBtnCacheWholeBook;
    private ClickReadPage mCurGoPage;
    private TextView mDownloadLoadingProgress;
    private ClickReadDTO mClickReadDTO;
    private long mSizeOfDirectory;
    private long time;
    private ClickReadCatalogFragmentDelegate mDelegate;
    private String mDownloadStatus;

    public void setDelegate(ClickReadCatalogFragmentDelegate delegate) {
        mDelegate = delegate;
    }

    public void setOperationCallback(OperationCallback callback) {
        this.mCallback = callback;
    }


    private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            String clickReadDir = getClickReadDir();
            Log.d("##", "progress handleMessage ... ");
            if (clickReadDir != null) {
                mHandler.sendEmptyMessageDelayed(1, 1000);
                File clickReadFile = new File(clickReadDir);
                if (!clickReadFile.exists()) {
                    return false;
                }
                long sizeOfDirectory = FileUtils.sizeOfDirectory(clickReadFile);
                if (mSizeOfDirectory != sizeOfDirectory) {
                    mSizeOfDirectory = sizeOfDirectory;
                    String costTime = "";
                    if (time > 0) {
                        costTime = " 目录大小改变耗时: " + (System.currentTimeMillis() - time);
                    }
                    Log.d("##", "sizeOfDirectory is "
                            + Formatter.formatFileSize(getContext(), mSizeOfDirectory)
                            + costTime);
                    time = System.currentTimeMillis();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mBtnCacheWholeBook.setText(String.format("下载中 ( %s / %s)",
                                    Formatter.formatFileSize(getContext(), mSizeOfDirectory),
                                    Formatter.formatFileSize(getContext(), mClickReadDTO.getLength())));
                        });
                    }
                }

            }
            return false;
        }
    });


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

    public void buySuccess() {
        mCatalogView.buySuccess();
    }

    public void refresh(ClickReadDTO clickReadDTO) {
        mClickReadDTO = clickReadDTO;
        mCatalogView.refresh(clickReadDTO.getChapters(),
                !ClickReadDTO.CLICKREAD_AUTHTYPE_NEED_BUY.equals(clickReadDTO.getAuthType()));
        renderDownloadStatus(null);
        getAndRenderDownloadStatus();
    }

    public void renderDownloadStatus(String status) {
        mDownloadStatus = status != null ? status : "";
        String text;
        switch (mDownloadStatus) {
            case "downloading":
                File clickReadFile = new File(getClickReadDir());
                if (clickReadFile.exists()) {
                    mSizeOfDirectory = FileUtils.sizeOfDirectory(clickReadFile);
                } else {
                    mSizeOfDirectory = 0;
                }
                text = String.format("下载中 ( %s / %s)",
                        Utils.fileSize(mSizeOfDirectory, this),
                        Utils.fileSize(mClickReadDTO.getLength(), this));
                break;
            case "paused":
                text = "已暂停";
                break;
            case "downloaded":
                text = Utils.format("删除离线包（${size}）",
                        Utils.fileSize(mClickReadDTO.getLength(), this));
                break;
            case "updateAvailable":
                text = "更新";
                break;
            case "failed":
                text = "下载失败";
                break;
            default:
                text = Utils.format("下载离线包（%s）",
                        Utils.fileSize(mClickReadDTO.getLength(), this));
                break;
        }
        mBtnCacheWholeBook.setText(text);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        mHandler.removeMessages(1);
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
        if (viewId == R.id.btn_cache_whole_book && mDownloadStatus != null) {
            onPressDownload();
        }
    }

    private void onPressDownload() {
        if (mDownloadStatus == null) {
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

    @Override
    public void onSectionClick(ClickReadCatalogDTO section) {
        if (mDelegate == null) {
            return;
        }
        if (mDelegate.isBought()) {
            mCatalogView.highLightCurSection(section.getPages().get(0));
            goCatalogSectionPage(section.getPages().get(0));
        } else {
            RNYtClickreadModule.alert(getActivity(), (dialog, which) -> {
                RNYtClickreadModule.openOrderHomeScreen(mClickReadDTO, getContext());
            });
        }
    }

    private void startDownload() {
        mHandler.removeMessages(1);
        RNYtClickreadModule.download(getContext(), mClickReadDTO);
    }

    private void pauseDownload() {
        mHandler.removeMessages(1);
        RNYtClickreadModule.pauseDownload(getContext(), mClickReadDTO.getId());
    }

    private void removeDownload() {
        mHandler.removeMessages(1);
        RNYtClickreadModule.removeDownload(getContext(), mClickReadDTO.getId());
    }

    private void sendProgressMessage() {
        mHandler.removeMessages(1);
        mHandler.sendEmptyMessage(1);
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
        RNYtClickreadModule.getStorageItem(getContext(),
                getDownloadStatusKey(),
                new RNYtClickreadModule.Callback() {
                    @Override
                    public void reject(String error) {
                        mHandler.removeMessages(1);
                    }

                    @Override
                    public void resolve(String result) {
                        if (result != null) {
                            Map<String, Object> resultMap = JSON.parseObject(result, Map.class);
                            mDownloadStatus = (String) resultMap.get("status");
                            if ("downloading".equals(mDownloadStatus)) {
                                sendProgressMessage();
                            } else {
                                mHandler.removeMessages(1);
                            }
                            renderDownloadStatus(mDownloadStatus);
                        } else {
                            renderDownloadStatus(null);
                        }

                    }
                });
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
