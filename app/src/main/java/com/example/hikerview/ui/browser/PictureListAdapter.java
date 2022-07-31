package com.example.hikerview.ui.browser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.utils.GlideUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

class PictureListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PictureListAdapter";
    private Context context;
    private List<String> list;
    private OnItemClickListener onItemClickListener;
    private RequestOptions options;
    private String url;

    PictureListAdapter(Context context, List<String> list, String url) {
        this.context = context;
        this.list = list;
        this.url = url;
        options = new RequestOptions();
        options.placeholder(context.getResources().getDrawable(R.mipmap.placeholder));
    }

    interface OnItemClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_pic, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof Holder) {
            Holder holder = (Holder) viewHolder;
            GlideUtil.loadFullPicDrawable(context, holder.imgView, GlideUtil.getGlideUrl(url, list.get(position)), options);
            holder.imgView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                        onItemClickListener.onClick(v, holder.getAdapterPosition());
                    }
                }
            });
            holder.imgView.setOnLongClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
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

    private class Holder extends RecyclerView.ViewHolder {
        ImageView imgView;

        Holder(View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.item_reult_img);
        }
    }
}
