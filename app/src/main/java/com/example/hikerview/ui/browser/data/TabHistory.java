package com.example.hikerview.ui.browser.data;

/**
 * 作者：By 15968
 * 日期：On 2022/4/26
 * 时间：At 15:50
 */

public class TabHistory {
    private String url;
    private boolean use;

    public TabHistory() {
    }

    public TabHistory(String url, boolean use) {
        this.url = url;
        this.use = use;
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}