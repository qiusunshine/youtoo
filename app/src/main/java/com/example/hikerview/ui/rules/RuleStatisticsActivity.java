package com.example.hikerview.ui.rules;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Objects;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.ui.base.BaseSlideActivity;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;

import org.litepal.LitePal;

/**
 * 作者：By 15968
 * 日期：On 2020/7/26
 * 时间：At 14:54
 */

public class RuleStatisticsActivity extends BaseSlideActivity {
    private static final String TAG = "RuleStatisticsActivity";

    @Override
    protected View getBackgroundView() {
        return findView(R.id.ad_list_window);
    }

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activit_rules_statistics;
    }

    @Override
    protected void initView2() {
        //初始化高度
        int marginTop = MyStatusBarUtil.getStatusBarHeight(getContext()) + DisplayUtil.dpToPx(getContext(), 86);
        View bg = findView(R.id.ad_list_bg);
        findView(R.id.ad_list_window).setOnClickListener(view -> finish());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bg.getLayoutParams();
        layoutParams.topMargin = marginTop;
        bg.setLayoutParams(layoutParams);

    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        LitePal.findAllAsync(ArticleListRule.class).listen(list -> {
            if (CollectionUtil.isNotEmpty(list)) {
                int allCount = list.size();
                int hasHomeCount = 0, hasSearchCount = 0;
                int superRuleCount = 0, dynamicRuleCount = 0, hasJsCount = 0;
                int groupCount = (int) Stream.of(list).map(ArticleListRule::getGroup).filter(Objects::nonNull).distinct().count();
                for (ArticleListRule articleListRule : list) {
                    if (StringUtil.isNotEmpty(articleListRule.getFind_rule())) {
                        hasHomeCount++;
                    }
                    if (StringUtil.isNotEmpty(articleListRule.getSearch_url())) {
                        hasSearchCount++;
                    }
                    if (isSuperRule(articleListRule)) {
                        superRuleCount++;
                    }
                    if (isDynamicRule(articleListRule)) {
                        dynamicRuleCount++;
                    }
                    if (hasJsRule(articleListRule)) {
                        hasJsCount++;
                    }
                }
                int finalHasHomeCount = hasHomeCount;
                int finalHasSearchCount = hasSearchCount;
                int finalSuperRuleCount = superRuleCount;
                int finalDynamicRuleCount = dynamicRuleCount;
                int finalHasJsCount = hasJsCount;
                runOnUiThread(()->{

                    ((TextView) findView(R.id.main_menu_head_collection_text)).setText(String.valueOf(allCount));
                    ((TextView) findView(R.id.main_menu_head_bookmark_text)).setText(String.valueOf(finalHasHomeCount));
                    ((TextView) findView(R.id.main_menu_head_history_text)).setText(String.valueOf(finalHasSearchCount));
                    ((TextView) findView(R.id.main_menu_head_download_text)).setText(String.valueOf(groupCount));

                    ((TextView) findView(R.id.main_menu_head_super_text)).setText(String.valueOf(finalSuperRuleCount));
                    ((TextView) findView(R.id.main_menu_head_monster_text)).setText(String.valueOf(finalDynamicRuleCount));
                    ((TextView) findView(R.id.main_menu_head_js_text)).setText(String.valueOf(finalHasJsCount));

                    findView(R.id.main_menu_head_collection).setOnClickListener(this::click);
                    findView(R.id.main_menu_head_bookmark).setOnClickListener(this::click);
                    findView(R.id.main_menu_head_history).setOnClickListener(this::click);
                    findView(R.id.main_menu_head_download).setOnClickListener(this::click);

                    findView(R.id.main_menu_head_super).setOnClickListener(this::click);
                    findView(R.id.main_menu_head_monster).setOnClickListener(this::click);
                    findView(R.id.main_menu_head_js).setOnClickListener(this::click);
                });
            }
        });
    }

    private void click(View v) {
        ToastMgr.shortBottomCenter(getContext(), "小棉袄咋这么棒呢！");
    }

    private boolean isSuperRule(ArticleListRule articleListRule) {
        if (StringUtil.isNotEmpty(articleListRule.getFind_rule()) && articleListRule.getFind_rule().startsWith("js:")) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getSearchFind()) && articleListRule.getSearchFind().startsWith("js:")) {
            return true;
        }

        return StringUtil.isNotEmpty(articleListRule.getSdetail_find_rule()) && articleListRule.getSdetail_find_rule().startsWith("js:");
    }

    private boolean isDynamicRule(ArticleListRule articleListRule) {
        if (StringUtil.isNotEmpty(articleListRule.getFind_rule()) && articleListRule.getFind_rule().contains("@lazyRule=")) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getSearchFind()) && articleListRule.getSearchFind().contains("@lazyRule=")) {
            return true;
        }

        return false;
    }

    private boolean hasJsRule(ArticleListRule articleListRule) {
        if (StringUtil.isNotEmpty(articleListRule.getFind_rule()) && articleListRule.getFind_rule().contains(".js:")) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getSearchFind()) && articleListRule.getSearchFind().contains(".js:")) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getUrl()) && articleListRule.getUrl().contains(".js:")) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getSearch_url()) && articleListRule.getSearch_url().contains(".js:")) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getPreRule())) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getSdetail_find_rule())) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getFind_rule()) && articleListRule.getFind_rule().contains("@rule=")) {
            return true;
        }

        if (StringUtil.isNotEmpty(articleListRule.getSearchFind()) && articleListRule.getSearchFind().contains("@rule=")) {
            return true;
        }

        return false;
    }
}
