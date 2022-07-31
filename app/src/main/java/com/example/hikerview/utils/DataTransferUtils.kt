package com.example.hikerview.utils

import com.alibaba.fastjson.JSON
import com.example.hikerview.ui.Application
import timber.log.Timber
import java.io.File

/**
 * 作者：By 15968
 * 日期：On 2021/11/5
 * 时间：At 22:46
 */
object DataTransferUtils {

    var temp: Any? = null
    var cache: Any? = null
    var cacheFile: String? = null

    inline fun <reified T> loadTemp(): T? {
        if (temp != null && temp is T) {
            val t = temp as T
            //用一次就清内存
            temp = null
            return t
        }
        val cache = readFromCache("temp")
        if (!cache.isNullOrEmpty()) {
            try {
                return JSON.parseObject(cache, T::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun putTemp(t: Any) {
        temp = t
        HeavyTaskUtil.executeNewTask {
            saveCache(JSON.toJSONString(t), "temp")
        }
    }

    inline fun <reified T> loadCache(file: String): T? {
        if (cache != null && cache is T && cacheFile == file) {
            val t = cache as T
            //用一次就清内存
            cache = null
            cacheFile = null
            return t
        }
        val cache = readFromCache(file)
        if (!cache.isNullOrEmpty()) {
            try {
                return JSON.parseObject(cache, T::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun loadCacheString(file: String): String? {
        return loadCache<String>(file)
    }

    fun putCacheString(t: String, file: String) {
        putCache(t, file)
    }

    fun putCache(t: Any, file: String) {
        cache = t
        cacheFile = file
        HeavyTaskUtil.executeNewTask {
            saveCache(JSON.toJSONString(t), file)
        }
    }

    /**
     * 避免被杀掉页面导致数据完全丢失，因此存一份到缓存
     */
    private fun saveCache(data: String, file: String) {
        val path =
            Application.application.cacheDir
                .toString() + File.separator + "rule-" + file + ".txt"
        Timber.d("setTempRuleData path: %s", path)
        FileUtil.stringToFile(data, path)
    }

    /**
     * 可能切换了深色模式，或者被后台杀掉了，可以从缓存取回数据
     */
    fun readFromCache(file: String?): String? {
        if (file.isNullOrEmpty()) {
            return null
        }
        HeavyTaskUtil.executeNewTask {
            clearCache()
        }
        val cache =
            Application.application.cacheDir.toString() + File.separator + "rule-" + file + ".txt"
        if (File(cache).exists()) {
            return FileUtil.fileToString(cache)
        }
        return null
    }

    private fun clearCache() {
        val cache = Application.application.cacheDir.toString()
        val file = File(cache)
        if (file.exists()) {
            var files = file.listFiles()
            if (!files.isNullOrEmpty()) {
                val now = System.currentTimeMillis()
                for (f in files) {
                    if (f.isFile && f.name.startsWith("rule-") && f.name.endsWith(".txt")) {
                        if (now - file.lastModified() > 3600 * 1000 * 24) {
                            //大于1天的删除
                            f.delete()
                        }
                    }
                }
            }
            //删除只保留一百个，避免一天打开无数次导致文件超级多
            files = file.listFiles()
            if (!files.isNullOrEmpty() && files.size > 100) {
                val ruleFiles = ArrayList<File>()
                for (f in files) {
                    if (f.isFile && f.name.startsWith("rule-") && f.name.endsWith(".txt")) {
                        ruleFiles.add(f)
                    }
                }
                if (ruleFiles.size > 100) {
                    ruleFiles.sortByDescending { it.lastModified() }
                    for (indexedValue in ruleFiles.withIndex()) {
                        if (indexedValue.index >= 100) {
                            indexedValue.value.delete()
                        }
                    }
                }
            }
        }
    }
}