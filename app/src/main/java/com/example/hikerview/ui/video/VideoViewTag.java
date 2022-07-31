package com.example.hikerview.ui.video;

/**
 * 作者：By 15968
 * 日期：On 2021/1/10
 * 时间：At 12:18
 */

public class VideoViewTag {
    private int delta;

    private int sourceLeft;

    private int sourceRight;

    private int sourceTop;

    private int sourceBottom;

    public VideoViewTag(int delta, int sourceLeft, int sourceRight) {
        this.delta = delta;
        this.sourceLeft = sourceLeft;
        this.sourceRight = sourceRight;
    }

    public int getSourceRight() {
        return sourceRight;
    }

    public void setSourceRight(int sourceRight) {
        this.sourceRight = sourceRight;
    }

    public int getSourceLeft() {
        return sourceLeft;
    }

    public void setSourceLeft(int sourceLeft) {
        this.sourceLeft = sourceLeft;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public int getSourceBottom() {
        return sourceBottom;
    }

    public void setSourceBottom(int sourceBottom) {
        this.sourceBottom = sourceBottom;
    }

    public int getSourceTop() {
        return sourceTop;
    }

    public void setSourceTop(int sourceTop) {
        this.sourceTop = sourceTop;
    }
}
