package com.example.hikerview.ui.browser.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.model.AdBlockUrl;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.StringUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.hikerview.service.subscribe.AdUrlSubscribe.AD_FILTER_URL_FILE;
import static com.example.hikerview.service.subscribe.AdUrlSubscribe.DOM_BLOCK_RULE_FILE;

/**
 * 作者：By 15968
 * 日期：On 2019/10/14
 * 时间：At 21:56
 */
public class AdUrlBlocker {
    private static final String TAG = "AdUrlBlocker";
    private volatile static AdUrlBlocker sInstance;
    private String[] whiteList = new String[]{"cdn.bootcss.com"};
    private String[] whiteDomainList = new String[]{"haikuoshijie.cn"};

    public List<AdBlockUrl> getBlockUrls() {
        return blockUrls;
    }

    private final List<AdBlockUrl> blockUrls = Collections.synchronizedList(new ArrayList<>());

    public List<String> getSubscribeAdUrls() {
        return subscribeAdUrls;
    }

    public int getSubscribeAdUrlsCount() {
        return CollectionUtil.isEmpty(subscribeAdUrls) ? 0 : subscribeAdUrls.size();
    }

    public int getSubscribeAdBlockRulesCount() {
        return subscribeAdBlockRules == null || subscribeAdBlockRules.isEmpty() ? 0 : subscribeAdBlockRules.keySet().size();
    }

    private final List<String> subscribeAdUrls = Collections.synchronizedList(new ArrayList<>());

    private Map<String, String> subscribeAdBlockRules = new HashMap<>();

