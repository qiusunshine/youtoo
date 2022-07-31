package com.example.hikerview.event.home;

/**
 * 作者：By 15968
 * 日期：On 2021/6/23
 * 时间：At 21:34
 */

public class SetPageTitleEvent {
    public SetPageTitleEvent(String title) {
        this.title = title;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
