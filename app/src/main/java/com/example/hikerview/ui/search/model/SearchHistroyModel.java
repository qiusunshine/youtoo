package com.example.hikerview.ui.search.model;

import android.content.Context;
import android.text.TextUtils;

import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.utils.PreferenceMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2019/11/9
 * 时间：At 14:49
 */
public class SearchHistroyModel {
    private static final String HISTROY_KEY = "search_his";

    public static List<String> getHisList(Context context) {
        if(SettingConfig.noWebHistory){
            return new ArrayList<>();
        }
        String[] hisArray = getHisArray(context);
        if (hisArray == null || hisArray.length <= 0) {
            return new ArrayList<>();
        }
        List<String> hisList = new ArrayList<>(hisArray.length);
        //反序
        for (int i = hisArray.length - 1; i >= 0; i--) {
            hisList.add(hisArray[i]);
        }
        return hisList;
    }

    private static String[] getHisArray(Context context) {
        String hisList = PreferenceMgr.getString(context, HISTROY_KEY, "");
        if (TextUtils.isEmpty(hisList)) {
            return null;
        } else {
            return hisList.split("&&");
        }
    }

    public static void addHis(Context context, String key) {
        if (TextUtils.isEmpty(key) || key.startsWith("http") || key.startsWith("file://")) {
            return;
        }
        String[] hisList = getHisArray(context);
        if (hisList == null || hisList.length <= 0) {
            PreferenceMgr.put(context, HISTROY_KEY, key);
        } else {
            List<String> hisLists = new ArrayList<>(Arrays.asList(hisList));
            int index = hisLists.indexOf(key);
            if (index >= 0) {
                hisLists.remove(index);
            }
            StringBuilder builder = new StringBuilder();
            for (int i = hisLists.size() >= 15 ? 1 : 0; i < hisLists.size(); i++) {
                builder.append(hisLists.get(i)).append("&&");
            }
            builder.append(key);
            PreferenceMgr.put(context, HISTROY_KEY, builder.toString());
        }
    }

    public static void clearAll(Context context) {
        PreferenceMgr.put(context, HISTROY_KEY, "");
    }
}
