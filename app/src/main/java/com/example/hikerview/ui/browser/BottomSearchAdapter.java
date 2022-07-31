package com.example.hikerview.ui.browser;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.model.SearchEngine;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class BottomSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private static final String TAG = "BottomSearchAdapter";
    private int layoutId = R.layout.item_button;

    public List<SearchEngine> getList() {
        return list;
    }

    private List<SearchEngine> list;
    private OnItemClickListener onItemClickListener;

    public BottomSearchAdapter(Context context, List<SearchEngine> list) {
        this.context = context;
        this.list = list;
    }

    /**
     * 构建
     *
     * @param context
     * @param list
     * @param layoutId 必须是TextView或子类
     */
    public BottomSearchAdapter(Context context, List<SearchEngine> list, int layoutId) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
    }

    public interface OnItemClickListener {
        void onClick(View view, int position, SearchEngine engine);

        void onLongClick(View view, int position, SearchEngine engine);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ArticleListRuleHolder(LayoutInflater.from(context).inflate(layoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ArticleListRuleHolder) {
            ArticleListRuleHolder holder = (ArticleListRuleHolder) viewHolder;
            String title = list.get(position).getTitle();
            holder.item_rect_text.setText(title);
            String titleColor = list.get(position).getTitleColor();
            TextPaint tp = holder.item_rect_text.getPaint();
            if (list.get(position).isUse()) {
                tp.setFakeBoldText(true);
                holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.yellowAction));
            } else if (TextUtils.isEmpty(titleColor)) {
                holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.black_666));
                tp.setFakeBoldText(false);
            } else {
                tp.setFakeBoldText(false);
                if (!titleColor.startsWith("#")) {
                    titleColor = "#" + titleColor;
                }
                try {
                    int c = Color.parseColor(titleColor);
                    holder.item_rect_text.setTextColor(c);
                } catch (Exception e) {
                    Log.w(TAG, "onBindViewHolder: " + e.getMessage(), e);
                    holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.black_666));
                }
            }
            if (list.get(position).isUse()) {
                holder.item_rect_text.setBackground(context.getDrawable(R.drawable.check_bg_search));
            } else {
                holder.item_rect_text.setBackground(context.getDrawable(R.drawable.button_layer_black));
            }
            holder.item_rect_text.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getTitle().equals(title)) {
                            list.get(i).setUse(true);
                            onItemClickListener.onClick(v, i, list.get(i));
                        } else {
                            list.get(i).setUse(false);
                        }
                    }
                    notifyDataSetChanged();
                }
            });
            holder.item_rect_text.setOnLongClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0) {
                        onItemClickListener.onLongClick(v, holder.getAdapterPosition(), list.get(position));
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
            item_rect_text = itemView.findViewById(R.id.item_button);
        }
    }
}
