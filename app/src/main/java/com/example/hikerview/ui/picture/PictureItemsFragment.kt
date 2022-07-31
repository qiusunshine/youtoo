package com.example.hikerview.ui.picture

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikerview.R
import com.example.hikerview.constants.ArticleColTypeEnum
import com.example.hikerview.event.web.UpdateBgEvent
import com.example.hikerview.ui.base.BaseFragment
import com.example.hikerview.ui.home.ArticleListAdapter
import com.example.hikerview.ui.home.model.ArticleList
import com.example.hikerview.ui.home.model.ArticleListRule
import com.example.hikerview.ui.picture.service.IPictureParser
import com.example.hikerview.ui.view.PopImageLoader
import com.example.hikerview.ui.view.PopImageLoaderNoView
import com.example.hikerview.ui.view.SmartRefreshLayout
import com.example.hikerview.ui.view.popup.MyXpopup
import com.example.hikerview.utils.ImgUtil
import com.example.hikerview.utils.ToastMgr
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.interfaces.XPopupImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 17:09
 */
class PictureItemsFragment(
    private val parser: IPictureParser,
    private val parent: ArticleList
) : BaseFragment() {

    init {
        lazy = false
    }

    private lateinit var adapter: ArticleListAdapter
    private lateinit var refresh_layout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private val list = ArrayList<ArticleList>()
    private val articleListRule = ArticleListRule()
    private var page = 1

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
                var imageView: ImageView? = null
                if (view is ImageView) {
                    imageView = view
                } else if (view != null) {
                    imageView = view.findViewById(R.id.item_reult_img)
                }
                val imageLoader: XPopupImageLoader = if (imageView == null) {
                    PopImageLoaderNoView(articleListRule.url)
                } else {
                    PopImageLoader(imageView, articleListRule.url)
                }

                val imageUrls: MutableList<Any> = java.util.ArrayList()
                for (i in adapter.list.indices) {
                    val articleList = adapter.list[i]
                    val pic = if (articleList.url == null || articleList.url.isEmpty()) {
                        articleList.pic
                    } else articleList.url
                    imageUrls.add(pic)
                }
                if (imageLoader is PopImageLoader) {
                    imageLoader.selectPos = position
                }
                val imageViewerPopupView = MyXpopup().Builder(context)
                    .asImageViewer(
                        imageView,
                        position,
                        imageUrls,
                        false,
                        true,
                        resources.getColor(R.color.gray_rice),
                        -1,
                        -1,
                        true,
                        Color.rgb(32, 36, 46),
                        { popupView: ImageViewerPopupView, position1: Int ->
                            // 作用是当Pager切换了图片，需要更新源View
                            try {
                                popupView.updateSrcView(null)
                                if (position1 < adapter.list.size && position1 >= 0) {
                                    recyclerView.scrollToPosition(position1)
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        },
                        imageLoader
                    )
                imageViewerPopupView.setOnShowTask {
                    imageViewerPopupView.saveView.text = "设为主题"
                    imageViewerPopupView.saveView.setOnClickListener {
                        useAsBg(imageViewerPopupView.pos)
                    }
                }
                imageViewerPopupView.show()
            }

            override fun onCodeClick(view: View?, position: Int, code: String?) {

            }

            override fun onLoadMore(view: View?, position: Int) {

            }

            override fun onClassClick(view: View?, url: String?, urltype: String?) {

            }

            override fun onLongClick(view: View?, position: Int) {
                val ops = arrayOf("设为主题", "保存图片")
                XPopup.Builder(context)
                    .asCenterList(null, ops) { _, text ->
                        when (text) {
                            "设为主题" -> {
                                useAsBg(position)
                            }
                            "保存图片" -> ImgUtil.savePic2Gallery(
                                context,
                                list[position].pic,
                                null,
                                object : ImgUtil.OnSaveListener {
                                    override fun success(paths: MutableList<String>?) {
                                        runOnUiThread {
                                            ToastMgr.shortBottomCenter(
                                                context,
                                                "已保存到相册"
                                            )
                                        }
                                    }

                                    override fun failed(msg: String) {
                                        runOnUiThread {
                                            ToastMgr.shortBottomCenter(
                                                context,
                                                msg
                                            )
                                        }
                                    }
                                })
                        }
                    }.show()
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
        refresh_layout.setEnableLoadMore(true)
        refresh_layout.setOnRefreshListener {
            page = 1
            initData()
        }
        refresh_layout.setOnLoadMoreListener {
            page++
            initData()
        }
    }

    private fun useAsBg(position: Int) {
        ImgUtil.savePic2Gallery(
            context,
            list[position].pic,
            null,
            object : ImgUtil.OnSaveListener {
                override fun success(paths: MutableList<String>?) {
                    runOnUiThread {
                        EventBus.getDefault()
                            .post(UpdateBgEvent(paths?.get(0)))
                        ToastMgr.shortBottomCenter(
                            context,
                            "已设为主题"
                        )
                    }
                }

                override fun failed(msg: String) {
                    runOnUiThread {
                        ToastMgr.shortBottomCenter(
                            context,
                            msg
                        )
                    }
                }
            })
    }

    override fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = parser.loadItems(page, parent)
                withContext(Dispatchers.Main) {
                    if (page == 1) {
                        refresh_layout.finishRefresh()
                        list.clear()
                        list.addAll(result)
                        adapter.notifyDataChanged()
                    } else {
                        refresh_layout.finishLoadMore()
                        val start = list.size
                        list.addAll(result)
                        adapter.notifyItemRangeInserted(start, result.size)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (page == 1) {
                        refresh_layout.finishRefresh()
                        list.clear()
                    } else {
                        page--
                        refresh_layout.finishLoadMore()
                    }
                    refresh_layout.finishRefresh()
                    ToastMgr.shortBottomCenter(context, "出错：" + e.message)
                }
            }
        }
    }

    fun runOnUiThread(runnable: Runnable) {
        lifecycleScope.launch(Dispatchers.Main) {
            runnable.run()
        }
    }
}