package com.example.hikerview.ui.base;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/10/31
 * 时间：At 15:00
 */
public interface BaseCallback<T> {


    void bindArrayToView(String actionType, List<T> data);

    void bindObjectToView(String actionType, T data);

    void error(String title, String msg, String code, Exception e);

    void loading(boolean isLoading);

}