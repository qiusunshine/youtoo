package com.example.hikerview.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class XiuTanResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    public List<DetectedMediaResult> getList() {
        return list;
    }

    private List<DetectedMediaResult> list;
    private OnClickListener clickListener;

    public XiuTanResultAdapter(Context context, List<DetectedMediaResult> list, OnClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
    }

    public interface OnClickListener {
        void click(int position, String url);

        void longClick(String url);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TitleHolder(LayoutInflater.from(context).inflate(R.layout.item_xiu_tan_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof TitleHolder) {
            TitleHolder holder = (TitleHolder) viewHolder;
            String title = list.get(position).getTitle() != null ? list.get(position).getTitle() : list.get(position).getUrl();
            holder.title.setText(title);
            String detectImageType = list.get(position).getMediaType().getType();
            if (!TextUtils.isEmpty(detectImageType)) {
                holder.item_video.setVisibility(View.VISIBLE);
                holder.item_video.setText(detectImageType);
            } else {
                holder.item_video.setVisibility(View.GONE);
            }
            if (list.get(position).isClicked()) {
                holder.item_bg.setBackground(context.getResources().getDrawable(R.drawable.ripple_gray_setting));
                holder.clickedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.clickedIcon.setVisibility(View.GONE);
                holder.item_bg.setBackground(context.getResources().getDrawable(R.drawable.ripple_white));
            }
            holder.item_bg.setOnClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.click(holder.getAdapterPosition(), list.get(holder.getAdapterPosition()).getUrl());
                }
            });
            holder.item_bg.setOnLongClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.longClick(list.get(holder.getAdapterPosition()).getUrl());
                }
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class TitleHolder extends RecyclerView.ViewHolder {
        TextView title, item_video;
        View item_bg, clickedIcon;

        TitleHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_ad_title);
            item_video = itemView.findViewById(R.id.item_video);
            item_bg = itemView.findViewById(R.id.item_bg);
            clickedIcon = itemView.findViewById(R.id.click_tag_icon);
        }
    }
}
