package com.example.hikerview.ui.browser;

import android.util.Base64;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.event.home.LoadingEvent;
import com.example.hikerview.event.home.OnRefreshX5HeightEvent;
import com.example.hikerview.event.web.BlobDownloadEvent;
import com.example.hikerview.event.web.BlobDownloadProgressEvent;
import com.example.hikerview.event.web.FindMagnetsEvent;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.ActivityManager;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.model.DetailPage;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.webdlan.LocalServerParser;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ThreadTool;
import com.example.hikerview.utils.ToastMgr;

import org.adblockplus.libadblockplus.android.Utils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2019/9/29
 * 时间：At 22:54
 */
public class VideoInterface {
    private BridgeListener bridgeListener;

    public VideoInterface(BridgeListener bridgeListener) {
        this.bridgeListener = bridgeListener;
    }

    @JavascriptInterface
    public void setAppBarColor(String color) {
        this.bridgeListener.setAppBarColor(color, "false");
    }

    @JavascriptInterface
    public void setAppBarColor(String color, String isTheme) {
        this.bridgeListener.setAppBarColor(color, isTheme);
    }

    @JavascriptInterface
    public void playVideo(String url) {
        bridgeListener.playVideo(url);
    }

    @JavascriptInterface
    public void playVideo(String title, String url) {
        bridgeListener.playVideo(title, url);
    }

    @JavascriptInterface
    public void playVideos(String chapters) {
        bridgeListener.playVideos(JSON.parseArray(chapters, VideoChapter.class));
    }

    @JavascriptInterface
    public void showPic(String url) {
        bridgeListener.showPic(url);
    }

    @JavascriptInterface
    public void setWebTitle(String title) {
        bridgeListener.setWebTitle(title);
    }

    @JavascriptInterface
    public void setWebUa(String ua) {
        bridgeListener.setWebUa(ua);
    }

    @JavascriptInterface
    public void setAdBlock(String html, String rule) {
        bridgeListener.setAdBlock(html, rule);
    }

    @JavascriptInterface
    public String fetch(String url, String options) {
        return bridgeListener.fetch(url, options);
    }

    @JavascriptInterface
    public String fetch(String url) {
        return bridgeListener.fetch(url, null);
    }

    @JavascriptInterface
    public String getNetworkRecords() {
        return bridgeListener.getNetworkRecords();
    }

    @JavascriptInterface
    public void fetchAsync(String url, String key, String funcName) {
        bridgeListener.fetchAsync(url, null, key, funcName);
    }

    @JavascriptInterface
    public void fetchAsync(String url, String options, String key, String funcName) {
        bridgeListener.fetchAsync(url, options, key, funcName);
    }

    @JavascriptInterface
    public void writeFile(String filePath, String content) {
        bridgeListener.writeFile(filePath, content);
    }

    @JavascriptInterface
    public String getResultByKey(String key) {
        return bridgeListener.getResultByKey(key);
    }

    @JavascriptInterface
    public void importRule(String rule) {
        bridgeListener.importRule(rule);
    }

    @JavascriptInterface
    public void putVar(String key, String value) {
        bridgeListener.putVar(key, value);
    }

    @JavascriptInterface
    public String getVar(String key) {
        return bridgeListener.getVar(key);
    }

    @JavascriptInterface
    public String getCookie(String url) {
        return bridgeListener.getCookie(url);
    }

    @JavascriptInterface
    public void allowSelect() {
        bridgeListener.allowSelect();
    }

    @JavascriptInterface
    public void saveAdBlockRule(String rule) {
        bridgeListener.saveAdBlockRule(rule);
    }

    @JavascriptInterface
    public void setImgUrls(String urls) {
        bridgeListener.setImgUrls(urls);
    }

    @JavascriptInterface
    public void setImgHref(String url) {
        bridgeListener.setImgHref(url);
    }

    @JavascriptInterface
    public void searchBySelect(String text) {
        bridgeListener.searchBySelect(text);
    }

    @JavascriptInterface
    public void translate(String text) {
        bridgeListener.translate(text);
    }

    @JavascriptInterface
    public void translateBolan(String text) {
        bridgeListener.translateBolan(text);
    }

