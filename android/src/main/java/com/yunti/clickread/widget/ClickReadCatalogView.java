package com.yunti.clickread.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yt.ytdeep.client.dto.ClickReadCatalogDTO;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yt.ytdeep.client.dto.EventDetailDTO;
import com.yunti.clickread.R;
import com.yunti.clickread.adapter.ClickReadCatalogAdapter;
import com.yunti.view.YTLinearLayout;

import java.util.ArrayList;
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
        mAdapter = new ClickReadCatalogAdapter(context, true);
        mRvCatalog.setAdapter(mAdapter);
    }

    public void setOperationCallback(OperationCallback callback) {
        this.mCallback = callback;
    }

    public void refresh(List<ClickReadCatalogDTO> chapters) {
        List<ClickReadCatalogDTO> catalogs = new ArrayList<>();

        //记录父节点的位置
        List<Integer> mParentCatalogIndexList = new ArrayList<>();
        for (ClickReadCatalogDTO catalog : chapters) {
            catalog.setLevel(ClickReadCatalogDTO.CLICKREADCATALOG_LEVEL_CHAPTER);
            catalogs.add(catalog);
            mParentCatalogIndexList.add(catalogs.size() - 1);
            if (catalog.getSections() != null && catalog.getSections().size() > 0) {
                for (ClickReadCatalogDTO section : catalog.getSections()) {
                    section.setLevel(ClickReadCatalogDTO.CLICKREADCATALOG_LEVEL_SECTION);
                    if (mParentCatalogIndexList.size() > 0) {
                        section.setPid(Long.valueOf(mParentCatalogIndexList.get(mParentCatalogIndexList.size() - 1)));
                    }
                    catalogs.add(section);
                }
            }

        }
        mAdapter.setData(chapters);
        if (mHighLightPage != null) {
            highLightCurSection(mHighLightPage);
        }
        mAdapter.setOnSectionItemClickListener(new ClickReadCatalogAdapter.OnSectionItemClickListener() {
            @Override
            public void onClick(ClickReadCatalogDTO section) {
                highLightCurSection(section.getPages().get(0));
                if (mCallback != null) {
                    mCallback.goCatalogSectionPage(section.getPages().get(0));
                }
            }
        });
    }

    public void refreshHasBuy() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void highLightCurSection(ClickReadPage page) {
        mHighLightPage = page;
        if (mAdapter.getCatalogs() == null) {
            return;
        }
        int highLightIndex = -1;
        int index = 0;
        for (ClickReadCatalogDTO catalogDTO : mAdapter.getCatalogs()) {
            index++;
            if (catalogDTO.getSections() != null) {
                for (ClickReadCatalogDTO section : catalogDTO.getSections()) {
                    index++;
                    if (section.getPages() != null && section.getPages().contains(page)) {
                        highLightIndex = index;
                        break;
                    }
                }
            }
        }
        highLightIndex--;
        if (highLightIndex > 0 && highLightIndex < mAdapter.getItemCount()) {
            mAdapter.highLight(highLightIndex);
            mLayoutManager.scrollToPositionWithOffset(highLightIndex, 0);
        }
    }

    public interface OperationCallback {
        void goCatalogSectionPage(ClickReadPage page);
    }

}
