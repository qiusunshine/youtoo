package com.example.hikerview.ui.picture.service

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.hikerview.R
import com.example.hikerview.constants.ArticleColTypeEnum
import com.example.hikerview.constants.UAEnum
import com.example.hikerview.service.parser.HttpHelper
import com.example.hikerview.ui.home.model.ArticleList

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 21:55
 */
object AdeskPictureParser : IPictureParser {

    override fun id(): Int {
        return R.id.source2
    }

    override suspend fun loadTypes(): ArrayList<ArticleList> {
        val headers: MutableMap<String?, String?> = HashMap()
        headers["User-Agent"] = UAEnum.MOBILE.code
        val data =
            HttpHelper.get("http://service.picasso.adesk.com/v1/lightwp/category", headers)
        val result = ArrayList<ArticleList>()
        val header = ArticleList()
        header.type = ArticleColTypeEnum.TEXT_CENTER_1.code
        header.title = "““””<small>PS：此壁纸收集于网络，如有侵权请联系作者</small>"
        result.add(header)

        val arr = JSON.parseObject(data).getJSONObject("res").getJSONArray("category")
        for (item in arr) {
            val it = item as JSONObject
            val articleList = ArticleList()
            articleList.type = ArticleColTypeEnum.CARD_PIC_2.code
            articleList.desc = "0"
            articleList.title = it.getString("rname")
            articleList.url = it.getString("id")
            articleList.pic = it.getString("cover")
            result.add(articleList)
        }
        return result
    }

    override suspend fun loadItems(page: Int, parent: ArticleList): ArrayList<ArticleList> {
        val skip = (page - 1) * 20
        val headers: MutableMap<String?, String?> = HashMap()
        headers["User-Agent"] = UAEnum.MOBILE.code
        val code = parent.url
        val data =
            HttpHelper.get(
                "http://service.picasso.adesk.com/v1/lightwp/category/$code/vertical?limit=20&skip=$skip&order=new",
                headers
            )
        val result = ArrayList<ArticleList>()
        val arr = JSON.parseObject(data).getJSONObject("res").getJSONArray("vertical")
        for (item in arr) {
            val it = item as JSONObject
            val articleList = ArticleList()
            articleList.type = ArticleColTypeEnum.PIC_3.code
            articleList.desc = ""
            articleList.title = it.getString("id")
            articleList.url = it.getString("img") + "#.jpg"
            articleList.pic = it.getString("img")
            result.add(articleList)
        }
        return result
    }
}