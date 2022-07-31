package com.example.hikerview.event.web;

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 19:23
 */

public class UpdateBgEvent {
    public UpdateBgEvent(String path) {
        this.path = path;
    }

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}