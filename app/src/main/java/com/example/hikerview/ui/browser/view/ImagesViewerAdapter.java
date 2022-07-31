package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.ui.view.BaseDividerItem;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.ScreenUtil;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class ImagesViewerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;
    private MyDividerItem dividerItem;

    public List<String> getList() {
        return list;
    }

    private List<String> list;
    private OnClickListener clickListener;

    public ImagesViewerAdapter(Activity context, List<String> list, OnClickListener clickListener) {
        this.context = context;
        this.list = list;
        dividerItem = new MyDividerItem();
        this.clickListener = clickListener;
    }

    public MyDividerItem getDividerItem() {
        return dividerItem;
    }

    public void setDividerItem(MyDividerItem dividerItem) {
        this.dividerItem = dividerItem;
    }

    public interface OnClickListener {
        void click(ImageView view, int pos);

        void longClick(View view, int pos);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PicThreeHolder(LayoutInflater.from(context).inflate(R.layout.item_pic_three_col, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof PicThreeHolder) {
            PicThreeHolder holder = (PicThreeHolder) viewHolder;
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) holder.resultBg.getLayoutParams();
            int dp_5 = DisplayUtil.dpToPx(context, 6);
            lp.width = (ScreenUtil.getScreenWidth(context) - dp_5 / 2 * 4) / 3;
            lp.setMargins(0, dp_5 / 2, 0, 0);
            if (context != null) {
                lp.height = (ScreenUtil.getScreenWidth(context) - DisplayUtil.dpToPx(context, 40)) / 2;
            } else {
                lp.height = DisplayUtil.dpToPx(context, 150);
            }
            holder.resultBg.setLayoutParams(lp);
            Glide.with(context)
                    .asDrawable()
                    .load(list.get(position))
                    .apply(new RequestOptions().placeholder(new ColorDrawable(context.getResources().getColor(R.color.gray_rice))))
                    .into(holder.img);
            holder.resultBg.setOnClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.click(holder.img, holder.getAdapterPosition());
                }
            });
            holder.resultBg.setOnLongClickListener(v -> {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < list.size()) {
                    clickListener.longClick(v, holder.getAdapterPosition());
                }
                return true;
            });
        }
    }


    public class MyDividerItem extends BaseDividerItem {

        @Override
        public int getLeftRight(int itemViewType) {
            return DisplayUtil.dpToPx(context, 3);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private static class PicThreeHolder extends RecyclerView.ViewHolder {
        ImageView img;
        View resultBg;

        PicThreeHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.item_reult_img);
            resultBg = itemView.findViewById(R.id.result_bg);
        }
    }
}
