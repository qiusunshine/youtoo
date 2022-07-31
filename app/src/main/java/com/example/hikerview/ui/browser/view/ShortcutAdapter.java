package com.example.hikerview.ui.browser.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.constants.ImageUrlMapEnum;
import com.example.hikerview.ui.browser.enums.ShortcutTypeEnum;
import com.example.hikerview.ui.browser.model.Shortcut;
import com.example.hikerview.ui.view.BaseDividerItem;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.StringUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static android.view.View.VISIBLE;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class ShortcutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    public List<Shortcut> getList() {
        return list;
    }

    private List<Shortcut> list;
    private OnItemClickListener onItemClickListener;
    private MyDividerItem dividerItem;

    public ShortcutAdapter(Context context, List<Shortcut> list) {
        this.context = context;
        this.list = list;
        dividerItem = new MyDividerItem();
    }

    @Override
    public int getItemViewType(int position) {
        return ShortcutTypeEnum.Companion.getByCode(list.get(position).getType()).getViewType();
    }

    public interface OnItemClickListener {
        void onClick(View v, int position);

        void onDel(View v, int position);

        void onLongClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ShortcutTypeEnum.POETRY.getViewType()) {
            return new PoetryHolder(LayoutInflater.from(context).inflate(R.layout.item_shortcut_poetry, parent, false));
        } else if (viewType == ShortcutTypeEnum.DATA.getViewType()) {
            return new DataHolder(LayoutInflater.from(context).inflate(R.layout.item_shortcut_data, parent, false));
        }
        return new ItemHolder(LayoutInflater.from(context).inflate(R.layout.item_shortcut, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof PoetryHolder) {
            bindPoetryHolder((PoetryHolder) viewHolder, list.get(position));
        } else if (viewHolder instanceof DataHolder) {
            bindDataHolder((DataHolder) viewHolder, list.get(position));
        } else if (viewHolder instanceof ItemHolder) {
            final ItemHolder holder = (ItemHolder) viewHolder;
            Shortcut shortcut = list.get(position);
            if (!shortcut.getIcon().equals(holder.shortcut_bg.getTag())) {
                holder.shortcut_bg.setTag(shortcut.getIcon());
                if (shortcut.getIcon().startsWith("color://")) {
                    try {
                        int color = Integer.parseInt(StringUtils.replaceOnce(shortcut.getIcon(), "color://", ""));
                        Glide.with(context)
                                .load(new ColorDrawable(color))
                                .apply(new RequestOptions().skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop())
                                .into(holder.icon);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    int padding = DisplayUtil.dpToPx(context, 2);
                    holder.icon.setPadding(padding, padding, padding, padding);
                } else {
                    int imgId = ImageUrlMapEnum.getIdByUrl(shortcut.getIcon());
                    Glide.with(context)
                            .load(imgId > 0 ? imgId : shortcut.getIcon())
                            .apply(new RequestOptions().skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop())
                            .into(holder.icon);
                    if (imgId > 0) {
                        holder.icon.setPadding(0, 0, 0, 0);
                    } else {
                        int padding = DisplayUtil.dpToPx(context, 2);
                        holder.icon.setPadding(padding, padding, padding, padding);
                    }
                }
            }
            if (shortcut.getIcon().startsWith("color://")) {
                holder.iconText.setText(shortcut.getName());
                holder.iconText.setVisibility(VISIBLE);
            } else {
                holder.iconText.setText("");
                holder.iconText.setVisibility(View.GONE);
            }
//            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.shortcut_bg.getLayoutParams();
//            int dp_5 = DisplayUtil.dpToPx(context, 5);
//            lp.width = (ScreenUtil.getScreenWidth3(context) - dp_5 * 6) / 5;
//            lp.setMargins(0, dp_5, 0, dp_5);
//            holder.shortcut_bg.setLayoutParams(lp);
            if (shortcut.isHasBackground()) {
                holder.text.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                holder.text.setTextColor(context.getResources().getColor(R.color.blackText));
            }
            holder.text.setText(shortcut.getName());
            bindDel(holder, shortcut, holder.del);
            bindBg(holder, holder.shortcut_bg);
        }
    }

    private void bindDataHolder(DataHolder holder, Shortcut shortcut) {
        if (StringUtils.isEmpty(shortcut.getIcon()) || shortcut.getIcon().startsWith("color://")) {
            holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.home_pic4));
        } else {
            int imgId = ImageUrlMapEnum.getIdByUrl(shortcut.getIcon());
            Glide.with(context)
                    .load(imgId > 0 ? imgId : shortcut.getIcon())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.home_pic4)
                            .skipMemoryCache(false)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    ).into(holder.imageView);
        }
        if (shortcut.isHasBackground()) {
            holder.textView1.setTextColor(context.getResources().getColor(R.color.white));
            holder.textView2.setTextColor(context.getResources().getColor(R.color.white));
            holder.textView3.setTextColor(context.getResources().getColor(R.color.white));
            holder.shortcut_bg1.setCardBackgroundColor(context.getResources().getColor(R.color.half_transparent3));
        } else {
            holder.textView1.setTextColor(context.getResources().getColor(R.color.text));
            holder.textView2.setTextColor(context.getResources().getColor(R.color.text));
            holder.textView3.setTextColor(context.getResources().getColor(R.color.text));
            holder.shortcut_bg1.setCardBackgroundColor(context.getResources().getColor(R.color.gray_rice2));
        }
        String[] data = StringUtil.isEmpty(shortcut.getUrl()) ? new String[]{} : shortcut.getUrl().split("@@");
        String count1 = "0", count2 = "0", count3 = "0";
        if (data.length == 3) {
            count1 = data[0];
            count2 = data[1];
            count3 = data[2];
        }
        holder.textView1.setText(("收藏书签：" + count1));
        holder.textView2.setText(("视频下载：" + count2));
        holder.textView3.setText(("网页插件：" + count3));
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.shortcut_bg.getLayoutParams();
        int dp_5 = DisplayUtil.dpToPx(context, 5);
        lp.setMargins(dp_5 * 2, dp_5, dp_5 * 2, dp_5);
        holder.shortcut_bg.setLayoutParams(lp);
        bindDel(holder, shortcut, holder.del);
        bindBg(holder, holder.shortcut_bg);
        bindBg(holder, holder.textView1);
        bindBg(holder, holder.textView2);
        bindBg(holder, holder.textView3);
        bindBg(holder, holder.shortcut_bg2);
    }

    private void bindPoetryHolder(PoetryHolder holder, Shortcut shortcut) {
        holder.text.setText(shortcut.getName());
        holder.author.setText(shortcut.getUrl());
        if (shortcut.isHasBackground()) {
            holder.text.setTextColor(context.getResources().getColor(R.color.white));
            holder.author.setTextColor(context.getResources().getColor(R.color.white));
            holder.shortcut_bg.setCardBackgroundColor(context.getResources().getColor(R.color.half_transparent3));
        } else {
            holder.text.setTextColor(context.getResources().getColor(R.color.text));
            holder.author.setTextColor(context.getResources().getColor(R.color.text));
            holder.shortcut_bg.setCardBackgroundColor(context.getResources().getColor(R.color.gray_rice2));
        }
//        AssetManager mgr = context.getAssets();
//        Typeface tf = Typeface.createFromAsset(mgr, "fonts/ming.ttf");
//        holder.text.setTypeface(tf);
//        holder.author.setTypeface(tf);
        bindDel(holder, shortcut, holder.del);
        bindBg(holder, holder.bg);
    }

    private void bindBg(RecyclerView.ViewHolder holder, View bg) {
        bg.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    onItemClickListener.onClick(v, holder.getAdapterPosition());
                }
            }
        });
        bg.setOnLongClickListener(v -> {
            if (onItemClickListener != null) {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    onItemClickListener.onLongClick(v, holder.getAdapterPosition());
                }
            }
            return true;
        });
    }

    private void bindDel(RecyclerView.ViewHolder holder, Shortcut shortcut, View del) {
        if (shortcut.isDragging()) {
            del.setVisibility(VISIBLE);
        } else {
            del.setVisibility(View.GONE);
        }
        del.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    onItemClickListener.onDel(v, holder.getAdapterPosition());
                }
            }
        });
    }


    public MyDividerItem getDividerItem() {
        return dividerItem;
    }

    public static class MyDividerItem extends BaseDividerItem {

        @Override
        public int getLeftRight(int itemViewType) {
            return ShortcutTypeEnum.Companion.getLRByViewType(itemViewType);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private static class ItemHolder extends RecyclerView.ViewHolder {
        ImageView icon, del;
        TextView text, iconText;
        View shortcut_bg;

        ItemHolder(View view) {
            super(view);
            shortcut_bg = view.findViewById(R.id.shortcut_bg);
            icon = view.findViewById(R.id.shortcut_icon);
            text = view.findViewById(R.id.shortcut_text);
            del = view.findViewById(R.id.shortcut_del);
            iconText = view.findViewById(R.id.shortcut_icon_text);
        }
    }

    private static class PoetryHolder extends RecyclerView.ViewHolder {
        TextView text, author;
        ImageView del;
        View bg;
        CardView shortcut_bg;

        PoetryHolder(View view) {
            super(view);
            shortcut_bg = view.findViewById(R.id.shortcut_bg);
            bg = view.findViewById(R.id.clickBg);
            text = view.findViewById(R.id.textView);
            author = view.findViewById(R.id.descView);
            del = view.findViewById(R.id.shortcut_del);
        }
    }

    private static class DataHolder extends RecyclerView.ViewHolder {
        ImageView del, imageView;
        View shortcut_bg;
        CardView shortcut_bg1, shortcut_bg2;
        TextView textView1, textView2, textView3;

        DataHolder(View view) {
            super(view);
            shortcut_bg = view.findViewById(R.id.shortcut_bg);
            shortcut_bg1 = view.findViewById(R.id.shortcut_bg1);
            shortcut_bg2 = view.findViewById(R.id.shortcut_bg2);
            imageView = view.findViewById(R.id.imageView);
            del = view.findViewById(R.id.shortcut_del);
            textView1 = view.findViewById(R.id.textView1);
            textView2 = view.findViewById(R.id.textView2);
            textView3 = view.findViewById(R.id.textView3);
        }
    }
}
