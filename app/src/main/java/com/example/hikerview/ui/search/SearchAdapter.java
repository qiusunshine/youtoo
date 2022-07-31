package com.example.hikerview.ui.search;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.download.util.RandomUtil;
import com.example.hikerview.ui.home.model.SearchResult;
import com.example.hikerview.utils.GlideUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<SearchResult> list;
    private OnItemClickListener onItemClickListener;
    private String keyWord;
    private static String[] words = {"蓝光", "超清", "高清", "BD", "HD", "国语", "粤语"};
    private int[] colors = {0xfff5f5f5, 0xfff0fbff, 0xfffef3ef, 0xfff7eeff, 0xf0e6f3e6};
    private SearchEngine searchEngine;

    SearchAdapter(Context context, List<SearchResult> list, String keyWord, SearchEngine searchEngine) {
        this.context = context;
        this.list = list;
        this.keyWord = keyWord;
        this.searchEngine = searchEngine;
    }

    public int getListSize() {
        return list.size();
    }

    public List<SearchResult> getList() {
        return list;
    }

    public boolean removeFooter() {
        if (list.size() > 0) {
            list.remove(list.size() - 1);
            notifyDataSetChanged();
        }
        return true;
    }

    public void add(List<SearchResult> lists) {
        list.addAll(lists);
        notifyDataSetChanged();
    }

    interface OnItemClickListener {
        void onClick(View view, int position, String type);

        void onLongClick(View view, int position, String type);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_search_all, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final MyViewHolder holder = (MyViewHolder) viewHolder;
        String myTitle = list.get(position).getTitle();
        if (myTitle != null && myTitle.contains(keyWord)) {
            int index = myTitle.indexOf(keyWord);
            int len = keyWord.length();
            Spanned temp = Html.fromHtml(
                    "<font color=#003D8F>" + myTitle.substring(0, index) + "</font>"
                            + "<font color=#FF0000>"
                            + myTitle.substring(index, index + len) + "</font>"
                            + "<font color=#003D8F>"
                            + myTitle.substring(index + len, myTitle.length()) + "</font>");
            holder.title.setText(temp);
        } else {
            holder.title.setText(myTitle);
        }
        int wordPos = getWordPos(myTitle);
        if (wordPos >= 0) {
            holder.notice.setText((" · " + words[wordPos]));
            holder.notice.setVisibility(View.VISIBLE);
        } else {
            holder.notice.setVisibility(View.GONE);
        }
        String imgUrl = list.get(position).getImg();
        if (TextUtils.isEmpty(imgUrl)) {
            holder.imgBg.setVisibility(View.GONE);
        } else {
            holder.imgBg.setVisibility(View.VISIBLE);
            int color = RandomUtil.getRandom(0, colors.length);
            if (color < 0 || color >= colors.length) {
                color = colors.length - 1;
            }
            Glide.with(context)
                    .load(GlideUtil.getGlideUrl(searchEngine.getSearch_url(), imgUrl))
                    .apply(new RequestOptions()
                            .placeholder(new ColorDrawable(colors[color]))
                    )
                    .into(holder.img);
        }
        String desc = list.get(position).getDesc();
        if (TextUtils.isEmpty(desc)) {
            holder.desc.setVisibility(View.GONE);
        } else {
            holder.desc.setVisibility(View.VISIBLE);
            holder.desc.setText(desc);
        }
        String content = list.get(position).getContent();
        if (TextUtils.isEmpty(content)) {
            holder.content.setVisibility(View.GONE);
        } else {
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setText(content);
        }
        String descMore = list.get(position).getDescMore();
        if (TextUtils.isEmpty(descMore)) {
            holder.descMore.setVisibility(View.GONE);
        } else {
            holder.descMore.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(desc)) {
                holder.descMore.setText(descMore);
            } else {
                holder.descMore.setText((" · " + descMore));
            }
        }
        holder.containner.setOnClickListener(view -> {
            int pos = holder.getLayoutPosition();
            onItemClickListener.onClick(view, pos, list.get(pos).getDesc());
        });
        holder.containner.setOnLongClickListener(view -> {
            int pos = holder.getLayoutPosition();
            onItemClickListener.onLongClick(view, pos, list.get(pos).getDesc());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, notice, descMore, content;
        RelativeLayout containner;
        View imgBg;
        ImageView img;

        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.chooseitem_all_title);
            content = itemView.findViewById(R.id.search_all_content);
            img = itemView.findViewById(R.id.chooseitem_all_img);
            imgBg = itemView.findViewById(R.id.chooseitem_all_img_bg);
            desc = itemView.findViewById(R.id.chooseitem_all_desc);
            containner = itemView.findViewById(R.id.chooseitem_all_RelativeLayout);
            notice = itemView.findViewById(R.id.choose_item_all_notice);
            descMore = itemView.findViewById(R.id.choose_item_all_desc_more);
        }
    }

    private int getWordPos(String title) {
        if (StringUtil.isEmpty(title)) {
            return -1;
        }
        for (int i = 0; i < words.length; i++) {
            if (title.contains(words[i])) {
                return i;
            }
        }
        return -1;
    }
}
