package com.example.hikerview.ui.miniprogram.extensions

import com.example.hikerview.ui.browser.model.UrlDetector
import com.example.hikerview.ui.thunder.ThunderManager

/**
 * 作者：By 15968
 * 日期：On 2022/7/3
 * 时间：At 11:34
 */
fun String.isImage(): Boolean {
    return UrlDetector.isImage(this)
}

fun String.isVideoMusic(): Boolean {
    return UrlDetector.isVideoOrMusic(this)
}

fun String.isMagnet(): Boolean {
    return ThunderManager.isMagnetOrTorrent(this)
}

fun String.isFTPOrEd2k(): Boolean {
    return ThunderManager.isFTPOrEd2k(this)
}