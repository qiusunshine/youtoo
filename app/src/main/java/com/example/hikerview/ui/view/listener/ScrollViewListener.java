package com.example.hikerview.ui.view.listener;

import com.example.hikerview.ui.view.ObservableScrollView;

/**
 * 作者：By 15968
 * 日期：On 2020/2/12
 * 时间：At 18:39
 */
public interface ScrollViewListener {
    void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy);
}
