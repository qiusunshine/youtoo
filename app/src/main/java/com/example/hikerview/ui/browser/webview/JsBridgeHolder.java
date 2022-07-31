package com.example.hikerview.ui.browser.webview;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.CookieManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.hikerview.constants.ArticleColTypeEnum;
import com.example.hikerview.constants.Media;
import com.example.hikerview.event.OnBackEvent;
import com.example.hikerview.event.home.OnRefreshPageEvent;
import com.example.hikerview.event.web.DestroyEvent;
import com.example.hikerview.event.web.OnEvalJsEvent;
import com.example.hikerview.event.web.OnImgHrefFindEvent;
import com.example.hikerview.event.web.OnSaveAdBlockRuleEvent;
import com.example.hikerview.event.web.OnSetAdBlockEvent;
import com.example.hikerview.event.web.OnSetWebTitleEvent;
import com.example.hikerview.event.web.ShowSearchEvent;
import com.example.hikerview.event.web.ShowTranslateEvent;
import com.example.hikerview.service.exception.ParseException;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.service.parser.PageParser;
import com.example.hikerview.ui.browser.HtmlSourceActivity;
import com.example.hikerview.ui.browser.PictureListActivity;
import com.example.hikerview.ui.browser.ViaInterface;
import com.example.hikerview.ui.browser.VideoInterface;
import com.example.hikerview.ui.browser.model.DetailPage;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.ui.browser.model.DetectorManager;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.browser.model.UAModel;
import com.example.hikerview.ui.home.FilmListActivity;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.js.JSEditActivity;
import com.example.hikerview.ui.miniprogram.MiniProgramRouter;
import com.example.hikerview.ui.miniprogram.data.RuleDTO;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.HttpUtil;
import com.example.hikerview.utils.ImgUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.UriUtils;
import com.example.hikerview.utils.WebUtil;
import com.lxj.xpopup.XPopup;

import org.adblockplus.libadblockplus.android.Utils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/2/2
 * 时间：At 20:13
 */

public class JsBridgeHolder {

    private WeakReference<? extends Activity> reference;
    private String bindDataToHtml = null, vConsole = null, jquery = null, vConsoleShow = null;
    private Map<String, String> requestAsyncMap = new HashMap<>();
    private String tempCode = "";
    private WebViewWrapper wrapper;

    public JsBridgeHolder(WeakReference<? extends Activity> reference, WebViewWrapper wrapper) {
        this.reference = reference;
        this.wrapper = wrapper;
        init();
    }

    public void update(Activity activity, WebViewWrapper wrapper) {
        if (reference != null) {
            reference.clear();
        }
        reference = new WeakReference<>(activity);
        this.wrapper = wrapper;
        init();
    }

    private void init() {
        wrapper.addJavascriptInterface(new ViaInterface(s -> {
            if (TextUtils.isEmpty(s) || reference.get() == null || reference.get().isFinishing()) {
                return;
            }
            String str2 = new String(Base64.decode(s, Base64.NO_WRAP));
//        Log.d(TAG, "ViaInterface: decode: " + str2);
            reference.get().runOnUiThread(() -> {
                if (!wrapper.isOnPause()) {
                    Intent intent = new Intent(reference.get(), JSEditActivity.class);
                    intent.putExtra("via", true);
                    intent.putExtra("viaJs", str2);
                    reference.get().startActivity(intent);
                }
            });
        }), "via");
        wrapper.addJavascriptInterface(getVideoInterface(), "fy_bridge_app");
    }

    private VideoInterface getVideoInterface() {
        return new VideoInterface(new VideoInterface.BridgeListener() {
            @Override
            public void setWebUa(final String ua) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    if (wrapper == null) {
                        return;
                    }
                    String nowUa = wrapper.getUserAgentString();
                    if (TextUtils.isEmpty(ua) && wrapper.getSystemUa().equals(nowUa)) {
                        return;
                    }
                    if (!nowUa.equals(ua)) {
                        wrapper.updateLastDom("");
                        if (TextUtils.isEmpty(ua)) {
                            wrapper.setUserAgentString(wrapper.getSystemUa());
                        } else {
                            wrapper.setUserAgentString(ua);
                        }
                        wrapper.reload();
                        UAModel.updateUa(wrapper.getUrl(), ua);
                    }
                });
            }

            @Override
            public void setWebTitle(String title) {
                EventBus.getDefault().post(new OnSetWebTitleEvent(title));
            }

