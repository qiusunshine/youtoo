package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.constants.ArticleColTypeEnum;
import com.example.hikerview.ui.browser.data.CardCol4Data;
import com.example.hikerview.ui.browser.model.IconTitle;
import com.example.hikerview.ui.view.popup.MenuViewAdapter;
import com.lxj.xpopup.core.BottomPopupView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class BrowserSubMenuPopup extends BottomPopupView {

    private Activity activity;
    private RecyclerView recyclerView;
    private List<IconTitle> iconTitles;
    private OnItemClickListener onItemClickListener;
    private MenuViewAdapter adapter;

    public BrowserSubMenuPopup(@NonNull Context context) {
        super(context);
    }

    public BrowserSubMenuPopup(@NonNull Activity activity, OnItemClickListener onItemClickListener) {
        super(activity);
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_browser_menu_popup;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();

        recyclerView = findViewById(R.id.recyclerView);
        iconTitles = new ArrayList<>();

        IconTitle iconTitle2 = new IconTitle(-1, null, null, ArticleColTypeEnum.CARD_COL_3);
        List<CardCol4Data> data1 = new ArrayList<>();
        data1.add(new CardCol4Data("页内查找", R.drawable.icon_find));
        data1.add(new CardCol4Data("开发调试", R.drawable.icon_dev));
        data1.add(new CardCol4Data("网页翻译", R.drawable.icon_trans));

        iconTitle2.setExtraParam(JSON.toJSONString(data1));
        iconTitle2.setExtraConsumer(op -> {
            dismissWith(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(new IconTitle(op, null));
                }
            });
        });
        iconTitles.add(iconTitle2);


        IconTitle iconTitle3 = new IconTitle(-1, null, null, ArticleColTypeEnum.CARD_COL_3);
        List<CardCol4Data> data2 = new ArrayList<>();
        data2.add(new CardCol4Data("保存网页", R.drawable.icon_save));
        data2.add(new CardCol4Data("离线页面", R.drawable.icon_offline));
        data2.add(new CardCol4Data("全屏模式", R.drawable.icon_eye));

        iconTitle3.setExtraParam(JSON.toJSONString(data2));
        iconTitle3.setExtraConsumer(op -> {
            dismissWith(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(new IconTitle(op, null));
                }
            });
        });
        iconTitles.add(iconTitle3);


        iconTitles.add(new IconTitle(null, ArticleColTypeEnum.BIG_BLANK_BLOCK));
        iconTitles.add(new IconTitle(null, ArticleColTypeEnum.BIG_BLANK_BLOCK));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 60);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter == null) {
                    return 60;
                }
                if (recyclerView.getAdapter() != adapter) {
                    return 60;
                }
                return ArticleColTypeEnum.getSpanCountByItemType(adapter.getItemViewType(position));
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MenuViewAdapter(activity, iconTitles, new MenuViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                dismissWith(() -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(iconTitles.get(position));
                    }
                });
            }

            @Override
            public void onLongClick(View view, int position) {
                dismiss();
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(adapter.getDividerItem());
    }

    public interface OnItemClickListener {
        void onClick(IconTitle iconTitle);
    }
}
