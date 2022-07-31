package com.example.hikerview.ui.rules.model

import com.alibaba.fastjson.annotation.JSONCreator

/**
 * 作者：By 15968
 * 日期：On 2021/11/12
 * 时间：At 17:40
 */
data class BailanResponse(
    var return_code: Int?,
    var result_code: String?,
    var data: BailanResponseData?,
    var message: String?
){
    @JSONCreator
    constructor(): this(0, null,null, null){

    }
}
