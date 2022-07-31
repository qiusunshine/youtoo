package com.example.hikerview.ui.rules.model;

/**
 * 作者：By 15968
 * 日期：On 2020/11/21
 * 时间：At 10:07
 */

public class DetailPageRule {

    private String title;
    private String data;
    private String picUrl;

    public DetailPageRule() {
    }

    public DetailPageRule(String title, String data, String picUrl) {
        this.title = title;
        this.data = data;
        this.picUrl = picUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
