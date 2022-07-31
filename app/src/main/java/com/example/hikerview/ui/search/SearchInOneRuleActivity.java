package com.example.hikerview.ui.search;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.ui.base.BaseSlideActivity;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.setting.model.SearchModel;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;

import java.util.List;


public class SearchInOneRuleActivity extends BaseSlideActivity {
    private String title;
    private String ruleTitle;

    @Override
    protected View getBackgroundView() {
        return findView(R.id.ad_list_window);
    }
    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activit_search_one_result;
    }

    @Override
    protected void initView2() {
        title = getIntent().getStringExtra("wd");
        ruleTitle = getIntent().getStringExtra("rule");
        String searchTitle = "“" + title + "”的搜索结果";
        ((TextView) findView(R.id.ad_list_title_text)).setText(searchTitle);

        //初始化高度
        int marginTop = MyStatusBarUtil.getStatusBarHeight(getContext()) + DisplayUtil.dpToPx(getContext(), 86);
        View bg = findView(R.id.ad_list_bg);
        findView(R.id.ad_list_window).setOnClickListener(view -> finish());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bg.getLayoutParams();
        layoutParams.topMargin = marginTop;
        bg.setLayoutParams(layoutParams);
        bg.setOnClickListener(v -> {

        });
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        boolean useIntent = getIntent().getBooleanExtra("useIntent", false);
        if (useIntent) {
            try {
                String data = getIntent().getStringExtra("data");
                if (StringUtil.isEmpty(data)) {
                    ToastMgr.longCenter(getContext(), "参数有误，data不能为空");
                    return;
                }
                SearchEngine engine = JSON.parseObject(data, SearchEngine.class);
                if (engine == null || StringUtil.isEmpty(engine.getSearch_url())) {
                    ToastMgr.longCenter(getContext(), "参数有误，反序列化失败");
                    return;
                }
                if (isFinishing() || getSupportFragmentManager().isDestroyed()) {
                    return;
                }
                SearchFragment fragment = new SearchFragment().newInstance(title, engine, -1, engine.getGroup());
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame_bg, fragment)
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        SearchModel.getEngines(getContext(), "百度", new SearchModel.LoadListener() {
            @Override
            public void success(List<SearchEngine> engines, SearchEngine theSearchEngine, int position) {
                if (CollectionUtil.isEmpty(engines)) {
                    runOnUiThread(() -> {
                        ToastMgr.shortCenter(getContext(), "没有可用的搜索引擎");
                    });
                    return;
                }
                SearchEngine searchEngine = null;
                for (SearchEngine engine : engines) {
                    if (StringUtil.isEmpty(ruleTitle)) {
                        searchEngine = engine;
                        break;
                    } else if (ruleTitle.equals(engine.getTitle())) {
                        searchEngine = engine;
                        break;
                    }
                }
                if (searchEngine == null) {
                    runOnUiThread(() -> {
                        ToastMgr.shortCenter(getContext(), "找不到名字为‘" + ruleTitle + "’的搜索引擎，请选择其它搜索引擎");
                        String engineTitle = PreferenceMgr.getString(getContext(), "searchEngine", "百度");
                        GlobalSearchPopup.startSearch(SearchInOneRuleActivity.this, engineTitle, title, "main", getContext().getResources().getColor(R.color.white));
                        finish();
                    });
                    return;
                }
                String group = StringUtil.isEmpty(searchEngine.getGroup()) ? "全部" : searchEngine.getGroup();
                SearchEngine finalSearchEngine = searchEngine;
                if (isFinishing()) {
                    return;
                }
                runOnUiThread(() -> {
                    try {
                        if (isFinishing() || getSupportFragmentManager().isDestroyed()) {
                            return;
                        }
                        SearchFragment fragment = new SearchFragment().newInstance(title, finalSearchEngine, -1, group);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.frame_bg, fragment)
                                .commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void error(String title, String msg, String code, Exception e) {

            }
        });
    }
}
