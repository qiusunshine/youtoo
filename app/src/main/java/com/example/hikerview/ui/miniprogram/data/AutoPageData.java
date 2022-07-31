package com.example.hikerview.ui.miniprogram.data;

import com.example.hikerview.ui.home.model.ArticleList;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/9/16
 * 时间：At 22:19
 */

public class AutoPageData {
    private List<ArticleList> nextData;
    private String CUrl;
    private String MTitle;
    private int currentPos;
    private boolean noRecordHistory;

    public AutoPageData() {

    }

    public AutoPageData(List<ArticleList> nextData, String CUrl, String MTitle,
                        int currentPos, boolean noRecordHistory) {
        this.nextData = nextData;
        this.CUrl = CUrl;
        this.MTitle = MTitle;
        this.currentPos = currentPos;
        this.noRecordHistory = noRecordHistory;
    }

    public List<ArticleList> getNextData() {
        return nextData;
    }

    public void setNextData(List<ArticleList> nextData) {
        this.nextData = nextData;
    }

    public String getMTitle() {
        return MTitle;
    }

    public void setMTitle(String MTitle) {
        this.MTitle = MTitle;
    }

    public String getCUrl() {
        return CUrl;
    }

    public void setCUrl(String CUrl) {
        this.CUrl = CUrl;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }

    public boolean isNoRecordHistory() {
        return noRecordHistory;
    }

    public void setNoRecordHistory(boolean noRecordHistory) {
        this.noRecordHistory = noRecordHistory;
    }
}