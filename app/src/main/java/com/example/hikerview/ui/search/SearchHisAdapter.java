package com.example.hikerview.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class SearchHisAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    public List<String> getList() {
        return list;
    }

    private List<String> list;
    private OnItemClickListener onItemClickListener;
    private static final String TAG = "SearchHisAdapter";

    public SearchHisAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
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
        return new ArticleListRuleHolder(LayoutInflater.from(context).inflate(R.layout.item_rect_radius_wrap_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ArticleListRuleHolder) {
            ArticleListRuleHolder holder = (ArticleListRuleHolder) viewHolder;
            String title = list.get(position);
            holder.item_rect_text.setText(title);
            holder.item_rect_text.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0) {
                        onItemClickListener.onClick(v, holder.getAdapterPosition());
                    }
                }
            });
            holder.item_rect_text.setOnLongClickListener(v -> {
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
        return list.size();
    }

    private class ArticleListRuleHolder extends RecyclerView.ViewHolder {
        TextView item_rect_text;

        ArticleListRuleHolder(View itemView) {
            super(itemView);
            item_rect_text = itemView.findViewById(R.id.item_rect_wrap_text);
        }
    }
}
