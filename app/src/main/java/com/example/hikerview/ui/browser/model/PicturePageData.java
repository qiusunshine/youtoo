package com.example.hikerview.ui.browser.model;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/9/10
 * 时间：At 23:13
 */

public class PicturePageData {
    private List<String> pics;
    private int preSize;
    private int pageNow;

    public PicturePageData(List<String> pics, String title) {
        this.pics = pics;
        this.title = title;
    }

    private String title;

    public List<String> getPics() {
        return pics;
    }

    public void setPics(List<String> pics) {
        this.pics = pics;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPreSize() {
        return preSize;
    }

    public void setPreSize(int preSize) {
        this.preSize = preSize;
    }

    public int getPageNow() {
        return pageNow;
    }

    public void setPageNow(int pageNow) {
        this.pageNow = pageNow;
    }
}