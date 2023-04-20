package com.example.hikerview.ui.browser.model;

/**
 * 作者：By 15968
 * 日期：On 2023/1/8
 * 时间：At 11:22
 */

public class JSUpdateDTO {
    /**
     * 更新地址
     */
    private String url;
    /**
     * 检查时间 checkTime
     */
    private long ct;
    /**
     * 实际更新时间，updateTime
     */
    private long ut;

    public JSUpdateDTO() {
    }

    public JSUpdateDTO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCt() {
        return ct;
    }

    public void setCt(long ct) {
        this.ct = ct;
    }

    public long getUt() {
        return ut;
    }

    public void setUt(long ut) {
        this.ut = ut;
    }
}