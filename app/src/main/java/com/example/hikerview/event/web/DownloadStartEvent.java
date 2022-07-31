package com.example.hikerview.event.web;


import com.example.hikerview.ui.view.HorizontalWebView;

/**
 * 作者：By 15968
 * 日期：On 2021/11/25
 * 时间：At 15:51
 */

public class DownloadStartEvent {

    public DownloadStartEvent(HorizontalWebView horizontalWebView, String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        this.horizontalWebView = horizontalWebView;
        this.url = url;
        this.userAgent = userAgent;
        this.contentDisposition = contentDisposition;
        this.mimetype = mimetype;
        this.contentLength = contentLength;
    }

    private HorizontalWebView horizontalWebView;
    private String url;
    private String userAgent;
    private String contentDisposition;
    private String mimetype;
    private long contentLength;

    public HorizontalWebView getHorizontalWebView() {
        return horizontalWebView;
    }

    public void setHorizontalWebView(HorizontalWebView horizontalWebView) {
        this.horizontalWebView = horizontalWebView;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}