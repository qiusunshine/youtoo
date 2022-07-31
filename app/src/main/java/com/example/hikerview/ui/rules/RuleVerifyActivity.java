package com.example.hikerview.ui.rules;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.event.OpenHomeRulesActivityEvent;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseSlideActivity;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.ArticleListRuleAdapter;
import com.example.hikerview.ui.home.enums.HomeActionEnum;
import com.example.hikerview.ui.home.model.ArticleList;
import com.example.hikerview.ui.home.model.ArticleListModel;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.home.model.SearchResult;
import com.example.hikerview.ui.search.model.SearchModel;
import com.example.hikerview.ui.view.CustomRecyclerViewPopup;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.WebUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作者：By 15968
 * 日期：On 2020/7/26
 * 时间：At 14:54
 */

public class RuleVerifyActivity extends BaseSlideActivity {
    private static final String TAG = "RuleVerifyActivity";
    private static final int ERROR_URL = 1;
    private static final int NO_ERROR = -1;
    private static final int ERROR_RULE = 2;

    private TextView dropDownMenu, groupDownMenu;
    private Button start_btn, del_btn, rename_btn;
    private RadioButton url_error, rule_error;
    private int checkThreadNum = 20;
    private boolean urlError = true;
    private AtomicBoolean loading = new AtomicBoolean(false);
    private List<ArticleListRule> articleListRules = Collections.synchronizedList(new ArrayList<>());
    private List<ArticleListRule> urlErrorRules = Collections.synchronizedList(new ArrayList<>());
    private List<ArticleListRule> ruleErrorRules = Collections.synchronizedList(new ArrayList<>());
    private LoadingPopupView loadingPopupView;
    private ArticleListRuleAdapter adapter;
    private List<ArticleListRule> allRules = new ArrayList<>();
    private AtomicInteger checkingSize = new AtomicInteger(0);
    private AtomicInteger loadingSize = new AtomicInteger(0);
    private RecyclerView recyclerView;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private LinkedBlockingDeque<Runnable> taskQueue = new LinkedBlockingDeque<>(8192);
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private ExecutorService executorService = new ThreadPoolExecutor(CORE_POOL_SIZE, 64,
            10L, TimeUnit.SECONDS, taskQueue);
    private String selectedGroup = "全部";

