package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.constants.ArticleColTypeEnum;
import com.example.hikerview.ui.browser.model.IconTitle;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.view.popup.MenuViewAdapter;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class MenuPopup extends BottomPopupView {

    private Activity activity;
    private RecyclerView recyclerView;
    private List<IconTitle> iconTitles;
    private OnItemClickListener onItemClickListener;
    private MenuViewAdapter adapter;

    public MenuPopup(@NonNull Context context) {
        super(context);
    }

    public MenuPopup(@NonNull Activity activity, OnItemClickListener onItemClickListener) {
        super(activity);
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_web_menu_popup;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        ImageView imageView = findViewById(R.id.imageView);
        Glide.with(activity)
                .load(activity.getResources().getDrawable(R.drawable.account_home))
                .apply(new RequestOptions().placeholder(new ColorDrawable(activity.getResources().getColor(R.color.gray_rice)))
                        .transform(new CircleCrop()))
                .into(imageView);

        recyclerView = findViewById(R.id.recyclerView);
        iconTitles = new ArrayList<>();
        iconTitles.add(new IconTitle(R.drawable.home_collection, "加入收藏"));
        iconTitles.add(new IconTitle(R.drawable.home_bookmark, "加入书签"));
        iconTitles.add(new IconTitle(R.drawable.home_bookmark, "书签收藏"));
        iconTitles.add(new IconTitle(R.drawable.home_net_history, "网络日志"));
        iconTitles.add(new IconTitle(R.drawable.main_menu_go, "分享链接"));
        iconTitles.add(new IconTitle(R.drawable.home_ua, "设置UA"));
        iconTitles.add(new IconTitle(R.drawable.home_block, "元素拦截"));
        iconTitles.add(new IconTitle(R.drawable.home_code, "查看源码"));
        iconTitles.add(new IconTitle(R.drawable.home_xiu_tan, "嗅探设置"));
        iconTitles.add(new IconTitle(R.drawable.home_refresh, "刷新页面"));
        iconTitles.add(new IconTitle(R.drawable.home_search, "页内查找"));
        iconTitles.add(new IconTitle(R.drawable.home_translate, "网页翻译"));
        iconTitles.add(new IconTitle(R.drawable.home_history, SettingConfig.noWebHistory ? "关闭无痕" : "无痕模式"));


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 12);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter == null) {
                    return 12;
                }
                if (recyclerView.getAdapter() != adapter) {
                    return 12;
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

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .7f);
    }

    public interface OnItemClickListener {
        void onClick(IconTitle iconTitle);
    }

}
