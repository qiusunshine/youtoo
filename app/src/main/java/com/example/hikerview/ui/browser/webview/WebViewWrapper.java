package com.example.hikerview.ui.browser.webview;

import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2021/2/2
 * 时间：At 20:21
 */

public interface WebViewWrapper {
    String getTitle();

    String getUrl();

    String getMyUrl();

    List<String> getUrls();

    void evaluateJavascript(String script);

    String getSystemUa();

    String getUserAgentString();

    void setUserAgentString(String userAgentString);

    void reload();

    boolean isOnPause();

    void loadUrl(String url);

    void updateLastDom(String dom);

    void setAppBarColor(String color, String isTheme);

    void addJavascriptInterface(Object obj, String interfaceName);

    Map<String, Map<String, String>> getRequestHeaderMap();

    String getCookie(String url);
}
