package com.example.hikerview.ui.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.constants.CollectionTypeConstant;
import com.example.hikerview.constants.JSONPreFilter;
import com.example.hikerview.event.SearchEvent;
import com.example.hikerview.model.ViewHistory;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.search.engine.SearchEngineMagActivity;
import com.example.hikerview.ui.search.model.SearchGroup;
import com.example.hikerview.ui.search.model.SearchHistroyModel;
import com.example.hikerview.ui.search.model.SuggestModel;
import com.example.hikerview.ui.setting.model.SearchModel;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.view.CenterLayoutManager;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.animator.PopupAnimator;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.util.XPopupUtils;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * 作者：By 15968
 * 日期：On 2021/1/15
 * 时间：At 19:46
 */

public class GlobalSearchPopup extends BottomPopupView {

    private Intent intent;
    private Activity activity;
    private EditText editText;
    private View cancel, clear;
    private TextView search;
    private SearchEngine theSearchEngine = new SearchEngine();
    private RecyclerView recyclerView, groupRecyclerView;
    private boolean fromWeb;
    private List<SearchEngine> allEngines = new ArrayList<>();
    private HomeSearchAdapter adapter;
    private SearchHisAdapter hisAdapter;
    private GridLayoutManager gridLayoutManager;
    private View searchHisClearView, searchHisBg;
    private TextView url, urlTitle;
    private View urlBg, urlCopy, editBg;
    private boolean hasClick = false;
    private Runnable dismissTask;

    public List<SearchGroup> getGroups() {
        return groups;
    }

    public SearchGroup getGroup() {
        return group;
    }

    private List<SearchGroup> groups = new ArrayList<>();
    private SearchGroup group = new SearchGroup("全部", true);


    public static void startSearch(Activity context, String title, String url, String tag, int color) {
        startSearch(context, title, url, tag, color, false);
    }

    public static void startSearch(Activity context, String title, String url, String tag, int color, boolean fromWeb) {
        startSearch(context, title, url, tag, color, fromWeb, false);
    }

    public static void startSearch(Activity context, String title, String url, String tag, int color, boolean fromWeb, boolean newWindow) {
        Intent starter = new Intent(context, EmptySearchActivity.class);
        //设置启动方式
        starter.putExtra("type", "Search");
        starter.putExtra("url", url);
        starter.putExtra("tag", tag);
        starter.putExtra("title", title);
        starter.putExtra("fromWeb", fromWeb);
        starter.putExtra("color", color);
        starter.putExtra("newWindow", newWindow);
//        context.startActivity(starter, ActivityOptions.makeSceneTransitionAnimation(context).toBundle());
        new XPopup.Builder(context)
                .autoOpenSoftInput(true)
                .moveUpToKeyboard(false)
                .asCustom(new GlobalSearchPopup(context).with(starter, context))
                .show();
    }


    public GlobalSearchPopup(@NonNull Context context) {
        super(context);
    }

    public GlobalSearchPopup with(Intent intent, Activity activity) {
        this.intent = intent;
        this.activity = activity;
        return this;
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.activity_search_v2;
    }

    private Intent getIntent() {
        return intent == null ? new Intent() : intent;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        String engineTitle = getIntent().getStringExtra("title");
        String text = getIntent().getStringExtra("url");
        fromWeb = getIntent().getBooleanExtra("fromWeb", false);

        editText = findViewById(R.id.edit);
        search = findViewById(R.id.search);
        cancel = findViewById(R.id.cancel);
        clear = findViewById(R.id.clear);
        cancel.setOnClickListener((v) -> dismiss());
        search.setOnClickListener(v -> goSearch());
        clear.setOnClickListener(v -> editText.setText(""));
        initEdit();
        initRules();
        initGroup();
        initHistory();
        initClipboard(text);

        initData(engineTitle);
    }

