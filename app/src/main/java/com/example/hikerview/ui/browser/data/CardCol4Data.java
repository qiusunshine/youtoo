package com.example.hikerview.ui.browser.data;

/**
 * 作者：By 15968
 * 日期：On 2021/7/18
 * 时间：At 20:22
 */

public class CardCol4Data {
    public CardCol4Data() {
    }

    public CardCol4Data(String operation, int icon) {
        this.operation = operation;
        this.icon = icon;
    }

    private String operation;

    private int icon;

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}