    @JavascriptInterface
    public void copy(String text) {
        ClipboardUtil.copyToClipboardForce(ActivityManager.getInstance().getCurrentActivity(), text);
    }

    @JavascriptInterface
    public void refreshPage(boolean scrollTop) {
        bridgeListener.refreshPage(scrollTop);
    }

    @JavascriptInterface
    public void back() {
        bridgeListener.back(true);
    }

    @JavascriptInterface
    public void back(boolean refreshPage) {
        bridgeListener.back(refreshPage);
    }

    @JavascriptInterface
    public String getInternalJs() {
        return bridgeListener.getInternalJs();
    }

    @JavascriptInterface
    public String parseDomForHtml(String html, String rule) {
        return bridgeListener.parseDomForHtml(html, rule);
    }

    @JavascriptInterface
    public String parseDomForArray(String html, String rule) {
        return bridgeListener.parseDomForArray(html, rule);
    }

    @JavascriptInterface
    public void refreshX5Desc(String desc) {
        EventBus.getDefault().post(new OnRefreshX5HeightEvent(desc));
    }

    @JavascriptInterface
    public void saveImage(String url) {
        bridgeListener.saveImage(url);
    }

    @JavascriptInterface
    public void toDetailPage(String ruleTitle, String url, String group, String colType, String detailFindRule, String preRule) {
        bridgeListener.toDetailPage(ruleTitle, url, group, colType, detailFindRule, preRule);
    }

    @JavascriptInterface
    public String isPc() {
        return bridgeListener.isPc();
    }

    @JavascriptInterface
    public void newPage(String title, String url) {
        bridgeListener.newPage(title, url);
    }

    @JavascriptInterface
    public String getUrls() {
        return bridgeListener.getUrls();
    }

    @JavascriptInterface
    public String base64Decode(String url) {
        if (StringUtil.isEmpty(url)) {
            return url;
        }
        return new String(Base64.decode(url, Base64.NO_WRAP));
    }

    @JavascriptInterface
    public String base64Encode(String url) {
        if (StringUtil.isEmpty(url)) {
            return url;
        }
        return new String(Base64.encode(url.getBytes(), Base64.NO_WRAP));
    }

    @JavascriptInterface
    public void downloadBlob(String url, String fileName, String headerMap, String result) {
        if (StringUtil.isEmpty(url)) {
            return;
        }
        EventBus.getDefault().post(new BlobDownloadEvent(url, fileName, headerMap, result));
    }

    @JavascriptInterface
    public void downloadBlobUpdate(String url, String progress) {
        EventBus.getDefault().post(new BlobDownloadProgressEvent(url, progress));
    }

    @JavascriptInterface
    public void findMagnetsNotify(String data) {
        if (StringUtil.isEmpty(data)) {
            return;
        }
        if (data.startsWith("\"[{") && data.endsWith("}]\"")) {
            data = data.substring(1, data.length() - 1).replace("\\\"", "\"");
        }
        EventBus.getDefault().post(new FindMagnetsEvent(data));
    }

    @JavascriptInterface
    public void clearVar(String key) {
        JSEngine.getInstance().clearVar(key);
    }

    @JavascriptInterface
    public void open(String page) {
        bridgeListener.open(JSON.parseObject(page, DetailPage.class));
    }

    @JavascriptInterface
    public String parseLazyRule(String url) {
        return bridgeListener.parseLazyRule(url);
    }

    @JavascriptInterface
    public void parseLazyRuleAsync(String url, String callback) {
        bridgeListener.parseLazyRuleAsync(url, callback);
    }

    @JavascriptInterface
    public void log(String msg) {
        Timber.d(msg);
    }

    @JavascriptInterface
    public void finishParse(String url, String mode, String ticket) {
        bridgeListener.finishParse(url, mode, ticket);
    }

    @JavascriptInterface
    public String getRequestHeaders0() {
        return bridgeListener.getRequestHeaders0();
    }

    @JavascriptInterface
    public String getHeaderUrl(String url) {
        return bridgeListener.getHeaderUrl(url);
    }

    @JavascriptInterface
    public String clearM3u8Ad(String url) {
        return bridgeListener.clearM3u8Ad(url);
    }

