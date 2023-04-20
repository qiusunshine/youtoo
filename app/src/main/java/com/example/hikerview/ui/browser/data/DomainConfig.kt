package com.example.hikerview.ui.browser.data

import com.alibaba.fastjson.annotation.JSONCreator
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.browser.service.DomainConfigService
import com.example.hikerview.utils.PreferenceMgr
import com.example.hikerview.utils.StringUtil

/**
 * 作者：By 15968
 * 日期：On 2022/7/3
 * 时间：At 10:34
 */
data class DomainConfig(
    var dom: String?,
    var disableAdBlock: Boolean?,
    var disableForceBlock: Boolean?,
    var disableXiuTan: Boolean?,
    var disableFloatVideo: Boolean?,
    var downloadStrongPrompt: Boolean?,
    //下载无需确认
    var d1: Boolean?,
    var disableOpenApp: Boolean?,
    var openAppMode: Int?,
    var disableGetLocation: Boolean?,
    var disablePCache: Boolean?,
    var disableForceNewWindow: Boolean?,
    var disableJsPlugin: Boolean?,
    var allowGeo: Boolean?,
) {
    @JSONCreator
    constructor() : this(
        null, false, false, false, false, false, false, false,
        0, false, false, false, false, false
    ) {

    }
}

fun isDisableAdBlock(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disableAdBlock == true
}

fun isDisableForceBlock(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disableForceBlock == true
}

fun isDisableXiuTan(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disableXiuTan == true
}

fun isDisableFloatVideo(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disableFloatVideo == true
}

fun isDownloadNoConfirm(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val d1 = PreferenceMgr.getBoolean(
        Application.getContext(),
        "download",
        "d1",
        false
    )
    if (d1) {
        return true
    }
    val dom = StringUtil.getDom(url)
    val domainConfig = DomainConfigService.getConfig(dom)
    return domainConfig.d1 == true
}

fun isDownloadStrongPrompt(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val dom = StringUtil.getDom(url)
    if (dom?.contains(".lanzou") == true && dom.endsWith(".com")) {
        return true
    }
    val whiteList = arrayOf(
        "123pan.com",
        "i95cloud.com",
        "ctfile.com",
        "haikuoshijie.cn",
        "github.com",
        "cloud.189.cn",
        "coolapk.com",
        "pan.bilnn.cn",
        "www.118pan.com",
        "catbox.moe"
    )
    for (s in whiteList) {
        if (dom?.contains(s) == true) {
            return true
        }
    }
    val domainConfig = DomainConfigService.getConfig(dom)
    return domainConfig.downloadStrongPrompt == true
}

fun isDisableOpenApp(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    if (domainConfig.openAppMode != 0) {
        return false
    }
    return domainConfig.disableOpenApp == true
}

/**
 * 0, 默认，1，总是提示，2，直接唤起无需提示
 */
fun getOpenAppMode(url: String?): Int {
    if (url.isNullOrEmpty()) {
        return 0
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.openAppMode ?: 0
}

fun isDisableGetLocation(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disableGetLocation == true
}

fun isAllowGetLocation(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    if (domainConfig.disableGetLocation == true) {
        return false
    }
    return domainConfig.allowGeo == true
}

fun isDisablePageCache(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disablePCache == true
}

fun isDisableForceNewWindow(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disableForceNewWindow == true
}

fun isDisableJsPlugin(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    val domainConfig = DomainConfigService.getConfig(StringUtil.getDom(url))
    return domainConfig.disableJsPlugin == true
}