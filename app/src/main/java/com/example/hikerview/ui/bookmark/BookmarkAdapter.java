package com.example.hikerview.ui.bookmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.model.Bookmark;
import com.example.hikerview.utils.StringUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

class BookmarkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Bookmark> list;
    private OnItemClickListener onItemClickListener;
    private int[] colors = {0xfff5f5f5, 0xfff0fbff, 0xfffef3ef, 0xfff7eeff, 0xf0e6f3e6};

    BookmarkAdapter(Context context, List<Bookmark> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    interface OnItemClickListener {
        void onClick(View v, int position);

        void onLongClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BookmarkHolder(LayoutInflater.from(context).inflate(R.layout.item_bookmark, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof BookmarkHolder) {
            final BookmarkHolder holder = (BookmarkHolder) viewHolder;
            Bookmark bookmark = list.get(position);
            String title = bookmark.getTitle();
            holder.textView.setText(title);
            if (StringUtil.isEmpty(bookmark.getUrl()) || bookmark.isDir()) {
                Glide.with(context)
                        .load(R.drawable.ic_bookmark_folder)
                        .into(holder.imageView);
            } else {
                String icon = bookmark.getIcon();
                if (StringUtil.isEmpty(icon)) {
                    icon = StringUtil.getHomeUrl(bookmark.getUrl()) + "favicon.ico";
                }
                Glide.with(context)
                        .load(icon)
                        .apply(new RequestOptions().placeholder(context.getResources().getDrawable(R.drawable.ic_bookmark_url)))
                        .into(holder.imageView);
            }

            holder.resultBg.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                        onItemClickListener.onClick(v, holder.getAdapterPosition());
                    }
                }
            });

            holder.resultBg.setOnLongClickListener(v -> {
                if (onItemClickListener != null) {
                    if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                        onItemClickListener.onLongClick(v, holder.getAdapterPosition());
                    }
                }
                return true;
            });
            if (bookmark.isSelected()) {
                holder.resultBg.setBackground(context.getResources().getDrawable(R.drawable.shape_bottom_border_selected));
            } else {
                holder.resultBg.setBackground(context.getResources().getDrawable(R.drawable.shape_bottom_border));
            }
        }

    }

    private void updateImg(ImageView imageView, int id) {
        Glide.with(context)
                .load(id)
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class BookmarkHolder extends RecyclerView.ViewHolder {
        View resultBg;
        ImageView imageView;
        TextView textView;

        BookmarkHolder(View itemView) {
            super(itemView);
            resultBg = itemView.findViewById(R.id.bg);
            imageView = itemView.findViewById(R.id.item_reult_img);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}
