package com.example.hikerview.ui.browser.webview;

import android.content.Context;
import android.text.TextUtils;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.ui.browser.data.DomainConfigKt;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/2/2
 * 时间：At 21:10
 */

public class JsPluginHelper {

    public static String getInternalJs(Context context) {
        //状态栏变色
        String js = "";
        String themeJs = JSManager.instance(context).getJsByFileName("theme");
        if (!TextUtils.isEmpty(themeJs)) {
            js = themeJs;
        }
        //长按识别广告
        String adBlockJs = JSManager.instance(context).getJsByFileName("adTouch");
        if (!TextUtils.isEmpty(adBlockJs)) {
            js = js + adBlockJs;
        }
        return js;
    }

    public static void loadMyJs(Context context, Consumer<String> webView, String url, VideoUrlsProvider videoUrlsProvider, boolean pageEnd) {
        loadMyJs(context, webView, url, videoUrlsProvider, pageEnd, true);
    }

    public static void loadMyJs(Context context, Consumer<String> webView, String url, VideoUrlsProvider videoUrlsProvider, boolean pageEnd, boolean loadGlobalJs) {
        //注入视频链接到网页供js调用
        webView.accept("(function(){\n" +
                "\twindow.videoUrls = \"" + videoUrlsProvider.getVideoUrls() + "\";\n" +
                "})();");
        //状态栏变色
        String themeJs = JSManager.instance(context).getJsByFileName("theme");
        if (!TextUtils.isEmpty(themeJs)) {
            webView.accept(themeJs);
        }
        //长按识别广告
        String adBlockJs = JSManager.instance(context).getJsByFileName("adTouch");
        if (!TextUtils.isEmpty(adBlockJs)) {
            webView.accept(adBlockJs);
        }
        if(StringUtil.isEmpty(url)){
            return;
        }
        if(DomainConfigKt.isDisableJsPlugin(url)){
            return;
        }
        if (!url.contains("/android_asset/")) {
            if (loadGlobalJs || url.startsWith("http")) {
                //全局插件
                List<String> globalJs = JSManager.instance(context).getJsByDom("global", pageEnd);
                if (!CollectionUtil.isEmpty(globalJs)) {
                    for (String js : globalJs) {
                        webView.accept("(function (){\n" + js + "})();");
                    }
                }
            }
        }
        //自定义插件
        List<String> jsList = JSManager.instance(context).getJsByDom(url, pageEnd);
        if (!CollectionUtil.isEmpty(jsList)) {
            for (String js : jsList) {
                webView.accept("(function (){\n" + js + "})();");
            }
        }
    }

    public interface VideoUrlsProvider {
        String getVideoUrls();
    }
}
