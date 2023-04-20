package com.example.hikerview.ui.browser.webview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.ServiceWorkerClientCompat;
import androidx.webkit.ServiceWorkerControllerCompat;
import androidx.webkit.WebViewFeature;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.constants.Media;
import com.example.hikerview.constants.MediaType;
import com.example.hikerview.event.web.DownloadStartEvent;
import com.example.hikerview.event.web.OnCreateWindowEvent;
import com.example.hikerview.event.web.OnFindInfoEvent;
import com.example.hikerview.event.web.OnHideCustomViewEvent;
import com.example.hikerview.event.web.OnLoadUrlEvent;
import com.example.hikerview.event.web.OnLongClickEvent;
import com.example.hikerview.event.web.OnMenuItemClickEvent;
import com.example.hikerview.event.web.OnOverrideUrlLoadingForHttp;
import com.example.hikerview.event.web.OnOverrideUrlLoadingForOther;
import com.example.hikerview.event.web.OnPageFinishedEvent;
import com.example.hikerview.event.web.OnPageStartEvent;
import com.example.hikerview.event.web.OnProgressChangedEvent;
import com.example.hikerview.event.web.OnSetWebTitleEvent;
import com.example.hikerview.event.web.OnShowCustomViewEvent;
import com.example.hikerview.event.web.OnShowFileChooserEvent;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.ViaInterface;
import com.example.hikerview.ui.browser.data.DomainConfigKt;
import com.example.hikerview.ui.browser.model.AdBlockModel;
import com.example.hikerview.ui.browser.model.AdUrlBlocker;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.ui.browser.model.DetectorManager;
import com.example.hikerview.ui.browser.model.JSMenu;
import com.example.hikerview.ui.browser.model.UAModel;
import com.example.hikerview.ui.browser.model.VideoTask;
import com.example.hikerview.ui.browser.service.BrowserProxy;
import com.example.hikerview.ui.browser.service.DomainConfigService;
import com.example.hikerview.ui.browser.view.BaseWebViewActivity;
import com.example.hikerview.ui.home.ArticleListRuleEditActivity;
import com.example.hikerview.ui.js.JSEditActivity;
import com.example.hikerview.ui.miniprogram.MiniProgramRouter;
import com.example.hikerview.ui.setting.TextSizeActivity;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.ui.view.popup.InputPopup;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StatusBarCompatUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ThreadTool;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.ConfirmPopupView;

import org.adblockplus.libadblockplus.android.Utils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.joor.Reflect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;
import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2020/4/5
 * 时间：At 13:03
 */
public class WebViewHelper implements HorizontalWebView.onLoadListener, ServiceWorkerInterceptor {
    // decisions
    public final static String RESPONSE_CHARSET_NAME = "UTF-8";
    public final static String RESPONSE_MIME_TYPE = "text/plain";


    private static final String TAG = "WebViewHelper";
    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;
    private HorizontalWebView horizontalWebView;
    private String systemUA = "";
    private boolean hasSetAppBarColor = false;
    private String myUrl = "";
    private JsBridgeHolder jsBridgeHolder;

    private WeakReference<BaseWebViewActivity> reference;
    private AtomicBoolean hasLoadJsOnProgress = new AtomicBoolean(false);
    private AtomicBoolean hasLoadJsOnPageEnd = new AtomicBoolean(false);
    private Map<String, Map<String, String>> requestHeaderMap = new HashMap<>();
    private static final Map<String, Integer> disallowLocationSet = new HashMap<>();
    public static final Map<String, Integer> disallowAppSet = new HashMap<>();
    private boolean adBlockMarking = false;
    private AdblockHolder adblockHolder;
    private List<JsResult> jsResults = new ArrayList<>();
    public static final WebResourceResponse RESPONSE_IGNORE =
            new WebResourceResponse(RESPONSE_MIME_TYPE, RESPONSE_CHARSET_NAME, null);

    public Map<String, Map<String, String>> getRequestHeaderMap() {
        return requestHeaderMap;
    }

    public static Map<String, String> getRequestHeaderMap(HorizontalWebView webView, String playUrl) {
        if (webView != null && webView.getWebViewHelper() != null) {
            return webView.getWebViewHelper().getRequestHeaderMap().get(playUrl);
        }
        return null;
    }


