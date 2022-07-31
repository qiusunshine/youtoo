package com.example.hikerview.ui.rules.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 作者：By 15968
 * 日期：On 2021/11/24
 * 时间：At 16:59
 */

public class RequireItem {
    @JSONField(ordinal = 1)
    private String url;
    @JSONField(ordinal = 2)
    private String file;
    @JSONField(ordinal = 3)
    private String proxy;
    @JSONField(ordinal = 4)
    private long accessTime;

    public RequireItem(String url, String proxy, String file, long accessTime) {
        this.url = url;
        this.proxy = proxy;
        this.file = file;
        this.accessTime = accessTime;
    }

    public RequireItem() {
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
}