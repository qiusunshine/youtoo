package com.example.hikerview.utils

import android.content.Context
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.hikerview.service.parser.CommonParser
import com.example.hikerview.service.parser.JSEngine
import com.google.android.exoplayer2.util.FileTypes
import com.jeffmony.m3u8library.listener.IVideoTransformListener
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.regex.Pattern

/**
 * 作者：By 15968
 * 日期：On 2021/11/10
 * 时间：At 10:38
 */
object M3u8Utils {

    private const val forceM3u8 = "#isM3u8#"

    /**
     * 替换本地m3u8文件索引相对路径为绝对路径
     */
    fun getLocalContent(
        context: Context?, m3u8Path: String, www: String,
        listener: IVideoTransformListener?, sync: Boolean
    ): String? {
        val m3u8 = File(m3u8Path)
        if (!m3u8.exists()) {
            if (!sync) {
                ToastMgr.shortCenter(context, "找不到m3u8文件")
            }
            listener?.onTransformFailed(Exception("找不到m3u8文件"))
            return null
        }
        //转成绝对地址
        val fileString: MutableList<String> = ArrayList()
        try {
            var hasTs = false
            m3u8.readLines().forEach {
                if (it.startsWith("/")) {
                    //经过下载的文件一定是/开头，不会是无/的相对路径
                    if (!hasTs && it.endsWith(".m3u8")) {
                        return getLocalContent(
                            context,
                            it.replace("/", "$www/"),
                            www,
                            listener,
                            sync
                        )
                    }
                    fileString.add(it.replace("/", "$www/"))
                    hasTs = true
                } else {
                    fileString.add(it)
                }
            }
        } catch (e: IOException) {
            Timber.d(e, "文件异常%s", e.message)
        }
        return StringUtil.listToString(fileString, "\n")
    }

    private fun isNotM3u8(
        url: String,
        options: Any?,
        ruleKey: Any?
    ): Boolean {
        if (url.contains(forceM3u8)) {
            return false
        }
        if (url.contains(".flv") || url.contains(".m4a")
            || url.contains(".mp3") || url.contains("mine_type=video_mp4")
        ) {
            return true
        }
        if (url.contains("mp4") && !url.contains("m3u8")) {
            return true
        }
        return false
    }

    /**
     * 下载m3u8索引文件
     */
    fun downloadM3u8(
        url: String,
        fileName: String = "video.m3u8",
        options: Any?,
        ruleKey: Any?
    ): String {
        val filePath: String = JSEngine.getFilePath("hiker://files/cache/${fileName}")
        val content = proxyM3u8(url, fileName, options, ruleKey)
        if (content.startsWith(url) || !content.contains("#EXT")) {
            return url
        }
        FileUtil.stringToFile(content, filePath)
        return "file://$filePath"
    }


    /**
     * 下载m3u8索引文件
     */
    fun proxyM3u8(
        url: String,
        fileName: String = "video.m3u8",
        options: Any?,
        ruleKey: Any?
    ): String {
        if (isNotM3u8(url, options, ruleKey)) {
            return url
        }
        val ignoreCheck = url.contains(forceM3u8)
        //校验header
        val response: String? = JSEngine.getInstance().fetchWithHeadersInterceptor(
            url, options, ruleKey
        ) {
            if (ignoreCheck) {
                false
            } else {
                //拦截header
                val type = HttpUtil.inferFileTypeFromResponse(url, it)
                (type == HttpUtil.HTML || (type != HttpUtil.M3U8 && type != FileTypes.UNKNOWN))
            }
        }
        var jsonObject = JSONObject()
        if (response != null && response.isNotEmpty()) {
            jsonObject = JSON.parseObject(response)
        }
        var content = if (jsonObject.containsKey("body")) jsonObject["body"] as String? else ""
        if (content == null || content.isEmpty()) {
            return url
        }
        var realUrl = if (jsonObject.containsKey("url")) jsonObject["url"] as String else url
        if (!realUrl.startsWith("http")) {
            realUrl = CommonParser.joinUrl(url, realUrl)
        }
        if (!realUrl.startsWith("http")) {
            return url
        }
        if (content == null || !content!!.contains("#EXT")) {
            return url
        }
        content.let {
            content = fixPath(it, url) {c ->
                proxyM3u8(
                    c,
                    fileName,
                    options,
                    ruleKey
                )
            }
        }
        if (content == null || !content!!.contains("#EXT")) {
            return url
        }
        return content ?: url
    }

    /**
     * 替换本地m3u8文件索引相对路径为绝对路径
     */
    private fun convertPath(
        content: String,
        url: String,
        fileName: String,
        options: Any?,
        ruleKey: Any?
    ): String {
        return fixPath(content, url) {
            downloadM3u8(
                it,
                fileName,
                options,
                ruleKey
            )
        }
    }

    fun fixPath(
        content: String,
        url: String,
        interceptor: (newUrl: String) -> String
    ): String {
        val fileString: MutableList<String> = ArrayList()
        val lines = content.split("\n")
        var hasTs = false
        for (valueString in lines) {
            if (valueString.startsWith("#EXT-X-KEY:") || valueString.startsWith("#EXT-X-MAP:")) {
                //替换key地址
                val searchKeyUri = Pattern.compile("URI=\"(.*?)\"").matcher(valueString)
                if (!searchKeyUri.find()) {
                    fileString.add(valueString)
                    continue
                }
                val keyUri = searchKeyUri.group(1)
                if (keyUri != null) {
                    val keyUrl =
                        if (keyUri.startsWith("http://") || keyUri.startsWith("https://")) {
                            keyUri
                        } else {
                            URL(URL(url), keyUri.trim { it <= ' ' } ?: "").toString()
                        }
                    fileString.add(valueString.replace(keyUri, keyUrl))
                } else {
                    fileString.add(valueString)
                }
            } else if (valueString.startsWith("/") ||
                (!valueString.startsWith("#") && !valueString.startsWith(
                    "http"
                )
                        )
            ) {
                //替换相对路径为绝对路径
                val newUrl = URL(URL(url), valueString).toString()
                if (!hasTs && valueString.endsWith(".m3u8")) {
                    if (newUrl != url) {
                        //没有死循环的重定向
                        return interceptor(newUrl)
                    }
                }
                fileString.add(newUrl)
                hasTs = true
            } else {
                fileString.add(valueString)
            }
        }
        return StringUtil.listToString(fileString, "\n")
    }
}