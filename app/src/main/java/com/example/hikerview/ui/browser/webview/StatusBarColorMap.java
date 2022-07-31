package com.example.hikerview.ui.browser.webview;

import android.content.Context;

import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2021/10/5
 * 时间：At 11:41
 */

public class StatusBarColorMap {
    private static Map<String, String> fullMap;
    private static Map<String, String> domMap;

    public synchronized static String get(Context context, String url) {
        if (StringUtil.isEmpty(url)) {
            return null;
        }
        if (fullMap == null || domMap == null) {
            fullMap = new HashMap<>();
            domMap = new HashMap<>();
            String colors = FilesInAppUtil.getAssetsString(context, "colors.txt");
            if (StringUtil.isNotEmpty(colors)) {
                String[] lines = colors.split("\n");
                for (String line : lines) {
                    if (StringUtil.isEmpty(line)) {
                        continue;
                    }
                    String[] rule = line.split(",");
                    if (rule.length != 2) {
                        continue;
                    }
                    if (rule[0].endsWith("*")) {
                        //整个域名的
                        domMap.put(rule[0].replace("*", ""), rule[1]);
                    } else {
                        fullMap.put(rule[0], rule[1]);
                    }
                }
            }
        }
        url = url.replace("http://", "").replace("https://", "");
        String dom = StringUtil.getDom(url);
        if (fullMap.containsKey(url)) {
            return fullMap.get(url);
        }
        return domMap.get(dom);
    }
} 