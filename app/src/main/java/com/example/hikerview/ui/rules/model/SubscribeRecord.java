package com.example.hikerview.ui.rules.model;

import java.util.Date;

/**
 * 作者：By 15968
 * 日期：On 2021/3/3
 * 时间：At 23:14
 */
public class SubscribeRecord {

    private String title;

    private String url;

    private Date createDate;

    private Date modifyDate;

    /**
     * 成功次数
     */
    private int successCount;

    /**
     * 失败次数
     */
    private int errorCount;

    /**
     * 所含规则数
     */
    private int rulesCount;

    /**
     * 上次更新规则数
     */
    private int lastUpdateCount;

    /**
     * 上次操作是否成功
     */
    private boolean lastUpdateSuccess;

    /**
     * 是否启用
     */
    private boolean use;

    /**
     * 是否开启只更新不新增
     */
    private boolean onlyUpdate;

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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getRulesCount() {
        return rulesCount;
    }

    public void setRulesCount(int rulesCount) {
        this.rulesCount = rulesCount;
    }

    public int getLastUpdateCount() {
        return lastUpdateCount;
    }

    public void setLastUpdateCount(int lastUpdateCount) {
        this.lastUpdateCount = lastUpdateCount;
    }

    public boolean isLastUpdateSuccess() {
        return lastUpdateSuccess;
    }

    public void setLastUpdateSuccess(boolean lastUpdateSuccess) {
        this.lastUpdateSuccess = lastUpdateSuccess;
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    public boolean isOnlyUpdate() {
        return onlyUpdate;
    }

    public void setOnlyUpdate(boolean onlyUpdate) {
        this.onlyUpdate = onlyUpdate;
    }
}
