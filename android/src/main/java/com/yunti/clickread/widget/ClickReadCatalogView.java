package com.yunti.clickread.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yt.ytdeep.client.dto.ClickReadCatalogDTO;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;
import com.yunti.clickread.adapter.ClickReadCatalogAdapter;
import com.yunti.view.YTLinearLayout;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hezhisu on 2017/12/20.
 */

public class ClickReadCatalogView extends YTLinearLayout {

    private RecyclerView mRvCatalog;
    private ClickReadCatalogAdapter mAdapter;
    private OperationCallback mCallback;

    private ClickReadPage mHighLightPage;
    private LinearLayoutManager mLayoutManager;

    private Context mContext;

    public void setSectionItemClickListener(ClickReadCatalogAdapter.OnSectionItemClickListener
                                                    sectionItemClickListener) {
        mAdapter.setOnSectionItemClickListener(sectionItemClickListener);
    }

    public ClickReadCatalogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ClickReadCatalogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ClickReadCatalogView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_child_catalog, this, true);
        mRvCatalog = (RecyclerView) findViewById(R.id.rv_catalog);
        mLayoutManager = new LinearLayoutManager(context);
        mRvCatalog.setLayoutManager(mLayoutManager);
        mAdapter = new ClickReadCatalogAdapter(context);
        mRvCatalog.setAdapter(mAdapter);
    }

    public void setOperationCallback(OperationCallback callback) {
        this.mCallback = callback;
    }

    public void refresh(List<ClickReadCatalogDTO> chapters, boolean isBookFree) {
        List<ClickReadCatalogDTO> catalogs = new ArrayList<>();

        //记录父节点的位置
        List<Integer> mParentCatalogIndexList = new ArrayList<>();
        for (ClickReadCatalogDTO catalog : chapters) {
            catalog.setLevel(ClickReadCatalogDTO.CLICKREADCATALOG_LEVEL_CHAPTER);
            catalogs.add(catalog);
            mParentCatalogIndexList.add(catalogs.size() - 1);
            if (CollectionUtils.isNotEmpty(catalog.getSections())) {
                for (ClickReadCatalogDTO section : catalog.getSections()) {
                    section.setAuthType(catalog.getAuthType());
                    section.setLevel(ClickReadCatalogDTO.CLICKREADCATALOG_LEVEL_SECTION);
                    if (mParentCatalogIndexList.size() > 0) {
                        section.setPid(Long.valueOf(
                                mParentCatalogIndexList.get(mParentCatalogIndexList.size() - 1)));
                    }
                    catalogs.add(section);
                }
            }

        }
        mAdapter.setData(catalogs, isBookFree);
        if (mHighLightPage != null) {
            highLightCurSection(mHighLightPage);
        }
    }

    public void buySuccess() {
        if (mAdapter != null) {
            CollectionUtils.filterInverse(mAdapter.getCatalogs(),
                    catalog -> ClickReadCatalogDTO.CRCODE_AUTH_CODE_BOOK_SHIDUCRCODE
                            .equals(catalog.getAuthType()));
            mAdapter.refresh(true);
        }
    }

    public void highLightCurSection(ClickReadPage page) {
        mHighLightPage = page;
        if (mAdapter.getCatalogs() == null) {
            return;
        }
        int highLightIndex = 0;
        for (ClickReadCatalogDTO catalogDTO : mAdapter.getCatalogs()) {
            if (ClickReadCatalogDTO.CLICKREADCATALOG_LEVEL_SECTION.equals(catalogDTO.getLevel())) {
                if (catalogDTO.getPages() != null && catalogDTO.getPages().contains(page)) {
                    break;
                }
            }
            highLightIndex++;
        }
        mAdapter.highLight(highLightIndex);
        mLayoutManager.scrollToPositionWithOffset(highLightIndex, 0);
    }

    public interface OperationCallback {
        void goCatalogSectionPage(ClickReadPage page);
    }

}
