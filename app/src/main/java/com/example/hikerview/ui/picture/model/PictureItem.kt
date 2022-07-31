package com.example.hikerview.ui.picture.model

import com.alibaba.fastjson.annotation.JSONCreator

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 17:32
 */
data class PictureItem(
    var name: String?,
    var url: String?
) {
    @JSONCreator
    constructor() : this(null, null) {

    }
}
