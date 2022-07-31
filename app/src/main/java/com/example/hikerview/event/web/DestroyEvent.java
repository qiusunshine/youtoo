package com.example.hikerview.event.web;

/**
 * 作者：By 15968
 * 日期：On 2021/9/20
 * 时间：At 14:29
 */

public class DestroyEvent {
    public DestroyEvent(String url, String mode, String ticket) {
        this.url = url;
        this.mode = mode;
        this.ticket = ticket;
    }

    private String url;
    private String mode;
    private String ticket;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}