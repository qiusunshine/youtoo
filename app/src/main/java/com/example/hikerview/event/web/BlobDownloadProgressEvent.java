package com.example.hikerview.event.web;

/**
 * 作者：By 15968
 * 日期：On 2022/5/16
 * 时间：At 11:50
 */

public class BlobDownloadProgressEvent {
    private String url;

    public BlobDownloadProgressEvent(String url, String progress) {
        this.url = url;
        this.progress = progress;
    }
    private String progress;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }
}