package com.example.hikerview.ui.search.model;


import com.alibaba.fastjson.annotation.JSONField;
import com.example.hikerview.model.SearchEngineDO;

import org.litepal.LitePal;

/**
 * 作者：By hdy
 * 日期：On 2018/2/11
 * 时间：At 16:36
 */
public class SearchRuleJO {

    @JSONField(ordinal = 1)
    private String title;
    @JSONField(ordinal = 10)
    private String find_rule;
    @JSONField(ordinal = 13)
    private int order;
    @JSONField(ordinal = 14)
    private String search_url;
    @JSONField(ordinal = 15)
    private String titleColor;
    @JSONField(ordinal = 16)
    private String group;
    @JSONField(ordinal = 17)
    private String adBlockUrls;

    public SearchRuleJO() {
    }

    public SearchRuleJO(SearchEngineDO rule) {
        setTitle(rule.getTitle());
        setFind_rule(rule.getFindRule());
        setOrder(rule.getOrder());
        setSearch_url(rule.getSearch_url());
        setTitleColor(rule.getTitleColor());
        setGroup(rule.getGroup());
    }

    public SearchEngineDO toEngineDO() {
        SearchEngineDO engineDO = LitePal.where("title = ?", getTitle()).findFirst(SearchEngineDO.class);
        if (engineDO == null) {
            engineDO = new SearchEngineDO();
        }
        return convertToDO(engineDO, true);
    }

    public SearchEngineDO toEngineDO(boolean override) {
        SearchEngineDO engineDO = LitePal.where("title = ?", getTitle()).findFirst(SearchEngineDO.class);
        if (engineDO == null) {
            engineDO = new SearchEngineDO();
        }
        return convertToDO(engineDO, override);
    }

    private SearchEngineDO convertToDO(SearchEngineDO engineDO, boolean override) {
        engineDO.setTitle(getTitle());
        engineDO.setFindRule(getFind_rule());
        if (override) {
            engineDO.setOrder(getOrder());
            engineDO.setTitleColor(getTitleColor());
            engineDO.setGroup(getGroup());
        }
        engineDO.setSearch_url(getSearch_url());
        return engineDO;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFind_rule(String find_rule) {
        this.find_rule = find_rule;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getSearch_url() {
        return search_url;
    }

    public void setSearch_url(String search_url) {
        this.search_url = search_url;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public String getAdBlockUrls() {
        return adBlockUrls;
    }

    public void setAdBlockUrls(String adBlockUrls) {
        this.adBlockUrls = adBlockUrls;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFind_rule() {
        return find_rule;
    }
}
