package com.example.hikerview.ui.rules;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.constants.ArticleColTypeEnum;
import com.example.hikerview.event.OnSubRefreshEvent;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.home.FilmListActivity;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.home.model.ArticleListRuleJO;
import com.example.hikerview.ui.rules.model.SubscribeRecord;
import com.example.hikerview.ui.rules.service.HomeRulesSubService;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.lxj.xpopup.util.XPopupUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class RulesSubscribePopup extends BottomPopupView {

    private List<SubscribeRecord> data = new ArrayList<>();
    private RulesSubscribeAdapter adapter;

    public RulesSubscribePopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_rules_sub;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubRefresh(OnSubRefreshEvent event) {
        if (adapter != null) {
            data.clear();
            data.addAll(HomeRulesSubService.getSubRecords());
            adapter.notifyDataSetChanged();
        }
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        findViewById(R.id.add).setOnClickListener(v -> {
            HomeRulesSubService.addSubWithPopup(getContext(), null, null, ok -> {
                if (ok) {
                    data.clear();
                    data.addAll(HomeRulesSubService.getSubRecords());
                    adapter.notifyDataSetChanged();
                    ToastMgr.shortBottomCenter(getContext(), "已添加订阅");
                }
            });
        });
        findViewById(R.id.problem).setOnClickListener(v -> {
            new XPopup.Builder(getContext())
                    .asConfirm("操作指南", "该订阅地址即合集规则地址，软件会定时（隔一天）去拉取所有规则，根据合集中所有规则的版本与本地规则进行比较，" +
                            "本地不存在的规则直接新增，同名同作者规则当版本有所更新的，软件将会后台覆盖更新，同名不同作者的规则不会覆盖更新，更新时将不会更新分组和标题颜色", () -> {
                    })
                    .show();
        });

        findViewById(R.id.menu).setOnClickListener(v -> {
            boolean homeRuleSubSilence = PreferenceMgr.getBoolean(getContext(), "subscribe", "homeRuleSubSilence", false);
            new XPopup.Builder(getContext())
                    .asBottomList("更新时是否提示", new String[]{"静默更新无提示", "更新时提示规则数"}, null, homeRuleSubSilence ? 0 : 1, new OnSelectListener() {
                        @Override
                        public void onSelect(int position, String text) {
                            PreferenceMgr.put(getContext(), "subscribe", "homeRuleSubSilence", position == 0);
                            ToastMgr.shortBottomCenter(getContext(), "已设置为" + text);
                        }
                    }).show();
        });

        data.addAll(HomeRulesSubService.getSubRecords());
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new RulesSubscribeAdapter(getContext(), data, new RulesSubscribeAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                onLongClick(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {
                SubscribeRecord record = data.get(position);
                String[] operations = record.isUse() ?
                        new String[]{"查看规则", "刷新订阅", "分享订阅", "更新策略", "停用订阅", "删除订阅"} :
                        new String[]{"查看规则", "分享订阅", "启用订阅", "更新策略", "删除订阅"};
                new XPopup.Builder(getContext())
                        .asCustom(new CustomCenterRecyclerViewPopup(getContext())
                                .with(operations, 2, new CustomCenterRecyclerViewPopup.ClickListener() {
                                    @Override
                                    public void click(String text, int p) {
                                        switch (text) {
                                            case "刷新订阅":
                                                record.setModifyDate(null);
                                                HomeRulesSubService.checkUpdateAsync(record);
                                                ToastMgr.shortBottomCenter(getContext(), "已提交刷新");
                                                break;
                                            case "查看规则":
                                                showRecord(record);
                                                break;
                                            case "分享订阅":
                                                AutoImportHelper.shareWithCommand(getContext(), record.getTitle() + "@@" + record.getUrl(), AutoImportHelper.HOME_SUB);
                                                break;
                                            case "更新策略":
                                                new XPopup.Builder(getContext())
                                                        .asBottomList("更新策略", new String[]{"同时新增和更新", "只更新已有规则"}, null, record.isOnlyUpdate() ? 1 : 0, true, (position1, text1) -> {
                                                            record.setOnlyUpdate(position1 == 1);
                                                            HomeRulesSubService.updateSubRecords(record);
                                                            adapter.notifyItemChanged(position);
                                                            ToastMgr.shortBottomCenter(getContext(), "更新策略已设置为" + text1);
                                                        }).show();
                                                break;
                                            case "停用订阅":
                                                record.setUse(false);
                                                HomeRulesSubService.updateSubRecords(record);
                                                adapter.notifyItemChanged(position);
                                                ToastMgr.shortBottomCenter(getContext(), "已停用订阅");
                                                break;
                                            case "启用订阅":
                                                record.setUse(true);
                                                HomeRulesSubService.updateSubRecords(record);
                                                HomeRulesSubService.checkUpdateAsync(record);
                                                data.clear();
                                                data.addAll(HomeRulesSubService.getSubRecords());
                                                adapter.notifyDataSetChanged();
                                                ToastMgr.shortBottomCenter(getContext(), "已启用订阅");
                                                break;
                                            case "删除订阅":
                                                HomeRulesSubService.removeSubRecords(data.get(position).getTitle());
                                                data.clear();
                                                data.addAll(HomeRulesSubService.getSubRecords());
                                                adapter.notifyDataSetChanged();
                                                ToastMgr.shortBottomCenter(getContext(), "已删除订阅");
                                                break;
                                        }
                                    }

                                    @Override
                                    public void onLongClick(String url, int position) {

                                    }
                                }).withTitle("请选择操作"))
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showRecord(SubscribeRecord record) {
        ArticleListRule articleListRule1 = new ArticleListRule();
        articleListRule1.setUrl("hiker://empty");
        JSEngine.getInstance().putVar("rulesImportUrl", record.getUrl());
        articleListRule1.setFind_rule(getSubDetailRule());
        articleListRule1.setCol_type(ArticleColTypeEnum.TEXT_3.getCode());
        articleListRule1.setTitle(record.getTitle());
        Intent intent = new Intent(getContext(), FilmListActivity.class);
        intent.putExtra("data", JSON.toJSONString(articleListRule1));
        intent.putExtra("title", record.getTitle());
        getContext().startActivity(intent);
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .75f);
    }

    @Override
    public void dismiss() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.dismiss();
    }

    private String getSubDetailRule() {
        String rule = FilesInAppUtil.getAssetsString(getContext(), "homeSubView.json");
        return JSON.parseObject(rule, ArticleListRuleJO.class).getDetail_find_rule();
    }
}
