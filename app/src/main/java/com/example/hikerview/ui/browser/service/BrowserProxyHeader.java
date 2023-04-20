package com.example.hikerview.ui.browser.service;

/**
 * 作者：By 15968
 * 日期：On 2023/4/19
 * 时间：At 10:14
 */

public class BrowserProxyHeader {
  private String match;
  private String replace;

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }
}