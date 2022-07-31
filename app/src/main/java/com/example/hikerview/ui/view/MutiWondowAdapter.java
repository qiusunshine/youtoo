package com.example.hikerview.ui.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.view.colorDialog.util.DisplayUtil;
import com.example.hikerview.utils.ScreenUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class MutiWondowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;

    public List<HorizontalWebView> getList() {
        return list;
    }

    public List<Bitmap> getBitmaps() {
        return bitmaps;
    }

    private List<Bitmap> bitmaps = new ArrayList<>();

    private List<HorizontalWebView> list;
    private OnClickListener clickListener;

    public MutiWondowAdapter(Activity context, List<HorizontalWebView> list, OnClickListener clickListener, View homeView) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
        for (HorizontalWebView webView : list) {
            if (StringUtil.isEmpty(webView.getUrl()) && homeView != null) {
                bitmaps.add(webView.capturePreview(homeView, true));
            } else {
                bitmaps.add(webView.capturePreview());
            }
        }
    }

    public interface OnClickListener {
        void click(View view, int pos);

        void remove(int pos);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TitleHolder(LayoutInflater.from(context).inflate(R.layout.item_muti_window, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof TitleHolder) {
            TitleHolder holder = (TitleHolder) viewHolder;
            if (position < bitmaps.size()) {
                holder.item_img.setImageBitmap(bitmaps.get(position));
            } else {
                holder.item_img.setImageDrawable(new ColorDrawable(context.getResources().getColor(R.color.white)));
            }
            try {
                holder.item_img.setContentDescription(list.get(position).getTitle());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String t = list.get(position).getTitle();
            holder.title.setText(StringUtil.isEmpty(list.get(position).getUrl()) ? "主页" : t);
            holder.item_img.setOnClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.click(v, holder.getAdapterPosition());
                }
            });
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.item_ad_bg.getLayoutParams();
            int width = Math.min(ScreenUtil.getScreenHeight(context), ScreenUtil.getScreenWidth(context));
            lp.height = (width - DisplayUtil.dp2px(context, 80)) / 2 / 9 * 16;
            lp.width = (width - DisplayUtil.dp2px(context, 80)) / 2;
            if (ScreenUtil.isOrientation(context)) {
                int temp = lp.height;
                lp.height = lp.width / 5 * 3;
                lp.width = temp / 5 * 3;
            }
            holder.item_ad_bg.setLayoutParams(lp);
            CardView.LayoutParams lp2 = (CardView.LayoutParams) holder.item_bg.getLayoutParams();
            int dp2 = 0;
            if (list.get(position).isUsed()) {
                dp2 = DisplayUtil.dp2px(context, 2);
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
        ImageView item_video, item_img;
        CardView item_ad_bg, item_bg;
        TextView title;

        TitleHolder(View itemView) {
            super(itemView);
            item_ad_bg = itemView.findViewById(R.id.item_ad_bg);
            item_bg = itemView.findViewById(R.id.item_bg);
            item_video = itemView.findViewById(R.id.item_video);
            item_img = itemView.findViewById(R.id.item_img);
            title = itemView.findViewById(R.id.item_title);
        }
    }
}
