package com.yunti.clickread.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yt.ytdeep.client.dto.ClickReadPage;
import com.yunti.clickread.R;
import com.yunti.util.YTDisplayHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/10/11 0011.
 * 点读缩略图adapter
 */

public class ThumbnailAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<ClickReadPage> mPages;

    public ThumbnailAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<ClickReadPage> pages) {
        this.mPages = pages;
        this.notifyDataSetChanged();
    }

    public void addItems(List<ClickReadPage> pages) {
        this.mPages.addAll(pages);
        this.notifyDataSetChanged();
    }

    public void addItem(ClickReadPage page) {
        this.mPages.add(page);
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_thumbnail_item, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(YTDisplayHelper.dpToPx(42),
                YTDisplayHelper.dpToPx(62)));
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ThumbnailViewHolder thumbnailViewHolder = (ThumbnailViewHolder) holder;
        thumbnailViewHolder.bind(mPages.get(position), position);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
//        Glide.clear(((ThumbnailViewHolder) holder).mIvThumbnial);
    }

    public ClickReadPage getItem(int position) {
        if (mPages == null || position >= getItemCount() || position < 0) {
            return null;
        }
        return mPages.get(position);
    }

    @Override
    public int getItemCount() {
        return mPages == null ? 0 : mPages.size();
    }

    private class ThumbnailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout mLayoutItem;
        private ImageView mIvThumbnial;
        private ClickReadPage page;
        private TextView mTvPageNumber;

        private ThumbnailViewHolder(View itemView) {
            super(itemView);
            mLayoutItem = (RelativeLayout) itemView.findViewById(R.id.layout_thumbnail_item);
            mIvThumbnial = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            mLayoutItem.setOnClickListener(this);
            mTvPageNumber = (TextView) itemView.findViewById(R.id.tv_page_number);
        }

        private void bind(ClickReadPage page, int position) {
            this.page = page;
            String uri = page.getThumbnails();
            if (mContext instanceof Activity && !((Activity) mContext).isFinishing()) {
                Glide.with(mContext)
                        .load(uri)
                        .into(mIvThumbnial);
                mTvPageNumber.setText(String.valueOf(position + 1));
            }

        }

        @Override
        public void onClick(View v) {
        }
    }


}
