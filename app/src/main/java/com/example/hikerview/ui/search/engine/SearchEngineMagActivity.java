package com.example.hikerview.ui.search.engine;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.ui.base.BaseActivity;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.bookmark.SearchEngineEditPopup;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.setting.model.SearchModel;
import com.example.hikerview.ui.view.DialogBuilder;
import com.example.hikerview.ui.view.ZLoadingDialog.ZLoadingDialog;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.ui.view.popup.SimpleHintPopupWindow;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * 作者：By hdy
 * 日期：On 2018/6/17
 * 时间：At 11:19
 */

public class SearchEngineMagActivity extends BaseActivity implements BaseCallback<ArticleListRule> {
    private RecyclerView recyclerView;
    private List<ArticleListRule> list = new ArrayList<>();
    private List<ArticleListRule> showList = new ArrayList<>();
    private SearchEngineMagAdapter adapter;
    private SearchEngineModel searchEngineModel = new SearchEngineModel();
    private ZLoadingDialog loadingDialog;
    private String groupSelected;
    private EditText search_edit;
    private ImageView search_clear;
    protected Map<String, Integer> orderMap = new HashMap<>();
    private SearchEngineEditPopup popup;
    public static final int[] colors = {0xFF6354EF, 0xFF717171, 0xFF62A6FB, 0xFFFF6877, 0xFFFE9700, 0xFF2196F3, 0xFF01BFA5};

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activity_engine_mag;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.engine_mag_options, menu);
        return true;
    }

    @Override
    protected void initView() {
        try {
            setSupportActionBar(findView(R.id.home_toolbar));
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        search_edit = findView(R.id.search_edit);
        search_clear = findView(R.id.search_clear);
        search_clear.setOnClickListener(v -> search_edit.setText(""));
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    return;
                }
                String key = s.toString();
                if (StringUtil.isEmpty(key)) {
                    search_clear.setVisibility(View.INVISIBLE);
                } else {
                    search_clear.setVisibility(View.VISIBLE);
                }
                generateShowList(key);
                adapter.notifyDataSetChanged();
            }
        });
        recyclerView = findView(R.id.home_recy);
        loadingDialog = DialogBuilder.createLoadingDialog(getContext(), false);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        if (StringUtil.isNotEmpty(groupSelected)) {
            setTitle(groupSelected);
        } else {
            setTitle("搜索引擎管理");
        }
        adapter = new SearchEngineMagAdapter(getContext(), showList);
        adapter.setOnItemClickListener(new SearchEngineMagAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (StringUtil.isEmpty(showList.get(position).getSearch_url())) {
                    //点击了分组
                    setTitle(showList.get(position).getTitle());
                    groupSelected = showList.get(position).getTitle();
                    String key = search_edit.getText().toString();
                    generateShowList(key);
                    adapter.notifyDataSetChanged();
                    return;
                }
                String text1 = showList.get(position).getTitle() + "￥" + showList.get(position).getSearch_url();
                if (StringUtil.isNotEmpty(showList.get(position).getGroup())) {
                    text1 = text1 + "￥" + showList.get(position).getGroup();
                }
                addSearchEngine(text1, showList.get(position).getId());
            }

            @Override
            public void onLongClick(View view, int position) {
                if (StringUtil.isEmpty(showList.get(position).getSearch_url())) {
                    manageGroup(showList.get(position).getTitle(), view);
                } else {
                    chooseLongClickOption(showList.get(position), position, view);
                }
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        String webs = getIntent().getStringExtra("webs");
        if (!TextUtils.isEmpty(webs)) {
            addSearchEngine(webs, -1);
        }
//        loadUrlFromClipboard();
        searchEngineModel.process("all", this);
    }

    private void chooseLongClickOption(ArticleListRule rule, int position, View view) {
        new SimpleHintPopupWindow(this,
                new String[]{"  删  除  ", "  分  享  ", "  编  辑  "}, s -> {
            switch (s) {
                case "  删  除  ":
                    new XPopup.Builder(getContext())
                            .asConfirm("温馨提示", "确认要删除”" + showList.get(position).getTitle() + "”吗？", () -> {
                                searchEngineModel.delete(getContext(), showList.get(position));
                                list.remove(showList.get(position));
                                if (showList.size() == 2) {
                                    this.setTitle("全部");
                                    groupSelected = "";
                                    showList.clear();
                                    showList.addAll(Stream.of(list).filter(articleListRule -> StringUtil.isEmpty(groupSelected) || groupSelected.equals(articleListRule.getGroup())).collect(Collectors.toList()));
                                    adapter.notifyDataSetChanged();
                                } else {
                                    showList.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                                ToastMgr.shortBottomCenter(getContext(), "删除成功");
                            }).show();
                    break;
                case "  分  享  ":
                    String text2 = showList.get(position).getTitle() + "￥" + showList.get(position).getSearch_url();
                    if (StringUtil.isNotEmpty(showList.get(position).getGroup())) {
                        text2 = text2 + "￥" + showList.get(position).getGroup();
                    }
                    AutoImportHelper.shareWithCommand(getContext(), text2, AutoImportHelper.SEARCH_ENGINE_v3);
                    break;
                case "  编  辑  ":
                    String text1 = showList.get(position).getTitle() + "￥" + showList.get(position).getSearch_url();
                    if (StringUtil.isNotEmpty(showList.get(position).getGroup())) {
                        text1 = text1 + "￥" + showList.get(position).getGroup();
                    }
                    addSearchEngine(text1, showList.get(position).getId());
                    break;
            }
        }).showPopupWindowCenter(view);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add:
                addSearchEngine(null, -1);
                break;
            case R.id.action_delete_inside:
                new XPopup.Builder(getContext())
                        .asConfirm("温馨提示", "暂不支持此功能。可以自行添加一些搜索引擎，然后搜索时候选择分组，这样下次搜索就会自动选择上次用的分组，就看不到自带引擎了", () -> {

                        }).show();
                break;
            case R.id.action_import:
                new XPopup.Builder(getContext()).asInputConfirm("剪贴板导入", "仅支持单个规则",
                        text1 -> {
                            if (TextUtils.isEmpty(text1)) {
                                ToastMgr.shortBottomCenter(getContext(), "规则不能为空");
                                return;
                            }
                            try {
                                if (text1.startsWith("[") && text1.endsWith("]")) {
                                    ToastMgr.shortBottomCenter(getContext(), "口令有误");
                                    return;
                                }
                                boolean canDeal = AutoImportHelper.checkText(getActivity(), text1);
                                if (!canDeal) {
                                    ToastMgr.shortCenter(getContext(), "口令无法识别，请确认规则是否正确！");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(getContext(), "格式有误：" + e.getMessage());
                            }
                        })
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void manageGroup(String group, View view) {
        new SimpleHintPopupWindow(this,
                new String[]{"修改分组名称", "清除分组名称", "删除整个分组"}, s -> {
            switch (s) {
                case "修改分组名称":
                    new XPopup.Builder(getContext()).asInputConfirm("修改分组名", null, group, "请输入新分组名，不能为空",
                            text1 -> {
                                if (TextUtils.isEmpty(text1)) {
                                    ToastMgr.shortBottomCenter(getContext(), "分组名不能为空");
                                    return;
                                }
                                HeavyTaskUtil.executeNewTask(() -> {
                                    ArticleListRule rule = new ArticleListRule();
                                    rule.setGroup(text1);
                                    if ("全部".equals(group)) {
                                        rule.updateAll();
                                    } else {
                                        rule.updateAll("group = ?", group);
                                    }
                                    runOnUiThread(() -> {
                                        if (group.equals(groupSelected) || ("".equals(groupSelected) && "全部".equals(group))) {
                                            groupSelected = text1;
                                            setTitle(text1);
                                        }
                                        searchEngineModel.process("all", this);
                                    });
                                });
                            })
                            .show();
                    break;
                case "清除分组名称":
                    new PromptDialog(getContext())
                            .setTitleText("确定删除该分组吗？")
                            .setContentText("确定后，该分组的所有频道的分组信息都将置空，但不会删除书签")
                            .setPositiveListener("确定", dialog -> {
                                dialog.dismiss();
                                HeavyTaskUtil.executeNewTask(() -> {
                                    ArticleListRule rule = new ArticleListRule();
                                    rule.setGroup("");
                                    rule.updateAll("group = ?", group);
                                    runOnUiThread(() -> {
                                        if (group.equals(groupSelected)) {
                                            groupSelected = "";
                                            setTitle("搜索引擎管理");
                                        }
                                        searchEngineModel.process("all", this);
                                    });
                                });
                            }).show();
                    break;
                case "删除整个分组":
                    new PromptDialog(getContext())
                            .setTitleText("确定删除该分组吗？")
                            .setContentText("确定后，该分组的所有频道都会被删除！注意：删除后无法恢复！")
                            .setPositiveListener("确定", dialog -> {
                                dialog.dismiss();
                                HeavyTaskUtil.executeNewTask(() -> {
                                    if ("全部".equals(group)) {
                                        try {
                                            LitePal.deleteAll(ArticleListRule.class);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        LitePal.deleteAll(ArticleListRule.class, "group = ?", group);
                                    }
                                    runOnUiThread(() -> {
                                        if (group.equals(groupSelected) || ("".equals(groupSelected) && "全部".equals(group))) {
                                            groupSelected = "";
                                            setTitle("搜索引擎管理");
                                        }
                                        searchEngineModel.process("all", this);
                                    });
                                });
                            }).show();
                    break;
            }
        }).showPopupWindowCenter(view);
    }


    private void addSearchEngine(@Nullable String addWeb, long id) {
        String title = null, url = null, group = null;
        if (StringUtil.isNotEmpty(addWeb)) {
            String[] detail = addWeb.split("￥");
            if (detail.length == 2) {
                title = detail[0];
                url = detail[1];
            } else if (detail.length == 3) {
                title = detail[0];
                url = detail[1];
                group = detail[2];
            }
        } else {
            if (StringUtil.isNotEmpty(groupSelected)) {
                group = groupSelected;
            }
        }
        List<String> groups = Stream.of(list).map(ArticleListRule::getGroup).filter(StringUtil::isNotEmpty).distinct().collect(Collectors.toList());
        for (SearchModel.EngineEnum value : SearchModel.EngineEnum.values()) {
            if (!groups.contains(value.getGroup())) {
                groups.add(0, value.getGroup());
            }
        }
        Collections.sort(groups);
        popup = new SearchEngineEditPopup(getContext())
                .bindHint("搜索引擎名称", "搜索链接，关键词用**代替")
                .bind(StringUtil.isNotEmpty(addWeb) ? "编辑" : "新增",
                        title, url, group, groups, (title1, url1, group1, addShortCut) -> {
                            if (StringUtil.isEmpty(group1)) {
                                group1 = "";
                            }
                            if (TextUtils.isEmpty(title1) || TextUtils.isEmpty(url1)) {
                                ToastMgr.shortBottomCenter(getContext(), "请输入完整信息");
                            } else {
                                if (!url1.startsWith("http") && !url1.startsWith("file://")) {
                                    url1 = "http://" + url1;
                                }
                                url1 = url1.replace("%s", "**");
                                if (id == -1) {
                                    ArticleListRule articleListRule = new ArticleListRule();
                                    ArticleListRule rule = LitePal.where("search_url = ?", url1).findFirst(ArticleListRule.class);
                                    if (rule != null) {
                                        articleListRule = rule;
                                    }
                                    articleListRule.setTitle(title1);
                                    articleListRule.setSearch_url(url1);
                                    articleListRule.setGroup(group1);
                                    searchEngineModel.add(getContext(), articleListRule);
                                    searchEngineModel.process("all", SearchEngineMagActivity.this);
                                    ToastMgr.shortBottomCenter(getContext(), "保存成功");
                                } else {
                                    ArticleListRule articleListRule = LitePal.find(ArticleListRule.class, id);
                                    if (articleListRule == null) {
                                        articleListRule = new ArticleListRule();
                                    }
                                    articleListRule.setTitle(title1);
                                    articleListRule.setSearch_url(url1);
                                    articleListRule.setGroup(group1);
                                    articleListRule.save();
                                    ToastMgr.shortBottomCenter(getContext(), "保存成功");
                                    searchEngineModel.process("all", SearchEngineMagActivity.this);
                                }
                            }
                        }
                );
        new XPopup.Builder(getContext())
                .asCustom(popup)
                .show();
    }

    @Override
    public void bindArrayToView(String actionType, List<ArticleListRule> data) {
        runOnUiThread(() -> {
            if ("all".equals(actionType)) {
                list.clear();
                list.addAll(data);
                if (popup != null && popup.isShow()) {
                    List<String> groups = Stream.of(list).map(ArticleListRule::getGroup).filter(StringUtil::isNotEmpty).distinct().collect(Collectors.toList());
                    popup.updateGroups(groups);
                }
                String key = search_edit.getText().toString();
                generateShowList(key);
                Timber.d("bindArrayToView: %s", list.size());
                Timber.d("bindArrayToView: %s", showList.size());
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void bindObjectToView(String actionType, ArticleListRule data) {

    }

    @Override
    public void error(String title, String msg, String code, Exception e) {
        runOnUiThread(() -> {
            loading(false);
            DebugUtil.showErrorMsg(this, getContext(), title, msg, code, e);
        });
    }

    @Override
    public void loading(boolean isLoading) {
        if (loadingDialog != null) {
            if (isLoading) {
                loadingDialog.show();
            } else {
                loadingDialog.dismiss();
            }
        }
    }

    private void generateShowList(String key) {
        showList.clear();
        Set<String> groups = new HashSet<>();
        String lKey = StringUtil.isEmpty(key) ? key : key.toLowerCase();
        showList.addAll(Stream.of(list)
                .filter(articleListRule -> {
                    if (StringUtil.isNotEmpty(articleListRule.getGroup())) {
                        groups.add(articleListRule.getGroup());
                    }
                    if (StringUtil.isEmpty(groupSelected)) {
                        return StringUtil.isEmpty(articleListRule.getGroup());
                    }
                    return groupSelected.equals(articleListRule.getGroup());
                })
                .filter(articleListRule -> StringUtil.isEmpty(lKey) || articleListRule.getTitle().toLowerCase().contains(lKey)
                        || (StringUtil.isNotEmpty(articleListRule.getSearch_url()) && articleListRule.getSearch_url().toLowerCase().contains(lKey)))
                .collect(Collectors.toList()));
        if (StringUtil.isEmpty(groupSelected)) {
            for (String group : groups) {
                if (StringUtil.isNotEmpty(lKey) && !group.toLowerCase().contains(lKey)) {
                    //分组也筛选一下
                    continue;
                }
                ArticleListRule articleListRule = new ArticleListRule();
                articleListRule.setTitle(group);
                showList.add(0, articleListRule);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (StringUtil.isNotEmpty(groupSelected)) {
            setTitle("搜索引擎管理");
            groupSelected = "";
            generateShowList(search_edit.getText().toString());
            Collections.sort(showList);
            adapter.notifyDataSetChanged();
            return;
        }
        super.onBackPressed();
    }
}