    private void initClipboard(String intentText) {
        editBg = findViewById(R.id.pop_input_edit_bg);
        url = findViewById(R.id.pop_input_url);
        urlBg = findViewById(R.id.pop_input_url_bg);
        urlTitle = findViewById(R.id.pop_input_url_title);
        urlBg.setOnClickListener(v -> {
            String text = url.getText().toString();
            if (StringUtil.isEmpty(text) || url.getVisibility() != View.VISIBLE
                    || "loading...".equals(text) || "无历史链接".equals(text)) {
                return;
            }
            dismissWith(() -> {
                if (!TextUtils.isEmpty(text)) {
                    click(text, theSearchEngine);
                }
            });
        });
        urlCopy = findViewById(R.id.pop_input_url_copy);
        urlBg.setOnLongClickListener(v -> {
            String text = url.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                editText.setText(text);
                editText.setSelection(text.length());
            }
            return true;
        });
        if (TextUtils.isEmpty(intentText)) {
            ClipboardUtil.getText(getContext(), editBg, t -> checkWebUrl(t, true), 200);
        } else {
            if (!checkWebUrl(intentText, false) && !TextUtils.isEmpty(intentText)) {
                editText.setText(intentText);
            }
        }
        urlCopy.setOnClickListener(v -> {
            String text = url.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                ClipboardUtil.copyToClipboard(getContext(), text, false);
                ToastMgr.shortCenter(getContext(), "复制成功");
            }
        });
    }

    private boolean checkWebUrl(String text, boolean fromClip) {
        if (StringUtil.isWebUrl(text)) {
            if (!fromClip) {
                urlTitle.setText("编辑：");
                url.setText(text);
                urlBg.setOnClickListener(v -> {
                    editText.setText(text);
                    editText.setSelection(text.length());
                });
            } else {
                url.setText(text);
                urlTitle.setText("访问：");
            }
            return true;
        } else if (StringUtil.isNotEmpty(text) && text.length() <= 30) {
            urlTitle.setText("搜索：");
            url.setText(text);
        } else {
            HeavyTaskUtil.executeNewTask(() -> {
                List<ViewHistory> list = LitePal.where("type = ?", CollectionTypeConstant.WEB_VIEW).find(ViewHistory.class);
                activity.runOnUiThread(() -> {
                    if (activity == null || activity.isFinishing() || isDismiss()) {
                        return;
                    }
                    if (CollectionUtil.isEmpty(list)) {
                        urlTitle.setText("历史：");
                        url.setText("无历史链接");
                        urlCopy.setVisibility(INVISIBLE);
                    } else {
                        Collections.sort(list);
                        url.setText(list.get(0).getUrl());
                        urlTitle.setText("历史：");
                    }
                });
            });
        }
        return false;
    }


    private void click(String text, SearchEngine searchEngine) {
        if (hasClick) {
            return;
        }
        hasClick = true;
        if (StringUtil.isUrl(text) && !text.startsWith("http") && !text.startsWith("file://") && !text.startsWith("hiker://")
                && !text.startsWith("ftp://") && !text.startsWith("magnet:?") && !text.startsWith("ed2k://")) {
            text = "http://" + text;
        }
        EventBus.getDefault().post(new SearchEvent(text, searchEngine, getIntent().getStringExtra("tag"), getGroups(), getGroup(),
                getIntent().getBooleanExtra("newWindow", false)));
        dismiss();
    }

    private void initHistory() {
        searchHisClearView = findViewById(R.id.pop_web_input_search_his_clear);
        searchHisBg = findViewById(R.id.pop_web_input_search_his_bg);
        View hisEditText = searchHisClearView.findViewById(R.id.hisEditText);
        View hisClear = searchHisClearView.findViewById(R.id.hisClear);
        hisEditText.setOnClickListener(v -> {
            searchHisClearView.setVisibility(View.GONE);
            String tag = (String) v.getTag();
            editText.setText(tag);
            editText.setSelection(tag.length());
        });
        hisClear.setOnClickListener(v -> {
            SearchHistroyModel.clearAll(getContext());
            ToastMgr.shortBottomCenter(getContext(), "已清除搜索记录");
            searchHisBg.setVisibility(View.GONE);
        });
        hisEditText.setOnLongClickListener(v -> {
            searchHisClearView.setVisibility(View.GONE);
            return true;
        });
        hisClear.setOnLongClickListener(v -> {
            searchHisClearView.setVisibility(View.GONE);
            return true;
        });
        RecyclerView hisRecyclerView = findViewById(R.id.pop_web_input_search_his_recycler);
        //搜索历史
        List<String> hisList = SearchHistroyModel.getHisList(getContext());
        if (CollectionUtil.isEmpty(hisList)) {
            searchHisBg.setVisibility(View.GONE);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        hisRecyclerView.setLayoutManager(linearLayoutManager);
        hisAdapter = new SearchHisAdapter(getContext(), hisList);
        hisAdapter.setOnItemClickListener(new SearchHisAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                try {
                    if (position >= 0 && position < hisAdapter.getList().size()) {
                        editText.setText(hisAdapter.getList().get(position));
                        editText.setSelection(hisAdapter.getList().get(position).length());
                        goSearch();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                View hisEditText = searchHisClearView.findViewById(R.id.hisEditText);
                hisEditText.setTag(hisAdapter.getList().get(position));
                searchHisClearView.setVisibility(View.VISIBLE);
            }
        });
        hisRecyclerView.setAdapter(hisAdapter);
    }

    private void refreshData(List<SearchEngine> engines, SearchEngine searchEngine) {
        if (activity == null || activity.isFinishing() || isDismiss()) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (JSON.toJSONString(engines, JSONPreFilter.getSimpleFilter()).equals(JSON.toJSONString(allEngines, JSONPreFilter.getSimpleFilter()))) {
                return;
            }
            theSearchEngine = searchEngine;
            allEngines.clear();
            allEngines.addAll(engines);
            initGroups();
            filterSearchEnginesByGroup();
        });
    }

    private void initData(String engineTitle) {
        if (CollectionUtil.isNotEmpty(SettingConfig.getRules())) {
            SearchModel.filterEngines(getContext(), engineTitle, fromWeb, SettingConfig.getRules(), new SearchModel.LoadListener() {
                @Override
                public void success(List<SearchEngine> engines, SearchEngine searchEngine, int position) {
                    refreshData(engines, searchEngine);
                }

                @Override
                public void error(String title, String msg, String code, Exception e) {

                }
            });
        }
        SearchModel.getEngines(getContext(), engineTitle, fromWeb, new SearchModel.LoadListener() {
            @Override
            public void success(List<SearchEngine> engines, SearchEngine searchEngine, int position) {
                refreshData(engines, searchEngine);
            }

            @Override
            public void error(String title, String msg, String code, Exception e) {

            }
        });
    }


    private void initGroups() {
        if (CollectionUtil.isNotEmpty(allEngines)) {
            groups.clear();
            String gp = PreferenceMgr.getString(getContext(), fromWeb ? "webSearchGroup" : "searchGroup", "全部");
            groups.add(new SearchGroup("全部", true));
            Set<String> g = Stream.of(allEngines).map(SearchEngine::getGroup).filter(StringUtil::isNotEmpty).collect(Collectors.toSet());
            List<SearchGroup> gs = Stream.of(g).map(group -> {
                if (gp.equals(group)) {
                    groups.get(0).setUse(false);
                }
                return new SearchGroup(group, gp.equals(group));
            }).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(gs)) {
                Collections.sort(gs);
                groups.addAll(gs);
            }
            group = new SearchGroup(gp, true);
            if (!"全部".equals(gp) && !g.contains(gp)) {
                group = new SearchGroup("全部", true);
            }
            int pos = 0;
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i).isUse()) {
                    pos = i;
                    break;
                }
            }
            activity.runOnUiThread(() -> {
                Objects.requireNonNull(groupRecyclerView.getAdapter()).notifyDataSetChanged();
            });
            if (recyclerView != null) {
                int finalPos = pos;
                recyclerView.post(() -> {
                    if (finalPos > 0) {
                        recyclerView.postDelayed(() ->
                                Objects.requireNonNull(groupRecyclerView.getLayoutManager())
                                        .smoothScrollToPosition(groupRecyclerView, new RecyclerView.State(), finalPos), 600);
                    }
                });
            }
        }
    }

    private void initRules() {
        recyclerView = findViewById(R.id.pop_web_input_recycler_view);
        gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        //必须先设置setLayoutManager
        adapter = new HomeSearchAdapter(activity, recyclerView, new ArrayList<>());
        adapter.setOnItemClickListener(new HomeSearchAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position, SearchEngine engine) {
                theSearchEngine = engine;
                if (fromWeb) {
                    PreferenceMgr.put(getContext(), "webSearchEngine", engine.getSearch_url());
                }
            }

            @Override
            public void onLongClick(View view, int position, SearchEngine engine) {
                try {
                    InputMethodManager imm=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(dialog.getWindow().getDecorView().getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new XPopup.Builder(getContext())
                        .asCenterList(null, new String[]{"管理规则"}, (i, s) -> {
                            dismissWith(() -> {
                                activity.startActivity(new Intent(activity, SearchEngineMagActivity.class));
                            });
                        }).show();
            }
        });
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(adapter.getDividerItem());
    }

    private void initGroup() {
        groupRecyclerView = findViewById(R.id.pop_web_input_search_group_recycler);
        CenterLayoutManager linearLayoutManager1 = new CenterLayoutManager(getContext());
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        groupRecyclerView.setLayoutManager(linearLayoutManager1);
        SearchGroupAdapter searchGroupAdapter = new SearchGroupAdapter(getContext(), groups);
        searchGroupAdapter.setOnItemClickListener(new SearchGroupAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                for (int i = 0; i < groups.size(); i++) {
                    if (i == position) {
                        groups.get(i).setUse(true);
                        group = groups.get(i);
                        PreferenceMgr.put(getContext(), fromWeb ? "webSearchGroup" : "searchGroup", group.getGroup());
                    } else {
                        groups.get(i).setUse(false);
                    }
                }
                adapter.closeAnimate();
                filterSearchEnginesByGroup();
                if (groupRecyclerView.getAdapter() != null) {
                    groupRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        });
        groupRecyclerView.setAdapter(searchGroupAdapter);
    }

    private void initEdit() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString();
                if (StringUtil.isEmpty(s1)) {
                    clear.setVisibility(View.INVISIBLE);
                    search.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                } else {
                    clear.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.INVISIBLE);
                }
                refreshSuggest(s1);
                if (TextUtils.isEmpty(s1)) {
                    return;
                }
                if (StringUtil.isUrl(s1)) {
                    search.setText("进入");
                } else {
                    search.setText("搜索");
                }
            }
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                dismissWith(() -> click(editText.getText().toString(), theSearchEngine));
                return true;
            }
            return false;
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                dismissWith(() -> click(editText.getText().toString(), theSearchEngine));
            }
            return false;
        });
    }

    private void goSearch() {
        dismissWith(() -> {
            click(editText.getText().toString(), theSearchEngine);
        });
    }


    private void filterSearchEnginesByGroup() {
        if (CollectionUtil.isNotEmpty(allEngines)) {
            List<SearchEngine> engines = Stream.of(allEngines).filter(engine -> "全部".equals(group.getGroup()) || group.getGroup().equals(engine.getGroup())).collect(Collectors.toList());
            int pos = 0;
            for (int i = 0; i < engines.size(); i++) {
                SearchEngine engine = engines.get(i);
                if (theSearchEngine.getTitle().equals(engine.getTitle()) && (theSearchEngine.getGroup().equals(group.getGroup()) || "全部".equals(group.getGroup()))) {
                    engine.setUse(true);
                    pos = i;
                } else {
                    engine.setUse(false);
                }
            }
            if (pos == 0 && engines.size() > 0) {
                engines.get(0).setUse(true);
                theSearchEngine = engines.get(0);
            }
            int finalPos1 = pos;
            recyclerView.post(() -> {
                if (recyclerView.getAdapter() != null) {
                    adapter.getList().clear();
                    adapter.getList().addAll(engines);
                    adapter.notifyDataSetChanged();
                }
                if (finalPos1 > 0) {
                    recyclerView.post(() -> {
                        recyclerView.scrollToPosition(finalPos1);
                        gridLayoutManager.scrollToPositionWithOffset(finalPos1, 0);
                    });
                }
            });
        }
    }


    /**
     * 刷新搜索建议
     *
     * @param word
     */
    private void refreshSuggest(String word) {
        SuggestModel.getRecommands(getContext(), word, new SuggestModel.OnSuggestFetchedListener() {
            @Override
            public void onSuccess(List<String> suggests) {
                if (activity == null || activity.isFinishing() || isDismiss()) {
                    return;
                }
                try {
                    activity.runOnUiThread(() -> {
                        if (searchHisBg.getVisibility() != View.VISIBLE) {
                            searchHisBg.setVisibility(View.VISIBLE);
                        }
                        if (searchHisClearView.getVisibility() != View.GONE) {
                            searchHisClearView.setVisibility(View.GONE);
                        }
                        hisAdapter.getList().clear();
                        hisAdapter.getList().addAll(suggests);
                        hisAdapter.notifyDataSetChanged();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                if (activity == null || activity.isFinishing() || isDismiss()) {
                    return;
                }
                try {
                    activity.runOnUiThread(() -> ToastMgr.shortBottomCenter(getContext(), msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .8f);
    }


    @Override
    protected PopupAnimator getPopupAnimator() {
        // 移除默认的动画器
        return new MyTranslateAnimator(getPopupContentView(), getAnimationDuration(), PopupAnimation.TranslateFromBottom);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (dismissTask != null) {
            dismissTask.run();
        }
    }

    @Override
    public BasePopupView show() {
        try {
            ((InputMethodManager) Objects.requireNonNull(getContext().getSystemService(INPUT_METHOD_SERVICE)))
                    .showSoftInput(editText, InputMethodManager.SHOW_FORCED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.show();
    }

    public GlobalSearchPopup setDismissTask(Runnable dismissTask) {
        this.dismissTask = dismissTask;
        return this;
    }
}
