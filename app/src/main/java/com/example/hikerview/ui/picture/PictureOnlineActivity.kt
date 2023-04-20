package com.example.hikerview.ui.picture

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.hikerview.R
import com.example.hikerview.ui.base.BaseActivity
import com.example.hikerview.ui.picture.service.AdeskPictureParser
import com.example.hikerview.ui.picture.service.PictureParserPool
import com.example.hikerview.utils.ToastMgr
import kotlinx.android.synthetic.main.activity_picture_list.*

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 17:05
 */
class PictureOnlineActivity : BaseActivity() {
    override fun initLayout(savedInstanceState: Bundle?): Int {
        return R.layout.activity_picture_list
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.picture_list_options, menu)
        return true
    }

    override fun initData(savedInstanceState: Bundle?) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_bg, PictureListFragment(AdeskPictureParser))
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
            R.id.source1, R.id.source2 ->
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_bg, PictureListFragment(PictureParserPool.getById(item.itemId)))
                    .commit()
            R.id.about ->
                ToastMgr.shortBottomCenter(context, "还没写")
        }
        return super.onOptionsItemSelected(item)
    }
}