    @JavascriptInterface
    public void showLoading(String str) {
        EventBus.getDefault().post(new LoadingEvent(str, true));
    }

    @JavascriptInterface
    public void hideLoading() {
        EventBus.getDefault().post(new LoadingEvent(null, false));
    }

    @JavascriptInterface
    public void toast(String msg) {
        ThreadTool.INSTANCE.runOnUI(() -> ToastMgr.shortBottomCenter(Application.getContext(), msg));
    }

    @JavascriptInterface
    public String escapeJavaScriptString(String code) {
        return Utils.escapeJavaScriptString(code);
    }


    @JavascriptInterface
    public String md5(String code) {
        return StringUtil.md5(code);
    }

    @JavascriptInterface
    public String getItem(String rule, String key) {
        return BigTextDO.getItem(rule, key);
    }

    @JavascriptInterface
    public String listItems(String rule) {
        return JSON.toJSONString(BigTextDO.listItems(rule));
    }

    @JavascriptInterface
    public void setItem(String rule, String key, String value) {
        BigTextDO.setItem(rule, key, value);
    }

    @JavascriptInterface
    public void removeItem(String rule, String key) {
        BigTextDO.removeItem(rule, key);
    }

    @JavascriptInterface
    public void registerMenuCommand(String rule, String menu, String func) {
        bridgeListener.registerMenuCommand(rule, menu, func);
    }

    @JavascriptInterface
    public void unregisterMenuCommand(String rule, String menu) {
        bridgeListener.unregisterMenuCommand(rule, menu);
    }

    @JavascriptInterface
    public String getResourceUrl(String url) {
        String fileName = "_fileSelect_" + StringUtil.md5(url) + "." + FileUtil.getExtension(url);
        String path = JSEngine.getFilePath("hiker://files/cache/" + fileName);
        try {
            if (!new File(path).exists()) {
                CodeUtil.downloadSync(url, path, null);
            }
            return LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), "file://" + path);
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }

    @JavascriptInterface
    public void openThirdApp(String scheme) {
        if (StringUtil.isNotEmpty(scheme)) {
            bridgeListener.openThirdApp(scheme);
        }
    }

    @JavascriptInterface
    public void saveFormInputItem(String href, String id, String value) {
        if (StringUtil.isNotEmpty(href) && StringUtil.isNotEmpty(id) && StringUtil.isNotEmpty(value)) {
            HeavyTaskUtil.executeNewTask(()-> BigTextDO.saveFormInputs(href, id, value));
        }
    }

    public interface BridgeListener {
        void setWebUa(String ua);

        void setWebTitle(String title);

        void showPic(String url);

        void playVideo(String title, String url);

        void playVideos(List<VideoChapter> chapters);

        void playVideo(String url);

        void setAppBarColor(String color, String isTheme);

        void setAdBlock(String html, String rule);

        String getNetworkRecords();

        void importRule(String rule);

        String fetch(String url, String options);

        void fetchAsync(String url, String options, String key, String funcName);

        String getResultByKey(String key);

        void writeFile(String filePath, String content);

        void putVar(String key, String value);

        String getVar(String key);

        String getCookie(String url);

        void allowSelect();

        void saveAdBlockRule(String rule);

        void setImgUrls(String urls);

        void setImgHref(String url);

        void searchBySelect(String text);

        void translate(String text);

        void translateBolan(String text);

        void refreshPage(boolean scrollTop);

        void back(boolean refreshPage);

        String getInternalJs();

        String parseDomForHtml(String html, String rule);

        String parseDomForArray(String html, String rule);

        void saveImage(String url);

        void toDetailPage(String ruleTitle, String url, String group, String colType, String detailFindRule, String preRule);

        void newPage(String title, String url);

        void open(DetailPage page);

        String isPc();

        void finishParse(String url, String mode, String ticket);

        String getUrls();

        String parseLazyRule(String url);

        void parseLazyRuleAsync(String url, String callback);

        String getRequestHeaders0();

        String getHeaderUrl(String url);

        String clearM3u8Ad(String url);

        void registerMenuCommand(String rule, String menu, String func);

        void unregisterMenuCommand(String rule, String menu);

        void openThirdApp(String scheme);
    }
}
