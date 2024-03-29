package com.example.hikerview.ui.browser.model;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.service.JSUpdaterKt;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.js.model.JsRule;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.UriUtils;

import org.adblockplus.libadblockplus.android.Utils;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 作者：By hdy
 * 日期：On 2019/4/14
 * 时间：At 18:35
 */
public class JSManager {
    private static final String TAG = "JSManager";
    private volatile static JSManager sInstance;
    private Map<String, Map<String, String>> jsLoader;
    private Map<String, Boolean> jsEnableMap = new HashMap<>();
    private String greasyJsTemplate;
    private Map<String, Boolean> greasyJSCache = new HashMap<>();
    private Map<String, GreasyMeta> greasyMetaMap = new HashMap<>();
    private VersionListener versionListener;

    public interface VersionListener {
        void change(String fileName, String version);
    }

    private JSManager(Context context) {
        jsLoader = scanDomainToMap(context);
        greasyJsTemplate = FilesInAppUtil.getAssetsString(context, "greasyfork.js");
    }

    public static JSManager instance(Context context) {
        if (sInstance == null) {
            synchronized (JSManager.class) {
                if (sInstance == null) {
                    sInstance = new JSManager(context);
                }
            }
        }
        return sInstance;
    }

    public void registerVersionListener(VersionListener versionListener) {
        this.versionListener = versionListener;
    }

    public void unregisterVersionListener() {
        this.versionListener = null;
    }

