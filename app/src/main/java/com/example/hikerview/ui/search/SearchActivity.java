package com.example.hikerview.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.hikerview.R;
import com.example.hikerview.event.OnArticleListRuleChangedEvent;
import com.example.hikerview.event.StartActivityEvent;
import com.example.hikerview.event.StatusEvent;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.ui.base.BaseSlideActivity;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.ArticleListRuleEditActivity;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.setting.model.SearchModel;
import com.example.hikerview.ui.view.CenterLayoutManager;
import com.example.hikerview.utils.AlertNewVersionUtil;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SearchActivity extends BaseSlideActivity {
    private static final String TAG = "SearchActivity";
    private String title;
    private ViewPager viewPager;
    private FragmentStatePagerAdapter pagerAdapter;
    private List<SearchEngine> searchEngines = new ArrayList<>();
    private List<SearchEngine> allEngines = new ArrayList<>();
    private RecyclerView bottomRecyclerView;
    private BottomSearchStatusAdapter bottomSearchAdapter;
    private CenterLayoutManager centerLayoutManager;
    private TextView dropDownMenu;
    private GridView gridView;
    private View maskView;
    private SearchResultGroupAdapter groupAdapter;
    private String[] groups;
    private int groupPosition;
    private int loadingCount = 0;
    private TextView titleView;
    private boolean isOnPause;

    @Override
    protected View getBackgroundView() {
        return findView(R.id.ad_list_window);
    }

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activit_search_result;
    }

    @Override
    protected void initView2() {
        viewPager = findView(R.id.search_result_view_pager);
        title = getIntent().getStringExtra("wd");
        dropDownMenu = findView(R.id.dropDownMenu);
        gridView = findView(R.id.gridView);
        maskView = findView(R.id.maskView);
        maskView.setOnClickListener(v -> closeGroupMenu());
        initGroupView();

        initViewPager();
        initBottomRecyclerView();
        String searchTitle = "“" + title + "”的搜索结果";
        ((TextView) findView(R.id.ad_list_title_text)).setText(searchTitle);
        titleView = findView(R.id.ad_list_desc_text);

        //初始化高度
        int marginTop = MyStatusBarUtil.getStatusBarHeight(getContext()) + DisplayUtil.dpToPx(getContext(), 86);
        View bg = findView(R.id.ad_list_bg);
        findView(R.id.ad_list_window).setOnClickListener(view -> finish());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bg.getLayoutParams();
        layoutParams.topMargin = marginTop;
        bg.setLayoutParams(layoutParams);
        bg.setOnClickListener(v -> {

        });
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        AlertNewVersionUtil.alert(this);
    }

    private void initGroupView() {
        groups = getIntent().getStringArrayExtra("groups");
        groupPosition = getIntent().getIntExtra("groupPosition", 0);
        Log.d(TAG, "initGroupView: " + groups.length);
        groupAdapter = new SearchResultGroupAdapter(this, Arrays.asList(groups));
        gridView.setAdapter(groupAdapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            groupAdapter.setCheckItem(position);
            dropDownMenu.setText(StringUtil.simplyGroup(groups[position]));
            groupPosition = position;
            closeGroupMenu();
            loadingCount = 0;
            filterEnginesByGroup();
        });
        groupAdapter.setCheckItem(groupPosition);
        dropDownMenu.setText(StringUtil.simplyGroup(groups[groupPosition]));
        dropDownMenu.setOnClickListener(v -> {
            if (gridView.getVisibility() == View.GONE) {
                showGroupMenu();
            } else {
                closeGroupMenu();
            }
        });
    }

    private void closeGroupMenu() {
        gridView.setVisibility(View.GONE);
        gridView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
        maskView.setVisibility(View.GONE);
        maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
    }

    private void showGroupMenu() {
        gridView.setVisibility(View.VISIBLE);
        gridView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
        maskView.setVisibility(View.VISIBLE);
        maskView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
    }

    private void initBottomRecyclerView() {
        bottomRecyclerView = findView(R.id.search_result_bottom_recycler_view);
        centerLayoutManager = new CenterLayoutManager(getContext());
        centerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bottomRecyclerView.setLayoutManager(centerLayoutManager);
        bottomSearchAdapter = new BottomSearchStatusAdapter(getContext(), searchEngines);
        bottomSearchAdapter.setOnItemClickListener(new BottomSearchStatusAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position, SearchEngine engine) {
                viewPager.setCurrentItem(position);
                bottomRecyclerView.scrollToPosition(position);
            }

            @Override
            public void onLongClick(View view, int position, SearchEngine engine) {
                new XPopup.Builder(getContext())
                        .asCenterList("请选择操作", new String[]{"编辑搜索规则", "删除搜索引擎"},
                                ((option, text) -> {
                                    switch (text) {
                                        case "编辑搜索规则":
                                            editEngine(engine.getTitle());
                                            break;
                                        case "删除搜索引擎":
                                            deleteEngine(engine.getTitle());
                                            break;
                                    }
                                }))
                        .show();
            }
        });
        bottomRecyclerView.setAdapter(bottomSearchAdapter);
