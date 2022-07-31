package com.example.hikerview.ui.rules.model;

/**
 * 作者：By 15968
 * 日期：On 2020/9/23
 * 时间：At 21:36
 */

public class DebuggingRule {
    private String url;
    private String listRule;
    private String nodeRule;
    private String code;
    private int scrollX;
    private int scrollY;
    private float textSize;
    private boolean useEditText;
    /**
     * 缩放比例
     */
    private float scale = 1.0f;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNodeRule() {
        return nodeRule;
    }

    public void setNodeRule(String nodeRule) {
        this.nodeRule = nodeRule;
    }

    public String getListRule() {
        return listRule;
    }

    public void setListRule(String listRule) {
        this.listRule = listRule;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }

    public int getScrollX() {
        return scrollX;
    }

    public void setScrollX(int scrollX) {
        this.scrollX = scrollX;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isUseEditText() {
        return useEditText;
    }

    public void setUseEditText(boolean useEditText) {
        this.useEditText = useEditText;
    }
}
