package com.example.hikerview.ui.browser.model;

import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2021/9/29
 * 时间：At 23:01
 */

public class DetailPage {
    private String title;
    private String rule;
    private String url;
    private String group;
    private String col_type;
    private String findRule;
    private String preRule;
    private Map<String, Object> extra;
    private String pages;
    private String pic_url;
    private String pic;
    private String picUrl;
    private String ua;

    public String getPreRule() {
        return preRule;
    }

    public void setPreRule(String preRule) {
        this.preRule = preRule;
    }

    public String getFindRule() {
        return findRule;
    }

    public void setFindRule(String findRule) {
        this.findRule = findRule;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getCol_type() {
        return col_type;
    }

    public void setCol_type(String col_type) {
        this.col_type = col_type;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getImageUrl() {
        if (pic_url != null && !pic_url.isEmpty()) {
            return pic_url;
        }
        if (pic != null && !pic.isEmpty()) {
            return pic;
        }
        if (picUrl != null && !picUrl.isEmpty()) {
            return picUrl;
        }
        return null;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }
}