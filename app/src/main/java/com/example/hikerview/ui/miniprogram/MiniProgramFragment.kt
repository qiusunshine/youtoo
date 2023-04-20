package com.example.hikerview.ui.miniprogram

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ValueCallback
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.annimon.stream.Stream
import com.annimon.stream.function.Consumer
import com.bumptech.glide.Glide
import com.example.hikerview.R
import com.example.hikerview.constants.ArticleColTypeEnum
import com.example.hikerview.event.OnBackEvent
import com.example.hikerview.event.home.LastClickShowEvent
import com.example.hikerview.event.home.OnRefreshPageEvent
import com.example.hikerview.event.home.OnRefreshWebViewEvent
import com.example.hikerview.event.home.OnRefreshX5HeightEvent
import com.example.hikerview.event.home.ShowFileChooserEvent
import com.example.hikerview.event.rule.CacheCode
import com.example.hikerview.event.rule.ClsItemsFindEvent
import com.example.hikerview.event.rule.ConfirmEvent
import com.example.hikerview.event.rule.ItemFindEvent
import com.example.hikerview.event.rule.ItemModifyEvent
import com.example.hikerview.event.web.DestroyEvent
import com.example.hikerview.service.parser.BaseParseCallback
import com.example.hikerview.service.parser.HttpParser
import com.example.hikerview.service.parser.JSEngine
import com.example.hikerview.service.parser.LazyRuleParser
import com.example.hikerview.service.parser.PageParser
import com.example.hikerview.service.parser.WebkitParser
import com.example.hikerview.service.parser.X5WebViewParser
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.base.BaseCallback
import com.example.hikerview.ui.base.BaseFragment
import com.example.hikerview.ui.browser.PictureListActivity
import com.example.hikerview.ui.browser.model.JSManager
import com.example.hikerview.ui.browser.model.UrlDetector
import com.example.hikerview.ui.browser.util.CollectionUtil
import com.example.hikerview.ui.detail.DetailUIHelper
import com.example.hikerview.ui.home.ArticleListAdapter
import com.example.hikerview.ui.home.enums.HomeActionEnum
import com.example.hikerview.ui.home.model.ArticleList
import com.example.hikerview.ui.home.model.ArticleListRule
import com.example.hikerview.ui.home.model.RouteBlocker
import com.example.hikerview.ui.home.model.TextConfig
import com.example.hikerview.ui.home.model.article.extra.InputExtra
import com.example.hikerview.ui.home.model.article.extra.RichTextExtra
import com.example.hikerview.ui.home.model.article.extra.X5Extra
import com.example.hikerview.ui.home.reader.ReadAloudHolder
import com.example.hikerview.ui.home.reader.ReadPageData
import com.example.hikerview.ui.home.view.ClickArea
import com.example.hikerview.ui.home.webview.ArticleWebViewHolder
import com.example.hikerview.ui.miniprogram.data.AutoPageData
import com.example.hikerview.ui.miniprogram.data.HistoryDTO
import com.example.hikerview.ui.miniprogram.data.RuleDTO
import com.example.hikerview.ui.miniprogram.extensions.isFTPOrEd2k
import com.example.hikerview.ui.miniprogram.extensions.isImage
import com.example.hikerview.ui.miniprogram.extensions.isMagnet
import com.example.hikerview.ui.miniprogram.extensions.isVideoMusic
import com.example.hikerview.ui.miniprogram.interfaces.ArticleListIsland
import com.example.hikerview.ui.miniprogram.service.ArticleListService
import com.example.hikerview.ui.miniprogram.service.HistoryMemoryService
import com.example.hikerview.ui.miniprogram.service.getCache
import com.example.hikerview.ui.miniprogram.service.hasCache
import com.example.hikerview.ui.miniprogram.service.initAutoCacheMd5
import com.example.hikerview.ui.miniprogram.service.isCacheOnly
import com.example.hikerview.ui.miniprogram.service.saveCache
import com.example.hikerview.ui.rules.HighLightEditActivity
import com.example.hikerview.ui.setting.file.FileDetailAdapter
import com.example.hikerview.ui.setting.file.FileDetailPopup
import com.example.hikerview.ui.setting.model.SettingConfig
import com.example.hikerview.ui.setting.text.TextConfigHelper
import com.example.hikerview.ui.thunder.ThunderManager
import com.example.hikerview.ui.video.PlayerChooser
import com.example.hikerview.ui.video.VideoChapter
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup
import com.example.hikerview.ui.view.PopImageLoader
import com.example.hikerview.ui.view.PopImageLoaderNoView
import com.example.hikerview.ui.view.SmartRefreshLayout
import com.example.hikerview.ui.view.popup.MyImageViewerPopupView
import com.example.hikerview.ui.view.popup.MyXpopup
import com.example.hikerview.ui.view.popup.XPopupImageLoader
import com.example.hikerview.utils.ClipboardUtil
import com.example.hikerview.utils.DataTransferUtils
import com.example.hikerview.utils.DebugUtil
import com.example.hikerview.utils.DisplayUtil
import com.example.hikerview.utils.FileUtil
import com.example.hikerview.utils.HeavyTaskUtil
import com.example.hikerview.utils.PreferenceMgr
import com.example.hikerview.utils.ShareUtil
import com.example.hikerview.utils.StringUtil
import com.example.hikerview.utils.StringUtil.SCHEME_FILE_SELECT
import com.example.hikerview.utils.ThreadTool
import com.example.hikerview.utils.ThreadTool.cancelTasks
import com.example.hikerview.utils.ThreadTool.runOnUI
import com.example.hikerview.utils.ToastMgr
import com.example.hikerview.utils.UriUtils
import com.example.hikerview.utils.WebUtil
import com.google.android.material.snackbar.Snackbar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.util.KeyboardUtils
import com.org.lqtk.fastscroller.RecyclerFastScroller
import com.org.lqtk.fastscroller.RecyclerFastScroller.onHandlePressedListener
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.jingbin.progress.WebProgress
import org.adblockplus.libadblockplus.android.Utils
import org.apache.commons.lang3.StringUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 17:09
 */
