package com.example.hikerview.ui.video.model;

import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2021/10/31
 * 时间：At 21:05
 */

public class PlayData {
    /**
     * 单线路
     */
    private String url;
    /**
     * 多线路
     */
    private List<String> urls;

    /**
     * 线路名字
     */
    private List<String> names;

    private List<Map<String, String>> headers;

    /**
     * 字幕
     */
    private String subtitle;

    /**
     * 弹幕
     */
    private String danmu;

    public List<String> getAudioUrls() {
        return audioUrls;
    }

    public void setAudioUrls(List<String> audioUrls) {
        this.audioUrls = audioUrls;
    }

    private List<String> audioUrls;

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<Map<String, String>> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Map<String, String>> headers) {
        this.headers = headers;
    }

    public String getDanmu() {
        return danmu;
    }

    public void setDanmu(String danmu) {
        this.danmu = danmu;
    }
}