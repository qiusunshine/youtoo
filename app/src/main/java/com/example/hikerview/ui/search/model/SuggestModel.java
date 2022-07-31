package com.example.hikerview.ui.search.model;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.annimon.stream.Collectors;
import com.annimon.stream.ComparatorCompat;
import com.annimon.stream.Stream;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 作者：By 15968
 * 日期：On 2020/6/14
 * 时间：At 11:36
 */
public class SuggestModel {
    private static final String TAG = "SuggestModel";
    private static boolean isLoading = false;

    public static void getRecommands(Context context, String word, OnSuggestFetchedListener suggestFetchedListener) {
        if (isLoading) {
            //防抖
            return;
        }
        isLoading = true;
        List<String> hisList = SearchHistroyModel.getHisList(context);
        if (StringUtil.isEmpty(word) || word.startsWith("http")) {
            suggestFetchedListener.onSuccess(hisList);
            isLoading = false;
            return;
        }
        List<String> list = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(hisList)) {
            for (String s : hisList) {
                if (s.contains(word)) {
                    list.add(s);
                }
            }
        }
        String url = "http://suggestion.baidu.com/su?wd=" + word + "&p=3&t=" + new Date().getTime();
        Log.i("看看看", "getRecommands: " + url);
        CodeUtil.get(url, new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                List<String> recommands = getRecommands(s);
                List<String> result;
                if (CollectionUtil.isNotEmpty(recommands)) {
                    Set<String> suggests = new HashSet<>(recommands);
                    suggests.addAll(list);
                    result = new ArrayList<>(suggests);
                } else {
                    result = list;
                }
                suggestFetchedListener.onSuccess(Stream.of(result).sorted(ComparatorCompat.comparing(String::length)).collect(Collectors.toList()));
                isLoading = false;
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                isLoading = false;
                suggestFetchedListener.onFailure(errorCode, msg);
            }
        });
    }

    private static List<String> getRecommands(String s) {
        List<String> list = new ArrayList<>();
        try {
            s = s.replace("window.baidu.sug(", "").replace(");", "");
            JSONArray jss = JSON.parseObject(s).getJSONArray("s");
            for (int i = 0; i < jss.size(); i++) {
                list.add(jss.get(i).toString());
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public interface OnSuggestFetchedListener {
        void onSuccess(List<String> suggests);

        void onFailure(int errorCode, String msg);
    }
}
