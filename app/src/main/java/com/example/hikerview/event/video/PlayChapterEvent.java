package com.example.hikerview.event.video;

/**
 * 作者：By 15968
 * 日期：On 2021/8/19
 * 时间：At 9:18
 */

public class PlayChapterEvent {

    private int position;

    public PlayChapterEvent(int position, String title) {
        this.position = position;
        this.title = title;
    }

    private String title;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}