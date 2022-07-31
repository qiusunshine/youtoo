package com.example.hikerview.ui.video.util

import android.content.Context
import com.example.hikerview.ui.Application
import com.example.hikerview.utils.PreferenceMgr


/**
 * 作者：By 15968
 * 日期：On 2022/3/26
 * 时间：At 16:28
 */
object VideoCacheHolder {
    var useProxy: Boolean = false
    private var hasInit = false
    private var useProxying = false
    private var lastUrl: String = ""
    private val failedSet: MutableSet<String> by lazy {
        initFailedSet()
    }

    fun initConfig() {
        useProxy = PreferenceMgr.getBoolean(Application.getContext(), "useProxy", false)
    }

    fun isProxyError(): Boolean {
        return false
    }

    @Synchronized
    fun getProxyUrl(
        context: Context,
        uu: String,
        streamType: Int,
        headers: Map<String, String?>?,
        failedConsumer: () -> Int
    ): String {
        return uu
    }

    fun pause() {

    }

    fun resume() {

    }

    fun release() {
        lastUrl = ""
        useProxying = false
    }

    fun seek(position: Long, duration: Long) {

    }

    fun destroy(context: Context) {
        lastUrl = ""
        useProxying = false
    }

    private fun initFailedSet(): HashSet<String> {
        val set = HashSet<String>()
        return set
    }
}