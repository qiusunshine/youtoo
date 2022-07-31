package com.example.hikerview.ui.browser.view

import com.annimon.stream.function.Consumer

/**
 * 作者：By 15968
 * 日期：On 2022/3/19
 * 时间：At 23:00
 */
interface IVideoWebView {
    fun postTask(task: Runnable)
    fun useFastPlay(use: Boolean)
    fun evaluateJS(js: String, resultCallback: Consumer<String>?)
    fun addJSInterface(obj: Any, interfaceName: String)
}