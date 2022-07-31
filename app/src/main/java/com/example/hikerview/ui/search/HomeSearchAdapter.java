package com.example.hikerview.ui.search;

import android.app.Activity;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.view.BaseDividerItem;
import com.example.hikerview.ui.view.RecyclerViewReboundAnimator;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class HomeSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private static final String TAG = "BottomSearchAdapter";
    private int layoutId;

    public List<SearchEngine> getList() {
        return list;
    }

    private List<SearchEngine> list;
    private OnItemClickListener onItemClickListener;
    private Activity activity;
    private MyDividerItem dividerItem;


    private RecyclerViewReboundAnimator mReboundAnimator;

    public int getmColumn() {
        return mColumn;
    }

    private int mColumn = 4;

    /**
     * 构建
     *
     * @param context
     * @param list
     */
    public HomeSearchAdapter(Activity context, RecyclerView recyclerView, List<SearchEngine> list) {
        this.context = context;
        this.activity = context;
        this.list = list;
        this.layoutId = R.layout.item_search_button_btn;
        dividerItem = new MyDividerItem();

        mReboundAnimator = new RecyclerViewReboundAnimator(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null) {
            if (layoutManager.getClass().equals(LinearLayoutManager.class)) {
                mColumn = 4;
            } else if (layoutManager.getClass().equals(GridLayoutManager.class)) {
                GridLayoutManager glm = (GridLayoutManager) layoutManager;
                mColumn = glm.getSpanCount();
            }
        }
    }


    MyDividerItem getDividerItem() {
        return dividerItem;
    }

    public void closeAnimate() {
        mReboundAnimator.setOpen(false);
    }

    public void openAnimate() {
        mReboundAnimator.setOpen(true);
    }

    public int getmLastPosition() {
        return mReboundAnimator.getmLastPosition();
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
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, parent, false);
//        mReboundAnimator.onCreateViewHolder(viewGroup, mColumn);
        return new ArticleListRuleHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ArticleListRuleHolder) {
            ArticleListRuleHolder holder = (ArticleListRuleHolder) viewHolder;
            String title = list.get(position).getTitle();
            holder.item_rect_text.setText(title);
            String titleColor = list.get(position).getTitleColor();

//            if (activity != null) {
//                GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.result_bg.getLayoutParams();
//                int dp_5 = DisplayUtil.dpToPx(context, 5);
//                lp.width = (ScreenUtil.getScreenWidth(activity) - dp_5 * 2 * 5) / 4;
//                lp.setMargins(0, 0, 0, dp_5 * 2);
//                holder.result_bg.setLayoutParams(lp);
//            }

            TextPaint tp = holder.item_rect_text.getPaint();
            if (list.get(position).isUse()) {
                tp.setFakeBoldText(true);
                holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.yellowAction));
            } else if (TextUtils.isEmpty(titleColor)) {
                tp.setFakeBoldText(false);
                holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.black_666));
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
                if (StringUtil.isEmpty(list.get(position).getFindRule())) {
                    holder.item_rect_text.setBackground(context.getDrawable(R.drawable.button_layer_gray));
                } else {
                    holder.item_rect_text.setBackground(context.getDrawable(R.drawable.button_layer_black));
                }
            }
            holder.item_rect_text.setOnClickListener(v -> {
                closeAnimate();
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
                closeAnimate();
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                        onItemClickListener.onLongClick(v, holder.getAdapterPosition(), list.get(holder.getAdapterPosition()));
                    }
                }
                return true;
            });
        }
//        mReboundAnimator.onBindViewHolder(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class ArticleListRuleHolder extends RecyclerView.ViewHolder {
        TextView item_rect_text;
        View result_bg;

        ArticleListRuleHolder(View itemView) {
            super(itemView);
            item_rect_text = itemView.findViewById(R.id.item_button);
            result_bg = itemView.findViewById(R.id.result_bg);
        }
    }

    public class MyDividerItem extends BaseDividerItem {

        @Override
        public int getLeftRight(int itemViewType) {
            return DisplayUtil.dpToPx(context, 10);
        }
    }
}
