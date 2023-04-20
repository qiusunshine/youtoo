package com.example.hikerview.ui.browser.service

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.hikerview.service.parser.HttpHelper
import com.example.hikerview.service.parser.HttpHelper.RuleFetchDelegate
import com.example.hikerview.service.parser.JSEngine
import com.example.hikerview.ui.Application
import com.example.hikerview.utils.FileUtil
import com.example.hikerview.utils.StringUtil
import com.example.hikerview.utils.UriUtils
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.regex.Pattern

/**
 * 作者：By 15968
 * 日期：On 2023/4/19
 * 时间：At 10:19
 */
object BrowserProxy {

    var rules: List<BrowserProxyRule>? = null
    private val WHITE = arrayOf(
        ".js",
        ".css",
        ".png",
        ".jpg",
        ".jpeg",
        ".gif",
        ".ico",
        ".svg",
        ".woff",
        ".ttf",
        ".woff2",
        ".eot",
        ".otf",
        ".mp4",
        ".webm",
        ".ogg",
        ".mp3",
        ".wav",
        ".flac",
        ".aac",
        ".m4a",
        ".3gp",
        ".mov",
        ".wmv",
        ".avi",
        ".swf",
        ".flv",
        ".mkv",
        ".webm",
        ".mpeg",
        ".mpg",
        ".ogg",
        ".ogv"
    )

    fun getFilePath(): String {
        return UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "browser_proxy_rules.json"
    }

    fun getFileContent(): String {
        val p = getFilePath()
        return if (File(p).exists()) {
            FileUtil.fileToString(p)
        } else {
            ""
        }
    }

    fun clearProxyRules() {
        val p = getFilePath()
        val f = File(p)
        if (f.exists()) {
            f.delete()
        }
        rules = null
    }

    fun saveFile(s: String) {
        FileUtil.stringToFile(s, getFilePath())
        initProxyRules()
    }

    fun initProxyRules() {
        try {
            val p = getFilePath()
            if (File(p).exists()) {
                val t = FileUtil.fileToString(p)
                if (!t.isNullOrEmpty()) {
                    rules = JSON.parseArray(t, BrowserProxyRule::class.java)
                }
            } else {
//                val proxyRule = BrowserProxyRule()
//                proxyRule.match = ".*haikuoshijie.*"
//                val h = BrowserProxyHeader()
//                h.match = "Content-Security-Policy"
//                val req = HashMap<String, String>()
//                req["x-requested-with"] = "com.android.browser1"
//                proxyRule.requestHeaders = req
//                proxyRule.responseHeaders = listOf(h)
//                proxyRule.isForce = true
//                rules = listOf(proxyRule)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun proxy(
        webView: WebView,
        referer: String,
        request: WebResourceRequest
    ): WebResourceResponse? {
        try {
            if (rules.isNullOrEmpty()) {
                return null
            }
            val method = request.method?.uppercase(Locale.getDefault())
            if ("POST" == method || "PUT" == method) {
                return null
            }
            val url = request.url.toString()
            if (!url.lowercase(Locale.getDefault()).startsWith("http")) {
                return null
            }
            //白名单
            var inWhite = false
            for (r in WHITE) {
                if (url.contains(r)) {
                    inWhite = true
                    break
                }
            }
            for (r in rules!!) {
                //不是html，又不是强制匹配，那么忽略
                if (inWhite && !r.isForce) {
                    continue
                }
                if (Pattern.matches(r.match, url)) {
                    val h = request.requestHeaders
                    var cookie: String? = ""
                    try {
                        if (StringUtil.isNotEmpty(referer)) {
                            cookie = CookieManager.getInstance().getCookie(referer)
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    val hd = HashMap<String, String>()
                    if (h != null) {
                        for (entry in h.entries) {
                            hd[entry.key] = entry.value
                        }
                    }
                    if (StringUtil.isNotEmpty(cookie) && !hd.containsKey("Cookie")) {
                        hd["Cookie"] = cookie!!
                    }
                    if (StringUtil.isNotEmpty(referer) && !hd.containsKey("Referer")) {
                        hd["Referer"] = referer
                    }
                    if (!r.requestHeaders.isNullOrEmpty()) {
                        for (item in r.requestHeaders.entries) {
                            hd[item.key] = item.value
                        }
                    }
                    val options = HashMap<String, Any>()
                    options["headers"] = hd
                    options["withHeaders"] = true
                    options["inputStream"] = true
                    if (method == "HEAD") {
                        options["method"] = method
                    }
                    var u1 = if (r.replace.isNullOrEmpty()) url else r.replace
                    if (!u1.lowercase(Locale.getDefault()).startsWith("http")) {
                        //是JS
                        u1 = JSEngine.getInstance().evalJS(u1, url)
                    }
                    val json =
                        HttpHelper.fetch0(u1,
                            options,
                            null,
                            RuleFetchDelegate { path: String?, toHex: Boolean, inputStream: Boolean -> false }) as JSONObject
                    val data = json["body"] as InputStream?
                    val headers = json["headers"] as Map<String, List<String>>
                    val statusCode = json["statusCode"] as Int
                    val error = json["error"] as String? ?: "OK"
                    val contentType = headers["Content-Type"]?.get(0) ?: "text/html;charset=utf-8"
                    val types = contentType.split(";")
                    var encoding = "utf-8"
                    for (item in types) {
                        if (item.contains("charset")) {
                            encoding = item.substring(item.indexOf("=") + "=".length)
                        }
                    }
                    //替换header
                    val hh = HashMap<String, String>()
                    for (entry in headers.entries) {
                        hh[entry.key] = entry.value[0]
                        if (!r.responseHeaders.isNullOrEmpty()) {
                            for (item in r.responseHeaders) {
                                if (Pattern.matches(item.match, entry.key)) {
                                    if (item.replace.isNullOrEmpty()) {
                                        hh.remove(entry.key)
                                    } else {
                                        hh[entry.key] = item.replace
                                    }
                                    break
                                }
                            }
                        }
                    }
                    return WebResourceResponse(types[0], encoding, statusCode, error, hh, data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}