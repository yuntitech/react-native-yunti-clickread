package com.yunti.clickread.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.yt.ytdeep.client.dto.ClickReadCatalogDTO;
import com.yunti.clickread.R;
import com.yunti.clickread.RNYtClickreadModule;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/10/11 0011.
 * 点读目录adapter
 */

public class ClickReadCatalogAdapter extends RecyclerView.Adapter {

    private static final int ITEM_TYPE_CHAPTER = 0x01;
    private static final int ITEM_TYPE_SECTION = 0x02;
    private List<ClickReadCatalogDTO> mCatalogs;
    private Context mContext;
    private OnSectionItemClickListener mSectionClickListener;
    private int mHighLightIndex;
    private boolean mIsBookFree;
    private boolean isBought;

    public ClickReadCatalogAdapter(Context context) {
        this.mContext = context;
    }

    public List<ClickReadCatalogDTO> getCatalogs() {
        return mCatalogs;
    }

    public void setData(List<ClickReadCatalogDTO> catalogs, boolean isBookFree) {
        mCatalogs = catalogs;
        mIsBookFree = isBookFree;
        notifyDataSetChanged();
    }

    public void refresh(boolean isBuy) {
        this.isBought = isBuy;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_CHAPTER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_catalog_chapter_item, null);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new ChapterViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_catalog_section_item, null);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new SectionViewHolder(view);
        }
    }

    public void setOnSectionItemClickListener(OnSectionItemClickListener listener) {
        this.mSectionClickListener = listener;
    }

    public void highLight(int index) {
        mHighLightIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ClickReadCatalogDTO catalog = mCatalogs.get(position);
        if (getItemViewType(position) == ITEM_TYPE_CHAPTER) {
            ChapterViewHolder mChapterViewHolder = (ChapterViewHolder) holder;
            mChapterViewHolder.bind(catalog, position);
        } else {
            SectionViewHolder mSectionViewHolder = (SectionViewHolder) holder;
            mSectionViewHolder.bind(catalog);
        }
    }

    @Override
    public int getItemCount() {
        return mCatalogs != null ? mCatalogs.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        ClickReadCatalogDTO catalog = mCatalogs.get(position);
        if (ClickReadCatalogDTO.CLICKREADCATALOG_LEVEL_CHAPTER.equals(catalog.getLevel())) {
            return ITEM_TYPE_CHAPTER;
        } else {
            return ITEM_TYPE_SECTION;
        }
    }

    private class ChapterViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvChapterName;
        private ClickReadCatalogDTO mCatalog;
        private TextView mTvChapterPage;
        private ImageView mIvLock;
        private View mDivider;

        private ChapterViewHolder(View itemView) {
            super(itemView);
            mTvChapterName = (TextView) itemView.findViewById(R.id.tv_chapter_name);
            mTvChapterPage = (TextView) itemView.findViewById(R.id.tv_chapter_page);
            mIvLock = (ImageView) itemView.findViewById(R.id.iv_chapter_lock);
            mDivider = itemView.findViewById(R.id.view_divider);
        }

        private void bind(ClickReadCatalogDTO catalog, int position) {
            mDivider.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
            this.mCatalog = catalog;
            mTvChapterName.setText(catalog.getName());
            mTvChapterPage.setText(String.valueOf(catalog.getPageNo()));
            if (mIsBookFree) {
                mTvChapterName.setTextColor(mContext.getResources().getColor(R.color.color_22));
                mIvLock.setVisibility(View.GONE);
                mTvChapterPage.setVisibility(View.VISIBLE);
            } else {
                if (ClickReadCatalogDTO.CRCODE_AUTH_CODE_BOOK_SHIDUCRCODE.equals(mCatalog.getAuthType())) {
                    mTvChapterName.setTextColor(mContext.getResources().getColor(R.color.color_22));
                    mIvLock.setVisibility(View.GONE);
                    mTvChapterPage.setVisibility(View.VISIBLE);
                } else {
                    mTvChapterName.setTextColor(mContext.getResources().getColor(R.color.color_99));
                    mIvLock.setVisibility(View.VISIBLE);
                    mTvChapterPage.setVisibility(View.GONE);
                }
            }
            //书籍已购买
            if (isBought) {
                mTvChapterName.setTextColor(mContext.getResources().getColor(R.color.color_22));
                mIvLock.setVisibility(View.GONE);
                mTvChapterPage.setVisibility(View.VISIBLE);
            }
        }
    }

    private class SectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTvSectionName;
        private TextView mTvSectionPage;
        private LinearLayout mLayoutSection;
        private View mViewDivider;
        private ClickReadCatalogDTO mCatalog;
        private ImageView mIvLock;

        private SectionViewHolder(View itemView) {
            super(itemView);
            mTvSectionName = (TextView) itemView.findViewById(R.id.tv_section_name);
            mTvSectionPage = (TextView) itemView.findViewById(R.id.tv_section_page);
            mLayoutSection = (LinearLayout) itemView.findViewById(R.id.layout_section);
            mLayoutSection.setOnClickListener(this);
            mViewDivider = itemView.findViewById(R.id.view_divider);
            mIvLock = (ImageView) itemView.findViewById(R.id.iv_section_lock);
        }

        private void bind(ClickReadCatalogDTO catalog) {
            this.mCatalog = catalog;
            mTvSectionName.setText(catalog.getName());
            mTvSectionPage.setText(String.valueOf(catalog.getPageNo()));
            int catalogIndex = mCatalogs.indexOf(catalog);
            if (catalogIndex == mCatalogs.size() - 1 ||
                    ClickReadCatalogDTO.CLICKREADCATALOG_LEVEL_CHAPTER.equals(mCatalogs.get(catalogIndex + 1).getLevel())) {
                mViewDivider.setVisibility(View.GONE);
            } else {
                mViewDivider.setVisibility(View.VISIBLE);
            }
            if (mCatalog.getPid() != null && mCatalog.getPid() > -1 && mCatalog.getPid() < mCatalogs.size()) {
                ClickReadCatalogDTO parentCatalog = mCatalogs.get(mCatalog.getPid().intValue());
                if (parentCatalog != null) {
                    if (mIsBookFree) {
                        if (mHighLightIndex == catalogIndex) {
                            mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.blue_a));
                        } else {
                            mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.color_66));
                        }
                        mIvLock.setVisibility(View.GONE);
                        mTvSectionPage.setVisibility(View.VISIBLE);
                    } else {
                        if (ClickReadCatalogDTO.CRCODE_AUTH_CODE_BOOK_SHIDUCRCODE.equals(parentCatalog.getAuthType())) {
                            if (mHighLightIndex == catalogIndex) {
                                mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.blue_a));
                            } else {
                                mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.color_66));
                            }
                            mIvLock.setVisibility(View.GONE);
                            mTvSectionPage.setVisibility(View.VISIBLE);
                        } else {
                            mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.color_99));
                            mIvLock.setVisibility(View.VISIBLE);
                            mTvSectionPage.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                if (mHighLightIndex == catalogIndex) {
                    mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.blue_a));
                } else {
                    mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.color_66));
                }
                mIvLock.setVisibility(View.GONE);
                mTvSectionPage.setVisibility(View.VISIBLE);
            }
            //书籍已购买
            if (isBought) {
                if (mHighLightIndex == catalogIndex) {
                    mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.blue_a));
                } else {
                    mTvSectionName.setTextColor(mContext.getResources().getColor(R.color.color_66));
                }
                mIvLock.setVisibility(View.GONE);
                mTvSectionPage.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onClick(View v) {
            if (mSectionClickListener != null
                    && mCatalog.getPages() != null && mCatalog.getPages().size() > 0) {
                mSectionClickListener.onSectionClick(mCatalog);
            } else {
                RNYtClickreadModule.showToast(mContext, mContext.getString(R.string.no_resource_add));
            }
        }
    }

    public interface OnSectionItemClickListener {
        void onSectionClick(ClickReadCatalogDTO section);
    }

}
