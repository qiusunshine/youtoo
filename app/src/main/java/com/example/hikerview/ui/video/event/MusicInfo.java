package com.example.hikerview.ui.video.event;

import android.graphics.Bitmap;

/**
 * 作者：By 15968
 * 日期：On 2021/10/10
 * 时间：At 23:53
 */

public class MusicInfo {
    public MusicInfo(String title, Bitmap bitmap, boolean pause) {
        this.title = title;
        this.bitmap = bitmap;
        this.pause = pause;
    }

    private String title;
    private Bitmap bitmap;
    private boolean pause;

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}