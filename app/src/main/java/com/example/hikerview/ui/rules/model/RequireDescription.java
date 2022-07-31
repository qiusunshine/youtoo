package com.example.hikerview.ui.rules.model;

/**
 * 作者：By 15968
 * 日期：On 2021/12/2
 * 时间：At 12:42
 */

public class RequireDescription {

    public RequireDescription(String url, int version) {
        this.url = url;
        this.version = version;
    }

    private String url;
    private int version;

    public RequireDescription() {
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}