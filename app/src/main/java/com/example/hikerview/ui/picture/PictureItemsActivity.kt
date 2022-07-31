package com.example.hikerview.ui.picture

import android.os.Bundle
import android.view.MenuItem
import com.alibaba.fastjson.JSON
import com.example.hikerview.R
import com.example.hikerview.ui.base.BaseActivity
import com.example.hikerview.ui.home.model.ArticleList
import com.example.hikerview.ui.picture.service.IPictureParser
import com.example.hikerview.ui.picture.service.PictureParserPool
import kotlinx.android.synthetic.main.activity_picture_list.*

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 17:05
 */
class PictureItemsActivity : BaseActivity() {
    override fun initLayout(savedInstanceState: Bundle?): Int {
        return R.layout.activity_picture_list
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        title = intent.getStringExtra("title")
    }

    override fun initData(savedInstanceState: Bundle?) {
        val parser: IPictureParser = PictureParserPool.getByName(intent.getStringExtra("parser"))
        val parent: ArticleList =
            JSON.parseObject(intent.getStringExtra("parent"), ArticleList::class.java)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_bg, PictureItemsFragment(parser, parent))
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }
}