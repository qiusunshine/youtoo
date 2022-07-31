package com.example.hikerview.event;

/**
 * 作者：By 15968
 * 日期：On 2019/12/13
 * 时间：At 8:11
 */
public class OnUrlChangeEvent {
    private String url;

    public OnUrlChangeEvent(String url, boolean newWindow, boolean useNotNow) {
        this.url = url;
        this.newWindow = newWindow;
        this.useNotNow = useNotNow;
    }

    private boolean newWindow;
    private boolean useNotNow;

    public OnUrlChangeEvent(String url) {
        this.url = url;
    }

    public OnUrlChangeEvent() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isUseNotNow() {
        return useNotNow;
    }

    public void setUseNotNow(boolean useNotNow) {
        this.useNotNow = useNotNow;
    }

    public boolean isNewWindow() {
        return newWindow;
    }

    public void setNewWindow(boolean newWindow) {
        this.newWindow = newWindow;
    }
}
