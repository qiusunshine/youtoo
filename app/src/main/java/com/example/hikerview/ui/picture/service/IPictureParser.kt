package com.example.hikerview.ui.picture.service

import com.example.hikerview.ui.home.model.ArticleList

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 21:52
 */
interface IPictureParser {

    fun id(): Int

    suspend fun loadTypes(): ArrayList<ArticleList>

    suspend fun loadItems(page: Int, parent: ArticleList): ArrayList<ArticleList>
}