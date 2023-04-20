package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.ui.view.colorDialog.util.DisplayUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class MultiWondowTextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;

    public List<HorizontalWebView> getList() {
        return list;
    }

    public List<String> getBitmaps() {
        return bitmaps;
    }

    private List<String> bitmaps = new ArrayList<>();

    private List<HorizontalWebView> list;
    private OnClickListener clickListener;

    public MultiWondowTextAdapter(Activity context, List<HorizontalWebView> list, OnClickListener clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
        for (HorizontalWebView webView : list) {
            if (webView == null) {
                bitmaps.add(null);
                continue;
            }
            if (StringUtil.isEmpty(webView.getUrl())) {
                String icon = StringUtil.getHomeUrl(webView.getUrl()) + "favicon.ico";
                bitmaps.add(icon);
            } else {
                bitmaps.add("");
            }
        }
    }

    public interface OnClickListener {
        void click(View view, int pos);

        void remove(int pos);
    }

    @Override
    public int getItemViewType(int position) {
        if (list.size() <= position || list.get(position) == null) {
            return 1;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new BlankHolder(LayoutInflater.from(context).inflate(R.layout.item_blank_block, parent, false));
        }
        return new TitleHolder(LayoutInflater.from(context).inflate(R.layout.item_multi_window_text, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof BlankHolder) {

        } else if (viewHolder instanceof TitleHolder) {
            TitleHolder holder = (TitleHolder) viewHolder;
            if (list.get(position).getFavicon() != null) {
                Glide.with(context)
                        .load(list.get(position).getFavicon())
                        .apply(new RequestOptions().placeholder(context.getResources().getDrawable(R.drawable.ic_bookmark_url)))
                        .into(holder.item_icon);
            } else if (position < bitmaps.size()) {
                Glide.with(context)
                        .load(bitmaps.get(position))
                        .apply(new RequestOptions().placeholder(context.getResources().getDrawable(R.drawable.ic_bookmark_url)))
                        .into(holder.item_icon);
            } else {
                Glide.with(context)
                        .load(context.getResources().getDrawable(R.drawable.ic_bookmark_url))
                        .into(holder.item_icon);
            }
            try {
                holder.item_icon.setContentDescription(list.get(position).getTitle());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String t = list.get(position).getTitle();
            holder.title.setText(StringUtil.isEmpty(list.get(position).getUrl()) ? "主页" : t);
            holder.item_ad_bg.setOnClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.click(v, holder.getAdapterPosition());
                }
            });
            CardView.LayoutParams lp2 = (CardView.LayoutParams) holder.item_bg.getLayoutParams();
            int dp2 = 0;
            if (list.get(position).isUsed()) {
                dp2 = DisplayUtil.dp2px(context, 1.5F);
            }
            lp2.leftMargin = dp2;
            lp2.rightMargin = dp2;
            lp2.bottomMargin = dp2;
            lp2.topMargin = dp2;
            holder.item_bg.setLayoutParams(lp2);
            holder.item_video.setOnClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.remove(holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class TitleHolder extends RecyclerView.ViewHolder {
        ImageView item_video, item_icon;
        CardView item_ad_bg, item_bg;
        TextView title;

        TitleHolder(View itemView) {
            super(itemView);
            item_ad_bg = itemView.findViewById(R.id.item_ad_bg);
            item_bg = itemView.findViewById(R.id.item_bg);
            item_video = itemView.findViewById(R.id.item_video);
            item_icon = itemView.findViewById(R.id.item_icon);
            title = itemView.findViewById(R.id.item_title);
        }
    }

    private class BlankHolder extends RecyclerView.ViewHolder {

        BlankHolder(View itemView) {
            super(itemView);
        }
    }
}
