package com.example.hikerview.ui.browser.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.service.UpdateEvent;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class JSUpdateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    public List<UpdateEvent> getList() {
        return list;
    }

    private List<UpdateEvent> list;
    private OnClickListener clickListener;

    public JSUpdateAdapter(Context context, List<UpdateEvent> list, OnClickListener onClickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = onClickListener;
    }

    public interface OnClickListener {
        void click(int position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TitleHolder(LayoutInflater.from(context).inflate(R.layout.item_js_update, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof TitleHolder) {
            try {
                TitleHolder holder = (TitleHolder) viewHolder;
                UpdateEvent event = list.get(position);
                String title = event.getFileName();
                holder.title.setText(title);
                String desc;
                if (StringUtils.equals(event.getOldVersion(), event.getNewVersion())) {
                    desc = "已更新到：" + event.getNewVersion();
                } else {
                    desc = "旧版本：" + event.getOldVersion() + "  新版本：" + event.getNewVersion();
                }
                holder.item_video.setText(desc);
                holder.item_bg.setOnClickListener(v -> {
                    try {
                        if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                            clickListener.click(holder.getAdapterPosition());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class TitleHolder extends RecyclerView.ViewHolder {
        TextView title, item_video;
        View item_bg;

        TitleHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_ad_title);
            item_video = itemView.findViewById(R.id.item_video);
            item_bg = itemView.findViewById(R.id.item_bg);
        }
    }
}
