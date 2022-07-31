package com.example.hikerview.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class CustomRecyclerViewPopup extends BottomPopupView {
    private List<String> data;
    private ClickListener clickListener;
    private String title;
    private int span = 2;
    private float height;
    private boolean dismissAfterClick = true;
    private CustomRecyclerViewAdapter adapter;

    public CustomRecyclerViewPopup with(String[] data, int span, ClickListener clickListener) {
        this.data = new ArrayList<>(Arrays.asList(data));
        this.clickListener = clickListener;
        this.span = span;
        return this;
    }

    public CustomRecyclerViewPopup with(List<String> data, int span, ClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
        this.span = span;
        return this;
    }

    public CustomRecyclerViewPopup withDismissAfterClick(boolean dismissAfterClick) {
        this.dismissAfterClick = dismissAfterClick;
        return this;
    }

    public CustomRecyclerViewPopup withTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomRecyclerViewPopup height(float height) {
        this.height = height;
        return this;
    }

    public CustomRecyclerViewPopup dismissAfterClick(boolean dismissAfterClick) {
        this.dismissAfterClick = dismissAfterClick;
        return this;
    }

    public void updateData(List<String> data) {
        this.data.clear();
        this.data.addAll(data);
        adapter.notifyDataSetChanged();
    }


    public CustomRecyclerViewPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_xiu_tan_result;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (StringUtil.isNotEmpty(title)) {
            TextView titleView = findViewById(R.id.title);
            titleView.setText(title);
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), span);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new CustomRecyclerViewAdapter(getContext(), data, new CustomRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (dismissAfterClick) {
                    dismissWith(() -> clickListener.click(data.get(position), position));
                } else {
                    clickListener.click(data.get(position), position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (dismissAfterClick) {
                    dismissWith(() -> clickListener.onLongClick(data.get(position), position));
                } else {
                    clickListener.onLongClick(data.get(position), position);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * (height > 0 ? height : .85f));
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * (height > 0 ? height : .75f));
    }

    public void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    public interface ClickListener {
        void click(String url, int position);

        void onLongClick(String url, int position);
    }
}