//        MySnapHelper snapHelper = new MySnapHelper(MySnapHelper.TYPE_SNAP_START);
//        snapHelper.attachToRecyclerView(bottomRecyclerView);
    }

    private void editEngine(String engineName) {
        List<ArticleListRule> articleListRules = null;
        try {
            articleListRules = LitePal.where("title = ?", engineName).limit(1).find(ArticleListRule.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CollectionUtil.isEmpty(articleListRules)) {
            ToastMgr.shortBottomCenter(getContext(), "找不到对应规则！");
            return;
        }
        Intent intent = new Intent(getContext(), ArticleListRuleEditActivity.class);
        String data = JSON.toJSONString(articleListRules.get(0));
        intent.putExtra("data", data);
        intent.putExtra("edit", true);
        startActivity(intent);
        finish();
    }

    private void deleteEngine(String engineName) {
        List<ArticleListRule> articleListRules = null;
        try {
            articleListRules = LitePal.where("title = ?", engineName).limit(1).find(ArticleListRule.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CollectionUtil.isEmpty(articleListRules)) {
            ToastMgr.shortBottomCenter(getContext(), "找不到对应规则！");
            return;
        }
        deleteHomeRule(engineName, articleListRules.get(0));
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onToast(ToastEvent event) {
//        Timber.d("onToast: %s", event.getMsg());
//        if (isOnPause) {
//            return;
//        }
//        DebugUtil.showErrorMsg(this, getContext(), event.getMsg(), event.getMsg(), "500", new Exception(event.getMsg()));
//    }

    private void deleteHomeRule(String engineName, ArticleListRule articleListRule) {
        new XPopup.Builder(getContext())
                .asCenterList("搜索规则来自首页频道，请选择操作", new String[]{"仅删除该首页的搜索规则", "删除整个首页频道", "不做任何操作"},
                        ((option1, text1) -> {
                            switch (text1) {
                                case "仅删除该首页的搜索规则":
                                    articleListRule.setSearchFind("");
                                    articleListRule.save();
                                    ToastMgr.shortBottomCenter(getContext(), "已删除该首页的搜索规则");
                                    //刷新页面，加载状态保留
                                    finish();
                                    Intent intent = getIntent();
                                    intent.putExtra("groupPosition", groupPosition);
                                    EventBus.getDefault().post(new StartActivityEvent(intent));
                                    break;
                                case "删除整个首页频道":
                                    articleListRule.delete();
                                    HeavyTaskUtil.executeNewTask(() -> {
                                        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.ARTICLE_LIST_ORDER_KEY).findFirst(BigTextDO.class);
                                        if (bigTextDO != null) {
                                            String value = bigTextDO.getValue();
                                            if (StringUtil.isNotEmpty(value)) {
                                                Map<String, Integer> orderMap = JSON.parseObject(value, new TypeReference<Map<String, Integer>>() {
                                                });
                                                orderMap.remove(engineName);
                                                bigTextDO.setValue(JSON.toJSONString(orderMap));
                                                bigTextDO.save();
                                            }
                                        }
                                    });
                                    ToastMgr.shortBottomCenter(getContext(), "已删除该首页");
                                    //刷新页面，加载状态保留
                                    finish();
                                    Intent intent1 = getIntent();
                                    intent1.putExtra("groupPosition", groupPosition);
                                    EventBus.getDefault().post(new StartActivityEvent(intent1));
                                    OnArticleListRuleChangedEvent event = new OnArticleListRuleChangedEvent();
                                    event.setFromClazz(this.getClass().getName());
                                    EventBus.getDefault().post(event);
                                    break;
                            }
                        })).show();
    }

    private void initViewPagerAdapter() {
        if (pagerAdapter != null) {
            pagerAdapter = null;
        }
        pagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position >= searchEngines.size() || position < 0) {
                    return null;
                }
                return new SearchFragment().newInstance(title, searchEngines.get(position), position, groups[groupPosition]);
            }

            @Override
            public int getCount() {
                return searchEngines.size();
            }
        };
        viewPager.setAdapter(pagerAdapter);
    }

    private void initViewPager() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (!searchEngines.get(position).isUse()) {
                    for (int i = 0; i < searchEngines.size(); i++) {
                        if (position == i) {
                            searchEngines.get(i).setUse(true);
                            centerLayoutManager.smoothScrollToPosition(bottomRecyclerView, new RecyclerView.State(), position);
//                            SearchModel.memoryEngine(getContext(), searchEngines.get(i));
                        } else {
                            searchEngines.get(i).setUse(false);
                        }
                    }
                    bottomSearchAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setOffscreenPageLimit(5);//左右几个页面
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        SearchModel.getEngines(getContext(), "百度", new SearchModel.LoadListener() {
            @Override
            public void success(List<SearchEngine> engines, SearchEngine theSearchEngine, int position) {
                allEngines.clear();
                if (engines != null) {
                    for (SearchEngine engine : engines) {
                        if (StringUtil.isNotEmpty(engine.getFindRule())) {
                            allEngines.add(engine);
                        }
                    }
                }
                runOnUiThread(() -> filterEnginesByGroup());
            }

            @Override
            public void error(String title, String msg, String code, Exception e) {

            }
        });
    }

    private void filterEnginesByGroup() {
        searchEngines.clear();
        if (allEngines != null) {
            for (SearchEngine engine : allEngines) {
                if ("全部".equals(groups[groupPosition]) || groups[groupPosition].equals(engine.getGroup())) {
                    searchEngines.add(engine);
                }
            }
        }
        if (searchEngines.isEmpty() && viewPager.getVisibility() != View.GONE) {
            viewPager.setVisibility(View.GONE);
        } else if (!searchEngines.isEmpty() && viewPager.getVisibility() == View.GONE) {
            viewPager.setVisibility(View.VISIBLE);
        }
        String searchEngine = getIntent().getStringExtra("searchEngine");
        int pos = 0;
        for (int i = 0; i < searchEngines.size(); i++) {
            if (searchEngines.get(i).getTitle().equals(searchEngine) && pos == 0) {
                searchEngines.get(i).setUse(true);
                pos = i;
            } else {
                searchEngines.get(i).setUse(false);
            }
        }
        if (pos == 0 && !searchEngines.isEmpty()) {
            searchEngines.get(0).setUse(true);
        }
        initViewPagerAdapter();
        bottomSearchAdapter.notifyDataSetChanged();
        int finalI = pos;
        viewPager.postDelayed(() -> {
            viewPager.setCurrentItem(finalI);
            bottomRecyclerView.scrollToPosition(finalI);
        }, 300);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusChanged(StatusEvent event) {
        boolean setCount = false;
        if (StringUtil.isEmpty(event.getGroup()) && "全部".equals(groups[groupPosition])) {
            setCount = true;
            bottomSearchAdapter.notifyItemChanged(event.getEnginePos());
        } else if (groups[groupPosition].equals(event.getGroup())) {
            setCount = true;
            bottomSearchAdapter.notifyItemChanged(event.getEnginePos());
        }
        if (setCount && searchEngines.size() > event.getEnginePos()) {
            int status = searchEngines.get(event.getEnginePos()).getStatus();
            if (status == 1) {
                loadingCount++;
            } else {
                loadingCount--;
            }
            if (loadingCount > 0) {
                titleView.setText(("聚合搜索中：" + loadingCount));
            } else {
                titleView.setText("无加载中的源");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onResume() {
        isOnPause = false;
        super.onResume();
    }

    @Override
    public void onPause() {
        isOnPause = true;
        super.onPause();
    }
}