    public List<String> getGreasyForkRules() {
        if (jsBridgeHolder == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(jsBridgeHolder.getGreasyForkMenuMap().keySet());
    }

    public List<String> getGreasyForkMenus(String rule) {
        List<JSMenu> menu = jsBridgeHolder.getGreasyForkMenuMap().get(rule);
        if (menu != null) {
            return Stream.of(menu).map(JSMenu::getName).toList();
        }
        return new ArrayList<>();
    }

    public void triggerGreasyForkMenu(String rule, String menu) {
        List<JSMenu> jsMenus = jsBridgeHolder.getGreasyForkMenuMap().get(rule);
        if (jsMenus != null) {
            for (JSMenu jsMenu : jsMenus) {
                if (menu.equals(jsMenu.getName())) {
                    if (StringUtil.isNotEmpty(jsMenu.getFunc())) {
                        horizontalWebView.evaluateJavascript("window['" + jsMenu.getFunc() + "']()", null);
                    }
                    return;
                }
            }
        }
    }

    public WeakReference<BaseWebViewActivity> getReference() {
        return reference;
    }

    public void updateProperties(BaseWebViewActivity activity) {
        if (reference != null) {
            this.reference.clear();
        }
        if (!jsResults.isEmpty()) {
            for (JsResult jsResult : jsResults) {
                try {
                    jsResult.cancel();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            jsResults.clear();
        }
        this.reference = new WeakReference<>(activity);
        InternalContext.getInstance().setBaseContext(activity);
        if (horizontalWebView != null) {
            horizontalWebView.setOnLoadListener(this);
        }
        initWebViewClient();
        initWebChromeClient();
        initDownloadListener();
        initFindListener();
        initLongClickListener();
        initOnMenuItemClickListener();
        adblockHolder = new AdblockHolder(activity, horizontalWebView);
        adblockHolder.initAbp();
        horizontalWebView.removeJavascriptInterface("via");
        horizontalWebView.addJavascriptInterface(new ViaInterface(s -> {
            if (TextUtils.isEmpty(s) || reference.get() == null || reference.get().isFinishing()) {
                return;
            }
            String str2 = new String(Base64.decode(s, Base64.NO_WRAP));
//        Log.d(TAG, "ViaInterface: decode: " + str2);
            reference.get().runOnUiThread(() -> {
                if (!reference.get().isOnPause()) {
                    Intent intent = new Intent(reference.get(), JSEditActivity.class);
                    intent.putExtra("via", true);
                    intent.putExtra("viaJs", str2);
                    reference.get().startActivity(intent);
                }
            });
        }), "via");
        if (jsBridgeHolder != null) {
            jsBridgeHolder.update(reference.get(), getWrapper());
        } else {
            jsBridgeHolder = new JsBridgeHolder(reference, getWrapper());
        }
    }

    private WebViewWrapper getWrapper() {
        return new WebViewWrapper() {
            @Override
            public String getTitle() {
                return horizontalWebView.getTitle();
            }

            @Override
            public String getUrl() {
                return horizontalWebView.getUrl();
            }

            @Override
            public List<String> getUrls() {
                return new ArrayList<>();
            }

            @Override
            public String getMyUrl() {
                return myUrl;
            }

            @Override
            public void evaluateJavascript(String script) {
                horizontalWebView.evaluateJavascript(script, null);
            }

            @Override
            public String getSystemUa() {
                return systemUA;
            }

            @Override
            public String getUserAgentString() {
                return ThreadTool.INSTANCE.getStrOnUIThread(urlHolder -> {
                    if (StringUtil.isNotEmpty(horizontalWebView.getUa())) {
                        urlHolder.setUrl(horizontalWebView.getUa());
                    } else {
                        urlHolder.setUrl(horizontalWebView.getSettings().getUserAgentString());
                    }
                });

            }

            @Override
            public void setUserAgentString(String userAgentString) {
                horizontalWebView.getSettings().setUserAgentString(userAgentString);
            }

            @Override
            public void reload() {
                horizontalWebView.reload();
            }

            @Override
            public boolean isOnPause() {
                return reference.get() != null && reference.get().isOnPause();
            }

            @Override
            public void loadUrl(String url) {
                horizontalWebView.loadUrl(url);
            }

            @Override
            public void updateLastDom(String dom) {
                lastDom = "";
            }

            @Override
            public void setAppBarColor(String color, String isTheme) {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return;
                }

                if ("true".equals(isTheme) && hasSetAppBarColor) {
                    return;
                }
                if (!"true".equals(isTheme)) {
                    hasSetAppBarColor = true;
                }
                try {
                    reference.get().runOnUiThread(() -> changeActionBarColor(color));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @SuppressLint("JavascriptInterface")
            @Override
            public void addJavascriptInterface(Object obj, String interfaceName) {
                horizontalWebView.removeJavascriptInterface(interfaceName);
                horizontalWebView.addJavascriptInterface(obj, interfaceName);
            }

            @Override
            public Map<String, Map<String, String>> getRequestHeaderMap() {
                return requestHeaderMap;
            }

            @Override
            public String getCookie(String url) {
                try {
                    return CookieManager.getInstance().getCookie(url);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void openThirdApp(String scheme) {

            }
        };
    }

    public void removeProperties() {
        horizontalWebView.removeJavascriptInterface("fy_bridge_app");
        horizontalWebView.removeJavascriptInterface("via");
        horizontalWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {

        });
        horizontalWebView.setFindListener((i, i1, b) -> {

        });
        horizontalWebView.setWebChromeClient(new WebChromeClient() {
        });
        horizontalWebView.setWebViewClient(new WebViewClient() {
        });
        horizontalWebView.setLongClickListener(v -> false);
        horizontalWebView.setOnMenuItemClickListener(item -> false);
        if (reference != null) {
            this.reference.clear();
        }
    }

    public String getLastDom() {
        return lastDom;
    }

    public void setLastDom(String lastDom) {
        this.lastDom = lastDom;
    }

    private String lastDom = "";
    private boolean geolocationGranted = false, geolocationTemp = false;

    public WebViewHelper(BaseWebViewActivity activity, HorizontalWebView horizontalWebView) {
        this.horizontalWebView = horizontalWebView;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        horizontalWebView.setLayoutParams(layoutParams);
        horizontalWebView.setFocusable(true);
        horizontalWebView.setFocusableInTouchMode(true);

        updateProperties(activity);

        horizontalWebView.setOnLongClickListener(horizontalWebView.getLongClickListener());
        WebSettings webSettings = horizontalWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 50);
        String appCachePath = activity.getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowContentAccess(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        webSettings.setMinimumFontSize(10);
        webSettings.setTextZoom(TextSizeActivity.getTextZoom(activity));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDefaultFixedFontSize(16);
        webSettings.setDefaultFontSize(16);
        //定位权限
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        String dir = activity.getDir("database", Context.MODE_PRIVATE).getPath();
        webSettings.setGeolocationDatabasePath(dir);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            horizontalWebView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        } else {
            webSettings.setSaveFormData(true);
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//            boolean forceDark = PreferenceMgr.getBoolean(activity, "forceDark", true);
//            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && forceDark) {
//                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
//                    WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON);
//                } else {
//                    webSettings.setForceDark(WebSettings.FORCE_DARK_ON);
//                }
//            } else {
//                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
//                    WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_OFF);
//                } else {
//                    webSettings.setForceDark(WebSettings.FORCE_DARK_OFF);
//                }
//            }
//        }
        systemUA = webSettings.getUserAgentString();

        if (StringUtil.isEmpty(SettingConfig.getGlideUA()) || !SettingConfig.getGlideUA().equals(systemUA)) {
            PreferenceMgr.put(activity, "glideUA", systemUA);
            SettingConfig.setGlideUA(systemUA);
        }
        String useUa = PreferenceMgr.getString(activity, "vip", "ua", null);
        if (StringUtil.isNotEmpty(useUa)) {
            String ua = StringUtil.replaceLineBlank(useUa);
            webSettings.setUserAgentString(ua);
            horizontalWebView.setUa(ua);
        }
        UAModel.setUseUa(useUa);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        horizontalWebView.setUa(webSettings.getUserAgentString());
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        DisplayMetrics metrics = new DisplayMetrics();
//        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        int mDensity = metrics.densityDpi;
//        if (mDensity == 240) {
//            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
//        } else if (mDensity == 160) {
//            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
//        } else if (mDensity == 120) {
//            webSettings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
//        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
//            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
//        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
//            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
//        } else {
//            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
//        }

        addServiceWorkerClient(this);
//        WebView.setWebContentsDebuggingEnabled(true);
    }

    private void initDownloadListener() {
        this.horizontalWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (reference.get() == null || reference.get().isFinishing()) {
                return;
            }
            EventBus.getDefault().post(new DownloadStartEvent(this.horizontalWebView, url, userAgent, contentDisposition, mimetype, contentLength));
        });
    }

    private void initFindListener() {
        this.horizontalWebView.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            if (isDoneCounting) {
                EventBus.getDefault().post(new OnFindInfoEvent(numberOfMatches != 0 ? String.format("%d/%d", (activeMatchOrdinal + 1), numberOfMatches) : "0/0"));
            }
        });
    }

    private void initOnMenuItemClickListener() {
        this.horizontalWebView.setOnMenuItemClickListener(item -> {
            EventBus.getDefault().post(new OnMenuItemClickEvent());
            return true;
        });
    }

    private void initLongClickListener() {
        this.horizontalWebView.setLongClickListener(webView -> {
            if (!(webView instanceof HorizontalWebView)) {
                return false;
            }
            HorizontalWebView view = (HorizontalWebView) webView;
            if (!view.isUsed()) {
                return false;
            }
            WebView.HitTestResult result = view.getHitTestResult();
            EventBus.getDefault().post(new OnLongClickEvent(result));
            return false;
        });
    }

    private String getWebTitle() {
        String t = horizontalWebView.getTitle().replace(" ", "");
        if (t.length() > 85) {
            t = t.substring(0, 85);
        }
        return t;
    }

    private void initWebChromeClient() {
        webChromeClient = new WebChromeClient() {

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                if (adBlockMarking) {
                    return true;
                }
                WebView webView1;
                String ua = view.getSettings().getUserAgentString();
                if (StringUtil.isEmpty(ua) || ua.contains("Android")) {
                    //为啥不都拦截shouldOverrideUrlLoading的方法来打开新窗口呢
                    //因为他娘的POST请求的新窗口不走shouldOverrideUrlLoading，且shouldInterceptRequest无法获取参数
                    HorizontalWebView webView = MultiWindowManager.instance(reference.get()).addWebView(null, true, view);
                    CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
                    EventBus.getDefault().post(new OnCreateWindowEvent(webView));
                    webView1 = webView;
                } else {
                    //不直接new一个的原因是有些网站（如哔哩）shouldOverrideUrlLoading的requestHeader会携带User-Agent
                    // 且为安卓UA，即使设置了PC的UA，估计是内核bug
                    webView1 = new WebView(view.getContext());
                    String referer = myUrl;
                    webView1.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view1, WebResourceRequest request) {
                            HorizontalWebView webView = MultiWindowManager.instance(reference.get()).addWebView(null, true, view);
                            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
                            String url = request.getUrl().toString();
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Referer", referer);
                            if (request.getRequestHeaders() != null && request.getRequestHeaders().containsKey("User-Agent")) {
                                request.getRequestHeaders().remove("User-Agent");
                                for (Map.Entry<String, String> entry : request.getRequestHeaders().entrySet()) {
                                    headers.put(entry.getKey(), entry.getValue());
                                }
                            }
                            webView.loadUrl(url, headers);
                            EventBus.getDefault().post(new OnCreateWindowEvent(webView));
                            try {
                                view1.onPause();
                                view1.destroy();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    });
                }
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(webView1);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (horizontalWebView.isUsed()) {
                    EventBus.getDefault().post(new OnSetWebTitleEvent(title));
                }
                injectThemeCss(view);
            }

            @Override
            public void onProgressChanged(WebView webView, int i) {
                super.onProgressChanged(webView, i);
                if (!(webView instanceof HorizontalWebView)) {
                    return;
                }
                if (!ArticleListRuleEditActivity.hasBlockDom(webView.getUrl()) && !DomainConfigKt.isDisableAdBlock(webView.getUrl())) {
                    adblockHolder.injectJsOnProgress();
                }
                HorizontalWebView webViewT = (HorizontalWebView) webView;
                if (!hasLoadJsOnProgress.get() && i >= 40 && i < 100) {
                    injectThemeCss(webView);
                    hasLoadJsOnProgress.set(true);
                    if (webViewT.isUseDevMode()) {
                        webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(reference.get(), "vConsole.js"), null);
                    }
                    loadAllJs(webViewT, webViewT.getUrl(), false);
                }
                if (i < 40) {
                    injectThemeCss(webView);
                }
                if (!webViewT.isUsed()) {
                    return;
                }
                EventBus.getDefault().post(new OnProgressChangedEvent(i));
//                Log.d(TAG, "onProgressChanged: " + i + ", webview=" + webView.hashCode());
            }

