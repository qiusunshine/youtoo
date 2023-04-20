package com.example.hikerview.ui.miniprogram.data

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONCreator
import com.example.hikerview.ui.home.model.ArticleListPageRule
import java.util.*

/**
 * 作者：By 15968
 * 日期：On 2022/7/3
 * 时间：At 10:34
 */
data class RuleDTO(
    var title: String?,
    var url: String?,
    var interceptor: String?,
    var rule: String?,
    var ua: String?,
    var pages: String?,
    var params: String?,
    var col_type: String?,
    var nextRule: String?,
    var nextColType: String?,
    var preRule: String?
) {
    @JSONCreator
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null) {

    }

    fun pageList(): List<ArticleListPageRule?> {
        return if (pages == null || pages!!.isEmpty()) {
            ArrayList()
        } else JSON.parseArray(pages, ArticleListPageRule::class.java)
    }
}
