package com.example.hikerview.event.web;

/**
 * 作者：By 15968
 * 日期：On 2021/7/30
 * 时间：At 21:16
 */

public class OnImgHrefFindEvent {
    public OnImgHrefFindEvent(String url) {
        this.url = url;
    }

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}