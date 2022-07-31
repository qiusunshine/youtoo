package com.example.hikerview.ui.search.engine;

import android.content.Context;

import com.annimon.stream.Stream;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseModel;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.utils.StringUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2019/10/3
 * 时间：At 22:13
 */
public class SearchEngineModel extends BaseModel<ArticleListRule> {
    @Override
    public void process(String actionType, BaseCallback<ArticleListRule> baseCallback) {
        try {
            LitePal.findAllAsync(ArticleListRule.class).listen(list -> {
                if (CollectionUtil.isEmpty(list)) {
                    baseCallback.bindArrayToView(actionType, new ArrayList<>());
                } else {
                    baseCallback.bindArrayToView(actionType,
                            Stream.of(list)
                                    .filter(it -> StringUtil.isNotEmpty(it.getSearch_url())
                                            && StringUtil.isEmpty(it.getSearchFind()))
                                    .toList());
                }
            });
        } catch (Exception e) {
            baseCallback.error(e.getMessage(), e.getMessage(), e.getMessage(), e);
        }
    }

    public void add(Context context, ArticleListRule articleListRule) {
        List<ArticleListRule> articleListRules = null;
        try {
            articleListRules = LitePal.where("search_url = ?", articleListRule.getSearch_url()).limit(1).find(ArticleListRule.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(articleListRules)) {
            articleListRules.get(0).setTitle(articleListRule.getTitle());
            articleListRules.get(0).setGroup(articleListRule.getGroup());
            articleListRules.get(0).save();
        } else {
            articleListRule.save();
        }
    }

    public void delete(Context context, ArticleListRule articleListRule) {
        List<ArticleListRule> articleListRules = null;
        try {
            articleListRules = LitePal.where("search_url = ?", articleListRule.getSearch_url()).limit(1).find(ArticleListRule.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(articleListRules)) {
            articleListRules.get(0).delete();
        }
    }
}
