package com.yunti.clickread.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunti.clickread.R;

import java.util.ArrayList;


public class BottomOptionDialog extends Dialog {

    private static final int OPTION_NORMAL = 0;
    private static final int OPTION_CANCEL = 1;
    private boolean refreshSkin;
    private Context context;
    private View content;
    private RecyclerView mList;
    private ArrayList<String> options;
    private OptionAdapter adapter;
    private OnOptionListener onOptionListener;

    public BottomOptionDialog(Context context) {
        super(context, R.style.bottom_option_dialog);
        this.context = context;
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager manager = window.getWindowManager();
        Display display = manager.getDefaultDisplay();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = display.getWidth();
        params.dimAmount = 0.35f;
        window.setBackgroundDrawableResource(R.color.white);
        window.setAttributes(params);
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        content = View.inflate(context, R.layout.dialog_login_option, null);
        mList = (RecyclerView) content.findViewById(R.id.rl_list);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        mList.setLayoutManager(manager);
        adapter = new OptionAdapter();
        mList.setAdapter(adapter);
        setContentView(content);
    }

    public void setOptions(int[] array) {
        if (options == null) {
            options = new ArrayList<>();
        } else {
            options.clear();
        }
        for (int option : array) {
            options.add(context.getResources().getString(option));
        }
        adapter = new OptionAdapter();
        mList.setAdapter(adapter);
    }

    public void setOptions(ArrayList<String> list) {
        options = list;
    }


    public void addOption(int index, String option) {
        if (options != null) {
            options.add(index, option);
            adapter.notifyDataSetChanged();
        }
    }

    public void removeOption(String option) {
        if (options != null) {
            options.remove(option);
            adapter.notifyDataSetChanged();
        }
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void addOnOptionListener(OnOptionListener listener) {
        this.onOptionListener = listener;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.dismiss();
    }

    private void setRefreshSkin(boolean refreshSkin) {
        this.refreshSkin = refreshSkin;
    }

    class OptionAdapter extends RecyclerView.Adapter<OptionViewHolder> implements View.OnClickListener {

        @Override
        public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(context, R.layout.adapter_option_view, null);
            OptionViewHolder holder = new OptionViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(OptionViewHolder holder, int position) {
            holder.optionView.getDescription().setTextColor(context.getResources().getColor(R.color.color_22));
            holder.optionView.getDescription().setBackgroundResource(R.drawable.selector_listview_item);
            holder.optionView.getDividerTop().setBackgroundColor(context.getResources().getColor(R.color.black_h));
            holder.optionView.getDividerBottom().setBackgroundColor(context.getResources().getColor(R.color.black_h));
            int type = getItemViewType(position);
            if (OPTION_NORMAL == type) {
                holder.optionView.hideDividerTop();
                if (position == getItemCount() - 2) {
                    holder.optionView.hideDividerBottom();
                } else {
                    holder.optionView.showDividerBottom();
                }
            } else if (OPTION_CANCEL == type) {
                holder.optionView.showDividerTop();
                holder.optionView.hideDividerBottom();
            }
            holder.optionView.setDescription(options.get(position));
            holder.optionView.getDescription().setOnClickListener(this);
            holder.optionView.getDescription().setTag(position);
        }

        @Override
        public int getItemViewType(int position) {
            if (position < options.size() - 1) {
                return OPTION_NORMAL;
            } else {
                return OPTION_CANCEL;
            }
        }

        @Override
        public int getItemCount() {
            return options == null ? 0 : options.size();
        }

        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            if (onOptionListener != null) {
                onOptionListener.onOption(position);
            }
            BottomOptionDialog.this.dismiss();
        }
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {

        public OptionItemView optionView;

        public OptionViewHolder(View itemView) {
            super(itemView);
            optionView = (OptionItemView) itemView.findViewById(R.id.option_view);
        }
    }
}