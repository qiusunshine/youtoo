package com.example.hikerview.ui.browser.model;

import com.example.hikerview.constants.Media;

/**
 * 作者：By 15968
 * 日期：On 2019/10/7
 * 时间：At 13:15
 */
public class DetectedMediaResult {
    private String url;
    private String title;
    private Media mediaType;
    private long timestamp;
    private boolean clicked;

    public DetectedMediaResult(String url) {
        this.url = url;
        this.mediaType = new Media(Media.OTHER);
    }

    public DetectedMediaResult(String url, String title) {
        this.url = url;
        this.title = title;
        this.mediaType = new Media(Media.OTHER);
    }

    public DetectedMediaResult() {
        this.mediaType = new Media(Media.OTHER);
    }

    public Media getMediaType() {
        return mediaType;
    }

    public void setMediaType(Media mediaType) {
        this.mediaType = mediaType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
