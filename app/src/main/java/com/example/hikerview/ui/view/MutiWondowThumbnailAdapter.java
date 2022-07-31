package com.example.hikerview.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class MutiWondowThumbnailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    public List<HorizontalWebView> getList() {
        return list;
    }

    private List<Bitmap> bitmaps;

    private List<HorizontalWebView> list;
    private OnClickListener clickListener;

    public MutiWondowThumbnailAdapter(Context context, List<HorizontalWebView> list, OnClickListener clickListener, List<Bitmap> bitmaps) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
        this.bitmaps = bitmaps;
    }

    public interface OnClickListener {
        void click(View view, int pos);

        void remove(int pos);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TitleHolder(LayoutInflater.from(context).inflate(R.layout.item_muti_window2, parent, false));
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
            holder.item_img.setOnClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.click(v, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class TitleHolder extends RecyclerView.ViewHolder {
        ImageView item_img;

        TitleHolder(View itemView) {
            super(itemView);
            item_img = itemView.findViewById(R.id.item_img);
        }
    }
}
