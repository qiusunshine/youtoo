package com.example.hikerview.ui.miniprogram.service

import android.content.Context
import com.alibaba.fastjson.JSON
import com.example.hikerview.ui.home.model.ArticleList
import com.example.hikerview.utils.FileUtil
import com.example.hikerview.utils.HeavyTaskUtil
import com.example.hikerview.utils.StringUtil
import com.example.hikerview.utils.UriUtils
import java.io.File

/**
 * 作者：By 15968
 * 日期：On 2022/10/1
 * 时间：At 0:11
 */

fun initAutoCacheMd5(pageTitle: String?, pageUrl: String?): String? {
    val autoCache =
        pageUrl != null && (pageUrl.contains("#autoCache#") || pageUrl.contains("#cacheOnly#"))
    return if (autoCache) {
        StringUtil.md5(pageTitle + "￥￥￥" + pageUrl)
    } else null
}

fun isCacheOnly(pageUrl: String?): Boolean {
    return pageUrl?.contains("#cacheOnly#") == true
}

fun hasCache(context: Context, cacheMd5: String?): Boolean {
    val file =
        File(
            UriUtils.getRootDir(context) +
                    File.separator + "cache" +
                    File.separator + "pages" +
                    File.separator + cacheMd5 + ".json"
        )
    return file.exists() && file.length() > 0
}

fun getCache(context: Context, cacheMd5: String?): PageCache {
    return if (cacheMd5.isNullOrEmpty()) {
        PageCache(emptyList())
    } else {
        val file =
            File(
                UriUtils.getRootDir(context) +
                        File.separator + "cache" +
                        File.separator + "pages" +
                        File.separator + cacheMd5 + ".json"
            )
        if (file.exists()) {
            try {
                val text = FileUtil.fileToString(file.absolutePath)
                if (!text.isNullOrEmpty()) {
                    val pageCache = PageCache()
                    if (text.startsWith("[") && text.endsWith("]")) {
                        pageCache.data = JSON.parseArray(text, ArticleList::class.java)
                    }
                    return JSON.parseObject(text, PageCache::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return PageCache(emptyList())
    }
}

fun saveCache(context: Context, cacheMd5: String?, data: List<ArticleList>) {
    saveCache(context, cacheMd5, data, null)
}

fun saveCache(context: Context, cacheMd5: String?, data: List<ArticleList>, code: String? = null) {
    if (cacheMd5.isNullOrEmpty()) {
        return
    }
    //避免后续data修改导致写入文件的数据变化
    val temp = ArrayList(data)
    HeavyTaskUtil.executeNewTask {
        val json =
            File(
                UriUtils.getRootDir(context) +
                        File.separator + "cache" +
                        File.separator + "pages" +
                        File.separator + cacheMd5 + ".json"
            )
        if (temp.isNullOrEmpty()) {
            json.delete()
        } else {
            val pageCache = PageCache(temp, code)
            FileUtil.bytesToFile(json.absolutePath, JSON.toJSONBytes(pageCache))
        }
    }
}

fun clearCache(context: Context) {
    HeavyTaskUtil.executeNewTask {
        //删除过期的文件
        val dir = File(
            UriUtils.getRootDir(context) +
                    File.separator + "cache" +
                    File.separator + "pages"
        )
        if (!dir.exists()) {
            return@executeNewTask
        }
        var files = dir.listFiles()
        if (files == null || files.isEmpty()) {
            return@executeNewTask
        }
        val now = System.currentTimeMillis()
        for (file in files) {
            if (now - file.lastModified() > 3600 * 1000 * 24 * 15) {
                //删除大于15天的
                if (!file.isDirectory) {
                    file.delete()
                }
            }
        }
        //删除超出200个的
        files = dir.listFiles()
        if (files == null || files.isEmpty() || files.size <= 200) {
            return@executeNewTask
        }
        val files2 = files.toMutableList()
        files2.sortByDescending { it.lastModified() }
        for (i in 200 until files2.size) {
            files2[i].delete()
        }
    }
}

fun clearAllCache(context: Context) {
    val dir = getCacheDir(context)
    if (!dir.exists()) {
        return
    }
    FileUtil.deleteDirs(dir.absolutePath)
}

fun getCacheDir(context: Context): File {
    return File(
        UriUtils.getRootDir(context) +
                File.separator + "cache" +
                File.separator + "pages"
    )
}

data class PageCache(
    var data: List<ArticleList>? = null,
    var code: String? = null
)