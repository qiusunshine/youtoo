package com.example.hikerview.ui.detail.model;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/9/21
 * 时间：At 16:47
 */

public class SelectExtra {
    private List<String> options;
    private String title;
    private int col = 3;
    private String js;

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getJs() {
        return js;
    }

    public void setJs(String js) {
        this.js = js;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}