package com.example.hikerview.ui.ext

import com.alibaba.fastjson.JSONObject
import com.example.hikerview.service.parser.HttpHelper
import com.example.hikerview.ui.Application
import com.example.hikerview.utils.FileUtil
import com.example.hikerview.utils.PreferenceMgr
import com.example.hikerview.utils.StringUtil
import com.example.hikerview.utils.UriUtils
import com.lzy.okgo.OkGo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.brotli.BrotliInterceptor
import java.io.File
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.TimeUnit


/**
 * Based on https://github.com/square/okhttp/blob/ef5d0c83f7bbd3a0c0534e7ca23cbc4ee7550f3b/okhttp-dnsoverhttps/src/test/java/okhttp3/dnsoverhttps/DohProviders.java
 */

const val PREF_DOH_CLOUDFLARE = 1
const val PREF_DOH_GOOGLE = 2
const val PREF_DOH_ADGUARD = 3
const val PREF_DOH_QUAD9 = 4
const val PREF_DOH_ALIDNS = 5
const val PREF_DOH_DNSPOD = 6
const val PREF_DOH_360 = 7
const val PREF_DOH_QUAD101 = 8

fun OkHttpClient.Builder.dohCloudflare() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("162.159.36.1"),
            InetAddress.getByName("162.159.46.1"),
            InetAddress.getByName("1.1.1.1"),
            InetAddress.getByName("1.0.0.1"),
            InetAddress.getByName("162.159.132.53"),
            InetAddress.getByName("2606:4700:4700::1111"),
            InetAddress.getByName("2606:4700:4700::1001"),
            InetAddress.getByName("2606:4700:4700::0064"),
            InetAddress.getByName("2606:4700:4700::6400"),
        )
        .build(),
)

fun OkHttpClient.Builder.dohGoogle() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://dns.google/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("8.8.4.4"),
            InetAddress.getByName("8.8.8.8"),
            InetAddress.getByName("2001:4860:4860::8888"),
            InetAddress.getByName("2001:4860:4860::8844"),
        )
        .build(),
)

// AdGuard "Default" DNS works too but for the sake of making sure no site is blacklisted,
// we use "Unfiltered"
fun OkHttpClient.Builder.dohAdGuard() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://dns-unfiltered.adguard.com/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("94.140.14.140"),
            InetAddress.getByName("94.140.14.141"),
            InetAddress.getByName("2a10:50c0::1:ff"),
            InetAddress.getByName("2a10:50c0::2:ff"),
        )
        .build(),
)

fun OkHttpClient.Builder.dohQuad9() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://dns.quad9.net/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("9.9.9.9"),
            InetAddress.getByName("149.112.112.112"),
            InetAddress.getByName("2620:fe::fe"),
            InetAddress.getByName("2620:fe::9"),
        )
        .build(),
)

fun OkHttpClient.Builder.dohAliDNS() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://dns.alidns.com/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("223.5.5.5"),
            InetAddress.getByName("223.6.6.6"),
            InetAddress.getByName("2400:3200::1"),
            InetAddress.getByName("2400:3200:baba::1"),
        )
        .build(),
)

fun OkHttpClient.Builder.dohDNSPod() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://doh.pub/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("1.12.12.12"),
            InetAddress.getByName("120.53.53.53"),
        )
        .build(),
)

fun OkHttpClient.Builder.doh360() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://doh.360.cn/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("101.226.4.6"),
            InetAddress.getByName("218.30.118.6"),
            InetAddress.getByName("123.125.81.6"),
            InetAddress.getByName("140.207.198.6"),
            InetAddress.getByName("180.163.249.75"),
            InetAddress.getByName("101.199.113.208"),
            InetAddress.getByName("36.99.170.86"),
        )
        .build(),
)

fun OkHttpClient.Builder.dohQuad101() = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url("https://dns.twnic.tw/dns-query".toHttpUrl())
        .bootstrapDnsHosts(
            InetAddress.getByName("101.101.101.101"),
            InetAddress.getByName("2001:de4::101"),
            InetAddress.getByName("2001:de4::102"),
        )
        .build(),
)

fun OkHttpClient.Builder.dohCustom(url: String, sourceUrl: String? = null) = dns(
    DnsOverHttps2.Builder().client(cacheClient())
        .url(url.checkToHttpUrl())
        .withHosts(url.toHostMap(sourceUrl))
        .build(),
)

fun String.checkToHttpUrl(): HttpUrl {
    if (!this.startsWith("http")) {
        return "http://127.0.0.1:8080".toHttpUrl()
    }
    return this.toHttpUrl()
}

fun String.toHostMap(sourceUrl: String? = null): Map<String, List<InetAddress>>? {
    if (!this.startsWith("http") && sourceUrl != null) {
        val map = HashMap<String, List<InetAddress>>()
        val uri = URL(sourceUrl)
        val host: String? = uri.host
        if (host != null) {
            map[host] = this.split(" ").map { InetAddress.getByName(it) }
            return map
        }
    }
    return null
}