class MiniProgramFragment(
    private var ruleDTO: RuleDTO,
    private val pageTitle: String
) : BaseFragment(), BaseCallback<ArticleList> {

    constructor() : this(RuleDTO(), "") {

    }

    init {
        lazy = false
    }

    private lateinit var adapter: ArticleListAdapter
    private lateinit var smartRefreshLayout: SmartRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private var fastScroller: RecyclerFastScroller? = null
    private val list = ArrayList<ArticleList>()
    private val articleListRule = ArticleListRule()
    private var atomicPage = AtomicInteger(1)
    private var page: Int
        get() = atomicPage.get()
        set(value) = atomicPage.set(value)
    private val articleListService = ArticleListService()
    private var myUrl: String = ""
    private var webViewHolder: ArticleWebViewHolder? = null
    private var filePathCallback: ValueCallback<Array<Uri?>>? = null
    private var webViewContainer: RelativeLayout? = null
    private var progress_bar: WebProgress? = null
    private var bgViewHeight = 0
    private var detailPopup: FileDetailPopup? = null
    private var onRefreshJS: String? = null
    private var onCloseJS: String? = null
    private var hasShowLastClick = false
    private var noHistory = false
    private val chapterTypes = arrayOf(
        ArticleColTypeEnum.TEXT_1.code,
        ArticleColTypeEnum.TEXT_CENTER_1.code,
        ArticleColTypeEnum.TEXT_2.code,
        ArticleColTypeEnum.TEXT_3.code,
        ArticleColTypeEnum.TEXT_4.code,
        ArticleColTypeEnum.TEXT_5.code,
        ArticleColTypeEnum.FLEX_BUTTON.code
    )
    private var isReadTheme = false
    private var immersiveTheme = false
    private var gridLayoutManager: GridLayoutManager? = null
    private var loadListener: LoadListener? = null
    private var noRecordHistory = false
    private var lastVisibleItem = 0
    private var cannotRefresh = false
    private var highlightCallback: Consumer<String>? = null
    private var read_chapter: TextView? = null
    private var read_time: TextView? = null
    private var read_power: TextView? = null
    private var read_bottom_bg: View? = null
    private val readMap: MutableMap<Int, ReadPageData> = HashMap<Int, ReadPageData>()
    private var scrollPos = -1
    private var fileSelectJs: String? = null
    private var fileSelectPosition: Int = 0
    private var isLoading = false
    private var loadingPage = -1
    private var maxLoadedPage = 0
    private var memoryTask: MemoryPageTask? = null
    var scrollListener: RecyclerView.OnScrollListener? = null
    private var scrollFlag = -2 //-2不启用，-1启用，0，已经滑动过

    /**
     * 自动缓存页面
     */
    private var autoCacheMd5: String? = null
    private var cacheOnly = false

    /**
     * 缓存合并状态，0：不需要合并，1：等待合并，2：已合并过
     */
    private var cacheMergeState = 0

    /**
     * 缓存的代码
     */
    private var cacheCode: String? = null
    private var playLastConsumed = false

    /**
     * 自动阅读、语音朗读
     */
    private var autoRead = false

    /**
     * 自动阅读、语音朗读
     */
    private var readAloud = false
    private var readAloudHolder: ReadAloudHolder? = null
    private var readPosition = -1
    private var lastSelectedPosition = -1

    fun initAutoCache(pageTitle: String?, pageUrl: String?) {
        autoCacheMd5 = initAutoCacheMd5(pageTitle, pageUrl)
        if (!autoCacheMd5.isNullOrEmpty()) {
            cacheOnly = isCacheOnly(pageUrl)
        }
    }

    fun updateParams(params: String?) {
        articleListRule?.params = params
    }


    override fun initLayout(): Int {
        return R.layout.fragment_mini_program
    }

    override fun initView() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        articleListRule.firstHeader = ""
        articleListRule.title = ruleDTO.title
        if (ruleDTO.url?.contains("#noHistory#") == true) {
            noHistory = true
        }
        cannotRefresh = ruleDTO.url?.contains("#noRefresh#") == true
        articleListRule.url = ruleDTO.url
        articleListRule.find_rule = ruleDTO.rule
        articleListRule.ua = ruleDTO.ua
        articleListRule.pages = ruleDTO.pages
        articleListRule.params = ruleDTO.params
        articleListRule.col_type = ruleDTO.col_type
        articleListRule.preRule = ruleDTO.preRule

        if (StringUtil.isEmpty(articleListRule.url)) {
            noHistory = true
            noRecordHistory = true
        } else {
            if (articleListRule.url.contains("#noHistory#")) {
                noHistory = true
                articleListRule.url = StringUtils.replaceOnce(
                    articleListRule.url,
                    "#noHistory#",
                    ""
                )
            }
            if (articleListRule.url.contains("#noRecordHistory#")) {
                noRecordHistory = true
                articleListRule.url = StringUtils.replaceOnce(
                    articleListRule.url,
                    "#noRecordHistory#",
                    ""
                )
            }
            if (articleListRule.url.contains("#autoPage#")) {
                noRecordHistory = true
                noHistory = true
            }
        }
        recyclerView = findView(R.id.recycler_view) as RecyclerView
        recyclerView.post {
            bgViewHeight = DisplayUtil.pxToDp(
                context,
                findView<View>(R.id.bg).measuredHeight
            )
        }
        adapter = ArticleListAdapter(activity, context, recyclerView, list, articleListRule)
        webViewContainer = findView(R.id.webview_container)
        val listener = object : ArticleListAdapter.OnItemClickListener {
            override fun onUrlClick(view: View?, position: Int, url: String?) {
                dealUrlPos(view, position, url)
            }

            override fun onClick(view: View?, position: Int) {
                if (ArticleColTypeEnum.RICH_TEXT == ArticleColTypeEnum.getByCode(adapter.list[position].type)) {
                    if (isReadTheme) {
                        scrollPageByTouch()
                    }
                    return
                } else if (ArticleColTypeEnum.LONG_TEXT == ArticleColTypeEnum.getByCode(adapter.list[position].type)) {
                    if (isReadTheme) {
                        scrollPageByTouch()
                    }
                    return
                }
                clickItem(view, position)
            }

            override fun onCodeClick(view: View?, position: Int, code: String?) {

            }

            override fun onLoadMore(view: View?, position: Int) {

            }

            override fun onClassClick(view: View?, url: String?, urltype: String?) {

            }

            override fun onLongClick(view: View?, position: Int) {
                val baseExtra = adapter.list[position].baseExtra
                val ops = if (baseExtra != null && !baseExtra.longClick.isNullOrEmpty()) {
                    val arr = baseExtra.longClick.map { it.title }.toMutableList()
                    arr.add("外部打开")
                    arr.add("复制链接")
                    arr.add("调试数据")
                    arr.toTypedArray()
                } else {
                    arrayOf("外部打开", "复制链接", "调试数据")
                }
                XPopup.Builder(context)
                    .asCustom(
                        CustomCenterRecyclerViewPopup(requireContext())
                            .withTitle("请选择操作")
                            .with(
                                ops,
                                if (ops.size >= 4) 2 else 1,
                                object : CustomCenterRecyclerViewPopup.ClickListener {
                                    override fun click(text: String?, option: Int) {
                                        val url = adapter.list[position].url
                                        val urlRule = (url ?: "").split("@rule=")
                                        val lazyRule = urlRule[0].split("@lazyRule=")
                                        when (text) {
                                            "外部打开" -> {
                                                ShareUtil.findChooserToDeal(
                                                    context,
                                                    HttpParser.getFirstPageUrl(lazyRule[0])
                                                )
                                            }
                                            "复制链接" -> {
                                                ClipboardUtil.copyToClipboard(
                                                    context,
                                                    HttpParser.getFirstPageUrl(lazyRule[0])
                                                )
                                            }
                                            "调试数据" -> showPosDetail(position)
                                            else -> {
                                                //自定义的长按操作
                                                for (clickExtra in baseExtra.longClick) {
                                                    if (text == clickExtra.title) {
                                                        val input = clickExtra.title
                                                        val js = clickExtra.js
                                                        HeavyTaskUtil.executeNewTask {
                                                            val result: String =
                                                                JSEngine.getInstance().evalJS(
                                                                    JSEngine.getMyRule(
                                                                        articleListRule
                                                                    )
                                                                            + JSEngine.getInstance()
                                                                        .generateMY(
                                                                            "MY_URL",
                                                                            Utils.escapeJavaScriptString(
                                                                                myUrl
                                                                            )
                                                                        ) + js, input
                                                                )
                                                            if (StringUtil.isNotEmpty(result) && !"undefined".equals(
                                                                    result,
                                                                    ignoreCase = true
                                                                )
                                                                && activity != null && !requireActivity().isFinishing
                                                            ) {
                                                                requireActivity().runOnUiThread {
                                                                    dealUrlPos(
                                                                        view,
                                                                        position,
                                                                        result
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    override fun onLongClick(url: String?, position: Int) {

                                    }
                                })
                    ).show()
            }

        }
        adapter.setOnItemClickListener(listener)
        recyclerView.adapter = adapter
        gridLayoutManager = GridLayoutManager(context, 60)
        gridLayoutManager!!.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (recyclerView.adapter !== adapter) {
                    60
                } else ArticleColTypeEnum.getSpanCountByItemType(adapter.getItemViewType(position))
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(adapter.dividerItem)
        fastScroller = findView(R.id.frag_article_list_recycler_scroller)
        fastScroller!!.attachRecyclerView(recyclerView)
        fastScroller!!.touchTargetWidth = 10
        fastScroller!!.setMarginLeft(10)
        fastScroller!!.setMinItemCount(if (isReadTheme) 2000000 else 200)
        fastScroller!!.barColor = requireContext().resources.getColor(R.color.transparent)
        fastScroller!!.setDrawable(
            requireContext().resources.getDrawable(R.drawable.fastscroll_handle),
            requireContext().resources.getDrawable(R.drawable.fastscroll_handle)
        )
        fastScroller!!.setMaxScrollHandleHeight(
            DisplayUtil.dpToPx(
                context, 40
            )
        )
        fastScroller!!.pressedListener = onHandlePressedListener { pressed: Boolean ->
            val enable = !pressed
            smartRefreshLayout.setEnableOverScrollDrag(enable)
            smartRefreshLayout.setEnableOverScrollBounce(enable)
            if (!cannotRefresh) {
                smartRefreshLayout.setEnableRefresh(enable)
            }
        }
        smartRefreshLayout = findView(R.id.refresh_layout) as SmartRefreshLayout
        smartRefreshLayout.setEnableRefresh(true)
        if (cannotRefresh) {
            smartRefreshLayout.setEnableRefresh(false);
        } else {
            smartRefreshLayout.setOnRefreshListener {
                page = 1
                if (StringUtil.isNotEmpty(onRefreshJS)) {
                    HeavyTaskUtil.executeNewTask {
                        val js = onRefreshJS
                        onRefreshJS = null
                        JSEngine.getInstance()
                            .evalJS(JSEngine.getMyRule(articleListRule) + hookBackJs() + js, "")
                        if (activity != null && !requireActivity().isFinishing) {
                            requireActivity().runOnUiThread {
                                loadData()
                            }
                        }
                    }
                } else {
                    loadData()
                }
            }
        }
        if (ruleDTO.url?.contains("fypage") == true) {
            smartRefreshLayout.setEnableLoadMore(true)
            smartRefreshLayout.setOnLoadMoreListener {
                page++
                loadData()
            }
        }
        progress_bar = findView(R.id.progress_bar)
        progress_bar?.setColor(resources.getColor(R.color.progress_blue))
        if (!noRecordHistory) {
            //记忆页面
            HistoryMemoryService.memoryPage(
                ruleDTO,
                pageTitle,
                requireActivity().intent.getStringExtra("picUrl")
            )
        }

        recyclerView.setOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if ((newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.itemCount)
                        || newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 2 == adapter.itemCount && webViewHolder != null && StringUtil.isNotEmpty(
                            webViewHolder!!.extra
                        ) && webViewHolder!!.extra.contains("float")
                        || (lastVisibleItem == -1 && adapter.itemCount <= 10)
                    ) {
                        if (smartRefreshLayout.state != RefreshState.None) {
                            //触发的刷新，因为界面元素过少lastVisibleItem + 1 == adapter.getItemCount()为true
                            return
                        }
                        if (!articleListRule.url.contains("fypage")) {
                            if (articleListRule.url.contains("#autoPage#")) {
                                autoPage(true)
                            }
                            return
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    lastVisibleItem = gridLayoutManager!!.findLastVisibleItemPosition()
                    val position = lastVisibleItem
                    if (scrollPos != position && position >= 0) {
                        scrollPos = position
                        val pageData = readMap[position]
                        if (pageData != null) {
                            val pageNow = pageData.pageNow
                            if (pageNow > 0) {
                                updateReadChapter(pageNow - 1)
                            }
                            val idx = max(position - pageData.preSize, 0)
                            val lastLoadedPage = maxLoadedPage
                            if (isReadTheme && loadingPage < 0 && idx >= floor((pageData.size + 0.0) / 2) && lastLoadedPage < pageData.pageNow + 1) {
                                Timber.d("will preload: lastLoadedPage: " + lastLoadedPage + ", pageNow: " + pageData.pageNow)
                                preloadNow(maxLoadedPage)
                            }
                            val firstPos = gridLayoutManager?.findFirstVisibleItemPosition() ?: 0
                            var pageIndex = firstPos - pageData.preSize
                            if (pageIndex < 0) {
                                pageIndex = 0
                            }
                            memoryPageIndex(pageData.chapterPos, pageData.chapterText, pageIndex)
                        }
                    }
                }
            })

        if (scrollListener != null) {
            recyclerView.addOnScrollListener(scrollListener!!)
            fastScroller?.setScrollListener { scrollOffset ->
                if (scrollFlag == -1) {
                    scrollFlag = 0
                    //这个scrollOffset不靠谱，再次校验一下
                    if (!isAtTop()) {
                        scrollListener!!.onScrolled(recyclerView, 0, scrollOffset)
                    }
                }
            }
        }
    }

    private fun preloadCheckForReadAloud() {
        //不用findLastVisibleItemPosition是因为熄屏状态下不准，它不会更新
        val position = readPosition
        Timber.d(
            "readAloudNext: Position: %s, scrollPos: %s, maxLoadedPage: %s",
            position,
            scrollPos,
            maxLoadedPage
        )
        if (scrollPos != position && position >= 0) {
            scrollPos = position
            val pageData = readMap[position]
            if (pageData != null) {
                val pageNow = pageData.pageNow
                if (pageNow > 0) {
                    updateReadChapter(pageNow - 1)
                }
                val idx = max(position - pageData.preSize, 0)
                val lastLoadedPage = maxLoadedPage
                if (isReadTheme && loadingPage < 0 && idx >= floor((pageData.size + 0.0) / 2) && lastLoadedPage < pageData.pageNow + 1) {
                    Timber.d("readAloudNext will preload: lastLoadedPage: " + lastLoadedPage + ", pageNow: " + pageData.pageNow)
                    preloadNow(maxLoadedPage)
                }
                val firstPos = readPosition
                var pageIndex = firstPos - pageData.preSize
                if (pageIndex < 0) {
                    pageIndex = 0
                }
                memoryPageIndex(pageData.chapterPos, pageData.chapterText, pageIndex)
            }
        }
    }

    fun isAtTop(): Boolean {
        if (gridLayoutManager == null) {
            return true
        }
        try {
            val firstVisibleItemPosition = gridLayoutManager!!.findFirstVisibleItemPosition()
            val lastVisibleItemPosition =
                gridLayoutManager!!.findLastCompletelyVisibleItemPosition()
            if (lastVisibleItemPosition >= adapter.list.size - 1 && gridLayoutManager!!.findFirstCompletelyVisibleItemPosition() == 0) {
                //最后一个可见view是最后一个元素
                return true
            }
            val dp15 = DisplayUtil.dpToPx(context, 15)
            if (firstVisibleItemPosition == 0 && recyclerView.childCount > 0 && recyclerView.getChildAt(
                    0
                ).y == 0f
            ) {
                return true
            } else if (firstVisibleItemPosition == 1 && recyclerView.getChildAt(0).visibility != View.VISIBLE && recyclerView.getChildAt(
                    1
                ).y == 0f
            ) {
                //可能前面塞了一个隐藏view
                return true
            } else if (firstVisibleItemPosition == 1 && recyclerView.getChildAt(0).visibility == View.VISIBLE) {
                //前面塞了一个webview
                if (recyclerView.adapter != null && recyclerView.adapter!!.getItemViewType(0) == ArticleColTypeEnum.X5_WEB_VIEW.itemType && recyclerView.getChildAt(
                        0
                    ).height == 0 && recyclerView.getChildAt(1).y <= dp15
                ) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun preloadNow(maxLoadedPage: Int) {
        if (isLoading) {
            return
        }
        if (!articleListRule.url.contains("fypage")) {
            if (articleListRule.url.contains("#autoPage#")) {
                autoPage(false)
            }
            return
        }
        page++
        loadData()
    }

    private fun getPageIndex(): Int {
        try {
            val title = requireActivity().intent.getStringExtra("parentTitle")
            val cUrl = requireActivity().intent.getStringExtra("parentUrl")

            val history = HistoryMemoryService.getHistory(title, cUrl)
            if (history != null) {
                val extraData = history.getExtraJson()
                if (extraData.pageIndex < adapter.list.size) {
                    return extraData.pageIndex
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun memoryPageIndex(position: Int, clickText: String?, pageIndex: Int) {
        if (!isReadTheme) {
            return
        }
        memoryTask = MemoryPageTask(
            requireActivity().intent.getStringExtra("parentTitle"),
            requireActivity().intent.getStringExtra("parentUrl"),
            position, clickText, pageIndex
        )
    }

    private class MemoryPageTask(
        val parentTitle: String?,
        val parentUrl: String?,
        val position: Int,
        val clickText: String?,
        val pageIndex: Int
    ) : Runnable {
        override fun run() {
            if (!parentTitle.isNullOrEmpty() && !clickText.isNullOrEmpty()) {
                HistoryMemoryService.memoryClick(
                    parentUrl,
                    parentTitle,
                    position,
                    clickText,
                    pageIndex
                )
            }
        }
    }

    override fun initData() {
        articleListService.withUrlParseCallBack {
            myUrl = it
        }
        val useCache = !autoCacheMd5.isNullOrEmpty()
        if (useCache) {
            HeavyTaskUtil.executeNewTask {
                val pageCache = getCache(requireContext(), autoCacheMd5)
                //执行代码获取
                if (StringUtil.isNotEmpty(pageCache.code)) {
                    val result = JSEngine.getInstance().evalJS(
                        JSEngine.getMyRule(articleListRule) + pageCache.code,
                        ruleDTO.url
                    )
                    if (StringUtil.isNotEmpty(result) && result.startsWith("[") && result.endsWith("]")) {
                        try {
                            val articleLists: List<ArticleList> =
                                JSEngine.getInstance().convertArticleLists(result)
                            if (CollectionUtil.isNotEmpty(articleLists)) {
                                pageCache.data = articleLists
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                if (CollectionUtil.isNotEmpty(pageCache.data)) {
                    bindArrayToView(
                        HomeActionEnum.ARTICLE_LIST_NEW,
                        pageCache.data?.toMutableList(),
                        true
                    )
                }
            }
        }
        if (cacheOnly && hasCache(requireContext(), autoCacheMd5)) {
            return
        }
        if (!useCache || !hasCache(requireContext(), autoCacheMd5)) {
            smartRefreshLayout.autoRefresh()
        }
        loadData()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun updateCacheCode(event: CacheCode) {
        if (activity == null || requireActivity().isFinishing || StringUtil.isEmpty(autoCacheMd5)) {
            return
        }
        if (requireActivity().javaClass != MiniProgramActivity::class.java) {
            return
        }
        val miniProgramActivity = activity as MiniProgramActivity
        if (miniProgramActivity.isOnPause) {
            return
        }
        cacheCode = event.code
    }

    fun getWebViewHolder(): ArticleWebViewHolder? {
        return webViewHolder
    }

    fun scrollTopSmooth() {
        if (getUserVisibleHint()) {
            scrollSmoothAuto(0)
        }
    }

    fun scrollBottomSmooth() {
        if (getUserVisibleHint()) {
            scrollSmoothAuto(max(adapter.itemCount - 1, 0))
        }
    }

    private fun scrollSmoothAuto(pos: Int) {
        val firstVisibleItemPosition: Int = gridLayoutManager!!.findFirstVisibleItemPosition()
        if (Math.abs(pos - firstVisibleItemPosition) < 50) {
            recyclerView.smoothScrollToPosition(pos)
        } else {
            recyclerView.scrollToPosition(pos)
        }
    }

    private fun loadData() {
        if (isLoading) {
            page = if (maxLoadedPage == 0) {
                1
            } else {
                maxLoadedPage
            }
            return
        }
        isLoading = true
        loadingPage = page
        lifecycleScope.launch(Dispatchers.IO) {
            articleListService.params(context, page, true, articleListRule, notLoadPreRule())
                .process("", this@MiniProgramFragment)
        }
    }

    fun getLoadListener(): LoadListener? {
        return loadListener
    }

    fun setLoadListener(loadListener: LoadListener) {
        this.loadListener = loadListener
    }

    interface LoadListener {
        fun complete()
    }

    fun setImmersiveTheme(immersiveTheme: Boolean) {
        this.immersiveTheme = immersiveTheme
    }

    fun setReadTheme(yes: Boolean) {
        isReadTheme = yes
    }

    private fun clickItem(view: View?, position: Int) {
        var url = adapter.list[position].url
        if (ArticleColTypeEnum.input.code == adapter.list[position].type) {
            if (view != null && view.tag is EditText) {
                val edit = view.tag as EditText
                val key = if (edit.text == null) "" else edit.text.toString()
                KeyboardUtils.hideSoftInput(edit)
                val inputExtra = adapter.list[position].requireExtra(
                    InputExtra::class.java
                )
                if (inputExtra.isHighlight) {
                    SettingConfig.highlightRule = key
                    val intent = Intent(
                        context,
                        HighLightEditActivity::class.java
                    )
                    intent.putExtra("title", "高亮")
                    if (activity is ArticleListIsland) {
                        highlightCallback = Consumer { s: String? ->
                            val edit0 = view.tag as EditText
                            if (!StringUtils.equals(s, edit0.text)) {
                                edit0.setText(s)
                            }
                        }
                    }
                    startActivityForResult(intent, 9090)
                    return
                }
                if (url.startsWith("js:")) {
                    url = StringUtils.replaceOnce(url, "js:", "")
                }
                val finalUrl = url
                HeavyTaskUtil.executeNewTask {
                    val result = JSEngine.getInstance().evalJS(
                        JSEngine.getMyRule(articleListRule)
                                + JSEngine.getInstance().generateMY(
                            "MY_URL",
                            Utils.escapeJavaScriptString(
                                myUrl
                            )
                        )
                                + finalUrl, key
                    )
                    if (StringUtil.isNotEmpty(result) && !"undefined".equals(
                            result,
                            ignoreCase = true
                        )
                        && activity != null && !requireActivity().isFinishing
                    ) {
                        requireActivity().runOnUiThread {
                            dealUrlPos(
                                view,
                                position,
                                result
                            )
                        }
                    }
                }
            }
        } else {
            dealUrlPos(view, position, url)
        }
    }

    private fun dealUrlPos(view: View?, position: Int, url: String?) {
        dealUrlPos(view, position, url, true, "")
    }

    private fun dealUrlPos(
        view: View?,
        position: Int,
        url: String?,
        firstUse: Boolean,
        parentCodeHeader: String
    ) {
        if (url == null) {
            return
        }
        var url: String = url
        if (url.contains("#noHistory#")) {
            noHistory = true
            url = StringUtils.replaceOnce(url, "#noHistory#", "")
        }
        if (PageParser.isPageUrl(url)) {
            toNextPage(position, url)
            return
        } else if (url.startsWith(SCHEME_FILE_SELECT)) {
            fileSelectJs = StringUtils.replaceOnceIgnoreCase(url, SCHEME_FILE_SELECT, "")
            fileSelectPosition = position
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            startActivityForResult(Intent.createChooser(i, "选择文件"), 9093)
            return
        } else if (DetailUIHelper.dealUrlSimply(
                requireActivity(),
                articleListRule,
                adapter.list[position],
                url
            ) {
                dealUrlPos(view, position, it)
            }
        ) {
            return
        }
        if (RouteBlocker.isRoute(activity, url)) {
            return
        }
        val urlRule = url.split("@rule=")
        val lazyRule = urlRule[0].split("@lazyRule=").toTypedArray()
        val urlWithUa = HttpParser.getUrlAppendUA(articleListRule.url, articleListRule.ua)
        var codeAndHeader = DetailUIHelper.getCodeAndHeader(urlWithUa, lazyRule)
        if (!firstUse && StringUtil.isEmpty(codeAndHeader)) {
            codeAndHeader = parentCodeHeader
        }
        if (lazyRule.size > 1) {
            LazyRuleParser.parse(
                lifecycle.coroutineScope,
                requireActivity(),
                ruleDTO,
                lazyRule,
                codeAndHeader,
                myUrl,
                object : BaseParseCallback<String?> {
                    override fun start() {
                        if (!lazyRule[0].contains("#noLoading#")) {
                            loading("动态解析规则中，请稍候", true)
                        }
                    }

                    override fun success(data: String?) {
                        dealUrlPos(view, position, data, false, codeAndHeader)
                        loading("动态解析规则中，请稍候", false)
                    }

                    override fun error(msg: String) {
                        loading("动态解析规则中，请稍候", false)
                    }
                })
            return
        }
        val clickText = getItemTitle(position)
        if (urlRule.size > 1) {
            val ruleDTO = RuleDTO()
            ruleDTO.title = this.ruleDTO.title
            ruleDTO.url = if (urlRule[0].contains(";")) urlRule[0] else urlRule[0] + codeAndHeader
            ruleDTO.rule = StringUtil.listToString(urlRule, 1, "@rule=")
            ruleDTO.ua = this.ruleDTO.ua
            ruleDTO.pages = this.ruleDTO.pages
            ruleDTO.preRule = this.ruleDTO.preRule
            if (StringUtil.isNotEmpty(adapter.list[position].extra)) {
                ruleDTO.params = adapter.list[position].extra
            }
            val autoPage1 = ruleDTO.url?.contains("#autoPage#") == true
            if (autoPage1) {
                val nextData: List<ArticleList> =
                    java.util.ArrayList(adapter.list.subList(position, adapter.list.size))
                val autoPageData = AutoPageData(
                    nextData, this.ruleDTO.url,
                    pageTitle, position, noRecordHistory
                )
                DataTransferUtils.putTemp(autoPageData)
            } else {
                DataTransferUtils.clearTemp()
            }
            memoryClick(this.ruleDTO, pageTitle, position, urlRule[0])
            MiniProgramRouter.startMiniProgram(
                requireContext(),
                ruleDTO.url!!,
                clickText,
                ruleDTO,
                false,
                pageTitle,
                this.ruleDTO.url,
                playLast = false,
                fromHome = false,
                picUrl = adapter.list[position].pic
            )
            return
        } else if (X5WebViewParser.canParse(url)) {
            val start = X5WebViewParser.parse0(
                activity, url, adapter.list[position].extra
            ) { u: String? ->
                loading(false)
                dealUrlPos(view, position, u, false, codeAndHeader)
            }
            if (start) {
                loading("动态解析规则中，请稍候", true)
            }
            return
        } else if (WebkitParser.canParse(url)) {
            val start = WebkitParser.parse0(
                activity, url, adapter.list[position].extra
            ) { u: String? ->
                loading(false)
                dealUrlPos(view, position, u, false, codeAndHeader)
            }
            if (start) {
                loading("动态解析规则中，请稍候", true)
            }
            return
        }

        if (url.isMagnet()) {
            memoryClick(ruleDTO, pageTitle, position, url)
            ThunderManager.startDownloadMagnet(context, url)
            return
        }
        if (url.isFTPOrEd2k()) {
            memoryClick(ruleDTO, pageTitle, position, url)
            ThunderManager.startParseFTPOrEd2k(requireContext(), url)
            return
        }
        if (url.startsWith("x5:")) {
            MiniProgramRouter.startWebPage(
                requireContext(),
                url.replace("x5://", ""),
                adapter.list[position].title
            )
            return
        }
        if (url.startsWith("web://")) {
            WebUtil.goWeb(context, url.replace("web://", ""))
            return
        }
        if (url.startsWith("pics://")) {
            var intentTitle = DetailUIHelper.getActivityTitle(activity)
            if (StringUtil.isNotEmpty(intentTitle)) {
                intentTitle = "$intentTitle-"
            }
            memoryClick(ruleDTO, pageTitle, position, url)
            val realRule = StringUtils.replaceOnceIgnoreCase(url, "pics://", "")
            val urls = realRule.split("&&").toTypedArray()
            val imageUrls = java.util.ArrayList(Arrays.asList(*urls))
            val intent = Intent(
                context,
                PictureListActivity::class.java
            )
            intent.putStringArrayListExtra("pics", imageUrls)
            intent.putExtra("rule", JSON.toJSONString(articleListRule))
            PlayerChooser.checkSize(intent)
            PlayerChooser.checkPicsSize(intent)
            intent.putExtra("url", articleListRule.url)
            intent.putExtra("title", DetailUIHelper.getTitleText(adapter.list[position].title))
            val chapters1: List<VideoChapter> =
                getChapters(url, position, intentTitle, codeAndHeader)
            val current = PlayerChooser.putChapters(chapters1)
            intent.putExtra("chapters", current)
            if (activity is MiniProgramActivity) {
                intent.putExtra("CUrl", articleListRule.url)
                intent.putExtra("MTitle", DetailUIHelper.getActivityTitle(activity))
            }
            intent.putExtra("fromLastPage", true)
            var nowPage = 0
            for (i in chapters1.indices) {
                if (chapters1[i].isUse) {
                    nowPage = i
                }
            }
            intent.putExtra("nowPage", nowPage)
            startActivity(intent)
            return
        }
        val nextRule = this.ruleDTO.nextRule
        if (firstUse && !nextRule.isNullOrEmpty() && !url.startsWith("video://")) {
            val ruleDTO = RuleDTO()
            ruleDTO.title = this.ruleDTO.title
            ruleDTO.url = url + codeAndHeader
            ruleDTO.rule = nextRule
            ruleDTO.col_type = this.ruleDTO.nextColType
            ruleDTO.ua = this.ruleDTO.ua
            ruleDTO.pages = this.ruleDTO.pages
            ruleDTO.preRule = this.ruleDTO.preRule
            if (StringUtil.isNotEmpty(adapter.list[position].extra)) {
                ruleDTO.params = adapter.list[position].extra
            }
            val autoPage1 = ruleDTO.url?.contains("#autoPage#") == true
            if (autoPage1) {
                val nextData: List<ArticleList> =
                    java.util.ArrayList(adapter.list.subList(position, adapter.list.size))
                val autoPageData = AutoPageData(
                    nextData, this.ruleDTO.url,
                    pageTitle, position, noRecordHistory
                )
                DataTransferUtils.putTemp(autoPageData)
            } else {
                DataTransferUtils.clearTemp()
            }
            memoryClick(this.ruleDTO, pageTitle, position, url)
            MiniProgramRouter.startMiniProgram(
                requireContext(),
                ruleDTO.url!!,
                clickText,
                ruleDTO,
                false,
                null,
                null,
                playLast = false,
                fromHome = false,
                picUrl = adapter.list[position].pic
            )
            return
        }
        if (url.isVideoMusic()) {
            memoryClick(ruleDTO, pageTitle, position, url)
            var intentTitle = DetailUIHelper.getActivityTitle(activity)
            if (StringUtil.isNotEmpty(intentTitle)) {
                intentTitle = "$intentTitle-"
            }
            val chapters = getChapters(url, position, intentTitle, codeAndHeader)
            val extraDataBundle = Bundle()
//            val viewCollectionExtraData: String = getViewCollectionExtraData()
//            if (!StringUtil.isEmpty(viewCollectionExtraData)) {
//                extraDataBundle.putString("viewCollectionExtraData", viewCollectionExtraData)
//            }
//            if (activity != null && requireActivity().title != null) {
//                extraDataBundle.putString("film", requireActivity().title.toString())
//            }
            extraDataBundle.putString("rule", JSON.toJSONString(articleListRule))
            if (chapters.size < 1) {
                PlayerChooser.startPlayer(context, clickText, url, extraDataBundle)
            } else {
                PlayerChooser.startPlayer(
                    context,
                    chapters,
                    this.ruleDTO.url,
                    pageTitle,
                    extraDataBundle
                )
            }
            return
        }
        if (url.isImage()) {
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
            if (url != adapter.list[position].url) {
                MyXpopup().Builder(context)
                    .asImageViewer(imageView, url, imageLoader)
                    .show()
                return
            }

            val imageUrls: MutableList<Any> = java.util.ArrayList()
            val currPos = 0
            val end = min(adapter.list.size, position + 20)
            for (i in position until end) {
                val articleList = adapter.list[i]
                val pic: String? = if (articleList.url == null || articleList.url.isEmpty()) {
                    articleList.pic
                } else articleList.url
                pic?.let {
                    if (UrlDetector.isImage(it)) {
                        imageUrls.add(it)
                    }
                }
            }
            if (imageLoader is PopImageLoader) {
                imageLoader.selectPos = currPos
            }
            val imageViewerPopupView = MyXpopup().Builder(context)
                .asImageViewer(
                    imageView,
                    currPos,
                    imageUrls,
                    false,
                    true,
                    resources.getColor(R.color.gray_rice),
                    -1,
                    -1,
                    true,
                    Color.rgb(32, 36, 46),
                    { popupView: MyImageViewerPopupView, position1: Int ->
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
            imageViewerPopupView.show()
            return
        }
        if (url.startsWith("hiker://")) {
            return
        }
        if (!url.startsWith("http")) {
            if (StringUtil.isScheme(url)) {
                //第三方软件
                ShareUtil.findChooserToDeal(context, url)
                return
            } else if (StringUtil.isCannotHandleScheme(url)) {
                //常用第三方软件
                ShareUtil.findChooserToDeal(context, url)
            } else {
                ToastMgr.shortBottomCenter(context, "未知链接：$url")
            }
            return
        }
        MiniProgramRouter.startWebPage(requireContext(), url, adapter.list[position].title)
    }


    private fun isPlayList(articleList: ArticleList?): Boolean {
        if (articleList == null) {
            return false
        }
        val baseExtra = articleList.baseExtra
        return baseExtra != null && StringUtil.isNotEmpty(baseExtra.cls) && baseExtra.cls.contains("playlist")
    }

    private fun getPlayListCls(articleList: ArticleList?): String? {
        if (articleList == null) {
            return ""
        }
        val baseExtra = articleList.baseExtra
        return if (baseExtra != null && StringUtil.isNotEmpty(baseExtra.cls)) baseExtra.cls else ""
    }

    private fun isSamePlayList(cls: String?, articleList: ArticleList?): Boolean {
        if (articleList == null) {
            return false
        }
        val baseExtra = articleList.baseExtra
        return baseExtra != null && StringUtils.equals(cls, baseExtra.cls)
    }

    private fun getChapters(
        url: String,
        position: Int,
        title: String,
        codeAndHeader: String?
    ): MutableList<VideoChapter> {
        val chapters: MutableList<VideoChapter> = java.util.ArrayList()
        val type = adapter.list[position].type
        val hasPlayListTag = isPlayList(adapter.list[position])
        val cls = getPlayListCls(adapter.list[position])

        var start: Int = position
        if (!hasPlayListTag) {
            //没用playlist标识，那么只要相同类型的
            for (i in position - 1 downTo 0) {
                start = if (StringUtils.equals(type, adapter.list[i].type)) {
                    i
                } else {
                    break
                }
            }
        } else {
            start = 0
        }
        for (i in start until position) {
            if ("header" == adapter.list[i].type) {
                continue
            }
            if (StringUtil.isEmpty(adapter.list[i].url) ||
                adapter.list[i].url.contains("ignoreVideo") ||
                adapter.list[i].url.contains("ignoreMusic") ||
                adapter.list[i].url.contains("@rule=")
            ) {
                continue
            }
            if (hasPlayListTag && !isSamePlayList(cls!!, adapter.list[i])) {
                //使用了playlist标识，而当前按钮标识不一致
                continue
            }
            val videoChapter = VideoChapter()
            videoChapter.memoryTitle = adapter.list[i].title
            videoChapter.title = getResourceTitle(title, i)
            videoChapter.url = adapter.list[i].url
            videoChapter.extra = adapter.list[i].extra
            videoChapter.realPos = i
            addChapterPic(videoChapter, i)
            videoChapter.isUse = false
            if (StringUtil.isNotEmpty(codeAndHeader)) {
                videoChapter.codeAndHeader = codeAndHeader
                videoChapter.originalUrl = adapter.list[i].url
            }
            chapters.add(videoChapter)
        }
        val videoChapter = VideoChapter()
        videoChapter.memoryTitle = adapter.list[position].title
        videoChapter.title = getResourceTitle(title, position)
        videoChapter.url = url
        videoChapter.isUse = true
        videoChapter.realPos = position
        videoChapter.extra = adapter.list[position].extra
        addChapterPic(videoChapter, position)
        if (StringUtil.isNotEmpty(codeAndHeader)) {
            videoChapter.codeAndHeader = codeAndHeader
            videoChapter.originalUrl = adapter.list[position].url
        }
        chapters.add(videoChapter)
        for (i in position + 1 until adapter.list.size) {
            if (StringUtil.isEmpty(adapter.list[i].url) ||
                adapter.list[i].url.contains("ignoreVideo") ||
                adapter.list[i].url.contains("ignoreMusic") ||
                adapter.list[i].url.contains("@rule=")
            ) {
                continue
            }

            if (hasPlayListTag) {
                if (!isSamePlayList(cls, adapter.list[i])) {
                    //使用了playlist标识，而当前按钮标识不一致
                    continue
                }
            } else {
                if (!StringUtils.equals(type, adapter.list[i].type)) {
                    break
                }
            }
            val chapter = VideoChapter()
            chapter.title = getResourceTitle(title, i)
            chapter.memoryTitle = adapter.list[i].title
            chapter.url = adapter.list[i].url
            chapter.extra = adapter.list[i].extra
            chapter.realPos = i
            addChapterPic(chapter, i)
            chapter.isUse = false
            if (StringUtil.isNotEmpty(codeAndHeader)) {
                chapter.codeAndHeader = codeAndHeader
                chapter.originalUrl = adapter.list[i].url
            }
            chapters.add(chapter)
        }
        return chapters
    }

    private fun addChapterPic(videoChapter: VideoChapter, pos: Int) {
        if (StringUtil.isNotEmpty(adapter.list[pos].pic)
            && "*" != adapter.list[pos].pic
        ) {
            videoChapter.picUrl = adapter.list[pos].pic
        } else if (activity != null) {
            val picUrl = requireActivity().intent.getStringExtra("picUrl")
            videoChapter.picUrl = picUrl
        }
    }

    private fun memoryClick(
        ruleDTO: RuleDTO,
        pageTitle: String,
        position: Int,
        url: String?
    ) {
        val clickText = adapter.list[position].title
        if (!noHistory && !clickText.isNullOrEmpty() && url?.contains("#noRecordHistory#") == false) {
            HistoryMemoryService.memoryClick(ruleDTO.url, pageTitle, position, clickText)
        }
    }

    fun runOnUiThread(runnable: Runnable) {
        lifecycleScope.launch(Dispatchers.Main) {
            runnable.run()
        }
    }

    fun runOnUi(runnable: () -> Unit) {
        lifecycleScope.launch(Dispatchers.Main) {
            runnable()
        }
    }

    override fun bindArrayToView(actionType: String?, data: MutableList<ArticleList>?) {
        bindArrayToView(actionType, data, false)
    }

    private fun bindArrayToView(
        actionType: String?,
        data0: MutableList<ArticleList>?,
        fromLocal: Boolean
    ) {
        if (fromLocal && data0.isNullOrEmpty()) {
            return
        }
        val data = data0 ?: ArrayList()
        if (!fromLocal && page == 1 && StringUtil.isNotEmpty(autoCacheMd5)) {
            saveCache(requireContext(), autoCacheMd5, data, cacheCode)
        }
        updateReadData(page == 1, data)
        updateWebViewHolder(data)
        val textConfig = SettingConfig.getTextConfig(context)
        injectTextConfig(textConfig, data, false)
        updateReadLayout(textConfig, page == 1, data)
        runWaitUI {
            maxLoadedPage = if (page == 1) page else Math.max(maxLoadedPage, loadingPage)
            loadingPage = -1
            if (page == 1) {
                smartRefreshLayout.finishRefresh()
                var dynamicUpdated = false
                if (fromLocal) {
                    //从本地加载数据，设置等待合并状态
                    cacheMergeState = 1
                    Glide.get(requireContext()).clearMemory()
                    updateWebView(false)
                } else {
                    //从网络加载数据
                    if (cacheMergeState == 1) {
                        //当成翻页处理，链接相同不更新
                        updateWebView(true)
                        if (!immersiveTheme) {
                            data.addAll(0, initHeaders()!!)
                        }
                        //需要合并，动态刷新
                        var lastIndex = -1
                        for (i in adapter.list.indices) {
                            if (data.size <= i) {
                                break
                            } else if (!ArticleList.equals(adapter.list[i], data[i])) {
                                break
                            }
                            lastIndex = i
                        }
                        if (lastIndex >= 0) {
                            if (data.size <= lastIndex + 1) {
                                if (lastIndex + 1 < adapter.list.size) {
                                    //减少了，移除
                                    val rmSize = adapter.list.size - lastIndex - 1
                                    for (i in adapter.list.size - 1 downTo lastIndex + 1) {
                                        adapter.list.removeAt(i)
                                    }
                                    adapter.notifyRangeRemoved(lastIndex + 1, rmSize)
                                } else {
                                    //啥也不需要做
                                }
                            } else {
                                if (lastIndex + 1 >= adapter.list.size) {
                                    //只需要追加即可
                                } else {
                                    //要移除不同的，追加新的
                                    val rmSize = adapter.list.size - lastIndex - 1
                                    val hasFlex = adapter.hasFlex(lastIndex + 1, rmSize)
                                    for (i in adapter.list.size - 1 downTo lastIndex + 1) {
                                        adapter.list.removeAt(i)
                                    }
                                    if (hasFlex) {
                                        adapter.notifyDataChanged()
                                    } else {
                                        adapter.notifyRangeRemoved(lastIndex + 1, rmSize)
                                    }
                                }
                                val appendList: List<ArticleList> =
                                    data.subList(lastIndex + 1, data.size)
                                val start = adapter.list.size
                                adapter.list.addAll(appendList)
                                adapter.notifyRangeInserted(start, appendList.size)
                            }
                            dynamicUpdated = true
                        }
                        //合并完成
                        cacheMergeState = 2
                    } else {
                        updateWebView(false)
                    }
                    if (cacheMergeState <= 0) {
                        Glide.get(requireContext()).clearMemory()
                    }
                }
                if (!dynamicUpdated) {
                    if (!immersiveTheme) {
                        data.addAll(0, initHeaders()!!)
                    }
                    list.clear()
                    list.addAll(data)
                    adapter.notifyDataChanged()
                }
                showLastClick()
            } else {
                smartRefreshLayout.finishLoadMore()
                updateWebView(true)
                val start = list.size
                if (list.size == 0 && !immersiveTheme) {
                    data.addAll(0, initHeaders()!!)
                }
                list.addAll(data)
                adapter.notifyItemRangeInserted(start, data.size)
            }
            isLoading = false
            if (loadListener != null) {
                loadListener!!.complete()
            }
        }
    }

    private fun initHeaders(): List<ArticleList>? {
        val lists: MutableList<ArticleList> = java.util.ArrayList()
        lists.add(ArticleList.newBigBlank())
        return lists
    }

    private fun runWaitUI(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
            return
        }
        //用锁锁住后台线程
        val lock = CountDownLatch(1)
        runOnUI {
            try {
                runnable.run()
            } finally {
                lock.countDown()
            }
        }
        try {
            lock.await(30, TimeUnit.SECONDS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun bindObjectToView(actionType: String?, data: ArticleList?) {
        isLoading = false
        loadingPage = -1
        if (StringUtil.isEmpty(actionType) || data == null) {
            return
        }
        when (actionType) {
            "onRefresh", "refresh" -> onRefreshJS = appendJs(onRefreshJS, data.title)
            "onClose", "close" -> onCloseJS = appendJs(onCloseJS, data.title)
            else -> {
            }
        }
    }

    private fun appendJs(old: String?, newjs: String): String? {
        if (StringUtil.isEmpty(newjs)) {
            return old
        }
        return if (StringUtil.isEmpty(old)) {
            newjs
        } else {
            if (old?.contains(newjs) == true) {
                old
            } else "$old\n;\n$newjs"
        }
    }

    private fun hookBackJs(): String {
        return """
var backTag = false;
back = (param0) => {
   if(!backTag) {       
        method_back.invoke(javaContext, param0);
        backTag = true;
   } else log('back 调用多次被拦截')
}
"""
    }

    override fun error(title: String?, msg: String?, code: String?, e: java.lang.Exception?) {
        runOnUi {
            isLoading = false
            loadingPage = -1
            page = maxLoadedPage
            loading(false)
            smartRefreshLayout.finishRefresh(true)
            smartRefreshLayout.finishLoadMore()
            loading(false)
            DebugUtil.showErrorMsg(
                activity,
                context, "规则执行过程中出现错误", msg, "home@" + ruleDTO.title, e
            )
        }
    }

    override fun loading(isLoading: Boolean) {
        loading("加载中，请稍候", isLoading)
    }

    fun loading(text: String, isLoading: Boolean) {
        runOnUi {
            if (activity is ArticleListIsland) {
                val island: ArticleListIsland? = activity as ArticleListIsland?
                if (isLoading) {
                    island?.showLoading(text)
                } else {
                    island?.hideLoading()
                }
            }
        }
    }


    /**
     * 找到并更新WebViewHolder
     *
     * @param data
     */
    private fun updateWebViewHolder(data: MutableList<ArticleList>) {
        if (CollectionUtil.isNotEmpty(data)) {
            for (datum in data) {
                if (ArticleColTypeEnum.getByCode(datum.type) == ArticleColTypeEnum.X5_WEB_VIEW) {
                    if (webViewHolder == null) {
                        synchronized(this) {
                            if (webViewHolder == null) {
                                webViewHolder = ArticleWebViewHolder()
                            }
                        }
                    }
                    webViewHolder?.setUrl(datum.url)
                    updateModeByDesc(datum.desc)
                    if (StringUtil.isNotEmpty(datum.extra)) {
                        val extra = JSON.parseObject(datum.extra, X5Extra::class.java)
                        webViewHolder!!.x5Extra = extra
                    }
                    break
                }
            }
        }
    }

    private fun updateModeByDesc(desc: String) {
        webViewHolder?.extra = desc
        if (StringUtil.isNotEmpty(desc)) {
            val extra = desc.split("&&").toTypedArray()
            for (s1 in extra) {
                val s = s1.trim { it <= ' ' }
                if (s.equals(ArticleWebViewHolder.Mode.FLOAT.name, ignoreCase = true)) {
                    webViewHolder?.mode = ArticleWebViewHolder.Mode.FLOAT
                    break
                } else if (s.equals(ArticleWebViewHolder.Mode.LIST.name, ignoreCase = true)) {
                    webViewHolder?.mode = ArticleWebViewHolder.Mode.LIST
                    break
                }
            }
        }
    }

    /**
     * UI线程刷新WebView
     */
    private fun updateWebView(changePage: Boolean) {
        if (activity == null || requireActivity().isFinishing) {
            return
        }
        if (webViewHolder != null && webViewHolder!!.webView == null) {
            if (webViewHolder!!.x5Extra != null && webViewHolder!!.x5Extra.isDisableX5) {
                //临时禁用，让后面的x5使用系统内核
                QbSdk.forceSysWebView()
            }
            val webView = WebView(activity)
            if (webViewHolder!!.x5Extra != null && webViewHolder!!.x5Extra.isDisableX5) {
                //不生效，要重启APP才能生效
                QbSdk.unForceSysWebView()
            }
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            webView.layoutParams = layoutParams
            webViewHolder!!.height = layoutParams.height
            webViewHolder!!.webView = webView
            webViewHolder!!.initWebView(activity)
            webViewHolder!!.initAdBlock()
            webViewHolder!!.initFloatVideo(
                webViewHolder!!.x5Extra != null && webViewHolder!!.x5Extra.isFloatVideo,
                findView(R.id.float_container)
            )
            webViewHolder!!.progressListener = object : ArticleWebViewHolder.ProgressListener {
                override fun onPageStarted(s: String) {}
                override fun onProgressChanged(s: String?, i: Int) {
                    if (StringUtil.isNotEmpty(s) && s?.startsWith("http") == true
                        && (webViewHolder?.x5Extra == null || webViewHolder?.x5Extra?.isShowProgress == true)
                    ) {
                        progress_bar!!.setWebProgress(i)
                    }
                }

                override fun onPageFinished(s: String) {
                    progress_bar!!.hide()
                }
            }
            if (webViewHolder!!.x5Extra != null) {
                if (StringUtil.isNotEmpty(webViewHolder!!.x5Extra.ua)) {
                    webView.settings.setUserAgent(webViewHolder!!.x5Extra.ua)
                }
            }
            adapter.setWebViewHolder(webViewHolder)
        }
        if (webViewHolder != null) {
            if (webViewHolder!!.mode == ArticleWebViewHolder.Mode.FLOAT) {
                if (webViewContainer!!.childCount <= 0) {
                    val height: Int = getWebHeight(
                        ArticleWebViewHolder.getHeightByExtra(
                            webViewHolder!!.url, webViewHolder!!.extra
                        )
                    )
                    val layoutParams =
                        webViewContainer!!.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.height = DisplayUtil.dpToPx(context, height)
                    layoutParams.setMargins(0, 0, 0, 0)
                    webViewContainer!!.layoutParams = layoutParams
                    webViewHolder!!.height = layoutParams.height
                    if (webViewHolder!!.webView.parent != null) {
                        (webViewHolder!!.webView.parent as RelativeLayout).removeAllViews()
                    }
                    webViewContainer!!.addView(webViewHolder!!.webView)
                } else {
                    //更新高度
                    updateWebViewHeight()
                }
            } else if (webViewContainer!!.childCount > 0) {
                val layoutParams = webViewContainer!!.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(0, 0, 0, 0)
                webViewContainer!!.layoutParams = layoutParams
                webViewContainer!!.removeAllViews()
            }
        }
        if (webViewHolder != null && webViewHolder!!.webView != null) {
            if (webViewHolder!!.x5Extra != null) {
                if (StringUtil.isNotEmpty(webViewHolder!!.x5Extra.ua)) {
                    webViewHolder!!.webView.settings.setUserAgent(webViewHolder!!.x5Extra.ua)
                }
            }
            if (StringUtil.isNotEmpty(webViewHolder!!.url) &&
                !WebUtil.equals(webViewHolder!!.webView.url, webViewHolder!!.url)
            ) {
                //链接更新了，load新链接
                webViewHolder!!.loadUrlCheckReferer(webViewHolder!!.url)
                if (StringUtil.isNotEmpty(webViewHolder!!.url) && webViewHolder!!.url.startsWith("http")
                    && (webViewHolder!!.x5Extra == null || webViewHolder!!.x5Extra?.isShowProgress == true)
                ) {
                    progress_bar!!.show()
                }
            } else if (StringUtil.isNotEmpty(webViewHolder!!.url) && WebUtil.equals(
                    webViewHolder!!.webView.url,
                    webViewHolder!!.url
                )
            ) {
                //链接没有更新，并且不是翻页，reload一下
                if (!changePage) {
                    webViewHolder!!.webView.reload()
                }
            } else if (StringUtil.isEmpty(webViewHolder!!.url) &&
                StringUtil.isNotEmpty(webViewHolder!!.webView.url)
            ) {
                //链接变空，但是原来有加载链接，那就静音
                val muteJs = JSManager.instance(context).getJsByFileName("mute")
                if (!TextUtils.isEmpty(muteJs)) {
                    webViewHolder!!.webView.evaluateJavascript(muteJs, null)
                }
            }
        }
    }

    private fun updateWebViewHeight() {
        if (webViewHolder!!.mode == ArticleWebViewHolder.Mode.FLOAT) {
            val height: Int = getWebHeight(
                ArticleWebViewHolder.getHeightByExtra(
                    webViewHolder!!.url,
                    webViewHolder!!.extra
                )
            )
            val layoutParams = webViewContainer!!.layoutParams as ViewGroup.MarginLayoutParams
            val px = DisplayUtil.dpToPx(context, height)
            if (px == layoutParams.height) {
                return
            }
            if (webViewHolder!!.anim != null && webViewHolder!!.anim.isRunning) {
                webViewHolder!!.anim.cancel()
            }
            //高度变为0，静音
            if (px == 0) {
                val muteJs = JSManager.instance(context).getJsByFileName("mute")
                if (!TextUtils.isEmpty(muteJs)) {
                    webViewHolder!!.webView.evaluateJavascript(muteJs, null)
                }
            }
            val lastHeight = layoutParams.height
            webViewHolder!!.height = px
            layoutParams.setMargins(0, 0, 0, 0)
            val anim = ValueAnimator.ofInt(lastHeight, px)
            anim.duration = 300
            anim.addUpdateListener { animation: ValueAnimator ->
                val value = animation.animatedValue as Int
                layoutParams.height = value
                webViewContainer!!.layoutParams = layoutParams
            }
            anim.start()
            webViewHolder!!.anim = anim
        }
    }

    private fun updateWebViewHeightForList(event: OnRefreshX5HeightEvent) {
        if (webViewHolder!!.mode == ArticleWebViewHolder.Mode.FLOAT) {
            return
        }
        if (CollectionUtil.isEmpty(adapter.list)) {
            return
        }
        for (i in adapter.list.indices) {
            val datum = adapter.list[i]
            if (ArticleColTypeEnum.getByCode(datum.type) == ArticleColTypeEnum.X5_WEB_VIEW) {
                datum.desc = event.desc
                adapter.notifyItemChanged(i)
                break
            }
        }
    }

    private fun getWebHeight(height: Int): Int {
        return if (height == -1000) {
            if (bgViewHeight <= 0) {
                bgViewHeight = DisplayUtil.pxToDp(context, findView<View>(R.id.bg).measuredHeight)
            }
            val h = 0
            if (bgViewHeight <= 0 || bgViewHeight <= h) {
                return 240
            }
            bgViewHeight - h - 5
        } else {
            height
        }
    }

    override fun onPause() {
        super.onPause()
        if (webViewHolder != null) {
            webViewHolder!!.onPause()
        }
        loading(false)
    }

    override fun onResume() {
        super.onResume()
        if (webViewHolder != null) {
            webViewHolder!!.onResume()
        }
    }

    override fun onBackPressed(): Boolean {
        return if (webViewHolder != null && webViewHolder!!.onBackPressed()) {
            true
        } else super.onBackPressed()
    }

    private fun isOnPause(): Boolean {
        if (activity is MiniProgramActivity) {
            val activity = activity as MiniProgramActivity?
            if (activity!!.isOnPause) {
                return true
            }
        }
        return !userVisibleHint
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        memoryTask?.let {
            HeavyTaskUtil.postTaskToQueue(it)
        }
        if (webViewHolder != null && webViewHolder!!.webView != null) {
            webViewHolder!!.release()
        }
        if (readAloudHolder != null) {
            readAloudHolder!!.shutdown()
        }
        Application.application.stopReadAloudService()
        X5WebViewParser.destroy0()
        WebkitParser.destroy0()
        if (StringUtil.isNotEmpty(onCloseJS)) {
            HeavyTaskUtil.executeNewTask {
                val js = onCloseJS
                onCloseJS = null
                JSEngine.getInstance()
                    .evalJS(JSEngine.getMyRule(articleListRule) + hookBackJs() + js, "")
            }
        }
        cancelTasks(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshWebView(event: OnRefreshWebViewEvent) {
        if (isOnPause()) {
            return
        }
        if (webViewHolder != null && webViewHolder!!.webView != null) {
            webViewHolder!!.url = event.url
            webViewHolder!!.webView.loadUrl(event.url)
            if (StringUtil.isNotEmpty(event.url) && event.url.startsWith("http")
                && webViewHolder!!.x5Extra.isShowProgress
            ) {
                progress_bar!!.show()
            }
            updateWebViewHeight()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun OnRefreshX5Height(event: OnRefreshX5HeightEvent) {
        if (isOnPause()) {
            return
        }
        if (webViewHolder != null && webViewHolder!!.webView != null) {
            updateModeByDesc(event.desc)
            updateWebViewHeight()
            updateWebViewHeightForList(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshPage(event: OnRefreshPageEvent) {
        if (activity is MiniProgramActivity) {
            val activity = activity as MiniProgramActivity?
            if (activity!!.isOnPause) {
                return
            }
        }
        if (userVisibleHint) {
            if (event.isScrollTop) {
                recyclerView.scrollToPosition(0)
            }
            page = 1
            loadData()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onConfirm(event: ConfirmEvent?) {
        if (activity == null || isOnPause() || requireActivity().isFinishing) {
            return
        }
        confirm(requireActivity(), articleListRule, event)
    }

    private fun confirm(
        activity: Activity?,
        articleListRule: ArticleListRule?,
        event: ConfirmEvent?
    ) {
        if (event == null || activity == null || activity.isFinishing) {
            return
        }
        if (StringUtil.isEmpty(event.title) || StringUtil.isEmpty(event.content)) {
            return
        }
        val runnable =
            Consumer { s: String ->
                HeavyTaskUtil.executeNewTask {
                    val result =
                        JSEngine.getInstance().evalJS(JSEngine.getMyRule(articleListRule) + s, "")
                    if (StringUtil.isNotEmpty(result) && !"undefined".equals(
                            result,
                            ignoreCase = true
                        )
                        && "hiker://empty" != result
                    ) {
                        if (!activity.isFinishing) {
                            activity.runOnUiThread(Runnable {
                                DetailUIHelper.dealUrlSimply(
                                    activity,
                                    articleListRule,
                                    null,
                                    result,
                                    null
                                )
                            })
                        }
                    }
                }
            }
        XPopup.Builder(activity)
            .asConfirm(event.title, event.content, {
                if (StringUtil.isEmpty(event.confirm)) {
                    return@asConfirm
                }
                runnable.accept(event.confirm)
            }) {
                if (StringUtil.isEmpty(event.cancel)) {
                    return@asConfirm
                }
                runnable.accept(event.cancel)
            }.show()
    }

    private fun getDetailData(position: Int): Array<String> {
        val articleList = adapter.list[position]
        val url =
            if (StringUtil.isNotEmpty(articleList.url) && articleList.url.length > 200) articleList.url.substring(
                0,
                200
            ) + "..." else articleList.url
        return arrayOf(
            "页面：$myUrl",
            "标题：" + articleList.title,
            "描述：" + articleList.desc,
            "类型：" + articleList.type,
            "链接：$url",
            "图片：" + articleList.pic,
            "附加：" + articleList.extra
        )
    }

    private fun showPosDetail(position: Int) {
        detailPopup = FileDetailPopup(requireActivity(), "调试数据", getDetailData(position))
            .withClickListener(object : FileDetailAdapter.OnClickListener {
                override fun click(text: String) {
                    longClick(null, text)
                }

                override fun longClick(view: View?, text: String) {
                    val t: String
                    val updater: Consumer<String>
                    if (text.startsWith("链接：")) {
                        t = adapter.list[position].url
                        updater = Consumer { s: String? ->
                            adapter.list[position].url = s
                        }
                    } else if (text.startsWith("标题：")) {
                        t = adapter.list[position].title
                        updater = Consumer { s: String? ->
                            adapter.list[position].title = s
                        }
                    } else if (text.startsWith("描述：")) {
                        t = adapter.list[position].desc
                        updater = Consumer { s: String? ->
                            adapter.list[position].desc = s
                        }
                    } else if (text.startsWith("类型：")) {
                        t = adapter.list[position].type
                        updater = Consumer { s: String? ->
                            adapter.list[position].type = s
                        }
                    } else if (text.startsWith("图片：")) {
                        t = adapter.list[position].pic
                        updater = Consumer { s: String? ->
                            adapter.list[position].pic = s
                        }
                    } else if (text.startsWith("附加：")) {
                        t = adapter.list[position].extra
                        updater = Consumer { s: String? ->
                            adapter.list[position].extra = s
                        }
                    } else if (text.startsWith("页面：")) {
                        ClipboardUtil.copyToClipboard(context, myUrl, false)
                        ToastMgr.shortCenter(context, "页面链接已复制到剪贴板")
                        return
                    } else {
                        return
                    }
                    XPopup.Builder(context)
                        .asInputConfirm(
                            "编辑数据", "点击确定后，界面数据会临时修改为传入的数据，刷新后失效",
                            t, null, { s: String ->
                                updater.accept(s)
                                adapter.notifyItemChanged(position)
                                if (detailPopup != null) {
                                    detailPopup?.updateData(getDetailData(position))
                                }
                            }, null, R.layout.xpopup_confirm_input_max
                        ).show()
                }
            })
        XPopup.Builder(context)
            .asCustom(detailPopup).show()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBack(event: OnBackEvent) {
        val activity: Activity? = activity
        // 限制能生效的 Activity
        if (activity == null || activity.javaClass != MiniProgramActivity::class.java) {
            return
        }
        // 限制调用一次只能返回一次
        val filmListActivity = activity as MiniProgramActivity
        if (filmListActivity.isOnPause) {
            return
        }
        if (event.isRefreshPage) {
            val intentData = Intent()
            intentData.putExtra("refreshPage", event.isRefreshPage) // 封装需要返回的数据
            intentData.putExtra("scrollTop", event.isScrollTop)
            activity.setResult(Activity.RESULT_OK, intentData)
        }
        activity.onBackPressed()
    }


    fun refreshLastClick(pos: Int, click: String?, count: Int) {
        if (count <= 0 || activity == null || requireActivity().isFinishing) {
            return
        }
        if (CollectionUtil.isEmpty(adapter.list)) {
            recyclerView.postDelayed({ refreshLastClick(pos, click, count - 1) }, 1000)
        } else {
            refreshLastClick0(pos, click)
        }
    }

    private fun refreshLastClick0(pos0: Int, click: String?) {
        var pos = pos0
        if (CollectionUtil.isEmpty(adapter.list)) {
            return
        }
        if (pos < 0 && StringUtil.isEmpty(click)) {
            return
        }
        if (pos < 0) {
            pos = findPosByTitle(pos, click ?: "")
        }
        if (pos >= 0 && pos < adapter.list.size) {
            if (lastSelectedPosition >= 0 && lastSelectedPosition < adapter.list.size && adapter.list[lastSelectedPosition].isSel
            ) {
                adapter.list[lastSelectedPosition].isSel = false
                adapter.notifyItemChanged(lastSelectedPosition)
            }
            lastSelectedPosition = pos
            if (!adapter.list[pos].isSel) {
                adapter.list[pos].isSel = true
                adapter.notifyItemChanged(pos)
            }
        }
    }

    /**
     * 显示上次播放位置
     */
    private fun showLastClick() {
        if (hasShowLastClick) {
            if (lastSelectedPosition >= 0 && lastSelectedPosition < adapter.list.size && !adapter.list[lastSelectedPosition].isSel
            ) {
                adapter.list[lastSelectedPosition].isSel = true
                adapter.notifyItemChanged(lastSelectedPosition)
            }
            return
        }
        hasShowLastClick = true
        if (isReadTheme) {
            val pos: Int = getPageIndex()
            if (pos > 0) {
                recyclerView.scrollToPosition(pos)
            }
            return
        }
        val historyDTO = HistoryMemoryService.getHistory(ruleDTO, pageTitle)
        if (historyDTO != null) {
            val pos: Int = findPosByTitle(historyDTO.clickPos, historyDTO.clickText)
            if (pos > 0) {
//                if (pos > 10) {
//                    scrollFlag = -1
//                }
                recyclerView.scrollToPosition(pos)
            }
            val history = LastClickShowEvent(historyDTO.clickText, historyDTO.clickPos)
            showLastClick(history, historyDTO)
            refreshLastClick(pos, historyDTO.clickText, 3)
        }
    }

    /**
     * 显示上次播放位置
     */
    fun showLastClickItem() {
        if (isReadTheme) {
            return
        }
        HeavyTaskUtil.executeNewTask {
            val historyDTO = HistoryMemoryService.getHistory(ruleDTO, pageTitle)
            if (historyDTO != null) {
                runOnUI {
                    val pos: Int = findPosByTitle(historyDTO.clickPos, historyDTO.clickText)
                    refreshLastClick(pos, historyDTO.clickText, 3)
                }
            }
        }
    }

    fun findPosByTitle(lastClickPos: Int, clickText: String?): Int {
        if (StringUtil.isEmpty(clickText)) {
            return 0
        }
        var minPosOffset = 100000
        var pos = -1
        for (i in adapter.list.indices) {
            if (isTitleEquals(clickText, adapter.list[i])) {
                //找到距离之前的记录最近的一个位置
                val posOffset = abs(lastClickPos - i)
                if (minPosOffset > posOffset) {
                    pos = i
                    minPosOffset = posOffset
                }
            }
        }
        return pos
    }

    private fun isTitleEquals(lastClick: String?, articleList: ArticleList): Boolean {
        if (lastClick == articleList.title) {
            return true
        }
        if (StringUtil.isEmpty(articleList.title)) {
            return false
        }
        return if (!isChapterType(articleList)) {
            false
        } else lastClick?.replace("-", "_") == articleList.title.replace("-", "_")
    }

    private fun isChapterType(articleList: ArticleList): Boolean {
        for (chapterType in chapterTypes) {
            if (chapterType == articleList.type) {
                return true
            }
        }
        return false
    }

    private fun clickByHistory(historyDTO: HistoryDTO?) {
        historyDTO?.let {
            val pos = findPosByTitle(it.clickPos, it.clickText)
            if (pos != -1) {
                val url = adapter.list[pos].url
                dealUrlPos(null, pos, url)
            }
        }
    }

    private fun showLastClick(event: LastClickShowEvent, historyDTO: HistoryDTO?) {
        val text: String? = genLastClickText(event)
        if (StringUtil.isEmpty(text)) {
            return
        }
        if (!playLastConsumed && requireActivity().intent.getBooleanExtra("playLast", false)) {
            requireActivity().intent.removeExtra("playLast")
            playLastConsumed = true
            try {
                clickByHistory(historyDTO)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Snackbar.make(findView(R.id.snack_bar_container), "足迹：$text", Snackbar.LENGTH_LONG)
            .setAction("续看") {
                clickByHistory(historyDTO)
            }.show()
    }

    private fun genLastClickText(event: LastClickShowEvent): String? {
        if (StringUtil.isEmpty(event.title)) {
            return null
        }
        val simpleTitle = DetailUIHelper.getTitleText(event.title)
        return if (simpleTitle.length > 30) {
            null
        } else simpleTitle
    }

    private fun scrollPageNow(down: Boolean) {
        val height = smartRefreshLayout.measuredHeight - adapter.lineHeight
        recyclerView.smoothScrollBy(0, if (down) height else -height)
    }

    private fun scrollPageByTouch() {
        val area = getTouchArea()
        if (area === ClickArea.BOTTOM || area === ClickArea.CENTER_RIGHT) {
            if (readAloud) {
                showReadMenu()
                return
            }
            scrollPageNow(true)
        } else if (area === ClickArea.TOP || area === ClickArea.CENTER_LEFT) {
            if (readAloud) {
                showReadMenu()
                return
            }
            scrollPageNow(false)
        } else if (area === ClickArea.CENTER && isReadTheme) {
            showReadMenu()
        }
    }

    private fun showReadMenu() {
        TextConfigHelper.showConfigView(
            activity,
            { textConfig: TextConfig? ->
                injectTextConfig(textConfig!!, adapter.list, true)
                adapter.notifyDataChanged()
                SettingConfig.updateTextConfig(context, textConfig)
                if (read_bottom_bg != null && read_chapter != null) {
                    read_chapter?.setTextColor(textConfig.textColor)
                    read_time?.setTextColor(textConfig.textColor)
                    read_power?.setTextColor(textConfig.textColor)
                }
            },
            { textConfig: TextConfig? -> }, autoRead, readAloud,
            { operation: String? ->
                when (operation) {
                    "自动阅读" -> {
                        autoRead = true
                        requireActivity().window
                            .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        startAutoRead()
                    }
                    "停止自动阅读" -> {
                        autoRead = false
                        requireActivity().window
                            .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    "语音朗读" -> {
                        readAloud = true
                        if (readAloudHolder == null) {
                            readAloudHolder = ReadAloudHolder()
                        }
                        startReadAloud(true)
                        Application.application.startReadAloudService(
                            activity
                        )
                    }
                    "停止语音朗读" -> {
                        readAloud = false
                        revertReadAloudView()
                        if (readAloudHolder != null) {
                            readAloudHolder!!.shutdown()
                        }
                        Application.application.stopReadAloudService()
                    }
                    "切换语音引擎" -> {
                        readAloud = true
                        revertReadAloudView()
                        if (readAloudHolder != null) {
                            readAloudHolder!!.shutdown()
                        }
                        if (readAloudHolder == null) {
                            readAloudHolder = ReadAloudHolder()
                        }
                        startReadAloud(false)
                    }
                }
            }, readAloudHolder
        )
    }

    /**
     * 标红文字
     */
    private fun updateReadAloudView() {
        if (readPosition > -1 && readPosition < adapter.list.size) {
            val text = adapter.list[readPosition].title
            if (StringUtil.isEmpty(text)) {
                return
            }
            adapter.list[readPosition].content = text
            adapter.list[readPosition].title = "<span style=\"color: #ed424b\">$text</span>"
            adapter.notifyItemChanged(readPosition)
        }
    }

    /**
     * 取消标红
     */
    private fun revertReadAloudView() {
        if (readPosition > -1 && readPosition < adapter.list.size) {
            val text = adapter.list[readPosition].content
            if (StringUtil.isEmpty(text)) {
                return
            }
            adapter.list[readPosition].title = text
            adapter.list[readPosition].content = null
            adapter.notifyItemChanged(readPosition)
        }
    }

    /**
     * 开始语音朗读
     */
    private fun startReadAloud(reGetPosition: Boolean) {
        if (!readAloud || readAloudHolder == null) {
            return
        }
        if (context == null || activity == null || requireActivity().isFinishing) {
            return
        }
        if (reGetPosition) {
            readPosition = gridLayoutManager!!.findFirstCompletelyVisibleItemPosition()
        } else {
            if (readPosition <= -1 || readPosition >= adapter.list.size) {
                readPosition = gridLayoutManager!!.findFirstCompletelyVisibleItemPosition()
            }
        }
        if (readPosition < 0) {
            readPosition = gridLayoutManager!!.findFirstVisibleItemPosition()
        }
        if (readPosition < 0) {
            readPosition = 0
        }
        readAloudNext()
    }

    private fun readAloudNext() {
        if (!readAloud || readAloudHolder == null) {
            return
        }
        if (readPosition > -1 && readPosition < adapter.list.size) {
            var text = adapter.list[readPosition].title
            text = clearText(text)
            if (StringUtil.isEmpty(text)) {
                //可能是个空元素
                Timber.d("readAloudNext, empty text")
                revertReadAloudView()
                readPosition++
                if (readPosition < adapter.list.size - 1) {
                    recyclerView.scrollToPosition(readPosition + 1)
                }
                readAloudNext()
                return
            }
            updateReadAloudView()
            readAloudHolder!!.read(requireContext(), text!!) {
                if (activity == null || requireActivity().isFinishing) {
                    return@read
                }
                runOnUI {
                    revertReadAloudView()
                    readPosition++
                    if (readPosition > -1 && readPosition < adapter.list.size) {
                        if (readPosition < adapter.list.size - 1) {
                            recyclerView.scrollToPosition(readPosition + 1)
                        }
                        readAloudNext()
                        preloadCheckForReadAloud()
                    } else {
                        readPosition = -1
                        Timber.d(
                            "readAloudNext, readPosition: %s, list: %s",
                            readPosition,
                            adapter.list.size
                        )
                    }
                }
            }
        } else {
            Timber.d("readAloudNext, readPosition: %s, list: %s", readPosition, adapter.list.size)
        }
    }

    private fun clearText(text: String?): String? {
        var text = text
        if (text == null || text.isEmpty()) {
            return text
        }
        text = StringUtil.trimBlanks(text.trim { it <= ' ' }).replace("&nbsp;", "")
        if (text.contains("</") || text.contains("<p>") || text.contains("<img") || text.contains("&lt;") || text.contains("&gt;")) {
            val document = Jsoup.parse(text)
            text = document.text()
        }
        return text
    }

    private fun startAutoRead() {
        if (!autoRead) {
            return
        }
        if (context == null || activity == null || requireActivity().isFinishing) {
            return
        }
        if (!isOnPause() && gridLayoutManager != null && gridLayoutManager!!.findLastCompletelyVisibleItemPosition() + 1 < adapter.itemCount) {
            val autoReadSpeed = PreferenceMgr.getInt(
                context, "autoReadSpeed", 2
            )
            recyclerView.scrollBy(0, autoReadSpeed)
        }
        recyclerView.postDelayed({ startAutoRead() }, 16)
    }

    private fun getTouchArea(): ClickArea {
        return if (activity is MiniProgramActivity) {
            (activity as MiniProgramActivity).getTouchArea()
        } else ClickArea.BOTTOM
    }

    private fun getItemTitle(position: Int): String? {
        return if (position < 0 || position >= adapter.list.size) {
            DetailUIHelper.getActivityTitle(activity)
        } else {
            val baseExtra = adapter.list[position].baseExtra
            if (baseExtra != null && StringUtil.isNotEmpty(baseExtra.pageTitle)) {
                return baseExtra.pageTitle
            }
            if (baseExtra != null && !baseExtra.isInheritTitle) {
                return DetailUIHelper.getTitleText(adapter.list[position].title)
            }
            if (requireActivity().intent.getBooleanExtra("fromHome", false)) {
                return DetailUIHelper.getTitleText(adapter.list[position].title)
            }
            DetailUIHelper.getItemTitle(activity, adapter.list[position].title)
        }
    }

    private fun getResourceTitle(activityTitle: String, position: Int): String? {
        val baseExtra = adapter.list[position].baseExtra
        return if (baseExtra != null && !baseExtra.isInheritTitle) {
            DetailUIHelper.getTitleText(adapter.list[position].title)
        } else activityTitle + DetailUIHelper.getTitleText(adapter.list[position].title)
    }

    private fun toNextPage(position: Int, url: String) {
        if (CollectionUtil.isEmpty(adapter.list)) {
            return
        }
        HeavyTaskUtil.executeNewTask {
            try {
                val nextPage = MiniProgramRouter.toRuleDTO(
                    PageParser.getNextPage(
                        articleListRule,
                        url,
                        adapter.list[position].extra
                    )
                )
                if (activity != null && !requireActivity().isFinishing) {
                    requireActivity().runOnUiThread {
                        //标题处理，将html转成纯文本
                        var title: String? = getItemTitle(position)
                        val paramsMap = HttpParser.getParamsByUrl(url)
                        if (paramsMap.containsKey("pageTitle")) {
                            val pageTitle = paramsMap["pageTitle"]
                            if (StringUtil.isNotEmpty(pageTitle)) {
                                title = pageTitle
                            }
                        }
                        if (nextPage.url?.contains("#noHistory#") == true) {
                            nextPage.url = StringUtils.replaceOnce(
                                nextPage.url,
                                "#noHistory#",
                                ""
                            )
                        } else {
                            memoryClick(ruleDTO, pageTitle, position, url)
                        }
                        val autoPage1 = nextPage.url?.contains("#autoPage#") == true
                        if (autoPage1) {
                            val nextData: List<ArticleList> =
                                java.util.ArrayList(
                                    adapter.list.subList(
                                        position,
                                        adapter.list.size
                                    )
                                )
                            val autoPageData = AutoPageData(
                                nextData, this.ruleDTO.url,
                                pageTitle, position, noRecordHistory
                            )
                            DataTransferUtils.putTemp(autoPageData)
                        } else {
                            DataTransferUtils.clearTemp()
                        }
                        MiniProgramRouter.startMiniProgram(
                            requireContext(),
                            nextPage.url!!,
                            title!!,
                            nextPage,
                            false,
                            pageTitle,
                            this.ruleDTO.url,
                            playLast = false,
                            fromHome = false,
                            picUrl = adapter.list[position].pic
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (activity != null && !requireActivity().isFinishing) {
                    requireActivity().runOnUiThread {
                        ToastMgr.shortCenter(
                            context,
                            e.message
                        )
                    }
                }
            }
        }
    }

    private fun autoPage(memoryClick: Boolean) {
        if (activity !is MiniProgramActivity) {
            return
        }
        val activity = activity as MiniProgramActivity
        val pageData: AutoPageData? = activity.getParentData()
        if (pageData == null || CollectionUtil.isEmpty(pageData.nextData) || isLoading) {
            return
        }
        try {
            page++
//            loading(true)
            val rule: ArticleListRule = articleListRule.clone()
            //ParentData只存了当前页及后面的内容
            if (page > pageData.nextData.size) {
//                loading(false)
                page--
                return
            }
            val currArticleList: ArticleList = pageData.nextData[page - 1]
            if (currArticleList.url.startsWith("hiker://page/")) {
                rule.url = PageParser.getUrl(currArticleList.url, currArticleList.extra)
            } else {
                val urls = rule.url.split(";").toTypedArray()
                urls[0] = currArticleList.url.split("@rule=").toTypedArray()[0].split(";")
                    .toTypedArray()[0]
                rule.url = StringUtil.arrayToString(urls, 0, ";")
            }
            rule.params = currArticleList.extra
            isLoading = true
            loadingPage = page
            lifecycleScope.launch(Dispatchers.IO) {
                articleListService.params(context, page, false, rule)
                    .process(HomeActionEnum.ARTICLE_LIST, this@MiniProgramFragment)
            }
            if (!pageData.isNoRecordHistory && memoryClick) {
                if (page <= pageData.nextData.size) {
                    //已经到底了，直接记到最后
                    updateAutoPageLastClick(
                        pageData,
                        pageData.nextData[page - 1],
                        pageData.currentPos + page - 1
                    )
                }
            }
        } catch (e: java.lang.Exception) {
//            loading(false)
            page--
            ToastMgr.shortBottomCenter(context, "出错：" + e.message)
            e.printStackTrace()
        }
    }

    private fun updateAutoPageLastClick(
        pageData: AutoPageData,
        articleList: ArticleList?,
        pos: Int
    ) {
        val click = articleList!!.title
        if (StringUtil.isEmpty(click) || click.length > 25) {
            if (click.length > 100 || !isChapterType(articleList)) {
                return
            }
        }
        HistoryMemoryService.memoryClick(pageData.cUrl, pageData.mTitle, pos, click)
    }

    private fun notLoadPreRule(): Boolean {
        return !(activity != null && page == 1 && requireActivity().intent.getBooleanExtra(
            "needPreParse",
            false
        ))
    }

    private fun injectTextConfig(
        textConfig: TextConfig,
        lists: MutableList<ArticleList>,
        refreshBackground: Boolean
    ) {
        if (CollectionUtil.isEmpty(lists) || !isReadTheme) {
            return
        }
        if (textConfig.textSize == TextConfig.UN_SET && textConfig.lineSpacingExtra == TextConfig.UN_SET) {
            return
        }
        var injectBackground = false
        for (articleList in lists) {
            if (ArticleColTypeEnum.RICH_TEXT.code == articleList.type) {
                val extra: RichTextExtra = if (StringUtil.isNotEmpty(articleList.extra)) {
                    JSON.parseObject(
                        articleList.extra,
                        RichTextExtra::class.java
                    )
                } else {
                    RichTextExtra()
                }
                if (isReadTheme) {
                    if (textConfig.textSize != TextConfig.UN_SET) {
                        extra.textSize = textConfig.textSize
                    }
                    if (textConfig.lineSpacingExtra != TextConfig.UN_SET) {
                        extra.lineSpacing = textConfig.lineSpacingExtra
                    }
                    extra.tc = textConfig.textColor
                    articleList.extra = JSON.toJSONString(extra)
                    if (refreshBackground && !injectBackground) {
                        injectBackground = true
                        refreshBackground(textConfig.background)
                    }
                }
            }
        }
    }

    private fun refreshBackground(bg: String) {
        if (activity is MiniProgramActivity) {
            (activity as MiniProgramActivity).refreshBackground(bg)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun finishParse(event: DestroyEvent) {
        if (activity == null || requireActivity().isFinishing || !userVisibleHint) {
            return
        }
        if ("webkit" == event.mode) {
            WebkitParser.finishParse(context, event.url, event.ticket)
        } else {
            X5WebViewParser.finishParse(context, event.url, event.ticket)
        }
    }

    fun clickSaveRuleBtn() {
        if (list.size > 0 && list[list.size - 1].title == "保存规则") {
            clickItem(null, list.size - 1)
        }
    }

    @Subscribe
    fun findItem(event: ItemFindEvent) {
        if (activity == null || requireActivity().isFinishing) {
            return
        }
        if (CollectionUtil.isEmpty(adapter.list)) {
            return
        }
        for (articleList in adapter.list) {
            if (StringUtils.equals(event.id, articleList.baseExtra.id)) {
                event.articleList = articleList
                event.countDownLatch.countDown()
                return
            }
        }
    }

    @Subscribe
    fun findItems(event: ClsItemsFindEvent) {
        if (activity == null || requireActivity().isFinishing) {
            return
        }
        if (CollectionUtil.isEmpty(adapter.list)) {
            return
        }
        val lists: MutableList<ArticleList?> = java.util.ArrayList()
        for (articleList in adapter.list) {
            if (hasCls(articleList.baseExtra.cls, event.cls)) {
                lists.add(articleList)
            }
        }
        if (CollectionUtil.isNotEmpty(lists)) {
            event.articleLists = lists
            event.countDownLatch.countDown()
        }
    }

    private fun hasCls(itemCls: String?, checkCls: String?): Boolean {
        return if (StringUtil.isEmpty(itemCls) || StringUtil.isEmpty(checkCls)) {
            false
        } else " $itemCls ".contains(" $checkCls ")
    }

    @Subscribe
    fun onItemModify(event: ItemModifyEvent) {
        if (activity == null || requireActivity().isFinishing) {
            return
        }
        if (CollectionUtil.isEmpty(adapter.list)) {
            return
        }
        if (event.action == ItemModifyEvent.Action.DELETE && StringUtil.isNotEmpty(event.cls)) {
            //根据cls批量删除
            runWaitUI {

                //看看删除的是不是连续节点
                var start = -1
                var count = 0
                //检查是不是有聚合元素（多个item聚合在一个View）
                var hasFlex = false
                for (i in adapter.list.indices) {
                    if (adapter.list[i] != null && hasCls(
                            adapter.list[i].baseExtra.cls, event.cls
                        )
                    ) {
                        if (start < 0) {
                            start = i
                        }
                        if (count == i - start) {
                            //还是连续的
                            count++
                            if (ArticleColTypeEnum.SCROLL_BUTTON.code == adapter.list[i]
                                    .type || ArticleColTypeEnum.FLEX_BUTTON.code == adapter.list[i].type
                            ) {
                                hasFlex = true
                            }
                        } else {
                            //第二段连续删除的点了，说明不是连续的
                            count = 0
                            break
                        }
                    }
                }
                //真正执行删除
                if (start < 0) {
                    //没找到一个可删除的元素，不必要再次遍历
                    return@runWaitUI
                }
                val iterator = adapter.list.iterator()
                while (iterator.hasNext()) {
                    val articleList = iterator.next()
                    if (articleList != null && hasCls(
                            articleList.baseExtra.getCls(),
                            event.cls
                        )
                    ) {
                        iterator.remove()
                    }
                }
                if (count > 0 && !hasFlex) {
                    //这样做是避免刷新了input组件，导致输入的内容丢失和焦点丢失
                    adapter.notifyRangeRemoved(start, count)
                } else {
                    adapter.notifyDataChanged()
                }
            }
        }
        if (event.action == ItemModifyEvent.Action.DELETE && CollectionUtil.isNotEmpty(event.list)) {
            //批量删除
            runWaitUI {
                val iterator = adapter.list.iterator()
                val ids: Set<String> =
                    HashSet(
                        Stream.of(event.list)
                            .map { it: ArticleList ->
                                it.baseExtra.id
                            }.toList()
                    )
                while (iterator.hasNext()) {
                    val articleList = iterator.next()
                    if (articleList != null && ids.contains(articleList.baseExtra.id)) {
                        iterator.remove()
                    }
                }
                adapter.notifyDataChanged()
            }
            return
        }
        var checkId: String? = ""
        checkId =
            if (event.action == ItemModifyEvent.Action.ADD || event.action == ItemModifyEvent.Action.UPDATE) {
                event.anchorId
            } else {
                if (event.articleList == null || StringUtil.isEmpty(event.articleList.extra)) {
                    return
                }
                event.articleList.baseExtra.id
            }
        if (StringUtil.isEmpty(checkId)) {
            return
        }
        for (i in adapter.list.indices) {
            if (adapter.list[i] != null && StringUtils.equals(
                    checkId,
                    adapter.list[i].baseExtra.id
                )
            ) {
                runWaitUI {
                    try {
                        if (i >= adapter.list.size) {
                            //已经变了
                            return@runWaitUI
                        }
                        if (event.action == ItemModifyEvent.Action.ADD) {
                            //加元素
                            val index = if (event.isAfter) i + 1 else i
                            if (CollectionUtil.isNotEmpty(event.list)) {
                                //批量添加
                                adapter.list.addAll(index, event.list)
                                adapter.notifyRangeInserted(index, event.list.size)
                            } else if (event.articleList != null) {
                                adapter.list.add(index, event.articleList)
                                adapter.notifyInserted(index)
                            }
                            return@runWaitUI
                        }
                        if (event.action == ItemModifyEvent.Action.DELETE) {
                            //删元素
                            val articleList = adapter.list.removeAt(i)
                            adapter.notifyRemoved(articleList, i)
                            return@runWaitUI
                        }
                        //更新元素
                        val newItem = event.articleList
                        val oldItem = adapter.list[i]
                        if (StringUtils.equals(
                                event.anchorId,
                                oldItem.baseExtra.id
                            )
                        ) {
                            if (newItem.title != null) {
                                oldItem.title = newItem.title
                            }
                            if (newItem.url != null) {
                                oldItem.url = newItem.url
                            }
                            if (newItem.type != null) {
                                oldItem.type = newItem.type
                            }
                            if (newItem.desc != null) {
                                oldItem.desc = newItem.desc
                            }
                            if (newItem.pic != null) {
                                oldItem.pic = newItem.pic
                            }
                            if (newItem.extra != null) {
                                oldItem.extra = newItem.extra
                            }
                            adapter.notifyChanged(i)
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                return
            }
        }
    }

    @Subscribe
    fun onShowFileChooser(event: ShowFileChooserEvent) {
        if (webViewHolder !== event.webViewHolder) {
            return
        }
        filePathCallback = event.filePathCallback
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        startActivityForResult(Intent.createChooser(i, "Chooser"), 9091)
    }

    /**
     * 将段落变成多个item，方便记忆进度
     *
     * @param data
     */
    private fun updateReadData(refresh: Boolean, data: MutableList<ArticleList>) {
        if (!isReadTheme || CollectionUtil.isEmpty(data)) {
            return
        }
        synchronized(readMap) {
            val newData: MutableList<ArticleList> =
                java.util.ArrayList()
            for (articleList in data) {
                if (ArticleColTypeEnum.RICH_TEXT == ArticleColTypeEnum.getByCode(articleList!!.type)) {
                    if (StringUtil.isNotEmpty(articleList.title)) {
                        newData.addAll(splitRichText(articleList))
                    } else {
                        newData.add(articleList)
                    }
                } else {
                    newData.add(articleList)
                }
            }
            data.clear()
            data.addAll(newData)
            var lastSize = adapter.list.size
            if (refresh) {
                readMap.clear()
                lastSize = 0
            }
            val pageData = ReadPageData()
            pageData.size = newData.size
            pageData.preSize = lastSize
            pageData.pageNow = page
            for (i in lastSize until newData.size + lastSize) {
                readMap[i] = pageData
            }
            try {
                //足迹
                if (activity is MiniProgramActivity) {
                    val activity = activity as MiniProgramActivity
                    val autoPageData: AutoPageData? = activity.getParentData()
                    if (autoPageData != null && page <= autoPageData.nextData.size) {
                        val articleList = autoPageData.nextData[page - 1]
                        val pos = autoPageData.currentPos + page - 1
                        val click = articleList.title
                        if (StringUtil.isEmpty(click) || click.length > 25) {
                            if (click.length > 100 || !isChapterType(articleList)) {
                                return
                            }
                        }
                        pageData.chapterText = click
                        pageData.chapterPos = pos
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun splitRichText(articleList: ArticleList): List<ArticleList> {
        val text = articleList.title
        val strings = text.split("<br>").toTypedArray()
        if (strings.size <= 1) {
            return listOf(articleList)
        }
        val contents: MutableList<String> = java.util.ArrayList()
        for (s in strings) {
            if (StringUtil.trimAll(s).isNotEmpty()) {
                contents.add(s)
            }
        }
        return contents.map {
            try {
                val at = articleList.clone()
                at.title = it
                return@map at
            } catch (e: Throwable) {
                e.printStackTrace()
                return@map null
            }
        }.filterNotNull().toList()
    }

    private fun updateReadLayout(textConfig: TextConfig, refresh: Boolean, data: List<ArticleList>) {
        if (!isReadTheme || data.isNullOrEmpty()) {
            return
        }
        runOnUI {
            try {
                if (read_bottom_bg == null) {
                    findView<View>(R.id.read_layout).visibility = View.VISIBLE
                    read_bottom_bg = findView(R.id.read_bottom_bg)
                    read_chapter = findView(R.id.read_chapter)
                    read_time = findView(R.id.read_time)
                    read_power = findView(R.id.read_power)
                    read_chapter?.setTextColor(textConfig.textColor)
                    read_time?.setTextColor(textConfig.textColor)
                    read_power?.setTextColor(textConfig.textColor)
                    ThreadTool.cancelTasks(this)
                    ThreadTool.loadScheduleTask(this, 1000) { this.updateReadMsg() }
                    read_bottom_bg?.post {
                        val bottomHeight = read_bottom_bg!!.measuredHeight
                        val body =
                            findView<FrameLayout>(R.id.bodyContainer)
                        body.setPadding(0, 0, 0, bottomHeight)
                    }
                    read_bottom_bg?.setOnClickListener {
                        showReadMenu()
                    }
                }
//                updateReadChapter(-1)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateReadMsg() {
        if (activity == null || requireActivity().isFinishing) {
            return
        }
        memoryTask?.let {
            HeavyTaskUtil.postTaskToQueue(it)
        }
        runOnUI {
            if (activity == null || requireActivity().isFinishing) {
                return@runOnUI
            }
            val currentTime = Date()
            val formatter = SimpleDateFormat("HH:mm")
            read_time!!.text = formatter.format(currentTime)
            val manager =
                requireActivity().getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
            if (manager != null) {
                val cap =
                    manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                read_power!!.text = "$cap%"
            }
        }
    }

    private fun updateReadChapter(pageNow0: Int) {
        var pageNow = pageNow0
        try {
            if (read_chapter != null && activity is MiniProgramActivity) {
                val activity = activity as MiniProgramActivity
                val pageData: AutoPageData? = activity.getParentData()
                if (pageData != null) {
                    if (pageNow < 0) {
                        //不是主动传的，取页面的页数
                        pageNow = Math.max(0, page - 2)
                    }
                    val now = pageData.nextData[pageNow]
                    read_chapter?.text = DetailUIHelper.getTitleText(now.title, false)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun scrollPage(down: Boolean): Boolean {
        if (isReadTheme) {
            scrollPageNow(down)
            return true
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!readAloud) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> return scrollPage(true)
                KeyEvent.KEYCODE_VOLUME_UP -> return scrollPage(false)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            9090 -> if (highlightCallback != null) {
                highlightCallback!!.accept(SettingConfig.highlightRule)
                highlightCallback = null
                SettingConfig.highlightRule = ""
            }
            9091 -> {
                if (filePathCallback != null) {
                    if (resultCode == RESULT_OK) {
                        val result = data?.data
                        filePathCallback!!.onReceiveValue(arrayOf(result))
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        filePathCallback!!.onReceiveValue(null)
                    }
                    filePathCallback = null
                }
            }
            9093 -> {
                if (StringUtil.isNotEmpty(fileSelectJs) && resultCode == RESULT_OK) {
                    val uri = data?.data ?: return
                    val finalJS: String = fileSelectJs!!
                    fileSelectJs = null
                    val fileName = "_fileSelect_" + UriUtils.getFileName(uri)
                    loading("文件提取中，请稍候", true)
                    val copyTo =
                        UriUtils.getRootDir(context) + File.separator + "cache" + File.separator + fileName
                    FileUtil.makeSureDirExist(copyTo)
                    UriUtils.getFilePathFromURI(
                        context,
                        uri,
                        copyTo,
                        object : UriUtils.LoadListener {
                            override fun success(s: String) {
                                if (activity != null && !activity!!.isFinishing) {
                                    HeavyTaskUtil.executeNewTask {
                                        val result = JSEngine.getInstance().evalJS(
                                            JSEngine.getMyRule(articleListRule)
                                                    + JSEngine.getInstance().generateMY(
                                                "MY_URL",
                                                Utils.escapeJavaScriptString(
                                                    myUrl
                                                )
                                            )
                                                    + finalJS,
                                            s
                                        )
                                        if (activity == null || requireActivity().isFinishing) {
                                            return@executeNewTask
                                        }
                                        loading(false)
                                        runOnUi {
                                            if (!result.isNullOrEmpty() && "undefined" != result) {
                                                dealUrlPos(
                                                    null,
                                                    fileSelectPosition,
                                                    result,
                                                    false,
                                                    ""
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            override fun failed(msg: String) {}
                        })
                }
            }
        }
    }
}