package com.example.hikerview.ui.browser.model;

import androidx.annotation.NonNull;

import com.example.hikerview.model.MovieRule;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.utils.StringUtil;

/**
 * 作者：By 15968
 * 日期：On 2019/10/10
 * 时间：At 21:26
 */
public class SearchEngine implements Comparable<SearchEngine> {
    private String title;
    private String search_url;
    private String titleColor;
    private boolean use;
    private String findRule;
    private int status;
    private String group;
    private String detail_col_type;
    private String detail_find_rule;
    private String ua;
    private String preRule;
    //    @Column(ignore = true)
    private int order;

    public SearchEngine(String title, String search_url) {
        this.title = title;
        this.search_url = search_url;
    }

    public SearchEngine() {
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    public String getSearch_url() {
        return search_url;
    }

    public void setSearch_url(String search_url) {
        this.search_url = search_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public String getFindRule() {
        return findRule;
    }

    public void setFindRule(String findRule) {
        this.findRule = findRule;
    }


    public MovieRule toMovieRule() {
        MovieRule movieRule = new MovieRule();
        movieRule.setTitle(getTitle());
        movieRule.setSearchUrl(getSearch_url());
        movieRule.setSearchFind(getFindRule());
        movieRule.setBaseUrl(StringUtil.getBaseUrl(getSearch_url()));
        return movieRule;
    }

    public static SearchEngine fromArticleRule(ArticleListRule articleListRule){
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
        searchEngine.setPreRule(articleListRule.getPreRule());
        return searchEngine;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getGroup() {
        if (group == null) {
            return "";
        }
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDetail_col_type() {
        return detail_col_type;
    }

    public void setDetail_col_type(String detail_col_type) {
        this.detail_col_type = detail_col_type;
    }

    public String getDetail_find_rule() {
        return detail_find_rule;
    }

    public void setDetail_find_rule(String detail_find_rule) {
        this.detail_find_rule = detail_find_rule;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getPreRule() {
        return preRule;
    }

    public void setPreRule(String preRule) {
        this.preRule = preRule;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(@NonNull SearchEngine articleListRule) {
        int n1 = getGroup().length();
        int n2 = articleListRule.getGroup().length();
        int min = Math.min(n1, n2);
        if (min == 0 && (n2 - n1) != 0) {
            return n2 - n1;
        }
        int g = this.getGroup().compareTo(articleListRule.getGroup());
        if (g == 0) {
            return this.getOrder() - articleListRule.getOrder();
        } else {
            return g;
        }
    }
}
