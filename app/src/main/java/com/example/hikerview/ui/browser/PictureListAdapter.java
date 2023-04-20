package com.example.hikerview.ui.browser;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.ui.base.IBaseHolder;
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

    public List<String> getList() {
        return list;
    }

    private List<String> list;
    private OnItemClickListener onItemClickListener;
    private RequestOptions options;
    private String url;
    private boolean horizontal;

    PictureListAdapter(Context context, List<String> list, String url, boolean horizontal) {
        this.context = context;
        this.list = list;
        this.url = url;
        options = new RequestOptions();
        this.horizontal = horizontal;
        options.placeholder(context.getResources().getDrawable(R.mipmap.placeholder)).diskCacheStrategy(DiskCacheStrategy.DATA);
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
        return new Holder(LayoutInflater.from(context).inflate(
                horizontal ? R.layout.item_pic_horizontal : R.layout.item_pic, parent, false));
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof IBaseHolder) {
            IBaseHolder baseHolder = (IBaseHolder) holder;
            if (baseHolder.getImageView() != null && context != null) {
                if (context instanceof Activity && ((Activity) context).isFinishing()) {
                    return;
                }
                try {
                    Glide.with(context).clear(baseHolder.getImageView());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

    private class Holder extends RecyclerView.ViewHolder implements IBaseHolder {
        ImageView imgView;

        Holder(View itemView) {
            super(itemView);
            imgView = itemView.findViewById(R.id.item_reult_img);
        }

        @Override
        public ImageView getImageView() {
            return imgView;
        }
    }
}
