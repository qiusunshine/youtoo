package com.example.hikerview.service.http

import com.example.hikerview.ui.Application
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 作者：By 15968
 * 日期：On 2022/6/6
 * 时间：At 20:51
 */
class CookieJarUtil {
    companion object {
        fun getCookie(url: String): String {
            if (Application.getCookieJar() != null) {
                val httpUrl = url.toHttpUrl()
                val cookies = Application.getCookieJar().loadForRequest(httpUrl)
                if (cookies.isNotEmpty()) {
                    return cookieHeader(cookies)
                }
            }
            return ""
        }

        private fun cookieHeader(cookies: List<Cookie>): String = buildString {
            cookies.forEachIndexed { index, cookie ->
                if (index > 0) append("; ")
                append(cookie.name).append('=').append(cookie.value)
            }
        }
    }
}