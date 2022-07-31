package com.example.hikerview.ui.miniprogram.data

import com.alibaba.fastjson.annotation.JSONCreator

/**
 * 作者：By 15968
 * 日期：On 2022/7/3
 * 时间：At 10:34
 */
data class HistoryDTO(
    var ruleDTO: RuleDTO?,
    var pageTitle: String?,
    var showTitle: String?,
    var clickPos: Int,
    var clickText: String?,
    var time: Long
) {
    @JSONCreator
    constructor() : this(null, null, null, 0, null, 0) {

    }
}
