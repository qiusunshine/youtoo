package com.example.hikerview.ui.rules.service.require;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.Stream;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.rules.model.RequireDescription;
import com.example.hikerview.ui.rules.model.RequireItem;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.UriUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/11/24
 * 时间：At 17:15
 */

public class RequireUtils {

    public static List<RequireItem> getRequireItems(String rule) {
        if (StringUtil.isEmpty(rule)) {
            return new ArrayList<>();
        }
        String requireFilePath = JSEngine.getFilesDir() + rule + File.separator + "require.json";
        File requireFile = new File(requireFilePath);
        if (requireFile.exists()) {
            return JSON.parseArray(FileUtil.fileToString(requireFilePath), RequireItem.class);
        } else {
            return new ArrayList<>();
        }
    }

    public static int getRequireVersion(String descPath, int defaultVersion) {
        File file = new File(descPath);
        if (file.exists()) {
            RequireDescription description = JSON.parseObject(FileUtil.fileToString(descPath), RequireDescription.class);
            if (description != null) {
                return description.getVersion();
            }
        }
        return Math.min(defaultVersion, 0);
    }

    public static void updateDescription(String descPath, String url, int version) {
        try {
            RequireDescription description = new RequireDescription(url, version);
            FileUtil.stringToFile(JSON.toJSONString(description), descPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateRequireMap(String rule, String url, String proxyUrl, String filePath) {
        if (StringUtil.isEmpty(rule) || StringUtil.isEmpty(url)) {
            return;
        }
        String requireFilePath = JSEngine.getFilesDir() + rule + File.separator + "require.json";
        File requireFile = new File(requireFilePath);
        List<RequireItem> requireItems;
        if (requireFile.exists()) {
            requireItems = JSON.parseArray(FileUtil.fileToString(requireFilePath), RequireItem.class);
        } else {
            requireItems = new ArrayList<>();
        }
        generateRequireMap(requireItems, rule, url, proxyUrl, filePath);
    }

    public static void generateRequireMap(List<RequireItem> requireItems, String rule, String url, String proxyUrl, String filePath) {
        boolean exist = false;
        long now = System.currentTimeMillis();
        for (RequireItem requireItem : requireItems) {
            if (url.equals(requireItem.getUrl())) {
                exist = true;
                requireItem.setAccessTime(now);
                requireItem.setProxy(StringUtils.equals(url, proxyUrl) ? null : proxyUrl);
                break;
            }
        }
        if (!exist) {
            RequireItem item = new RequireItem(url, StringUtils.equals(url, proxyUrl) ? null : proxyUrl, filePath, now);
            requireItems.add(item);
        }
        //剔除长时间没用的
        if (requireItems.size() > 100) {
            long daysAgo = now - 1000 * 3600 * 24 * 7;
            requireItems = Stream.of(requireItems)
                    .filter(item -> item.getAccessTime() > daysAgo)
                    .toList();
        }
        try {
            String requireFilePath = JSEngine.getFilesDir() + rule + File.separator + "require.json";
            FileUtil.stringToFile(JSON.toJSONString(requireItems), requireFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCache(String url) throws Exception {
        if (StringUtil.isEmpty(url) || !url.startsWith("http")) {
            throw new Exception("require地址必须为http地址");
        }
        String md5 = StringUtil.md5(url);
        String fileName = md5 + ".js";
        String descName = md5 + ".json";
        String dir = UriUtils.getRootDir(Application.application.getApplicationContext()) + File.separator + "libs";
        String filePath = dir + File.separator + fileName;
        String descPath = dir + File.separator + descName;
        File file = new File(filePath);
        File desc = new File(descPath);
        if (file.exists()) {
            File dest = new File(file.getAbsolutePath() + ".bak");
            FileUtil.copy(file, dest);
            file.delete();
        }
        if (desc.exists()) {
            desc.delete();
        }
    }

    public static void deleteCacheByRule(String rule){
        List<RequireItem> requireItems = RequireUtils.getRequireItems(rule);
        if (CollectionUtil.isNotEmpty(requireItems)) {
            for (RequireItem requireItem : requireItems) {
                RequireUtils.deleteRequireItem(requireItem, false);
            }
            RequireUtils.deleteRequireMap(rule);
        }
    }

    public static void deleteRequireItem(RequireItem requireItem, boolean backup) {
        if (requireItem == null) {
            return;
        }
        String filePath = requireItem.getFile();
        String descPath = filePath.replace(".js", ".json");
        File file = new File(filePath);
        File desc = new File(descPath);
        if (file.exists()) {
            if (backup) {
                File dest = new File(file.getAbsolutePath() + ".bak");
                FileUtil.copy(file, dest);
            }
            file.delete();
        }
        if (desc.exists()) {
            desc.delete();
        }
    }

    public static void deleteRequireMap(String rule) {
        String requireFilePath = JSEngine.getFilesDir() + rule + File.separator + "require.json";
        File requireFile = new File(requireFilePath);
        if (requireFile.exists()) {
            requireFile.delete();
        }
    }
} 