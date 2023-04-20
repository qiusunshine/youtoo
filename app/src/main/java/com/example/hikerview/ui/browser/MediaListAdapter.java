package com.example.hikerview.ui.browser;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.constants.Media;
import com.example.hikerview.constants.MediaType;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.utils.GlideUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

class MediaListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<DetectedMediaResult> list;
    private OnItemClickListener onItemClickListener;
    private RequestOptions options;
    private String simpleDom = "";
    private String baseUrl;
    private static final String[] whiteList = new String[]{
            "//cdn.jsdelivr.net/npm/",
            "//cdn.staticfile.org/",
            ".bytecdntp.com/cdn/",
            "/jquery.min.js",
            "//cdn.bootcdn.net/",
            "//cdn.bootcss.com/",
            "//at.alicdn.com/t/font"
    };

    MediaListAdapter(Context context, List<DetectedMediaResult> list, String url) {
        this.context = context;
        this.list = list;
        this.baseUrl = url;
        this.simpleDom = StringUtil.getSimpleDom(url);
        options = new RequestOptions();
        options.placeholder(new ColorDrawable(context.getResources().getColor(R.color.gray_rice)));
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
        return new ArticleListRuleHolder(LayoutInflater.from(context).inflate(R.layout.item_rect_radius_no_center, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ArticleListRuleHolder) {
            ArticleListRuleHolder holder = (ArticleListRuleHolder) viewHolder;
            String title;
            String type = list.get(position).getMediaType().getType();
            if (TextUtils.isEmpty(type)) {
                title = list.get(position).getUrl();
            } else {
                title = "【" + type + "】" + list.get(position).getUrl();
            }
            Media media = list.get(position).getMediaType();
            if (media.getName().equals(MediaType.IMAGE.getName())) {
                holder.imgView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(GlideUtil.getGlideUrl(baseUrl, GlideUtil.getImageUrl(list.get(position).getUrl())))
                        .apply(options)
                        .into(holder.imgView);
            } else {
                holder.imgView.setVisibility(View.GONE);
                //被拦截的图片也显示出来
                if (list.get(position).getMediaType().getName().equals(MediaType.BLOCK.getName())) {
                    if (UrlDetector.isImage(list.get(position).getUrl())) {
                        holder.imgView.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(GlideUtil.getGlideUrl(baseUrl, list.get(position).getUrl()))
                                .apply(options)
                                .into(holder.imgView);
                    }
                }
            }
            String dom = StringUtil.getDom(list.get(position).getUrl());
            if (Media.BLOCK.equals(list.get(position).getMediaType().getName())) {
                holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.video));
            } else if (StringUtil.isNotEmpty(dom) && StringUtil.isNotEmpty(simpleDom) && dom.contains(simpleDom)) {
                holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.black_666));
            } else if (StringUtil.isNotEmpty(dom)) {
                boolean inWhiteList = false;
                for (String s : whiteList) {
                    if (list.get(position).getUrl().contains(s)) {
                        inWhiteList = true;
                        break;
                    }
                }
                if (inWhiteList) {
                    holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.black_666));
                } else {
                    holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.color_type_warning));
                }
            }  else {
                holder.item_rect_text.setTextColor(context.getResources().getColor(R.color.color_type_warning));
            }
            if (context.getResources().getString(R.string.home_ip).equals(dom)) {
                title = title.replace(context.getResources().getString(R.string.home_ip), context.getResources().getString(R.string.home_domain));
            }
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
        ImageView imgView;

        ArticleListRuleHolder(View itemView) {
            super(itemView);
            item_rect_text = itemView.findViewById(R.id.item_rect_text_no_center);
            imgView = itemView.findViewById(R.id.item_rect_text_no_center_img);
        }
    }
}