            /*** 视频播放相关的方法 **/
            @Override
            public View getVideoLoadingProgressView() {
                if (reference.get() == null || reference.get().isFinishing()) {
                    return super.getVideoLoadingProgressView();
                }
                FrameLayout frameLayout = new FrameLayout(reference.get());
                frameLayout.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }

            @Override
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
                EventBus.getDefault().post(new OnShowCustomViewEvent(view, callback));
            }

            @Override
            public void onHideCustomView() {
                EventBus.getDefault().post(new OnHideCustomViewEvent());
            }

            @Override
            public boolean onJsAlert(WebView webView, String url, String message, final JsResult result) {
                if (!(webView instanceof HorizontalWebView)) {
                    return super.onJsAlert(webView, url, message, result);
                }
                HorizontalWebView view = (HorizontalWebView) webView;
                if (!view.isUsed()) {
                    return super.onJsAlert(webView, url, message, result);
                }
                if (reference.get() == null || reference.get().isFinishing()) {
                    result.cancel();
                    return true;
                }
                JsResultHolder jsResultHolder = new JsResultHolder();
                jsResults.add(result);
                ConfirmPopupView popupView = new XPopup.Builder(reference.get())
                        .asConfirm("网页提示", message, () -> {
                            jsResultHolder.consumed = true;
                            jsResults.remove(result);
                            result.confirm();
                        }, () -> {
                            jsResultHolder.consumed = true;
                            jsResults.remove(result);
                            result.cancel();
                        });
                Reflect.on(popupView).set("dismissWithRunnable", (Runnable) () -> {
                    try {
                        if (!jsResultHolder.consumed) {
                            jsResultHolder.consumed = true;
                            jsResults.remove(result);
                            result.cancel();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                popupView.show();
                return true;
            }

            final class JsResultHolder {
                boolean consumed;
            }

            @Override
            public boolean onJsConfirm(WebView webView, String url, String message, final JsResult result) {
                if (!(webView instanceof HorizontalWebView)) {
                    return super.onJsConfirm(webView, url, message, result);
                }
                HorizontalWebView view = (HorizontalWebView) webView;
                if (!view.isUsed()) {
                    return super.onJsConfirm(webView, url, message, result);
                }
                if (reference.get() == null || reference.get().isFinishing()) {
                    result.cancel();
                    return true;
                }
                JsResultHolder jsResultHolder = new JsResultHolder();
                jsResults.add(result);
                ConfirmPopupView popupView = new XPopup.Builder(reference.get())
                        .asConfirm("网页提示", message, () -> {
                            jsResultHolder.consumed = true;
                            jsResults.remove(result);
                            result.confirm();
                        }, () -> {
                            jsResultHolder.consumed = true;
                            jsResults.remove(result);
                            result.cancel();
                        });
                Reflect.on(popupView).set("dismissWithRunnable", (Runnable) () -> {
                    try {
                        if (!jsResultHolder.consumed) {
                            jsResultHolder.consumed = true;
                            jsResults.remove(result);
                            result.cancel();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                popupView.show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView webView, String url, String message, String defaultValue, final JsPromptResult result) {
                if (!(webView instanceof HorizontalWebView)) {
                    return super.onJsPrompt(webView, url, message, defaultValue, result);
                }
                HorizontalWebView view = (HorizontalWebView) webView;
                if (!view.isUsed()) {
                    return super.onJsPrompt(webView, url, message, defaultValue, result);
                }
                if (reference.get() == null || reference.get().isFinishing()) {
                    result.cancel();
                    return true;
                }
                new XPopup.Builder(reference.get())
                        .dismissOnBackPressed(false)
                        .dismissOnTouchOutside(false)
                        .asInputConfirm("来自网页的输入请求", message, defaultValue, null, result::confirm, result::cancel, R.layout.xpopup_confirm_input).show();
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                if (!(webView instanceof HorizontalWebView)) {
                    return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
                }
                HorizontalWebView view = (HorizontalWebView) webView;
                if (!view.isUsed()) {
                    return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
                }
                EventBus.getDefault().post(new OnShowFileChooserEvent(filePathCallback));
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                try {
                    Timber.d(consoleMessage.message() + ", " + consoleMessage.messageLevel() + ", " +
                            consoleMessage.lineNumber() + ", " + consoleMessage.sourceId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Nullable
            @Override
            public Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
            }

            @Override
            public void onGeolocationPermissionsHidePrompt() {
                Timber.d("onGeolocationPermissionsHidePrompt");
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                Timber.d("onGeolocationPermissionsShowPrompt");
                if (!SettingConfig.openGeoNotify) {
                    callback.invoke(origin, false, false);
                    return;
                }
                if (geolocationGranted) {
                    Timber.d("onGeolocationPermissionsShowPrompt, geolocationGranted");
                    callback.invoke(origin, geolocationGranted, false);
                } else if (DomainConfigKt.isAllowGetLocation(origin)) {
                    callback.invoke(origin, true, false);
                } else if (reference.get() != null) {
                    geolocationTemp = false;
                    String dom = StringUtil.getDom(origin);
                    Integer disallowLocation = disallowLocationSet.get(dom);
                    if ((disallowLocation != null && disallowLocation > 1) || DomainConfigKt.isDisableGetLocation(dom)) {
                        Timber.d("onGeolocationPermissionsShowPrompt, disallowLocationSet.contains: %s", origin);
                        geolocationGranted = geolocationTemp;
                        callback.invoke(origin, geolocationTemp, false);
                        return;
                    }
                    ViewGroup snackBg = reference.get().findViewById(R.id.snack_bar_bg);
                    Snackbar.make(snackBg, "允许获取手机位置？", Snackbar.LENGTH_LONG)
                            .setAction("允许", v -> {
                                if (Build.VERSION.SDK_INT >= 23) {
                                    int checkPermission = ContextCompat.checkSelfPermission(reference.get(), Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(reference.get(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                    }
                                }
                                geolocationTemp = true;
                                DomainConfigService.INSTANCE.allowGetLocation(origin);
                            }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    Timber.d("onGeolocationPermissionsShowPrompt, onDismissed: %s", geolocationTemp);
                                    geolocationGranted = geolocationTemp;
                                    callback.invoke(origin, geolocationTemp, false);
                                    if (!geolocationTemp) {
                                        disallowLocationSet.put(dom, disallowLocation == null ? 1 : 2);
                                    }
                                    super.onDismissed(transientBottomBar, event);
                                }
                            }).show();
                } else {
                    super.onGeolocationPermissionsShowPrompt(origin, callback);
                }
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        };

        horizontalWebView.setWebChromeClient(webChromeClient);
    }

    public static void injectThemeCss(WebView view) {
        String eye = PreferenceMgr.getString(view.getContext(), "eye", null);
        if (StringUtil.isEmpty(eye)) {
            return;
        }
        view.post(() -> view.evaluateJavascript(getThemeInjectCss(eye), null));
    }

    public static void clearThemeCss(WebView view) {
        ThreadTool.INSTANCE.runOnUI(() -> view.evaluateJavascript("window.injectCss0101 = null;if(document.getElementById('link_extra_css_id')) document.getElementsByTagName('head')[0].removeChild(document.getElementById('link_extra_css_id'));", null));
    }

    public static int getDefaultThemeColor(Context context) {
        String eye = PreferenceMgr.getString(context == null ? Application.getContext() : context, "eye", null);
        if (StringUtil.isNotEmpty(eye)) {
            return Color.parseColor(eye);
        } else {
            return Color.WHITE;
        }
    }

    private static String mapAColor(String color) {
        switch (color) {
            case "#F9F9F9":
            case "#DDDDDD":
                return "#67A1E1";
            case "#F4E8D0":
                return "#AC8A65";
            case "#D0E8D0":
                return "#67A363";
            case "#BDD6EC":
                return "#659FDE";
            case "#CEC9E7":
                return "#9686DE";
            case "#343C3E":
                return "#3D77B6";
            default:
                return "#4B4B4B";
        }
    }

    private static String mapDivColor(String color) {
        switch (color) {
            case "#343C3E":
                return "#AAB7BD";
            default:
                return "#444444";
        }
    }

    public static String getThemeInjectCss(String color) {
        StringBuilder buffer = new StringBuilder();
//        String color = "#D3E1D0";
        buffer.append("html,body,table,tr,td,th,tbody,form,article,dt,ul,ol,li,dl,dd,section,footer,nav,strong,aside,header,label,address,bdo,big,blockquote,caption,em,center,cite,dialog,dir,fieldset,figcaption,figure,main,pre,small,h1,h2,h3,h4,h5,h6{background:");
        buffer.append(color);
        buffer.append("!important;background-image:none!important;background-color:");
        buffer.append(color);
        buffer.append("!important;");
        String divColor = mapDivColor(color);
        if (StringUtil.isEmpty(divColor)) {
            buffer.append("}");
        } else {
            buffer.append("color: ").append(divColor).append("!important;}")
                    .append("p,div{color:").append(divColor).append("!important;}");
        }
        buffer.append("a{color:").append(mapAColor(color)).append("!important;}");
        buffer.append("div,p,font{background:transparent!important;background-color:transparent!important;}");
        buffer.append("html,body,a,div,p,img,textarea{-webkit-touch-callout:text !important;-webkit-user-select:text !important;user-select:text !important;}");
        return "function addCss11(styles) {\n" +
                "          let css;\n" +
                "          styles = styles.replace(/\\n+\\s*/g, ' ');\n" +
                "          css = document.createElement('style');\n" +
                "          css.id = 'link_extra_css_id';\n" +
                "          if (css.styleSheet) css.styleSheet.cssText = styles;\n" +
                "          // Support for IE\n" +
                "          else css.appendChild(document.createTextNode(styles)); // Support for the rest\n" +
                "          css.type = 'text/css';\n" +
                "          document.getElementsByTagName('head')[0].appendChild(css);\n" +
                "        }\n" +
                "function injectCss0(){try{\n" +
                "   //console.log('injectCss0: ' + location.host + '--->' + window.injectCss0101);\n" +
                "   if(window.injectCss0101 > 60) {\n" +
                "       window.injectCss0101 = null;\n" +
                "       return;\n" +
                "   }\n" +
                "   window.injectCss0101++;\n" +
                "   let css0001 = \"" + Utils.escapeJavaScriptString(buffer.toString()) + "\";\n" +
                "   if(!document.getElementById('link_extra_css_id')) addCss11(css0001);\n" +
                "   } catch(e){ console.log('injectCss0: ' + e.toString()); }\n" +
                "   setTimeout(injectCss0, 50);\n" +
                "}\n" +
                "if(!window.injectCss0101){\n" +
                "   window.injectCss0101 = 1;\n" +
                "   injectCss0();\n" +
                "}";
    }

    private void initWebViewClient() {
        webViewClient = new WebViewClient() {
            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                injectThemeCss(view);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                String username = null;
                String password = null;

                boolean reuseHttpAuthUsernamePassword = handler
                        .useHttpAuthUsernamePassword();

                if (reuseHttpAuthUsernamePassword && view != null) {
                    String[] credentials = view.getHttpAuthUsernamePassword(host,
                            realm);
                    if (credentials != null && credentials.length == 2) {
                        username = credentials[0];
                        password = credentials[1];
                    }
                }

                if (username != null && password != null) {
                    handler.proceed(username, password);
                } else {
                    if (view != null && reference.get() != null && !reference.get().isFinishing()) {
                        InputPopup inputPopup = new InputPopup(reference.get())
                                .bind("网页登录", "用户名", "", "密码", "", (title, code) -> {
                                    if (StringUtil.isNotEmpty(title) && StringUtil.isNotEmpty(code)) {
                                        view.setHttpAuthUsernamePassword(host, realm, title, code);
                                    }
                                    handler.proceed(title, code);
                                }).setCancelListener(handler::cancel);
                        new XPopup.Builder(reference.get())
                                .dismissOnBackPressed(false)
                                .dismissOnTouchOutside(false)
                                .asCustom(inputPopup)
                                .show();
                    } else {
                        handler.cancel();
                    }
                }
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                myUrl = s;
                geolocationGranted = false;
                requestHeaderMap.clear();
                if (jsBridgeHolder != null) {
                    jsBridgeHolder.clearGreasyForkMenu();
                }
                Timber.d("onPageStarted: %s", s);
                if (!(webView instanceof HorizontalWebView)) {
                    super.onPageStarted(webView, s, bitmap);
                    return;
                }
                HorizontalWebView view = (HorizontalWebView) webView;
                if (!view.isUsed()) {
                    super.onPageStarted(webView, s, bitmap);
                    return;
                }
                if (view.getParentWebView() != null) {
                    view.getParentWebView().setLastViewedUrl(s);
                }
                injectThemeCss(webView);
                if (adblockHolder.isLoading()) {
                    adblockHolder.stopAbpLoading();
                }
                adblockHolder.startAbpLoading(s);
                hasSetAppBarColor = false;
                String dom = StringUtil.getDom(s);
                if (dom != null && !dom.equals(lastDom)) {
//                Log.d(TAG, "onPageStarted: setUserAgentString===>" + s);
                    lastDom = dom;
                    String ua = UAModel.getAdjustUa(s);
                    if (!TextUtils.isEmpty(ua)) {
                        updateUA(view, ua);
                    } else {
                        if (!TextUtils.isEmpty(UAModel.getUseUa())) {
                            updateUA(view, UAModel.getUseUa());
                        } else if (StringUtil.isNotEmpty(systemUA)) {
                            Timber.d("onPageStarted: systemUA");
                            updateUA(view, systemUA);
                        }
                    }
                }
                hasLoadJsOnProgress.set(false);
                hasLoadJsOnPageEnd.set(false);
                EventBus.getDefault().post(new OnPageStartEvent(s));
                super.onPageStarted(webView, s, bitmap);
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                Timber.d("onPageFinished: %s", s);
                if (!(webView instanceof HorizontalWebView)) {
                    super.onPageFinished(webView, s);
                    return;
                }
                HorizontalWebView webViewT = (HorizontalWebView) webView;
                if (webViewT.isNeedClearHistory()) {
                    webViewT.setNeedClearHistory(false);
                    webViewT.clearHistory();
                }
                injectThemeCss(webView);
                if (!hasLoadJsOnPageEnd.get()) {
                    hasLoadJsOnPageEnd.set(true);
                    loadAllJs(webViewT, webViewT.getUrl(), true);
                    if (webViewT.isUseDevMode()) {
                        webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(reference.get(), "vConsole.js"), null);
                    }
                    if (SettingConfig.saveForm) {
                        webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(reference.get(), "forminput.js"), null);
                        HeavyTaskUtil.executeNewTask(() -> {
                            Map<String, String> formData = BigTextDO.getFormInputs(s);
                            if (formData != null && !formData.isEmpty()) {
                                String d = JSON.toJSONString(formData);
                                String js = "(function(){\n" +
                                        "function aaaa1(){\n" +
                                        "   let json1230 = JSON.parse(\"" + Utils.escapeJavaScriptString(d) + "\");\n" +
                                        "   for(let key of Object.keys(json1230)) {\n" +
                                        "    let v = json1230[key];\n" +
                                        "    let b = key.split(',');\n" +
                                        "    let index = (b.length > 1 ? parseInt(b[1]) : 0) || 0;\n" +
                                        "    if(key.startsWith('#')) {\n" +
                                        "      document.querySelectorAll(b[0])[index].value = v;\n" +
                                        "    } else if(key.startsWith('tagInput,')) {\n" +
                                        "      document.querySelectorAll('input')[index].value = v;\n" +
                                        "    } else {\n" +
                                        "      document.querySelectorAll('input[name=' + b[0] + ']')[index].value = v;\n" +
                                        "    }\n" +
                                        "   }\n" +
                                        "}\n" +
                                        "aaaa1();\n" +
                                        "setTimeout(aaaa1, 500);\n" +
                                        "})();";
                                webView.post(() -> webView.evaluateJavascript(js, null));
                            }
                        });
                    }
                }
                if (!webViewT.isUsed()) {
                    super.onPageFinished(webView, s);
                    return;
                }
                EventBus.getDefault().post(new OnPageFinishedEvent(webView.getTitle(), s));
                super.onPageFinished(webView, s);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
                WebResourceResponse response = shouldInterceptRequest0(webView, request);
                if (response != null) {
                    return response;
                }
                return super.shouldInterceptRequest(webView, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                if (adBlockMarking) {
                    return true;
                }
                String url = request.getUrl().toString();
                Timber.d("shouldOverrideUrlLoading: %s", url);
                if (url.startsWith("about:")) {
                    return false;
                }
                if (url.startsWith("http")) {
                    if (MiniProgramRouter.INSTANCE.shouldOverrideUrlLoading(reference.get(), url, webView.getSettings().getUserAgentString())) {
                        return true;
                    }
                    if (!ArticleListRuleEditActivity.hasBlockDom(lastDom) && !DomainConfigKt.isDisableAdBlock(lastDom)) {
                        try {
                            long id = AdUrlBlocker.instance().shouldBlock(lastDom, url);
                            if (id >= 0) {
                                return true;
                            }
                            if (adblockHolder != null) {
                                AdblockHolder.AbpShouldBlockResult abpBlockResult = adblockHolder.shouldAbpBlockRequest(myUrl, request);
                                if (AdblockHolder.AbpShouldBlockResult.BLOCK_LOAD.equals(abpBlockResult)) {
                                    return true;
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    boolean pageCache = PreferenceMgr.getBoolean(webView.getContext(), "pageCache", false);
                    if (pageCache && DomainConfigKt.isDisablePageCache(myUrl)) {
                        pageCache = false;
                    }
                    pCache:
                    if (pageCache && StringUtil.isNotEmpty(myUrl) && myUrl.startsWith("http") && request.hasGesture()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            if (request.isRedirect()) {
                                break pCache;
                            }
                        }
                        //强制使用新窗口打开
                        goPageFromPageCache(webView, url, request);
                        return true;
                    }
                    boolean forceNewWindow = PreferenceMgr.getBoolean(webView.getContext(), "forceNewWindow", false);
                    if (forceNewWindow && DomainConfigKt.isDisableForceNewWindow(myUrl)) {
                        forceNewWindow = false;
                    }
                    forceNew:
                    if (forceNewWindow && StringUtil.isNotEmpty(myUrl) && myUrl.startsWith("http") && request.hasGesture()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            if (request.isRedirect()) {
                                break forceNew;
                            }
                        }
                        //强制使用新窗口打开
                        HorizontalWebView webView2 = MultiWindowManager.instance(reference.get()).addWebView(null, true, webView);
                        CookieManager.getInstance().setAcceptThirdPartyCookies(webView2, true);
                        Map<String, String> headers = request.getRequestHeaders();
                        if (headers == null) {
                            headers = new HashMap<>();
                        }
                        headers.put("Referer", myUrl);
                        webView2.loadUrl(url, headers);
                        EventBus.getDefault().post(new OnCreateWindowEvent(webView2));
                        return true;
                    }
//                    EventBus.getDefault().post(new OnOverrideUrlLoadingForHttp(url));
//                    if (Build.VERSION.SDK_INT < 26) {
//                        webView.loadUrl(url);
//                        return true;
//                    }
                    return false;
                } else {
                    EventBus.getDefault().post(new OnOverrideUrlLoadingForOther(url));
                    return true;
                }
            }

            private void goPageFromPageCache(WebView webView, String url, @Nullable WebResourceRequest request) {
                HorizontalWebView webView1 = (HorizontalWebView) webView;
                HorizontalWebView webView2 = MultiWindowManager.instance(reference.get()).addWebView(null, true, webView);
                webView2.setParentWebView(webView1);
                webView1.setForwardMock((w, u) -> goPageFromPageCache(w, u, null));
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView2, true);
                Map<String, String> headers = request == null ? null : request.getRequestHeaders();
                if (headers == null) {
                    headers = new HashMap<>();
                }
                headers.put("Referer", myUrl);
                webView2.loadUrl(url, headers);
                EventBus.getDefault().post(new OnCreateWindowEvent(webView2));
                MultiWindowManager.instance(reference.get()).checkTooManyParents(webView2);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (adBlockMarking) {
                    return true;
                }
                Timber.d("shouldOverrideUrlLoading: %s", url);
                if (url.startsWith("about:")) {
                    return false;
                }
                if (url.startsWith("http")) {
                    EventBus.getDefault().post(new OnOverrideUrlLoadingForHttp(url));
//                    if (Build.VERSION.SDK_INT < 26) {
//                        webView.loadUrl(url);
//                        return true;
//                    }
                    return false;
                } else {
                    EventBus.getDefault().post(new OnOverrideUrlLoadingForOther(url));
                    return true;
                }
            }
        };
        horizontalWebView.setWebViewClient(webViewClient);
    }

    private void loadAllJs(WebView webView, String url, boolean pageEnd) {
        try {
            JsPluginHelper.loadMyJs(reference.get(), js -> {
                webView.evaluateJavascript(js, null);
            }, url, WebViewHelper::getVideoUrls, pageEnd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //广告拦截
        String adBlockJs = AdBlockModel.getBlockJs(url);
        if (!TextUtils.isEmpty(adBlockJs)) {
            webView.evaluateJavascript(adBlockJs, null);
        }
    }

    public static String getVideoUrls() {
        List<String> urls = new ArrayList<>();
        List<DetectedMediaResult> results = DetectorManager.getInstance().getDetectedMediaResults(MediaType.VIDEO_MUSIC);
        if (results.size() > 0) {
            for (int i = 0; i < results.size(); i++) {
                urls.add(results.get(i).getUrl());
            }
        }
        return StringUtil.listToString(urls);
    }

    private void updateUA(WebView webView, String ua) {
        if (StringUtil.isNotEmpty(ua) && !ua.equals(webView.getSettings().getUserAgentString())) {
            ua = StringUtil.replaceLineBlank(ua);
            Timber.d("onPageStarted: getUseUa");
            webView.getSettings().setUserAgentString(ua);
            if (webView instanceof HorizontalWebView) {
                ((HorizontalWebView) webView).setUa(ua);
            }
        }
    }

    private void changeActionBarColor(String color) {
//        Timber.d("hiker, changeActionBarColor=%s", color);
        try {
            if (horizontalWebView != null && horizontalWebView.getUrl() != null && horizontalWebView.getUrl().contains("haikuoshijie.cn")) {
                color = "#ffffff";
            }
            if (color != null && color.length() > 0) {
                String c = StatusBarColorMap.get(reference.get(), horizontalWebView.getUrl());
                if (StringUtil.isNotEmpty(c)) {
                    color = c;
                }
                int statusBarColor = Color.parseColor(color);
                horizontalWebView.setStatusBarColor(statusBarColor);
                if (horizontalWebView.isUsed()) {
                    StatusBarCompatUtil.setStatusBarColor(reference.get(), statusBarColor);
                }
                Log.d(TAG, "changeActionBarColor: " + color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadUrl(String url) {
        adblockHolder.stopBeforeLoad();
        if (horizontalWebView.isUsed()) {
            EventBus.getDefault().post(new OnLoadUrlEvent(url));
        }
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        adblockHolder.stopBeforeLoad();
        if (horizontalWebView.isUsed()) {
            EventBus.getDefault().post(new OnLoadUrlEvent(url));
        }
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        adblockHolder.stopBeforeLoad();
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        adblockHolder.stopBeforeLoad();
    }

    @Override
    public void reload() {
        adblockHolder.stopBeforeLoad();
        String dom = StringUtil.getDom(horizontalWebView.getUrl());
        if (dom != null) {
            disallowLocationSet.remove(dom);
            disallowAppSet.remove(dom);
        }
    }

    @Override
    public void goBack() {
        if (adblockHolder.isLoading()) {
            adblockHolder.stopAbpLoading();
        }
    }

    @Override
    public void goForward() {
        if (adblockHolder.isLoading()) {
            adblockHolder.stopAbpLoading();
        }
    }

    @Override
    public void stopLoading() {
        adblockHolder.stopAbpLoading();
    }

    @Override
    public void destroy() {
        removeServiceWorkerClient(this);
    }

    public boolean isAdBlockMarking() {
        return adBlockMarking;
    }

    public void setAdBlockMarking(boolean adBlockMarking) {
        this.adBlockMarking = adBlockMarking;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebResourceRequest request) {
        if (horizontalWebView != null && horizontalWebView.isUsed() &&
                reference.get() != null && !reference.get().isFinishing() && !reference.get().isOnPause()) {
            return shouldInterceptRequest0(horizontalWebView, request);
        }
        return RESPONSE_IGNORE;
    }

    private WebResourceResponse shouldInterceptRequest0(WebView webView, WebResourceRequest request) {
        try {
            Timber.d("shouldInterceptRequest: request:%s", request.getUrl().toString());
            if (!(webView instanceof HorizontalWebView)) {
                return null;
            }
            HorizontalWebView view = (HorizontalWebView) webView;
            if (!view.isUsed()) {
                return null;
            }
            String url = request.getUrl().toString();
            requestHeaderMap.put(url, request.getRequestHeaders());
            if (ArticleListRuleEditActivity.hasBlockDom(lastDom) || DomainConfigKt.isDisableAdBlock(lastDom)) {
                DetectorManager.getInstance().addTask(new VideoTask(request.getRequestHeaders(), request.getMethod(), url, url));
                WebResourceResponse proxyResponse = BrowserProxy.INSTANCE.proxy(webView, myUrl, request);
                if (proxyResponse != null) {
                    return proxyResponse;
                }
                return WebViewCacheInterceptorInst.getInstance().interceptRequest(request);
            }
            long id = AdUrlBlocker.instance().shouldBlock(lastDom, url);
            if (id >= 0) {
                DetectedMediaResult mediaResult = new DetectedMediaResult(url);
                mediaResult.setMediaType(new Media(Media.BLOCK, id + ""));
                DetectorManager.getInstance().addMediaResult(mediaResult);
                return new WebResourceResponse(null, null, null);
            }
            if (reference.get() == null || reference.get().isFinishing()) {
                return null;
            }

            try {
                AdblockHolder.AbpShouldBlockResult abpBlockResult = adblockHolder.shouldAbpBlockRequest(myUrl, request);
                // if url should be blocked, we are not performing any further actions
                if (AdblockHolder.AbpShouldBlockResult.BLOCK_LOAD.equals(abpBlockResult)) {
                    if (reference.get() == null || reference.get().isFinishing()) {
                        return null;
                    }
                    DetectedMediaResult mediaResult = new DetectedMediaResult(url);
                    mediaResult.setMediaType(new Media(Media.BLOCK, "abp"));
                    DetectorManager.getInstance().addMediaResult(mediaResult);
                    return WebResponseResult.BLOCK_LOAD;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            DetectorManager.getInstance().addTask(new VideoTask(request.getRequestHeaders(), request.getMethod(), request.getUrl().toString(), request.getUrl().toString()));
            WebResourceResponse proxyResponse = BrowserProxy.INSTANCE.proxy(webView, myUrl, request);
            if (proxyResponse != null) {
                return proxyResponse;
            }
            return WebViewCacheInterceptorInst.getInstance().interceptRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class WebResponseResult {
        static final WebResourceResponse ALLOW_LOAD = null;
        static final WebResourceResponse BLOCK_LOAD =
                new WebResourceResponse(RESPONSE_MIME_TYPE, RESPONSE_CHARSET_NAME, null);
    }

    private volatile static List<ServiceWorkerInterceptor> serviceWorkerInterceptors = null;

    public static void addServiceWorkerClient(ServiceWorkerInterceptor interceptor) {
        try {
            if (serviceWorkerInterceptors == null) {
                synchronized (WebViewHelper.class) {
                    if (serviceWorkerInterceptors == null) {
                        serviceWorkerInterceptors = new ArrayList<>();
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BASIC_USAGE)) {
                            ServiceWorkerControllerCompat.getInstance().setServiceWorkerClient(new ServiceWorkerClientCompat() {
                                @Nullable
                                @Override
                                public WebResourceResponse shouldInterceptRequest(@NotNull WebResourceRequest request) {
                                    try {
                                        if (serviceWorkerInterceptors != null) {
                                            for (int i = serviceWorkerInterceptors.size() - 1; i >= 0; i--) {
                                                WebResourceResponse response = serviceWorkerInterceptors.get(i).shouldInterceptRequest(request);
                                                if (RESPONSE_IGNORE != response) {
                                                    return response;
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            });
                        }
                    }
                }
            }
            if (!serviceWorkerInterceptors.contains(interceptor)) {
                serviceWorkerInterceptors.add(interceptor);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void removeServiceWorkerClient(ServiceWorkerInterceptor interceptor) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && serviceWorkerInterceptors != null) {
                serviceWorkerInterceptors.remove(interceptor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}