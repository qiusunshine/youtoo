package com.example.hikerview.ui.video;

import com.example.hikerview.model.DownloadRecord;
import com.xunlei.downloadlib.parameter.TorrentFileInfo;

/**
 * 作者：By 15968
 * 日期：On 2020/5/13
 * 时间：At 23:20
 */
public class VideoChapter {
    private String title;
    private String memoryTitle;
    private String url;
    private boolean use;
    private DownloadRecord downloadRecord;
    private String originalUrl;
    private String codeAndHeader;
    private String picUrl;
    private String extra;
    private String urlCache;
    private String cache0;
    private boolean cacheLoading;
    /**
     * 真正在二级列表中的位置
     */
    private int realPos;
    private TorrentFileInfo torrentFileInfo;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DownloadRecord getDownloadRecord() {
        return downloadRecord;
    }

    public void setDownloadRecord(DownloadRecord downloadRecord) {
        this.downloadRecord = downloadRecord;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCodeAndHeader() {
        return codeAndHeader;
    }

    public void setCodeAndHeader(String codeAndHeader) {
        this.codeAndHeader = codeAndHeader;
    }

    public String getMemoryTitle() {
        return memoryTitle;
    }

    public void setMemoryTitle(String memoryTitle) {
        this.memoryTitle = memoryTitle;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getUrlCache() {
        return urlCache;
    }

    public String getCache() {
        //只能用一次
        String cache = urlCache;
        setUrlCache(null);
        return cache;
    }

    public void setUrlCache(String urlCache) {
        setCacheLoading(false);
        this.urlCache = urlCache;
    }

    public boolean isCacheLoading() {
        return cacheLoading;
    }

    public void setCacheLoading(boolean cacheLoading) {
        this.cacheLoading = cacheLoading;
    }

    public int getRealPos() {
        return realPos;
    }

    public void setRealPos(int realPos) {
        this.realPos = realPos;
    }

    public String getCache0() {
        return cache0;
    }

    public void setCache0(String cache0) {
        this.cache0 = cache0;
    }

    public TorrentFileInfo getTorrentFileInfo() {
        return torrentFileInfo;
    }

    public void setTorrentFileInfo(TorrentFileInfo torrentFileInfo) {
        this.torrentFileInfo = torrentFileInfo;
    }
}