fun cacheClient() =
    OkHttpClient.Builder()
        .cache(Cache(Application.getContext().cacheDir, 20 * 1024 * 1024))
        .addInterceptor(BrotliInterceptor)
        .addNetworkInterceptor(Interceptor { chain ->
            //强制缓存10分钟
            val response = chain.proceed(chain.request())
            val cacheControl: CacheControl = CacheControl.Builder()
                .maxAge(10, TimeUnit.MINUTES)
                .build()
            response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", cacheControl.toString())
                .build()
        })
        .build()

var dnsAuto = true
var autoDomain = HashMap<String, String>()

private fun clearAutoDomain() {
    autoDomain.clear()
    //固定的DNS解析
    autoDomain["pasteme.tyrantg.com"] = "https://doh.18bit.cn/dns-query"
}

const val autoDNSCheckUrl =
    "http://gh.haikuoshijie.cn/https://github.com/qiusunshine/hiker-rules/blob/master/dns0.txt"

fun initDnsAuto() {
    dnsAuto = PreferenceMgr.getString(Application.getContext(), "custom", "dns", "智能") == "智能"
    if (dnsAuto) {
        loadAutoDNSFromFile()
        loadAutoDNSFromRemote()
    }
}

private fun loadAutoDNSFromFile() {
    GlobalScope.launch(Dispatchers.IO) {
        val path =
            UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "dns0.txt"
        clearAutoDomain()
        if (File(path).exists()) {
            val t = FileUtil.fileToString(path)
            loadAutoDNS(t)
        }
    }
}

fun loadAutoDNSFromRemote() {
    GlobalScope.launch(Dispatchers.IO) {
        val path =
            UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "dns0.txt"
        val file = File(path)
        if (file.exists()) {
            val last = file.lastModified()
            if (System.currentTimeMillis() - last < 3600 * 1000 * 24) {
                //小于1天只更新一次
                return@launch
            }
        }
        val text = HttpHelper.get(autoDNSCheckUrl, HashMap())
        if (text.isEmpty()) {
            if (file.exists()) {
                file.delete()
            }
            clearAutoDomain()
        } else {
            FileUtil.stringToFile(text, path)
            clearAutoDomain()
            loadAutoDNS(text)
        }
    }
}

fun registerDNS(jsonObject: JSONObject) {
    for (key in jsonObject.keys) {
        val v = jsonObject[key]
        if (v == null || v !is String) {
            continue
        }
        autoDomain[key] = v
    }
}

private fun loadAutoDNS(text: String) {
    val t = text.split("\n")
    for (s in t) {
        if (s.isEmpty()) {
            continue
        }
        val s1 = s.split(" ")
        if (s1.size > 1) {
            autoDomain[s1[0]] = StringUtil.listToString(s1, 1, " ")
        } else {
            autoDomain[s] = ""
        }
    }
}

fun shouldUseAutoDNS(url: String?): Boolean {
    if (url == null || url.isEmpty()) {
        return false
    }
    if (!dnsAuto) {
        return false
    }
    try {
        val uri = URL(url)
        val host: String? = uri.host
        if (host.isNullOrEmpty()) {
            return false
        }
        if (host.contains("haikuoshijie")) {
            return true
        }
        if (autoDomain.containsKey(host)) {
            return true
        }
        for (key in autoDomain.keys) {
            if (host.endsWith(key)) {
                return true
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun buildAutoClient(url: String?): OkHttpClient? {
    return if (shouldUseAutoDNS(url)) {
        buildAutoDNSClient(url, null)
    } else {
        OkGo.getInstance().okHttpClient
    }
}

fun buildAutoDNSClient(url: String?, client: OkHttpClient?): OkHttpClient? {
    var client = client
    if (client == null) {
        client = OkGo.getInstance().okHttpClient
    }
    val doh = getDohUrl(url)
    return try {
        if (doh.isNullOrEmpty()) {
            client?.newBuilder()?.dohAliDNS()?.build() ?: client
        } else {
            client?.newBuilder()?.dohCustom(doh, url)?.build() ?: client
        }
    } catch (e: Exception) {
        e.printStackTrace()
        client
    }
}

fun buildAutoDNSClient(doh: String?, url: String?, client: OkHttpClient?): OkHttpClient? {
    var client = client
    if (client == null) {
        client = OkGo.getInstance().okHttpClient
    }
    return try {
        if (doh.isNullOrEmpty()) {
            client
        } else {
            client?.newBuilder()?.dohCustom(doh, url)?.build() ?: client
        }
    } catch (e: Exception) {
        e.printStackTrace()
        client
    }
}

private fun getDohUrl(url: String?): String? {
    if (url.isNullOrEmpty()) {
        return null
    }
    try {
        val uri = URL(url)
        val host: String? = uri.host
        if (host.isNullOrEmpty()) {
            return null
        }
        val sm = autoDomain[host]
        if (sm.isNullOrEmpty()) {
            for (key in autoDomain.keys) {
                if (host.endsWith(key)) {
                    return autoDomain[key]
                }
            }
        }
        return sm
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}