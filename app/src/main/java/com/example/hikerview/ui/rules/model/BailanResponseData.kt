package com.example.hikerview.ui.rules.model

import com.alibaba.fastjson.annotation.JSONCreator

/**
 * 作者：By 15968
 * 日期：On 2021/11/12
 * 时间：At 17:40
 */
data class BailanResponseData(
    var path: String?,
    var password: String?
) {

    @JSONCreator
    constructor() : this(null, null){

    }
}
