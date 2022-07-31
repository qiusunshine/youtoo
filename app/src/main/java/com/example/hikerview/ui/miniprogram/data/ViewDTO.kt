package com.example.hikerview.ui.miniprogram.data

import com.alibaba.fastjson.annotation.JSONCreator

/**
 * 作者：By 15968
 * 日期：On 2022/7/3
 * 时间：At 10:34
 */
data class ViewDTO(
    var title: String?,
    var url: String?
) {
    @JSONCreator
    constructor() : this(null, null) {

    }
}