    @Override
    protected View getBackgroundView() {
        return findView(R.id.ad_list_window);
    }
    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activit_rules_verify;
    }

    @Override
    protected void initView2() {
        recyclerView = findView(R.id.ad_list_recycler_view);
        dropDownMenu = findView(R.id.dropDownMenu);
        groupDownMenu = findView(R.id.groupDropDownMenu);
        start_btn = findView(R.id.start_btn);
        del_btn = findView(R.id.del_btn);
        rename_btn = findView(R.id.rename_btn);
        url_error = findView(R.id.url_error);
        rule_error = findView(R.id.rule_error);

        dropDownMenu.setOnClickListener(v -> {
            if (loading.get()) {
                ToastMgr.shortBottomCenter(getContext(), "检测中无法修改线程数");
            } else {
                new XPopup.Builder(getContext()).asCenterList("选择检测线程数", new String[]{"3", "8", "16", "20", "32", "64"}, (position, text) -> {
                    checkThreadNum = Integer.parseInt(text);
                    dropDownMenu.setText(("线程数：" + checkThreadNum));
                    ToastMgr.shortBottomCenter(getContext(), "检测线程数已修改为" + checkThreadNum);
                }).show();
            }
        });

        groupDownMenu.setOnClickListener(v -> {
            if (loading.get()) {
                ToastMgr.shortBottomCenter(getContext(), "检测中无法修改分组");
            } else {
                LitePal.findAllAsync(ArticleListRule.class).listen(list -> {
                    if (CollectionUtil.isNotEmpty(list)) {
                        List<String> groups = new ArrayList<>(Stream.of(list).map(ArticleListRule::getGroup).filter(StringUtil::isNotEmpty).collect(Collectors.toSet()));
                        Collections.sort(groups);
                        groups.add(0, "全部");
                        CustomRecyclerViewPopup popup = new CustomRecyclerViewPopup(getContext())
                                .withTitle("请选择标签/分组")
                                .with(groups, 2, new CustomRecyclerViewPopup.ClickListener() {
                                    @Override
                                    public void click(String s, int position) {
                                        selectedGroup = s;
                                        groupDownMenu.setText(("检测分组：" + selectedGroup));
                                        ToastMgr.shortBottomCenter(getContext(), "检测分组已修改为" + selectedGroup);
                                    }

                                    @Override
                                    public void onLongClick(String url, int position) {
                                    }
                                });
                        new XPopup.Builder(getContext())
                                .asCustom(popup)
                                .show();
                    }
                });
            }
        });

        start_btn.setOnClickListener(v -> {
            if (loading.get()) {
                new XPopup.Builder(getContext())
                        .asConfirm("温馨提示", "当前正在检测中，是否确认暂停？暂停后无法继续检测，只能从头开始，是否继续暂停？", () -> {
                            loading.set(false);
                            start_btn.setText("开始检测");
                        }).show();
            } else {
                startCheck();
            }
        });

        del_btn.setOnClickListener(v -> {
            if (loading.get()) {
                ToastMgr.shortBottomCenter(getContext(), "检测中无法删除规则");
            } else {
                new XPopup.Builder(getContext())
                        .asConfirm("温馨提示", "是否删除选中的" + articleListRules.size() + "条" + (urlError ? "网址无法访问" : "仅规则失效") + "的规则？", () -> {
                            if (loadingPopupView == null) {
                                loadingPopupView = new XPopup.Builder(getContext()).asLoading("正在删除规则，请稍候");
                            }
                            loadingPopupView.show();
                            executeNewTask(() -> {
                                for (ArticleListRule articleListRule : articleListRules) {
                                    articleListRule.delete();
                                }
                                articleListRules.clear();
                                if (urlError) {
                                    urlErrorRules.clear();
                                } else {
                                    ruleErrorRules.clear();
                                }
                                if (!isFinishing()) {
                                    runOnUiThread(() -> {
                                        loadingPopupView.dismiss();
                                        adapter.notifyDataSetChanged();
                                        ToastMgr.shortBottomCenter(getContext(), "规则已删除");
                                    });
                                }
                            });
                        }).show();
            }
        });
        rename_btn.setOnClickListener(v -> {
            if (loading.get()) {
                ToastMgr.shortBottomCenter(getContext(), "检测中无法重置分组");
            } else {
                String group = urlError ? "无法访问" : "已失效";
                new XPopup.Builder(getContext())
                        .asConfirm("温馨提示", "是否重命名选中的" + articleListRules.size() + "条" + (urlError ? "网址无法访问" : "仅规则失效")
                                + "的规则的分组为‘" + group + "’？", () -> {
                            if (loadingPopupView == null) {
                                loadingPopupView = new XPopup.Builder(getContext()).asLoading("正在重置分组，请稍候");
                            }
                            loadingPopupView.show();
                            executeNewTask(() -> {
                                for (ArticleListRule articleListRule : articleListRules) {
                                    articleListRule.setGroup(group);
                                    articleListRule.save();
                                }
                                if (!isFinishing()) {
                                    runOnUiThread(() -> {
                                        loadingPopupView.dismiss();
                                        adapter.notifyDataSetChanged();
                                        ToastMgr.shortBottomCenter(getContext(), "规则已重置分组");
                                    });
                                }
                            });
                        }).show();
            }
        });


        url_error.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rule_error.setChecked(!isChecked);
            urlError = isChecked;
            articleListRules.clear();
            articleListRules.addAll(urlError ? urlErrorRules : ruleErrorRules);
            adapter.notifyDataSetChanged();
        });
        rule_error.setOnCheckedChangeListener((buttonView, isChecked) -> {
            url_error.setChecked(!isChecked);
            urlError = !isChecked;
            articleListRules.clear();
            articleListRules.addAll(urlError ? urlErrorRules : ruleErrorRules);
            adapter.notifyDataSetChanged();
        });
        adapter = new ArticleListRuleAdapter(getContext(), articleListRules);
        adapter.setOnItemClickListener(new ArticleListRuleAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position >= 0 && position < articleListRules.size()) {
                    String url = articleListRules.get(position).getUrl();
                    if (StringUtil.isEmpty(url)) {
                        url = articleListRules.get(position).getSearch_url();
                    }
                    WebUtil.goWeb(getContext(), StringUtil.getBaseUrl(url));
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (position >= 0 && position < articleListRules.size()) {
                    new XPopup.Builder(getContext())
                            .asCenterList("选择操作", new String[]{"网站首页", "删除规则", "再次校验规则", "移出失效列表"}, (p, text) -> {
                                switch (text) {
                                    case "网站首页":
                                        String url = articleListRules.get(position).getUrl();
                                        if (StringUtil.isEmpty(url)) {
                                            url = articleListRules.get(position).getSearch_url();
                                        }
                                        WebUtil.goWeb(getContext(), StringUtil.getBaseUrl(url));
                                        break;
                                    case "删除规则":
                                        new XPopup.Builder(getContext())
                                                .asConfirm("温馨提示", "确定删除规则“" + articleListRules.get(position).getTitle() + "”？", new OnConfirmListener() {
                                                    @Override
                                                    public void onConfirm() {
                                                        articleListRules.get(position).delete();
                                                        articleListRules.remove(position);
                                                        adapter.notifyDataSetChanged();
                                                        if (urlError) {
                                                            urlErrorRules.remove(position);
                                                        } else {
                                                            ruleErrorRules.remove(position);
                                                        }
                                                        ToastMgr.shortBottomCenter(getContext(), "已删除规则");
                                                    }
                                                }).show();
                                        break;
                                    case "移出失效列表":
                                        articleListRules.remove(position);
                                        adapter.notifyDataSetChanged();
                                        if (urlError) {
                                            urlErrorRules.remove(position);
                                        } else {
                                            ruleErrorRules.remove(position);
                                        }
                                        ToastMgr.shortBottomCenter(getContext(), "已移出失效列表");
                                        break;
                                    case "再次校验规则":
                                        ArticleListRule articleListRule = null;
                                        try {
                                            articleListRule = articleListRules.get(position).clone();
                                        } catch (CloneNotSupportedException e) {
                                            e.printStackTrace();
                                        }
                                        if (articleListRule == null) {
                                            ToastMgr.shortBottomCenter(getContext(), "规则拷贝失败");
                                            return;
                                        }
                                        checkRule(articleListRule, new OnCheckFinished() {
                                            @Override
                                            public void finish(int errorType) {
                                                runOnUiThread(() -> {
                                                    if (errorType == NO_ERROR) {
                                                        ToastMgr.shortBottomCenter(getContext(), "校验完成，规则正常");
                                                    } else if (errorType == ERROR_URL) {
                                                        ToastMgr.shortBottomCenter(getContext(), "校验完成，频道地址无法访问");
                                                    } else {
                                                        ToastMgr.shortBottomCenter(getContext(), "校验完成，仅规则失效");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void justBreak() {

                                            }
                                        });
                                        break;
                                }
                            }).show();
                }
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    /**
     * 开始检测
     */
    private void startCheck() {
        LitePal.findAllAsync(ArticleListRule.class).listen(list -> {
            if (!"全部".equals(selectedGroup)) {
                list = Stream.of(list).filter(articleListRule -> selectedGroup.equals(articleListRule.getGroup())).collect(Collectors.toList());
            }
            allRules.clear();
            allRules.addAll(list);
            urlErrorRules.clear();
            ruleErrorRules.clear();
            articleListRules.clear();
            checkingSize.set(0);
            loadingSize.set(0);
            loading.set(true);
            if (loadingPopupView == null) {
                loadingPopupView = new XPopup.Builder(getContext()).asLoading("正在检测中");
            }
            start_btn.setText("检测中");
            loadingPopupView.show();
            loadingPopupView.setTitle("正在检测第1个规则");
            for (int i = 0; i < checkThreadNum && i < allRules.size(); i++) {
                checkingSize.addAndGet(1);
                ArticleListRule articleListRule = allRules.get(i);
                executeNewTask(new CheckRunnable(articleListRule));
            }
            loadingPopupView.setTitle("正在检测第" + checkingSize.get() + "个规则");
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private synchronized void pushCheckTask() {
        if (!loading.get()) {
            return;
        }
        checkingSize.addAndGet(1);
        if (checkingSize.get() >= allRules.size()) {
            if (loadingSize.get() <= 0) {
                finishAll();
            } else {
                runOnUiThread(() -> loadingPopupView.setTitle("还有最后" + loadingSize.get() + "个规则"));
            }
            return;
        }
        runOnUiThread(() -> loadingPopupView.setTitle("正在检测第" + checkingSize.get() + "个规则"));
        ArticleListRule articleListRule = allRules.get(checkingSize.get() - 1);
        try {
            executeNewTask(new CheckRunnable(articleListRule));
        } catch (Exception e) {
            e.printStackTrace();
            ToastMgr.shortBottomCenter(getContext(), "提交任务失败：" + e.getMessage());
        }
    }

    private void executeNewTask(Runnable command) {
//        Log.d(TAG, "executeNewTask: CPU_COUNT=" + CPU_COUNT + ", CORE_POOL_SIZE=" + CORE_POOL_SIZE);
        executorService.execute(command);
    }


    private void finishAll() {
        runOnUiThread(() -> {
            start_btn.setText("检测完毕");
            loading.set(false);
            loadingPopupView.dismiss();
            ToastMgr.longCenter(getContext(), "失效源检测完毕");
        });
    }

    /**
     * 检测完毕
     *
     * @param articleListRule
     * @param errorType
     */
    private synchronized void checkFinished(ArticleListRule articleListRule, int errorType) {
        loadingSize.decrementAndGet();
        if (isFinishing()) {
            return;
        }
        if (errorType == ERROR_URL) {
            urlErrorRules.add(articleListRule);
            if (urlError) {
                articleListRules.add(articleListRule);
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            }
        } else if (errorType == ERROR_RULE) {
            ruleErrorRules.add(articleListRule);
            if (!urlError) {
                articleListRules.add(articleListRule);
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            }
        }
        pushCheckTask();
    }

    @Override
    public void onBackPressed() {
        new XPopup.Builder(getContext())
                .asConfirm("温馨提示", "是否直接退出该页面？请确保已重置了分组或者删除了失效规则，否则直接返回将不会对原规则产生任何影响！", new OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        finish();
                    }
                }).show();
    }

    @Override
    public void finish() {
        EventBus.getDefault().post(new OpenHomeRulesActivityEvent());
        super.finish();
    }

    class CheckRunnable implements Runnable {

        private ArticleListRule articleListRuleDTO;

        CheckRunnable(ArticleListRule articleListRule) {
            this.articleListRuleDTO = articleListRule;
        }

        @Override
        public void run() {
            if (!loading.get() || articleListRuleDTO == null) {
                return;
            }
            ArticleListRule articleListRule = null;
            try {
                articleListRule = articleListRuleDTO.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (!loading.get() || articleListRule == null) {
                return;
            }
            loadingSize.addAndGet(1);

            checkRule(articleListRule, new OnCheckFinished() {
                @Override
                public void finish(int errorType) {
                    checkFinished(articleListRuleDTO, errorType);
                }

                @Override
                public void justBreak() {
                    loadingSize.decrementAndGet();
                }
            });
        }
    }

    interface OnCheckFinished {
        void finish(int errorType);

        void justBreak();
    }

    private void checkRule(ArticleListRule articleListRule, OnCheckFinished onCheckFinished) {
        if (StringUtil.isEmpty(articleListRule.getFind_rule())) {
            //纯搜索引擎
            SearchModel searchModel = new SearchModel();
            SearchEngine searchEngine = new SearchEngine();
            searchEngine.setTitle(articleListRule.getTitle());
            searchEngine.setSearch_url(articleListRule.getSearch_url());
            searchEngine.setTitleColor(articleListRule.getTitleColor());
            searchEngine.setFindRule(articleListRule.getSearchFind());
            searchEngine.setGroup(articleListRule.getGroup());
            searchEngine.setUa(articleListRule.getUa());
            if ("*".equals(articleListRule.getSdetail_find_rule())) {
                searchEngine.setDetail_col_type(articleListRule.getDetail_col_type());
                searchEngine.setDetail_find_rule(articleListRule.getDetail_find_rule());
            } else {
                searchEngine.setDetail_col_type(articleListRule.getSdetail_col_type());
                searchEngine.setDetail_find_rule(articleListRule.getSdetail_find_rule());
            }
            searchModel.params(getContext(), "我", searchEngine, 1, true).process(SearchModel.SEARCH_BY_RULE, new BaseCallback<SearchResult>() {
                @Override
                public void bindArrayToView(String actionType, List<SearchResult> data) {
                    if (isFinishing()) {
                        onCheckFinished.justBreak();
                        return;
                    }
                    if (CollectionUtil.isNotEmpty(data)) {
                        onCheckFinished.finish(NO_ERROR);
                    } else {
                        onCheckFinished.finish(ERROR_RULE);
                    }
                }

                @Override
                public void bindObjectToView(String actionType, SearchResult data) {

                }

                @Override
                public void error(String title, String msg, String code, Exception e) {
                    if (isFinishing()) {
                        loadingSize.decrementAndGet();
                        return;
                    }
                    if (StringUtil.isNotEmpty(msg) && msg.contains("HttpRequestError")) {
                        onCheckFinished.finish(ERROR_URL);
                    } else {
                        onCheckFinished.finish(ERROR_RULE);
                    }
                }

                @Override
                public void loading(boolean isLoading) {

                }
            });
        } else {
            //首页
            if (!TextUtils.isEmpty(articleListRule.getClass_name())) {
                articleListRule.setFirstHeader("class");
            } else if (!TextUtils.isEmpty(articleListRule.getArea_name())) {
                articleListRule.setFirstHeader("area");
            } else if (!TextUtils.isEmpty(articleListRule.getYear_name())) {
                articleListRule.setFirstHeader("year");
            } else if (!TextUtils.isEmpty(articleListRule.getSort_name())) {
                articleListRule.setFirstHeader("sort");
            } else {
                articleListRule.setFirstHeader("class");
            }
            if (!TextUtils.isEmpty(articleListRule.getArea_url())) {
                articleListRule.setArea_url(articleListRule.getArea_url().split("&")[0]);
            }
            if (!TextUtils.isEmpty(articleListRule.getClass_url())) {
                articleListRule.setClass_url(articleListRule.getClass_url().split("&")[0]);
            }
            if (!TextUtils.isEmpty(articleListRule.getYear_url())) {
                articleListRule.setYear_url(articleListRule.getYear_url().split("&")[0]);
            }
            if (!TextUtils.isEmpty(articleListRule.getSort_url())) {
                articleListRule.setSort_url(articleListRule.getSort_url().split("&")[0]);
            }
            ArticleListModel articleListModel = new ArticleListModel();
            articleListModel
                    .params(getContext(), 1, true, articleListRule, false)
                    .process(HomeActionEnum.ARTICLE_LIST_NEW, new BaseCallback<ArticleList>() {
                        @Override
                        public void bindArrayToView(String actionType, List<ArticleList> data) {
                            if (isFinishing()) {
                                loadingSize.decrementAndGet();
                                return;
                            }
                            if (CollectionUtil.isNotEmpty(data)) {
                                onCheckFinished.finish(NO_ERROR);
                            } else {
                                onCheckFinished.finish(ERROR_RULE);
                            }
                        }

                        @Override
                        public void bindObjectToView(String actionType, ArticleList data) {

                        }

                        @Override
                        public void error(String title, String msg, String code, Exception e) {
                            if (isFinishing()) {
                                loadingSize.decrementAndGet();
                                return;
                            }
                            if (StringUtil.isNotEmpty(msg) && msg.contains("HttpRequestError")) {
                                onCheckFinished.finish(ERROR_URL);
                            } else {
                                onCheckFinished.finish(ERROR_RULE);
                            }
                        }

                        @Override
                        public void loading(boolean isLoading) {

                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