    public void updateSubscribe(Context context) {
        subscribeAdUrls.clear();
        try {
            String s = FilesInAppUtil.read(context, AD_FILTER_URL_FILE);
            if (TextUtils.isEmpty(s)) {
                return;
            }
            if (s.startsWith("version:2")) {
                String[] ss = s.replace("\r", "").split("\n");
                for (int i = 1; i < ss.length; i++) {
                    if (StringUtil.isNotEmpty(ss[i])) {
                        subscribeAdUrls.add(ss[i]);
                    }
                }
            } else {
                subscribeAdUrls.addAll(Arrays.asList(s.split("&&")));
            }
            Log.d(TAG, "updateSubscribe: " + JSON.toJSONString(subscribeAdUrls));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDomBlockSubscribe(Context context) {
        subscribeAdBlockRules.clear();
        try {
            String s = FilesInAppUtil.read(context, DOM_BLOCK_RULE_FILE);
            if (TextUtils.isEmpty(s)) {
                return;
            }
            if (s.startsWith("version:1")) {
                String[] ss = s.replace("\r", "").split("\n");
                for (int i = 1; i < ss.length; i++) {
                    if (StringUtil.isNotEmpty(ss[i])) {
                        String[] sss = ss[i].split("::");
                        if (StringUtil.isNotEmpty(sss[0]) && sss.length == 2 && StringUtil.isNotEmpty(sss[1])) {
                            subscribeAdBlockRules.put(sss[0], sss[1]);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public String getBlockRules(String url) {
        if (StringUtil.isEmpty(url)) {
            return null;
        }
        String dom = StringUtil.getDom(url);
        if (subscribeAdBlockRules != null && subscribeAdBlockRules.containsKey(dom)) {
            return subscribeAdBlockRules.get(dom);
        }
        return null;
    }

    private AdUrlBlocker() {
        try {
            blockUrls.addAll(LitePal.findAll(AdBlockUrl.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String[] urls = url.split("&&");
        if (urls.length > 1) {
            addUrls(Arrays.asList(urls));
        } else {
            addOneUrl(url);
        }
    }

    public void addOneUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        for (int i = 0; i < blockUrls.size(); i++) {
            if (blockUrls.get(i).getUrl().equals(url)) {
                return;
            }
        }
        AdBlockUrl blockUrl = new AdBlockUrl(url);
        blockUrl.save();
        blockUrls.add(blockUrl);
    }

    public int addUrls(List<String> urls) {
        if (CollectionUtil.isEmpty(urls)) {
            return 0;
        }
        Set<String> blockSet = new HashSet<>();
        for (int i = 0; i < blockUrls.size(); i++) {
            blockSet.add(blockUrls.get(i).getUrl());
        }
        List<AdBlockUrl> needAddUrls = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            if (TextUtils.isEmpty(urls.get(i))) {
                continue;
            }
            AdBlockUrl adBlockUrl = new AdBlockUrl(urls.get(i));
            if (!blockSet.contains(adBlockUrl.getUrl())) {
                needAddUrls.add(adBlockUrl);
                blockSet.add(adBlockUrl.getUrl());
            }
        }
        blockUrls.addAll(needAddUrls);
        try {
            LitePal.saveAll(needAddUrls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return needAddUrls.size();
    }

    public void removeAll() {
        LitePal.deleteAll(AdBlockUrl.class);
        blockUrls.clear();
    }

    public void removeUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        for (int i = 0; i < blockUrls.size(); i++) {
            if (blockUrls.get(i).getUrl().equals(url)) {
                blockUrls.get(i).delete();
                blockUrls.remove(i);
                return;
            }
        }
    }

    public synchronized long shouldBlock(String domainUrl, String url) {
        if (!SettingConfig.shouldBlock()) {
            return -1;
        }
        String dom = StringUtil.getDom(domainUrl);
        if (url == null) {
            return -1;
        }
        for (String white : whiteDomainList) {
            if (domainUrl.contains(white)) {
                return -1;
            }
        }
        for (String white : whiteList) {
            if (url.contains(white)) {
                return -1;
            }
        }
        if(CollectionUtil.isNotEmpty(blockUrls)){
            synchronized (blockUrls){
                for (AdBlockUrl blockUrl : blockUrls) {
                    if (checkBlockRule(url, dom, blockUrl.getUrl())) {
                        return blockUrl.getId();
                    }
                }
            }
        }
        if (!CollectionUtil.isEmpty(subscribeAdUrls)) {
            synchronized (subscribeAdUrls) {
                for (String blockUrl : subscribeAdUrls) {
                    if (checkBlockRule(url, dom, blockUrl)) {
                        return 0;
                    }
                }
            }
        }
        return -1;
    }

    public static boolean shouldBlock(List<String> rules, String domainUrl, String url) {
        if (CollectionUtil.isEmpty(rules)) {
            return false;
        }
        String dom = StringUtil.getDom(domainUrl);
        if (url == null) {
            return false;
        }
        for (String blockUrl : rules) {
            if (checkBlockRule(url, dom, blockUrl)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkBlockRule(String url, String dom, String blockUrl) {
        if (StringUtil.isEmpty(blockUrl) || StringUtil.isEmpty(url)) {
            return false;
        }
        String[] tmp1 = blockUrl.split("@domain=");
        if (tmp1.length >= 2) {
            blockUrl = tmp1[0];
            if (!dom.contains(tmp1[1])) {
                return false;
            }
        }
        String[] tmp2 = blockUrl.split("\\*");
        if (tmp2.length > 2) {
            String u = url;
            for (String s : tmp2) {
                int i = u.indexOf(s);
                if (i < 0) {
                    return false;
                }
                u = u.substring(i + s.length());
            }
            return true;
        } else if (tmp2.length == 2) {
            if (StringUtil.isEmpty(tmp2[0])) {
                return url.contains(tmp2[1]);
            }
            if (StringUtil.isEmpty(tmp2[1])) {
                return url.contains(tmp2[0]);
            }
            int index = url.indexOf(tmp2[0]);
            return index >= 0 && url.substring(index + tmp2[0].length()).contains(tmp2[1]);
        } else {
            return url.contains(tmp2[0]);
        }
    }

    public static AdUrlBlocker instance() {
        if (sInstance == null) {
            synchronized (AdUrlBlocker.class) {
                if (sInstance == null) {
                    sInstance = new AdUrlBlocker();
                }
            }
        }
        return sInstance;
    }
}
