package com.example.hikerview.event.web;

/**
 * 作者：By 15968
 * 日期：On 2022/5/16
 * 时间：At 11:50
 */

public class BlobDownloadEvent {
    private String url;

    public BlobDownloadEvent(String url, String fileName, String headerMap, String result) {
        this.url = url;
        this.fileName = fileName;
        this.headerMap = headerMap;
        this.result = result;
    }

    private String fileName;

    public BlobDownloadEvent(String url, String result) {
        this.url = url;
        this.result = result;
    }

    public BlobDownloadEvent(String url, String headerMap, String result) {
        this.url = url;
        this.headerMap = headerMap;
        this.result = result;
    }

    private String headerMap;

    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(String headerMap) {
        this.headerMap = headerMap;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}