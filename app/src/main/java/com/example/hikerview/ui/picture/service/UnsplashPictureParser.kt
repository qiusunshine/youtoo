package com.example.hikerview.ui.picture.service

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.hikerview.R
import com.example.hikerview.constants.ArticleColTypeEnum
import com.example.hikerview.constants.UAEnum
import com.example.hikerview.service.parser.CommonParser
import com.example.hikerview.service.parser.HttpHelper
import com.example.hikerview.ui.home.model.ArticleList

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 21:55
 */
object UnsplashPictureParser : IPictureParser {

    override fun id(): Int {
        return R.id.source1
    }

    override suspend fun loadTypes(): ArrayList<ArticleList> {
        val headers: MutableMap<String?, String?> = HashMap()
        headers["User-Agent"] = UAEnum.MOBILE.code
        val data =
            HttpHelper.get("https://unsplash.dogedoge.com/wallpapers", headers)
        val result = ArrayList<ArticleList>()
        val header = ArticleList()
        header.type = ArticleColTypeEnum.TEXT_CENTER_1.code
        header.title = "““””<small>PS：此壁纸收集于网络，如有侵权请联系作者</small>"
        result.add(header)

        val arr = CommonParser.parseDomForList(data, "body&&a[href^=/wallpapers/]")
        for (item in arr) {
            if (item != null && item.contains("<img ")) {
                var url =
                    CommonParser.parseDomForUrl(item, "a&&href", "").replace("/wallpapers/", "");
                val id = if (url == "phone") "" else "/$url"
                url =
                    "https://unsplash.dogedoge.com/napi/landing_pages/wallpapers$id?page=fypage&per_page=20"
                val articleList = ArticleList()
                articleList.type = ArticleColTypeEnum.CARD_PIC_2.code
                articleList.desc = "0"
                articleList.title = CommonParser.parseDomForUrl(item, "div,-1&&Text", "")
                articleList.url = url
                articleList.pic = CommonParser.parseDomForUrl(item, "img&&src", url)
                if (articleList.title != null && articleList.title.contains("Android")) {
                    result.add(1, articleList)
                } else {
                    result.add(articleList)
                }
            }
        }
        return result
    }

    override suspend fun loadItems(page: Int, parent: ArticleList): ArrayList<ArticleList> {
        val headers: MutableMap<String?, String?> = HashMap()
        headers["User-Agent"] = UAEnum.MOBILE.code
        val code: String = parent.url
        val data =
            HttpHelper.get(
                code.replace("fypage", page.toString()),
                headers
            )
        val result = ArrayList<ArticleList>()
        val arr = JSON.parseObject(data).getJSONArray("photos")
        for (item in arr) {
            val it = item as JSONObject
            val articleList = ArticleList()
            articleList.type = ArticleColTypeEnum.PIC_3.code
            articleList.desc = ""
            articleList.title = it.getString("description")
            articleList.url = it.getJSONObject("urls")
                .getString("regular") + "@Referer=https://unsplash.dogedoge.com/wallpapers"
            articleList.pic = it.getJSONObject("urls")
                .getString("thumb") + "@Referer=https://unsplash.dogedoge.com/wallpapers"
            result.add(articleList)
        }
        return result
    }
}