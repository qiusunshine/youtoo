package com.example.hikerview.ui.video.model;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/6/13
 * 时间：At 20:34
 */

public class RemotePlaySourceBatchData {
    public RemotePlaySourceBatchData(List<RemotePlaySource> playSources) {
        this.playSources = playSources;
    }

    private List<RemotePlaySource> playSources;

    public List<RemotePlaySource> getPlaySources() {
        return playSources;
    }

    public void setPlaySources(List<RemotePlaySource> playSources) {
        this.playSources = playSources;
    }
}