            @Override
            public void showPic(final String url) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    if (!wrapper.isOnPause()) {
                        new XPopup.Builder(reference.get())
                                .asImageViewer(null, url, new PopImageLoaderNoView(wrapper.getUrl()))
                                .show();
                    }
                });
            }

            @Override
            public void playVideo(final String title, final String url) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    if (!wrapper.isOnPause()) {
                        startPlayVideo(url);
                    }
                });
            }

            @Override
            public void playVideos(List<VideoChapter> chapters) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    if (!wrapper.isOnPause()) {
                        startPlayVideo(chapters);
                    }
                });
            }

            @Override
            public void playVideo(final String url) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    if (!wrapper.isOnPause()) {
                        startPlayVideo(url);
                    }
                });
            }

            @Override
            public void setAppBarColor(String color, String isTheme) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                wrapper.setAppBarColor(color, isTheme);
            }

            @Override
            public void setAdBlock(String html, String rule) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                if (!wrapper.isOnPause()) {
                    EventBus.getDefault().post(new OnSetAdBlockEvent(html, rule));
                }
            }

            @Override
            public String getNetworkRecords() {
                List<DetectedMediaResult> results = DetectorManager.getInstance().getDetectedMediaResults((Media) null);
                return JSON.toJSONString(results);
            }

            @Override
            public void importRule(String rule) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                if (!wrapper.isOnPause()) {
                    reference.get().runOnUiThread(() -> AutoImportHelper.checkAutoText(reference.get(), rule));
                }
            }

            @Override
            public String fetch(String url, String option) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return "";
                }
                if (url.startsWith("hiker://files/")) {
                    String content = "";
                    String fileName = url.replace("hiker://files/", "");
                    if ("bindDataToHtml.js".equals(fileName)) {
//                        if (bindDataToHtml == null) {
//                            bindDataToHtml = FilesInAppUtil.getAssetsString(reference.get(), fileName);
//                        }
//                        content = bindDataToHtml;
                    } else if ("vConsole.js".equals(fileName)) {
                        if (vConsole == null) {
                            vConsole = FilesInAppUtil.getAssetsString(reference.get(), fileName);
                        }
                        content = vConsole;
                    } else if ("aes.js".equals(fileName)) {
                        content = FilesInAppUtil.getAssetsString(reference.get(), fileName);
                    } else if ("vConsoleShow.js".equals(fileName)) {
                        if (vConsoleShow == null) {
                            vConsoleShow = FilesInAppUtil.getAssetsString(reference.get(), fileName);
                        }
                        content = vConsoleShow;
                    } else if ("jquery.min.js".equals(fileName)) {
                        if (jquery == null) {
                            jquery = FilesInAppUtil.getAssetsString(reference.get(), fileName);
                        }
                        content = jquery;
                    } else {
                        File file = new File(UriUtils.getRootDir(reference.get()) + File.separator + fileName);
                        if (file.exists()) {
                            content = FileUtil.fileToString(file.getAbsolutePath());
                        }
                    }
                    return content;
                } else if (url.startsWith("file://")) {
                    url = url.replace("file://", "");
                    File file = new File(url);
                    if (file.exists()) {
                        return FileUtil.fileToString(file.getAbsolutePath());
                    } else {
                        return "";
                    }
                }
                return JSEngine.getInstance().fetch(url, JSON.parseObject(option));
            }

            @Override
            public void fetchAsync(String url, String options, String key, String funcName) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                HeavyTaskUtil.executeNewTask(() -> {
                    if (reference.get() == null || reference.get().isFinishing()) {
                        return;
                    }
                    try {
                        String result = fetch(url, options);
                        requestAsyncMap.put(key, result);
                        EventBus.getDefault().post(new OnEvalJsEvent("(function() {" +
                                funcName + "('" + key + "', fy_bridge_app.getResultByKey('" + key + "'))" +
                                "})();"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public String getResultByKey(String key) {
                if (requestAsyncMap.containsKey(key)) {
                    String result = requestAsyncMap.get(key);
                    requestAsyncMap.remove(key);
                    return result;
                }
                return "";
            }

            @Override
            public void writeFile(String filePath, String content) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                if (filePath.startsWith("hiker://files/")) {
                    String fileName = filePath.replace("hiker://files/", "");
                    filePath = UriUtils.getRootDir(reference.get()) + File.separator + fileName;
                } else if (filePath.startsWith("file://")) {
                    filePath = filePath.replace("file://", "");
                }
                if (filePath.startsWith(UriUtils.getRootDir(reference.get()))) {
                    if (JSEngine.inJsDir(filePath)) {
                        //插件目录，禁止读写插件
                        return;
                    }
                    try {
                        FileUtil.stringToFile(content, filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void putVar(String key, String value) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", key);
                jsonObject.put("value", value);
                JSEngine.getInstance().putVar(jsonObject);
            }

            @Override
            public String getVar(String key) {
                return JSEngine.getInstance().getVar(key, "");
            }

            @Override
            public String getCookie(String url) {
                if (StringUtil.isEmpty(url)) {
                    url = StringUtil.getDom(wrapper.getMyUrl()) + "/";
                }
                if (!StringUtils.equals(StringUtil.getDom(url), StringUtil.getDom(wrapper.getMyUrl()))) {
                    return "error:cannot get cookie from another domain";
                }
                try {
                    return CookieManager.getInstance().getCookie(url);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "error:" + e.getMessage();
                }
            }

            @Override
            public void allowSelect() {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
//                reference.get().runOnUiThread(() -> {
//                new Thread(new TouchEventRunnable((int) horizontalWebView.getFocusX(), (int) (horizontalWebView.getFocusY() + ScreenUtil.getStatusBarHeight(reference.get())), true)).start();
//                horizontalWebView.getHandler().postDelayed(() -> horizontalWebView.setOnLongClickListener(horizontalWebView.getLongClickListener()), 2500);
//                });
            }

            @Override
            public void saveAdBlockRule(String rule) {
                EventBus.getDefault().post(new OnSaveAdBlockRuleEvent(rule));
            }

            @Override
            public void setImgUrls(String urls) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    List<String> images = Arrays.asList((urls == null ? "" : urls).split("&&"));
                    ArrayList<String> imageUrls = new ArrayList<>();
                    for (int i = 0; i < images.size(); i++) {
                        imageUrls.add(HttpUtil.getRealUrl(wrapper.getUrl(), images.get(i)));
                    }
                    Intent intent = new Intent(reference.get(), PictureListActivity.class);
                    intent.putStringArrayListExtra("pics", imageUrls);
                    intent.putExtra("url", wrapper.getUrl());
                    reference.get().startActivity(intent);
                });
            }

            @Override
            public void setImgHref(String url) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                EventBus.getDefault().post(new OnImgHrefFindEvent(url));
            }

            @Override
            public void searchBySelect(String text) {
                if (StringUtil.isNotEmpty(text)) {
                    EventBus.getDefault().post(new ShowSearchEvent(text));
                }
            }

            @Override
            public void translate(String text) {
                if (StringUtil.isNotEmpty(text)) {
                    EventBus.getDefault().post(new ShowTranslateEvent(text));
                }
            }

            @Override
            public void refreshPage(boolean scrollTop) {
                if (EventBus.getDefault().hasSubscriberForEvent(OnRefreshPageEvent.class)) {
                    EventBus.getDefault().post(new OnRefreshPageEvent(scrollTop));
                }
            }

            @Override
            public void back(boolean refreshPage) {
                if (EventBus.getDefault().hasSubscriberForEvent(OnBackEvent.class)) {
                    EventBus.getDefault().post(new OnBackEvent(refreshPage, false));
                }
            }

            @Override
            public String getInternalJs() {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return "";
                }
                return JsPluginHelper.getInternalJs(reference.get());
            }

            @Override
            public String parseDomForHtml(String html, String rule) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return "";
                }
                return JSEngine.getInstance().parseDomForHtml(html, rule);
            }

            @Override
            public String parseDomForArray(String html, String rule) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return "";
                }
                return JSEngine.getInstance().parseDomForArray(html, rule);
            }

            @Override
            public void saveImage(String url) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                if (StringUtil.isEmpty(url)) {
                    ToastMgr.shortBottomCenter(reference.get(), "图片地址不能为空");
                }
                ImgUtil.savePic2Gallery(reference.get(), url, null, new ImgUtil.OnSaveListener() {
                    @Override
                    public void success(List<String> paths) {
                        if (reference.get() != null && !reference.get().isFinishing()) {
                            reference.get().runOnUiThread(() -> ToastMgr.shortBottomCenter(reference.get(), "图片已保存到相册"));
                        }
                    }

                    @Override
                    public void failed(String msg) {
                        if (reference.get() != null && !reference.get().isFinishing()) {
                            reference.get().runOnUiThread(() -> ToastMgr.shortBottomCenter(reference.get(), "保存失败：" + msg));
                        }
                    }
                });
            }

            @Override
            public void toDetailPage(String ruleTitle, String url, String group, String colType, String detailFindRule, String preRule) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    ArticleListRule articleListRule1 = new ArticleListRule();
                    articleListRule1.setUrl(url);
                    articleListRule1.setFind_rule(detailFindRule);
                    articleListRule1.setCol_type(colType);
                    articleListRule1.setGroup(group);
                    articleListRule1.setPreRule(preRule);
                    articleListRule1.setTitle(ruleTitle);
                    Intent intent = new Intent(reference.get(), FilmListActivity.class);
                    intent.putExtra("data", JSON.toJSONString(articleListRule1));
                    intent.putExtra("title", ruleTitle);
                    reference.get().startActivityForResult(intent, FilmListActivity.REFRESH_PAGE_CODE);
                });
            }

            @Override
            public void open(DetailPage page) {
                if (reference.get() == null || reference.get().isFinishing() || page == null) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    if (PageParser.isPageUrl(page.getUrl())) {
                        toNextPage(page.getTitle(), page.getUrl(), JSON.toJSONString(page.getExtra()), page);
                        return;
                    }
                    RuleDTO ruleDTO = new RuleDTO();
                    ruleDTO.setTitle(StringUtil.isNotEmpty(page.getRule()) ? page.getRule() : page.getTitle());
                    ruleDTO.setUrl(page.getUrl());
                    ruleDTO.setUa(page.getUa());
                    ruleDTO.setRule(page.getFindRule());
                    if (StringUtil.isNotEmpty(page.getPages())) {
                        ruleDTO.setPages(page.getPages());
                    }
                    MiniProgramRouter.INSTANCE.startMiniProgram(reference.get(), page.getUrl(), page.getTitle(), ruleDTO);
                });
            }

            private void toNextPage(String title, String url, String params, DetailPage page) {
                HeavyTaskUtil.executeNewTask(() -> {
                    try {
                        ArticleListRule nextPage = PageParser.getNextPage(null, url, params);
                        if (page != null && StringUtil.isNotEmpty(page.getPages())) {
                            nextPage.setPages(page.getPages());
                        }
                        if (reference.get() != null && !reference.get().isFinishing()) {
                            reference.get().runOnUiThread(() -> {
                                RuleDTO ruleDTO = new RuleDTO();
                                ruleDTO.setTitle(nextPage.getTitle());
                                ruleDTO.setUrl(nextPage.getUrl());
                                ruleDTO.setUa(nextPage.getUa());
                                ruleDTO.setRule(nextPage.getFind_rule());
                                if (StringUtil.isNotEmpty(nextPage.getPages())) {
                                    ruleDTO.setPages(nextPage.getPages());
                                }
                                MiniProgramRouter.INSTANCE.startMiniProgram(reference.get(), nextPage.getUrl(), title, ruleDTO);
                            });
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        if (reference.get() != null && !reference.get().isFinishing()) {
                            reference.get().runOnUiThread(() -> ToastMgr.shortCenter(reference.get(), e.getMessage()));
                        }
                    }
                });
            }

            @Override
            public void newPage(String title, String url) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }
                reference.get().runOnUiThread(() -> {
                    ArticleListRule articleListRule1 = new ArticleListRule();
                    articleListRule1.setUrl("hiker://empty");
                    articleListRule1.setFind_rule("js:setResult([{\n" +
                            "    url:\"" + title + "\",\n" +
                            "desc:\"100%&&float\"\n" +
                            "}]);");
                    articleListRule1.setCol_type(ArticleColTypeEnum.X5_WEB_VIEW.getCode());
                    Intent intent = new Intent(reference.get(), FilmListActivity.class);
                    intent.putExtra("data", JSON.toJSONString(articleListRule1));
                    intent.putExtra("title", title);
                    reference.get().startActivityForResult(intent, FilmListActivity.REFRESH_PAGE_CODE);
                });
            }

            @Override
            public String isPc() {
                boolean pc = false;
                try {
                    pc = UAModel.WebUA.PC.getContent().equals(wrapper.getUserAgentString());
                } catch (Exception e) {
                    //不能在thread 'JavaBridge'获取UA
                    e.printStackTrace();
                }
                Timber.d("is pic: %s", pc);
                return Boolean.valueOf(pc).toString();
            }

            @Override
            public void finishParse(String url, String mode, String ticket) {
                EventBus.getDefault().post(new DestroyEvent(url, mode, ticket));
            }

            @Override
            public String getUrls() {
                return JSON.toJSONString(wrapper.getUrls());
            }

            @Override
            public String parseLazyRule(String url) {
                return parseLazy(url, wrapper.getMyUrl());
            }

            @Override
            public void parseLazyRuleAsync(String url, String callback) {
                if (StringUtil.isEmpty(callback)) {
                    return;
                }
                String webUrl = wrapper.getMyUrl();
                HeavyTaskUtil.executeNewTask(() -> {
                    String res = parseLazy(url, webUrl);
                    EventBus.getDefault().post(new OnEvalJsEvent("(function() {" +
                            "var input = '" + Utils.escapeJavaScriptString(res) + "'; eval('" + Utils.escapeJavaScriptString(callback) + "')" +
                            "})();"));
                });
            }

            private String parseLazy(String url, String webUrl) {
                if (StringUtil.isEmpty(url) || !url.contains("@lazyRule=")) {
                    return url;
                }
                String[] lazyRule = url.split("@lazyRule=");
                if (lazyRule.length < 2) {
                    return url;
                }
                if (lazyRule[1].startsWith(".js:")) {
                    return JSEngine.getInstance().evalJS(JSEngine.getInstance().generateMY("MY_URL", webUrl)
                            + StringUtils.replaceOnce(lazyRule[1], ".js:", ""), lazyRule[0]);
                }
                return url;
            }

            @Override
            public String getHeaderUrl(String url) {
                String referer = wrapper.getMyUrl();
                String cookie = "";
                try {
                    if (StringUtil.isNotEmpty(referer)) {
                        cookie = wrapper.getCookie(referer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return PlayerChooser.decorateHeader(
                        wrapper.getRequestHeaderMap().get(url),
                        referer,
                        url,
                        cookie
                );
            }

            @Override
            public String getRequestHeaders0() {
                return JSON.toJSONString(wrapper.getRequestHeaderMap());
            }
        });
    }


    public static void showCode(Activity activity, String url, String userAgent) {
        if (StringUtil.isEmpty(url)) {
            ToastMgr.shortBottomCenter(activity, "当前网页不支持该模式查看源码");
            return;
        }
        Intent intent1 = new Intent(activity, HtmlSourceActivity.class);
//                                    intent1.putExtra("code", tempCode);
        try {
            String cookie = CookieManager.getInstance().getCookie(url);
            if (StringUtil.isEmpty(cookie)) {
                cookie = "";
            }
            cookie = cookie.replace(";", "；；");
            userAgent = userAgent.replace(";", "；；");
            String header = "{User-Agent@" + userAgent + "&&Cookie@" + cookie + "}";
            intent1.putExtra("url", url + ";get;UTF-8;" + header);
        } catch (Throwable e) {
            e.printStackTrace();
            intent1.putExtra("url", url);
        }
        activity.startActivity(intent1);
    }

    public void startPlayVideo(String videoUrl) {
        if (reference.get() == null || reference.get().isFinishing()) {
            return;
        }
        WebUtil.setShowingUrl(wrapper.getUrl());
        String muteJs = JSManager.instance(reference.get()).getJsByFileName("mute");
        if (!TextUtils.isEmpty(muteJs)) {
            wrapper.evaluateJavascript(muteJs);
        }
        if (wrapper.isOnPause()) {
            return;
        }
        String dom = StringUtil.getDom(wrapper.getUrl());
        String url = StringUtil.getDom(videoUrl);
        DetectorManager.getInstance().putIntoXiuTanLiked(reference.get(), dom, url);
        HeavyTaskUtil.updateHistoryVideoUrl(wrapper.getUrl(), videoUrl);
        PlayerChooser.startPlayer(reference.get(), getWebTitle(), videoUrl);
    }

    public void startPlayVideo(List<VideoChapter> chapters) {
        if (reference.get() == null || reference.get().isFinishing()) {
            return;
        }
        WebUtil.setShowingUrl(wrapper.getUrl());
        String muteJs = JSManager.instance(reference.get()).getJsByFileName("mute");
//        Log.d(TAG, "startPlayVideo:1 ");
        if (!TextUtils.isEmpty(muteJs)) {
//            Log.d(TAG, "startPlayVideo:2 ");
            wrapper.evaluateJavascript(muteJs);
        }
        if (wrapper.isOnPause()) {
            return;
        }
        PlayerChooser.startPlayer(reference.get(), chapters);
    }

    private String getWebTitle() {
        String t = wrapper.getTitle().replace(" ", "");
        if (t.length() > 85) {
            t = t.substring(0, 85);
        }
        return t;
    }
}
