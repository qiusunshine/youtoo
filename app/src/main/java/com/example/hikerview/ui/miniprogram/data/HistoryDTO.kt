package com.example.hikerview.ui.miniprogram.data

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONCreator
import com.alibaba.fastjson.annotation.JSONField
import com.example.hikerview.model.ViewCollectionExtraData

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
    var time: Long,
    var extraData: String?,
    var top: Boolean?,
    var pic: String?
) {
    @JSONCreator
    constructor() : this(null, null, null, 0, null, 0, null, false, null) {

    }

    @JSONField(deserialize = false, serialize = false)
    fun getExtraJson(): ViewCollectionExtraData {
        return if (extraData.isNullOrEmpty()) {
            ViewCollectionExtraData()
        } else {
            JSON.parseObject(extraData, ViewCollectionExtraData::class.java)
        }
    }
}
