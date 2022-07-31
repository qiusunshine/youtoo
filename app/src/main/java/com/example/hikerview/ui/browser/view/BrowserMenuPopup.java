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
import com.example.hikerview.ui.browser.data.CardMultiData;
import com.example.hikerview.ui.browser.model.IconTitle;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.view.popup.MenuViewAdapter;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.core.BottomPopupView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class BrowserMenuPopup extends BottomPopupView {

    private Activity activity;
    private RecyclerView recyclerView;
    private List<IconTitle> iconTitles;
    private OnItemClickListener onItemClickListener;
    private MenuViewAdapter adapter;

    public BrowserMenuPopup(@NonNull Context context) {
        super(context);
    }

    public BrowserMenuPopup(@NonNull Activity activity, OnItemClickListener onItemClickListener) {
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
        IconTitle iconTitle = new IconTitle(R.drawable.account_home, "书签", "查看、编辑、管理书签", ArticleColTypeEnum.CARD_MULTI);
        CardMultiData cardMultiData = new CardMultiData("加入书签", R.drawable.icon_browser_bookmark, "历史记录", R.drawable.icon_browser_history);
        iconTitle.setExtraParam(JSON.toJSONString(cardMultiData));
        iconTitle.setExtraConsumer(op -> {
            dismissWith(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(new IconTitle(op, null));
                }
            });
        });
        iconTitles.add(iconTitle);


        IconTitle iconTitle2 = new IconTitle(-1, null, null, ArticleColTypeEnum.CARD_COL_4);
        List<CardCol4Data> data1 = new ArrayList<>();

        data1.add(new CardCol4Data("网络日志", R.drawable.icon_his));
        data1.add(new CardCol4Data("查看源码", R.drawable.icon_code));
        data1.add(new CardCol4Data("视频嗅探", R.drawable.icon_xiutan));
        data1.add(new CardCol4Data("工具箱", R.drawable.icon_box));

        iconTitle2.setExtraParam(JSON.toJSONString(data1));
        iconTitle2.setExtraConsumer(op -> {
            dismissWith(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(new IconTitle(op, null));
                }
            });
        });
        iconTitles.add(iconTitle2);


        IconTitle iconTitle1 = new IconTitle(-1, null, null, ArticleColTypeEnum.CARD_COL_4_2);
        List<CardCol4Data> data = new ArrayList<>();

        data.add(new CardCol4Data("设置UA", R.drawable.icon_ua));
        data.add(new CardCol4Data("分享链接", R.drawable.icon_share_green));
        data.add(new CardCol4Data("插件管理", R.drawable.icon_plugin));
        data.add(new CardCol4Data("下载管理", R.drawable.icon_download));
        data.add(new CardCol4Data("无图模式", R.drawable.icon_filter));
        data.add(new CardCol4Data(SettingConfig.noWebHistory ? "关闭无痕" : "无痕模式", R.drawable.icon_pure));
        data.add(new CardCol4Data("退出软件", R.drawable.icon_exit));
        data.add(new CardCol4Data("更多设置", R.drawable.icon_setting));

        iconTitle1.setExtraParam(JSON.toJSONString(data));
        iconTitle1.setExtraConsumer(op -> {
            dismissWith(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(new IconTitle(op, null));
                }
            });
        });
        iconTitles.add(iconTitle1);


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

    @Override
    public BasePopupView show() {
        if (CollectionUtil.isNotEmpty(iconTitles) && adapter != null) {
            for (int i = 0; i < iconTitles.size(); i++) {
                IconTitle iconTitle = iconTitles.get(i);
                if ("无痕模式".equals(iconTitle.getTitle()) || "关闭无痕".equals(iconTitle.getTitle())) {
                    if (SettingConfig.noWebHistory && "无痕模式".equals(iconTitle.getTitle())) {
                        iconTitle.setTitle("关闭无痕");
                        adapter.notifyItemChanged(i);
                    } else if (!SettingConfig.noWebHistory && "关闭无痕".equals(iconTitle.getTitle())) {
                        iconTitle.setTitle("无痕模式");
                        adapter.notifyItemChanged(i);
                    }
                    break;
                }
            }
        }
        return super.show();
    }

    public interface OnItemClickListener {
        void onClick(IconTitle iconTitle);
    }

    public void notifyInBookmarkItem() {
        if (CollectionUtil.isEmpty(iconTitles)) {
            return;
        }
        IconTitle iconTitle = iconTitles.get(0);
        CardMultiData cardMultiData = new CardMultiData("移除书签", R.drawable.icon_browser_bookmark, "历史记录", R.drawable.icon_browser_history);
        iconTitle.setExtraParam(JSON.toJSONString(cardMultiData));
        iconTitle.setExtraConsumer(op -> {
            dismissWith(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(new IconTitle(op, null));
                }
            });
        });
        adapter.notifyItemChanged(0);
    }
}
