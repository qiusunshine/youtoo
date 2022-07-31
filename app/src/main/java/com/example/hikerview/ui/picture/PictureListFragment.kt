package com.example.hikerview.ui.picture

import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.example.hikerview.R
import com.example.hikerview.constants.ArticleColTypeEnum
import com.example.hikerview.ui.base.BaseFragment
import com.example.hikerview.ui.home.ArticleListAdapter
import com.example.hikerview.ui.home.model.ArticleList
import com.example.hikerview.ui.home.model.ArticleListRule
import com.example.hikerview.ui.picture.service.IPictureParser
import com.example.hikerview.ui.view.SmartRefreshLayout
import com.example.hikerview.utils.ToastMgr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 17:09
 */
class PictureListFragment(
    private val parser: IPictureParser
) : BaseFragment() {

    init {
        lazy = false
    }

    private lateinit var adapter: ArticleListAdapter
    private lateinit var refresh_layout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private val list = ArrayList<ArticleList>()
    private val articleListRule = ArticleListRule()

    override fun initLayout(): Int {
        return R.layout.fragment_picture_list
    }

    override fun initView() {
        articleListRule.url = ""
        articleListRule.firstHeader = ""
        recyclerView = findView(R.id.recycler_view) as RecyclerView
        adapter = ArticleListAdapter(activity, context, recyclerView, list, articleListRule)
        val listener = object : ArticleListAdapter.OnItemClickListener {
            override fun onUrlClick(view: View?, position: Int, url: String?) {

            }

            override fun onClick(view: View?, position: Int) {
                val item = list[position]
                if (item.url == null || item.url.isEmpty()) {
                    return
                }
                val intent = Intent(context, PictureItemsActivity::class.java)
                intent.putExtra("title", item.title)
                intent.putExtra("parser", parser::class.simpleName)
                intent.putExtra("parent", JSON.toJSONString(list[position]))
                startActivity(intent)
            }

            override fun onCodeClick(view: View?, position: Int, code: String?) {

            }

            override fun onLoadMore(view: View?, position: Int) {

            }

            override fun onClassClick(view: View?, url: String?, urltype: String?) {

            }

            override fun onLongClick(view: View?, position: Int) {

            }

        }
        adapter.setOnItemClickListener(listener)
        recyclerView.adapter = adapter
        val gridLayoutManager = GridLayoutManager(context, 60)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (recyclerView.adapter !== adapter) {
                    60
                } else ArticleColTypeEnum.getSpanCountByItemType(adapter.getItemViewType(position))
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(adapter.dividerItem)
        refresh_layout = findView(R.id.refresh_layout) as SmartRefreshLayout
        refresh_layout.setEnableRefresh(true)
        refresh_layout.setEnableLoadMore(false)
        refresh_layout.setOnRefreshListener {
            initData()
        }
    }

    override fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = parser.loadTypes()
                withContext(Dispatchers.Main) {
                    refresh_layout.finishRefresh()
                    list.clear()
                    list.addAll(result)
                    adapter.notifyDataChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    refresh_layout.finishRefresh()
                    ToastMgr.shortBottomCenter(context, "出错：" + e.message)
                }
            }
        }
    }
}