    private Map<String, Map<String, String>> scanDomainToMap(Context context) {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.JS_ENABLE_MAP_KEY).findFirst(BigTextDO.class);
        if (bigTextDO != null) {
            String value = bigTextDO.getValue();
//            Log.d(TAG, "loadOrderMap: " + value);
            if (StringUtil.isNotEmpty(value)) {
                jsEnableMap = JSON.parseObject(value, new TypeReference<Map<String, Boolean>>() {
                });
            }
        }
        Map<String, Map<String, String>> jsLoader = new HashMap<>();
        //APP自带的JS插件
        Map<String, String> theme = new HashMap<>(1);
        theme.put("theme", FilesInAppUtil.getAssetsString(context, "theme.js"));
        jsLoader.put("theme", theme);
        Map<String, String> mute = new HashMap<>(1);
        mute.put("mute", FilesInAppUtil.getAssetsString(context, "mute.js"));
        jsLoader.put("mute", mute);
        Map<String, String> adTouch = new HashMap<>(1);
        adTouch.put("adTouch", FilesInAppUtil.getAssetsString(context, "adTouch.js"));
        jsLoader.put("adTouch", adTouch);
        //扫描用户自定义的插件
        File jsDir = new File(getJsDirPath());
        initJsDir(jsDir);
        String[] fileNames = jsDir.list((dir, name) -> name.endsWith(".js"));
        List<String> jsEnableMapNoKeys = new ArrayList<>();
        Set<String> jsEnableMapKeys = jsEnableMap.keySet();
        if (fileNames != null) {
            for (String fileName : fileNames) {
                fileName = fileName.replace(".js", "");
//                Log.d(TAG, "find js file = " + fileName);
                String dom = getDomFromFileName(fileName);
                if (!jsEnableMapKeys.isEmpty() && !jsEnableMapKeys.contains(fileName)) {
                    jsEnableMapNoKeys.add(fileName);
                }
                if (jsLoader.containsKey(dom) && jsLoader.get(dom) != null) {
                    Objects.requireNonNull(jsLoader.get(dom)).put(fileName, "");
                } else {
                    Map<String, String> files = new HashMap<>();
                    files.put(fileName, "");
                    jsLoader.put(dom, files);
                }
            }
        }
        //移除控制禁用与否的map中的无效key，即插件已经被删除，将jsEnableMap清洗一遍
        if (CollectionUtil.isNotEmpty(jsEnableMapNoKeys)) {
            for (String jsEnableMapNoKey : jsEnableMapNoKeys) {
                jsEnableMap.remove(jsEnableMapNoKey);
            }
            if (bigTextDO != null) {
                bigTextDO.setValue(JSON.toJSONString(jsEnableMap));
            } else {
                bigTextDO = new BigTextDO();
                bigTextDO.setKey(BigTextDO.JS_ENABLE_MAP_KEY);
            }
            bigTextDO.save();
        }
        return jsLoader;
    }

    public List<JsRule> listAllJsFileNames() {
        List<String> jsList = new ArrayList<>();
        if (jsLoader == null) {
            return new ArrayList<>();
        }
        Set<String> keys = jsLoader.keySet();
        if (CollectionUtil.isEmpty(keys)) {
            return new ArrayList<>();
        } else {
            for (String key : keys) {
                if (!"theme".equals(key) && !"mute".equals(key) && !"adTouch".equals(key)) {
                    Map<String, String> jsFiles = jsLoader.get(key);
                    if (jsFiles == null || jsFiles.isEmpty()) {
                        continue;
                    }
                    jsList.addAll(jsFiles.keySet());
                }
            }
            List<JsRule> jsRules = new ArrayList<>();
            for (String s : jsList) {
                JsRule jsRule = new JsRule();
                jsRule.setName(s);
                jsRule.setEnable(jsEnableMap.containsKey(s) ? jsEnableMap.get(s) : true);
                jsRules.add(jsRule);
            }
            return jsRules;
        }
    }

    private String getDomFromFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        String[] names = fileName.split("_");
        String dom = names[0];
        if (names.length == 1) {
            dom = fileName;
        }
        return dom;
    }


    public static String getNameFromFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        String[] names = fileName.split("_");
        if (names.length < 2) {
            return "";
        }
        return names[1];
    }

    public static boolean getPageEndByFileName(String fileName) {
        String name = getNameFromFileName(fileName);
        if (StringUtil.isEmpty(name)) {
            return true;
        }
        String[] names = name.split("#");
        if (names.length <= 1) {
            return true;
        }
        return !"0".equals(names[1]);
    }

    public static String revertPageEndByFileName(String fileName) {
        String[] names = fileName.split("_");
        if (names.length < 2) {
            return fileName + "_" + fileName + "#0";
        }
        boolean pageEnd = getPageEndByFileName(fileName);
        if (pageEnd) {
            //改为加载中和加载完成
            return names[0] + "_" + names[1].split("#")[0] + "#0";
        } else {
            //改为加载完成
            return names[0] + "_" + names[1].split("#")[0];
        }
    }

    public boolean deleteJs(String fileName) {
        if (jsLoader == null || jsLoader.isEmpty() || TextUtils.isEmpty(fileName)) {
            return true;
        }
        for (String key : jsLoader.keySet()) {
            if (jsLoader.get(key) != null) {
                for (String file : jsLoader.get(key).keySet()) {
                    if (fileName.equals(file)) {
                        jsLoader.get(key).remove(file);
                        String jsDirPath = getJsDirPath();
                        initJsDir(new File(jsDirPath));
                        String jsFilePath = jsDirPath + File.separator + fileName + ".js";
                        File jsFile = new File(jsFilePath);
                        String rule = Utils.escapeJavaScriptString(getNameFromFileName(fileName));
                        BigTextDO.clearItems(rule);
                        if (jsFile.exists()) {
                            return jsFile.delete();
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean updateJs(String fileName, String js) {
        return updateJs(fileName, js, null);
    }

    public boolean updateJs(String fileName, String js, String url) {
        if (TextUtils.isEmpty(js) || TextUtils.isEmpty(fileName)) {
            return true;
        }
        String dom = getDomFromFileName(fileName);
        if (jsLoader.containsKey(dom)) {
            jsLoader.get(dom).put(fileName, js);
        } else {
            //插件域名不存在
            Map<String, String> jsFiles = new HashMap<>();
            jsFiles.put(fileName, js);
            jsLoader.put(dom, jsFiles);
        }
        //更新文件内容
        String jsDirPath = getJsDirPath();
        initJsDir(new File(jsDirPath));
        String jsFilePath = jsDirPath + File.separator + fileName + ".js";
        greasyJSCache.remove(fileName);
        try {
            FileUtil.stringToFile(js, jsFilePath);
            try {
                if (versionListener != null) {
                    versionListener.change(fileName, JSUpdaterKt.findVersion(js));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            JSUpdaterKt.saveGreasyUpdateInfo(fileName, js, url);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void reloadJSFile(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        String js = loadContentFromFile(fileName);
        if (TextUtils.isEmpty(js)) {
            return;
        }
        greasyJSCache.remove(fileName);
        try {
            JSUpdaterKt.saveGreasyUpdateInfo(fileName, js, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasJs(String fileName) {
        for (String key : jsLoader.keySet()) {
            if (jsLoader.get(key) != null) {
                if (jsLoader.get(key).containsKey(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void enableJsByFileName(String fileName, boolean enable) {
        jsEnableMap.put(fileName, enable);
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.JS_ENABLE_MAP_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(BigTextDO.JS_ENABLE_MAP_KEY);
        }
        bigTextDO.setValue(JSON.toJSONString(jsEnableMap));
        bigTextDO.save();
    }

    /**
     * 全部启用或者全部禁用
     *
     * @param enable true:启用/false：禁用
     */
    public void enableAllJs(boolean enable) {
        if (jsLoader != null && !jsLoader.isEmpty()) {
            if (enable) {
                //全部启用，只要擦除即可
                jsEnableMap.clear();
            } else {
                //全部禁用，所有插件都设置为禁用
                Set<String> keys = jsLoader.keySet();
                List<String> jsList = new ArrayList<>();
                for (String key : keys) {
                    if (!"theme".equals(key) && !"mute".equals(key) && !"adTouch".equals(key)) {
                        Map<String, String> jsFiles = jsLoader.get(key);
                        if (jsFiles == null || jsFiles.isEmpty()) {
                            continue;
                        }
                        jsList.addAll(jsFiles.keySet());
                    }
                }
                for (String s : jsList) {
                    jsEnableMap.put(s, false);
                }
            }
        }
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.JS_ENABLE_MAP_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(BigTextDO.JS_ENABLE_MAP_KEY);
        }
        bigTextDO.setValue(JSON.toJSONString(jsEnableMap));
        bigTextDO.save();
    }

    public String getJsByFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        if ("theme".equals(fileName) || "mute".equals(fileName) || "adTouch".equals(fileName)) {
            return jsLoader.get(fileName).get(fileName);
        }
        for (String key : jsLoader.keySet()) {
            if (jsLoader.get(key) != null) {
                for (String file : jsLoader.get(key).keySet()) {
                    if (fileName.equals(file)) {
                        if (TextUtils.isEmpty(jsLoader.get(key).get(fileName))) {
                            //没有初始化
                            return loadContentFromFile(fileName);
                        }
                        return jsLoader.get(key).get(fileName);
                    }
                }
            }
        }
        return null;
    }

    public List<String> getJsByDom(String url, boolean pageEnd) {
        String dom = StringUtil.getDom(url);
        if (!TextUtils.isEmpty(dom) && jsLoader.get(dom) != null) {
            List<String> jsList = new ArrayList<>();
            List<String> fileNames = new ArrayList<>(jsLoader.get(dom).keySet());
            Collections.sort(fileNames);
            fileNames = Stream.of(fileNames).filter(fileName -> !jsEnableMap.containsKey(fileName) || jsEnableMap.get(fileName)).collect(Collectors.toList());
//            Log.d(TAG, "getJsByDom: fileNames=" + fileNames.toString());
            for (String fileName : fileNames) {
                if (!pageEnd) {
                    //加载中
                    if (getPageEndByFileName(fileName)) {
                        //只在加载完成生效
                        //再检查一下油猴脚本的meta
                        if (!shouldLoadAtStart(fileName)) {
                            continue;
                        }
                    }
                }
                jsList.add(getRealJS(dom, fileName));
            }
            return jsList;
        }
        return null;
    }

    private boolean shouldLoadAtStart(String fileName) {
        if (greasyMetaMap.containsKey(fileName)) {
            GreasyMeta meta = greasyMetaMap.get(fileName);
            return meta != null && "document-start".equals(meta.getRunAt());
        }
        return false;
    }

    private String getRealJS(String dom, String fileName) {
        String content = null;
        try {
            if (TextUtils.isEmpty(jsLoader.get(dom).get(fileName))) {
                //没有初始化
                content = loadContentFromFile(fileName);
            } else {
                content = jsLoader.get(dom).get(fileName);
            }
            if (!greasyJSCache.containsKey(fileName)) {
                boolean isGreasy = content != null && !content.isEmpty() &&
                        (content.contains("/UserScript==") || content.contains("==UserScript=="))
                        && !content.contains("RequireStack =");
                greasyJSCache.put(fileName, isGreasy);
                if (isGreasy) {
                    GreasyMeta meta = new GreasyMeta();
                    String[] c = content.split("/UserScript==");
                    if (c.length > 0 && c[0].contains("@run-at")) {
                        if (c[0].contains("document-start")) {
                            meta.setRunAt("document-start");
                        } else if (c[0].contains("document-end")) {
                            meta.setRunAt("document-end");
                        } else if (c[0].contains("document-idle")) {
                            meta.setRunAt("document-idle");
                        }
                    }
                    greasyMetaMap.put(fileName, meta);
                    JSUpdaterKt.checkUpdate(dom, fileName, false);
                }
            }
            Boolean isGreasy = greasyJSCache.get(fileName);
            if (isGreasy != null && isGreasy) {
                //兼容油猴脚本
                content = greasyJsTemplate
                        .replace("${installUrl}", Utils.escapeJavaScriptString("hiker://jsfile/" + dom + "/" + fileName))
                        .replace("${RULE_NAME}", Utils.escapeJavaScriptString(getNameFromFileName(fileName)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public String getJSFileContent(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        try {
            url = url.replace("hiker://jsfile/", "");
            String[] s = url.split("/");
            String dom = s[0];
            String fileName = StringUtil.arrayToString(s, 1, "/");
            String content = jsLoader.get(dom).get(fileName);
            if (TextUtils.isEmpty(content)) {
                //没有初始化
                return loadContentFromFile(fileName);
            }
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void initJsDir(File jsDir) {
        if (!jsDir.exists()) {
            jsDir.mkdirs();
        }
        if (!jsDir.isDirectory()) {
            try {
                jsDir.delete();
                jsDir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String jsFilePath = getJsDirPath() + File.separator + "help.txt";
        File file = new File(jsFilePath);
        if (!file.exists()) {
            String text = "一个网站一个js文件，例如m.iqiyi.com.js，那么整个m.iqiyi.com域名下所有网站都会加载这个js文件里面的代码，如果需要区分不同的url比如http://m.iqiyi.com/a/和/b/下面不同网址，需要自己在js代码里面判断和处理。全局js写到global.js文件里面。修改或者新增js后重启方圆生效。";
            FileUtil.bytesToFile(jsFilePath, text.getBytes());
        }
    }

    public static String getJsDirPath() {
        String rulesPath = UriUtils.getRootDir(Application.getContext()) + File.separator + "rules";
        File dir = new File(rulesPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return rulesPath + File.separator + "js";
    }

    public String getFilePathByName(String fileName) {
        String jsDirPath = getJsDirPath();
        initJsDir(new File(jsDirPath));
        return jsDirPath + File.separator + fileName + ".js";
    }

    private String loadContentFromFile(String fileName) {
        try {
            String jsDirPath = getJsDirPath();
            initJsDir(new File(jsDirPath));
            String jsFilePath = jsDirPath + File.separator + fileName + ".js";
            String dom = getDomFromFileName(fileName);
            byte[] bytes = FileUtil.fileToBytes(jsFilePath);
            if (bytes == null) {
                if (jsLoader.get(dom) != null) {
                    jsLoader.get(dom).remove(fileName);
                }
                return null;
            }
            String text = new String(bytes);
            if (TextUtils.isEmpty(text)) {
                if (jsLoader.get(dom) != null) {
                    jsLoader.get(dom).remove(fileName);
                }
                return null;
            } else {
                if (jsLoader.get(dom) != null) {
                    jsLoader.get(dom).put(fileName, text);
                } else {
                    Map<String, String> jsFiles = new HashMap<>();
                    jsFiles.put(fileName, text);
                    jsLoader.put(dom, jsFiles);
                }
            }
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
