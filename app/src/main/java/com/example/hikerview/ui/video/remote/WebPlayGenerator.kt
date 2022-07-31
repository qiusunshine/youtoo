package com.example.hikerview.ui.video.remote

import com.example.hikerview.ui.webdlan.model.DlanUrlDTO

/**
 * 作者：By 15968
 * 日期：On 2021/10/21
 * 时间：At 20:33
 */
interface WebPlayGenerator {
    fun generate(playUrl: String) : DlanUrlDTO;
}