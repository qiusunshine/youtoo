package com.example.hikerview.event.home;

/**
 * 作者：By 15968
 * 日期：On 2021/8/25
 * 时间：At 21:57
 */

public class LoadingEvent {
    public LoadingEvent(String text, boolean show) {
        this.text = text;
        this.show = show;
    }

    private String text;

    private boolean show;

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}