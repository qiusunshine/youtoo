package com.example.hikerview.event.rule;

/**
 * 作者：By 15968
 * 日期：On 2021/9/1
 * 时间：At 20:23
 */

public class ConfirmEvent {
    private String title;
    private String content;
    private String confirm;
    private String cancel;

    public ConfirmEvent() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public String getCancel() {
        return cancel;
    }

    public void setCancel(String cancel) {
        this.cancel = cancel;
    }
}