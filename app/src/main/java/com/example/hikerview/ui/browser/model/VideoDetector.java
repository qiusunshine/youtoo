package com.example.hikerview.ui.browser.model;

import android.content.Context;

import com.example.hikerview.constants.MediaType;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2022/5/23
 * 时间：At 11:21
 */

public interface VideoDetector {
    void putIntoXiuTanLiked(Context context, String dom, String url);
    List<DetectedMediaResult> getDetectedMediaResults(MediaType mediaType);
    void addTask(VideoTask video);
    void startDetect();
    void reset();
}