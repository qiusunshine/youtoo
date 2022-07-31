package com.example.hikerview.ui.rules.model

import com.alibaba.fastjson.annotation.JSONCreator

/**
 * 作者：By 15968
 * 日期：On 2021/11/12
 * 时间：At 17:40
 */
data class BailanBodyResponse(
    var return_code: Int?,
    var result_code: String?,
    var data: String?,
    var message: String?
){
    @JSONCreator
    constructor(): this(0, null,null, null){

    }
}
