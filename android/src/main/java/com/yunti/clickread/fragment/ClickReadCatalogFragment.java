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

import com.badoo.mobile.util.WeakHandler;
import com.yt.ytdeep.client.dto.ClickReadCatalogDTO;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yunti.clickread.FetchInfo;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.widget.ClickReadCatalogView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Locale;

/**
 * Created by Administrator on 2016/10/9 0009.
 * <p>
 * 点读目录列表
 */

public class ClickReadCatalogFragment extends Fragment implements ClickReadCatalogView.OperationCallback,
        View.OnClickListener {
    private Long mBookId;
    private OperationCallback mCallback;
    private ClickReadCatalogView mCatalogView;
    private Button mBtnCacheWholeBook;
    private ClickReadPage mCurGoPage;
    private TextView mDownloadLoadingProgress;
    private ClickReadDTO mClickReadDTO;
    private long mSizeOfDirectory;
    private long time;

    public void setOperationCallback(OperationCallback callback) {
        this.mCallback = callback;
    }


    private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            String clickReadDir = getClickReadDir();
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBtnCacheWholeBook.setText(String.format("下载 ( %s / %s)",
                                        Formatter.formatFileSize(getContext(), mSizeOfDirectory),
                                        Formatter.formatFileSize(getContext(), mClickReadDTO.getLength())
                                ));
                            }
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

    public void refreshHasBuy() {
        mCatalogView.refreshHasBuy();
    }

    public void refresh(ClickReadDTO clickReadDTO) {
        mClickReadDTO = clickReadDTO;
        mCatalogView.refresh(clickReadDTO.getChapters());
        mHandler.sendEmptyMessage(1);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            RNYtClickreadModule.download(getContext(), mClickReadDTO);
        }
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

    private String getClickReadDir() {
        Activity activity = getActivity();
        if (activity != null && activity.getExternalFilesDir(null) != null) {
            String externalDirectory = activity.getExternalFilesDir(null).getAbsolutePath();
            return String.format(Locale.CHINA, "%s/%s/clickRead/u%d/%d", externalDirectory,
                    activity.getPackageName(), FetchInfo.USER_ID, mClickReadDTO.getId());
        }
        return null;
    }


}
