package com.example.hikerview.ui.browser.data;

/**
 * 作者：By 15968
 * 日期：On 2021/4/1
 * 时间：At 22:48
 */

public class CardMultiData {
    public CardMultiData() {
    }

    private String operation1;

    private int opIcon1;

    private String operation2;

    private int opIcon2;

    public CardMultiData(String operation1, int opIcon1, String operation2, int opIcon2) {
        this.operation1 = operation1;
        this.opIcon1 = opIcon1;
        this.operation2 = operation2;
        this.opIcon2 = opIcon2;
    }

    public int getOpIcon2() {
        return opIcon2;
    }

    public void setOpIcon2(int opIcon2) {
        this.opIcon2 = opIcon2;
    }

    public String getOperation2() {
        return operation2;
    }

    public void setOperation2(String operation2) {
        this.operation2 = operation2;
    }

    public int getOpIcon1() {
        return opIcon1;
    }

    public void setOpIcon1(int opIcon1) {
        this.opIcon1 = opIcon1;
    }

    public String getOperation1() {
        return operation1;
    }

    public void setOperation1(String operation1) {
        this.operation1 = operation1;
    }
}
