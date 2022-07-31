package com.example.hikerview.event.web;

/**
 * 作者：By 15968
 * 日期：On 2022/6/1
 * 时间：At 18:08
 */

public class FindMagnetsEvent {
    public FindMagnetsEvent(String data) {
        this.data = data;
    }

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}