package com.example.hikerview.ui.browser.service

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.hikerview.service.parser.JSEngine
import com.example.hikerview.ui.browser.model.Shortcut

/**
 * 作者：By 15968
 * 日期：On 2021/12/26
 * 时间：At 13:56
 */
object PoetryService {

    fun getPoetry(shortcut: Shortcut): Shortcut {
        val code = JSEngine.getInstance().fetch("https://v1.jinrishici.com/all.json", JSONObject())
        if (code != null && code.isNotEmpty() && code.startsWith("{")) {
            val obj = JSON.parseObject(code)
            shortcut.name =
                if (obj.containsKey("content") && obj.getString("content").isNotEmpty()) {
                    obj.getString("content")
                } else shortcut.name
            shortcut.url = if (obj.containsKey("author") && obj.getString("author").isNotEmpty()) {
                obj.getString("author")
            } else shortcut.url
        }
        return shortcut
    }
}