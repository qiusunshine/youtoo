package com.example.hikerview.ui.rules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.rules.model.SubscribeRecord;
import com.example.hikerview.utils.TimeUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class RulesSubscribeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<SubscribeRecord> list;
    private OnItemClickListener onItemClickListener;

    public RulesSubscribeAdapter(Context context, List<SubscribeRecord> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ArticleListRuleHolder(LayoutInflater.from(context).inflate(R.layout.item_sub_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ArticleListRuleHolder) {
            ArticleListRuleHolder holder = (ArticleListRuleHolder) viewHolder;
            SubscribeRecord record = list.get(position);
            String title = record.getTitle();
            holder.title.setText(title);
            if (!record.isUse()) {
                holder.desc.setText("当前未启用，请先启用");
            } else if (record.getModifyDate() != null) {
                String time = TimeUtil.formatTime(record.getModifyDate().getTime());
                String desc;
                if (record.isLastUpdateSuccess()) {
                    desc = "总规则数：" + record.getRulesCount() + "，上次更新规则数：" + record.getLastUpdateCount() + "，更新时间：" + time;
                } else if (record.getErrorCount() > 10) {
                    desc = "总规则数：" + record.getRulesCount() + "，连续失败超过10次" + "，最后更新时间：" + time;
                } else {
                    desc = "总规则数：" + record.getRulesCount() + "，上次更新失败（" + time + "），连续失败次数：" + record.getErrorCount();
                }
                holder.desc.setText(desc);
            } else {
                holder.desc.setText("无更新记录");
            }
            holder.bg.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0) {
                        onItemClickListener.onClick(v, holder.getAdapterPosition());
                    }
                }
            });
            holder.bg.setOnLongClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0) {
                        onItemClickListener.onLongClick(v, holder.getAdapterPosition());
                    }
                }
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private static class ArticleListRuleHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        View bg;

        ArticleListRuleHolder(View itemView) {
            super(itemView);
            bg = itemView.findViewById(R.id.item_bg);
            title = itemView.findViewById(R.id.item_ad_title);
            desc = itemView.findViewById(R.id.item_video);
        }
    }
}
