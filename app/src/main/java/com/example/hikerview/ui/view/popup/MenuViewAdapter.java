package com.example.hikerview.ui.view.popup;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.function.Consumer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.constants.ArticleColTypeEnum;
import com.example.hikerview.ui.browser.data.CardCol4Data;
import com.example.hikerview.ui.browser.data.CardMultiData;
import com.example.hikerview.ui.browser.model.IconTitle;
import com.example.hikerview.ui.view.BaseDividerItem;
import com.example.hikerview.ui.view.DrawableTextView;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.ScreenUtil;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class MenuViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private WeakReference<Activity> weakReference;
    private Context context;
    private List<IconTitle> list;
    private OnItemClickListener onItemClickListener;
    private RequestOptions options;
    private MyDividerItem dividerItem;

    public MenuViewAdapter(Activity context, List<IconTitle> list, OnItemClickListener onItemClickListener) {
        this.weakReference = new WeakReference<>(context);
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
        options = new RequestOptions().placeholder(new ColorDrawable(context.getResources().getColor(R.color.gray_rice)));
        dividerItem = new MyDividerItem();
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public MyDividerItem getDividerItem() {
        return dividerItem;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ArticleColTypeEnum.ICON_2.getItemType()) {
            return new IconTwoHolder(LayoutInflater.from(context).inflate(R.layout.item_icon_two_small_col, parent, false));
        } else if (viewType == ArticleColTypeEnum.TEXT_2.getItemType()) {
            return new ChannelHolder(LayoutInflater.from(context).inflate(R.layout.item_card_rect, parent, false));
        } else if (viewType == ArticleColTypeEnum.TEXT_3.getItemType()) {
            return new TextThreeHolder(LayoutInflater.from(context).inflate(R.layout.item_card_rect, parent, false));
        } else if (viewType == ArticleColTypeEnum.TEXT_4.getItemType()) {
            return new TextFourHolder(LayoutInflater.from(context).inflate(R.layout.item_card_rect, parent, false));
        } else if (viewType == ArticleColTypeEnum.CARD_MULTI.getItemType()) {
            return new CardMultiHolder(LayoutInflater.from(context).inflate(R.layout.item_card_multi, parent, false));
        } else if (viewType == ArticleColTypeEnum.CARD_COL_4_2.getItemType()) {
            return new CardCol42Holder(LayoutInflater.from(context).inflate(R.layout.item_card_col_4_2, parent, false));
        } else if (viewType == ArticleColTypeEnum.CARD_COL_4.getItemType()) {
            return new CardCol4Holder(LayoutInflater.from(context).inflate(R.layout.item_card_col_4, parent, false));
        } else if (viewType == ArticleColTypeEnum.CARD_COL_3.getItemType()) {
            return new CardCol3Holder(LayoutInflater.from(context).inflate(R.layout.item_card_col_3, parent, false));
        } else if (viewType == ArticleColTypeEnum.BIG_BLANK_BLOCK.getItemType()) {
            return new BlankBlockHolder(LayoutInflater.from(context).inflate(R.layout.item_big_blank_block, parent, false));
        } else {
            return new ChannelHolder(LayoutInflater.from(context).inflate(R.layout.item_card_rect, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof IconTwoHolder) {
            final IconTwoHolder holder = (IconTwoHolder) viewHolder;
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.resultBg.getLayoutParams();
            int dp_5 = DisplayUtil.dpToPx(context, 5);
            lp.width = (ScreenUtil.getScreenWidth(weakReference.get()) - dp_5 * 2 * 3) / 2;
            lp.setMargins(0, dp_5, 0, dp_5);
            holder.resultBg.setLayoutParams(lp);
            holder.textView.setText(list.get(position).getTitle());
            Glide.with(context)
                    .load(context.getResources().getDrawable(list.get(position).getIcon()))
                    .apply(options)
                    .into(holder.imageView);
            bindBgClick(holder, holder.resultBg);
        } else if (viewHolder instanceof CardMultiHolder) {
            final CardMultiHolder holder = (CardMultiHolder) viewHolder;
            holder.title.setText(list.get(position).getTitle());
            holder.desc.setText(list.get(position).getDesc());
            Glide.with(context)
                    .load(context.getResources().getDrawable(list.get(position).getIcon()))
                    .apply(new RequestOptions().placeholder(new ColorDrawable(context.getResources().getColor(R.color.gray_rice)))
                            .transform(new CircleCrop())
                    ).into(holder.imageView);
            bindBgClick(holder, holder.resultBg);

            String extraParam = list.get(position).getExtraParam();
            CardMultiData cardMultiData = JSON.parseObject(extraParam, CardMultiData.class);
            holder.textView1.setText(cardMultiData.getOperation1());
            holder.textView2.setText(cardMultiData.getOperation2());
            Glide.with(context)
                    .load(context.getResources().getDrawable(cardMultiData.getOpIcon1()))
                    .apply(options)
                    .into(holder.imageView1);
            Glide.with(context)
                    .load(context.getResources().getDrawable(cardMultiData.getOpIcon2()))
                    .apply(options)
                    .into(holder.imageView2);
            bindExtraClick(holder, holder.clickBg1, cardMultiData.getOperation1(), list.get(position).getExtraConsumer());
            bindExtraClick(holder, holder.clickBg2, cardMultiData.getOperation2(), list.get(position).getExtraConsumer());
        } else if (viewHolder instanceof ChannelHolder) {
            bindForChannelHolder(viewHolder, position);
        } else if (viewHolder instanceof TextThreeHolder) {
            bindForTextThreeHolder(viewHolder, position);
        } else if (viewHolder instanceof TextFourHolder) {
            bindForTextFourHolder(viewHolder, position);
        } else if (viewHolder instanceof CardCol42Holder) {
            final CardCol42Holder holder = (CardCol42Holder) viewHolder;
            String extraParam = list.get(position).getExtraParam();
            List<CardCol4Data> data = JSON.parseArray(extraParam, CardCol4Data.class);
            for (int i = 0; i < data.size(); i++) {
                CardCol4Data datum = data.get(i);
                holder.views[i].setText(datum.getOperation());
                Glide.with(context)
                        .load(context.getResources().getDrawable(datum.getIcon()))
                        .apply(options)
                        .into(holder.views[i].getImageView());
                bindExtraClick(holder, holder.views[i], datum.getOperation(), list.get(position).getExtraConsumer());
            }
        } else if (viewHolder instanceof CardCol4Holder) {
            final CardCol4Holder holder = (CardCol4Holder) viewHolder;
            String extraParam = list.get(position).getExtraParam();
            List<CardCol4Data> data = JSON.parseArray(extraParam, CardCol4Data.class);
            for (int i = 0; i < data.size(); i++) {
                CardCol4Data datum = data.get(i);
                holder.views[i].setText(datum.getOperation());
                Glide.with(context)
                        .load(context.getResources().getDrawable(datum.getIcon()))
                        .apply(options)
                        .into(holder.views[i].getImageView());
                bindExtraClick(holder, holder.views[i], datum.getOperation(), list.get(position).getExtraConsumer());
            }
        } else if (viewHolder instanceof CardCol3Holder) {
            final CardCol3Holder holder = (CardCol3Holder) viewHolder;
            String extraParam = list.get(position).getExtraParam();
            List<CardCol4Data> data = JSON.parseArray(extraParam, CardCol4Data.class);
            for (int i = 0; i < data.size(); i++) {
                CardCol4Data datum = data.get(i);
                holder.views[i].setText(datum.getOperation());
                Glide.with(context)
                        .load(context.getResources().getDrawable(datum.getIcon()))
                        .apply(options)
                        .into(holder.views[i].getImageView());
                bindExtraClick(holder, holder.views[i], datum.getOperation(), list.get(position).getExtraConsumer());
            }
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ArticleColTypeEnum.getItemTypeByCode(list.get(position).getColTypeEnum().getCode());
    }

    private void bindBgClick(RecyclerView.ViewHolder holder, View resultBg) {
        resultBg.setOnClickListener(view -> {
            try {
                if (holder.getAdapterPosition() >= 0) {
                    onItemClickListener.onClick(view, holder.getAdapterPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        resultBg.setOnLongClickListener(view -> {
            try {
                if (holder.getAdapterPosition() >= 0) {
                    onItemClickListener.onLongClick(view, holder.getAdapterPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });
    }

    private void bindExtraClick(RecyclerView.ViewHolder holder, View resultBg, String extraKey, Consumer<String> extraConsumer) {
        resultBg.setOnClickListener(view -> {
            try {
                if (holder.getAdapterPosition() >= 0) {
                    extraConsumer.accept(extraKey);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void bindForChannelHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final ChannelHolder holder = (ChannelHolder) viewHolder;
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.resultBg.getLayoutParams();
        int dp_5 = DisplayUtil.dpToPx(context, 5);
        lp.width = (ScreenUtil.getScreenWidth(weakReference.get()) - dp_5 * 2 * 3) / 2;
        lp.setMargins(0, dp_5, 0, dp_5);
        holder.resultBg.setLayoutParams(lp);
        holder.title.setText(list.get(position).getTitle());
        bindBgClick(viewHolder, holder.resultBg);
    }

    private void bindForTextThreeHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final TextThreeHolder holder = (TextThreeHolder) viewHolder;
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.resultBg.getLayoutParams();
        int dp_5 = DisplayUtil.dpToPx(context, 5);
        lp.width = (ScreenUtil.getScreenWidth(weakReference.get()) - dp_5 * 2 * 4) / 3;
        lp.setMargins(0, dp_5, 0, dp_5);
        holder.resultBg.setLayoutParams(lp);
        holder.title.setText(list.get(position).getTitle());
        bindBgClick(viewHolder, holder.resultBg);
    }

    private void bindForTextFourHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final TextFourHolder holder = (TextFourHolder) viewHolder;
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.resultBg.getLayoutParams();
        int dp_5 = DisplayUtil.dpToPx(context, 5);
        lp.width = (ScreenUtil.getScreenWidth(weakReference.get()) - dp_5 * 2 * 5) / 4;
        lp.setMargins(0, dp_5, 0, dp_5);
        holder.resultBg.setLayoutParams(lp);
        holder.title.setText(list.get(position).getTitle());
        bindBgClick(viewHolder, holder.resultBg);
    }

    private static class IconTwoHolder extends RecyclerView.ViewHolder {
        View resultBg, clickBg;
        ImageView imageView;
        TextView textView;

        IconTwoHolder(View itemView) {
            super(itemView);
            resultBg = itemView.findViewById(R.id.bg);
            clickBg = itemView.findViewById(R.id.clickBg);
            imageView = itemView.findViewById(R.id.item_reult_img);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    private static class ChannelHolder extends RecyclerView.ViewHolder {
        TextView title;
        View resultBg;

        ChannelHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_chapter_jishu);
            resultBg = itemView.findViewById(R.id.bg);
        }
    }

    private static class TextThreeHolder extends RecyclerView.ViewHolder {
        TextView title;
        View resultBg;

        TextThreeHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_chapter_jishu);
            resultBg = itemView.findViewById(R.id.bg);
        }
    }

    private static class TextFourHolder extends RecyclerView.ViewHolder {
        TextView title;
        View resultBg;

        TextFourHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_chapter_jishu);
            resultBg = itemView.findViewById(R.id.bg);
        }
    }

    private static class CardMultiHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        ImageView imageView;
        View resultBg;
        View bg;
        View clickBg1, clickBg2;
        ImageView imageView1, imageView2;
        TextView textView1, textView2;

        CardMultiHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textView);
            desc = itemView.findViewById(R.id.descView);
            imageView = itemView.findViewById(R.id.item_reult_img);
            resultBg = itemView.findViewById(R.id.clickBg);
            bg = itemView.findViewById(R.id.bg);

            clickBg1 = itemView.findViewById(R.id.clickBg1);
            imageView1 = itemView.findViewById(R.id.item_reult_img1);
            textView1 = itemView.findViewById(R.id.textView1);

            clickBg2 = itemView.findViewById(R.id.clickBg2);
            imageView2 = itemView.findViewById(R.id.item_reult_img2);
            textView2 = itemView.findViewById(R.id.textView2);
        }
    }


    private static class CardCol4Holder extends RecyclerView.ViewHolder {
        DrawableTextView[] views = new DrawableTextView[8];

        CardCol4Holder(View itemView) {
            super(itemView);
            views[0] = itemView.findViewById(R.id.dt1);
            views[1] = itemView.findViewById(R.id.dt2);
            views[2] = itemView.findViewById(R.id.dt3);
            views[3] = itemView.findViewById(R.id.dt4);
        }
    }

    private static class CardCol3Holder extends RecyclerView.ViewHolder {
        DrawableTextView[] views = new DrawableTextView[3];

        CardCol3Holder(View itemView) {
            super(itemView);
            views[0] = itemView.findViewById(R.id.dt1);
            views[1] = itemView.findViewById(R.id.dt2);
            views[2] = itemView.findViewById(R.id.dt3);
        }
    }

    private static class CardCol42Holder extends RecyclerView.ViewHolder {
        DrawableTextView[] views = new DrawableTextView[8];

        CardCol42Holder(View itemView) {
            super(itemView);
            views[0] = itemView.findViewById(R.id.dt1);
            views[1] = itemView.findViewById(R.id.dt2);
            views[2] = itemView.findViewById(R.id.dt3);
            views[3] = itemView.findViewById(R.id.dt4);
            views[4] = itemView.findViewById(R.id.dt5);
            views[5] = itemView.findViewById(R.id.dt6);
            views[6] = itemView.findViewById(R.id.dt7);
            views[7] = itemView.findViewById(R.id.dt8);
        }
    }

    private static class BlankBlockHolder extends RecyclerView.ViewHolder {

        BlankBlockHolder(View itemView) {
            super(itemView);
        }
    }

    public class MyDividerItem extends BaseDividerItem {

        @Override
        public int getLeftRight(int itemViewType) {
            return DisplayUtil.dpToPx(context, ArticleColTypeEnum.getLeftRightByItemType(itemViewType));
        }
    }

}
