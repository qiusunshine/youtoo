package com.example.hikerview.ui.browser.model;

import java.util.Map;

/**
 * 作者：By hdy
 * 日期：On 2018/8/25
 * 时间：At 22:40
 */

public class VideoTask {
    private String sourceUrl;
    private Map<String, String> requestHeaders;
    private String title;
    private String url;
    private int pos;
    private long timestamp;
    private String method;

    public VideoTask(){

    }

    public VideoTask(String title, String url){
        this.title = title;
        this.url = url;
    }

    public VideoTask(String title, String url, int pos) {
        this.title = title;
        this.url = url;
        this.pos = pos;
    }

    public VideoTask(Map<String, String> requestHeaders, String title, String url) {
        this.requestHeaders = requestHeaders;
        this.title = title;
        this.url = url;
    }

    public VideoTask(Map<String, String> requestHeaders, String method, String title, String url) {
        this.requestHeaders = requestHeaders;
        this.title = title;
        this.url = url;
        this.method = method;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
