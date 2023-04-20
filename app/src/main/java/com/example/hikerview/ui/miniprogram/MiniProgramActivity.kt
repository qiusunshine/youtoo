package com.example.hikerview.ui.miniprogram

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.hikerview.R
import com.example.hikerview.event.home.*
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.base.BaseActivity
import com.example.hikerview.ui.browser.util.UUIDUtil
import com.example.hikerview.ui.home.model.TextConfig
import com.example.hikerview.ui.home.view.ClickArea
import com.example.hikerview.ui.miniprogram.data.AutoPageData
import com.example.hikerview.ui.miniprogram.data.RuleDTO
import com.example.hikerview.ui.miniprogram.interfaces.ArticleListIsland
import com.example.hikerview.ui.miniprogram.logs.LogsPopup
import com.example.hikerview.ui.miniprogram.service.HistoryMemoryService
import com.example.hikerview.ui.setting.file.FileDetailAdapter
import com.example.hikerview.ui.setting.file.FileDetailPopup
import com.example.hikerview.ui.setting.model.SettingConfig
import com.example.hikerview.ui.setting.office.MiniProgramOfficer
import com.example.hikerview.utils.*
import com.example.hikerview.utils.view.loadDominantColor
import com.github.mmin18.widget.MyRealtimeBlurView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import com.smarx.notchlib.INotchScreen.NotchScreenInfo
import com.smarx.notchlib.NotchScreenManager
import org.apache.commons.lang3.StringUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 17:05
 */
const val TEXT_CONFIG_IMG_CODE = 111

class MiniProgramActivity : BaseActivity(), ArticleListIsland {
    private var miniProgramFragment: MiniProgramFragment? = null
    private var loadingPopupView: LoadingPopupView? = null
    var isOnPause = false
    var ruleDTO: RuleDTO? = null
    var pageTitle = ""
    private var touchX: Float = 0F
    private var touchY: Float = 0F
    private var globalLoadingView: LoadingPopupView? = null
    private var toolbarIm: Toolbar? = null
    private var toolbar_im_bg: MyRealtimeBlurView? = null
    private var overallXScroll = 0
    private var toolbarHeight = 0
    private var background: String? = null
    private var gestureDetector: GestureDetector? = null
    private var homeBlur = false
    private var hasFirstLoad = false
    private var parentData: AutoPageData? = null
    private var parentDataLoaded = false
    private var myMenu: Menu? = null
    private var ruleServiceStarted = false

    override fun initLayout(savedInstanceState: Bundle?): Int {
        return R.layout.activity_mini_program
    }

    override fun initView() {
        if (intent.getBooleanExtra("RuleForegroundService", false)) {
            finish()
            return
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        ruleDTO = DataTransferUtils.loadCache<RuleDTO>(intent.getStringExtra("rule")!!)
        pageTitle = intent.getStringExtra("title") ?: ""
        if (pageTitle.isNotEmpty()) {
            title = pageTitle
        }
        toolbarIm = findView(R.id.toolbar_im)
        toolbarIm?.postDelayed({
            if (!isFinishing) {
                if (myMenu != null && (pageTitle == "编辑规则" || pageTitle == "新增规则") &&
                    ruleDTO?.rule?.contains("UA标识，mobile/pc/自定义") == true
                ) {
                    myMenu?.findItem(R.id.about)?.title = "保存规则"
                } else if (myMenu != null && (pageTitle == "奇妙工具箱")) {
                    myMenu?.findItem(R.id.about)?.title = "加到主页"
                }
            }
        }, 500)
        homeBlur = PreferenceMgr.getBoolean(context, "homeBlur", true)
        setDoubleTab(toolbarIm!!)
        setDoubleTab(findView<View>(R.id.toolbar))
        toolbar_im_bg = findView(R.id.toolbar_im_bg)
        miniProgramFragment = MiniProgramFragment(ruleDTO!!, pageTitle)
        miniProgramFragment?.setReadTheme(isReadTheme())
        val immersiveOrFullTheme = isImmersiveTheme() || isFullTheme()
        val requireBackground = isRequireBackgroundService()
        miniProgramFragment?.setImmersiveTheme(immersiveOrFullTheme)
        miniProgramFragment?.initAutoCache(pageTitle, ruleDTO?.url)
        miniProgramFragment?.setLoadListener(object : MiniProgramFragment.LoadListener {
            override fun complete() {
                if (isImmersiveTheme()) {
                    runOnUiThread {
                        if (!hasFirstLoad) {
                            hasFirstLoad = true
                            AndroidBarUtils.setTranslucentStatusBar2(activity, false)
                            changeToImmersive()
                        }
                    }
                }
            }
        })

        if (isImmersiveTheme()) {
            miniProgramFragment?.scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && toolbarHeight > 0) {
                        //强行纠正overallXScroll，因为onScrolled里面的判断有时候不准确
                        if (miniProgramFragment?.isAtTop() == true) {
                            overallXScroll = 0
                            adjustToolbarImBg()
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (toolbarHeight > 0) {
                        overallXScroll = overallXScroll + dy // 累加y值 解决滑动一半y值为0
                        adjustToolbarImBg()
                    }
                }
            }
        }
        ScreenUtil.setDisplayInNotch(this)
        if (immersiveOrFullTheme) {
            showImmersive()
        } else {
            val toolbar = findView<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            }
        }
        supportFragmentManager
            .beginTransaction()
            .add(R.id.frame_bg, miniProgramFragment!!)
            .commit()
        if (requireBackground) {
            ruleServiceStarted = Application.application.startRuleForegroundService(
                context
            )
        }
    }

    private fun changeToImmersive() {
        showNav()
        toolbarIm!!.setTitleTextColor(resources.getColor(R.color.white))
        toolbarIm!!.setNavigationIcon(R.drawable.ico_action_back_white)
    }

    private fun setDoubleTab(view: View) {
        if (gestureDetector == null) {
            gestureDetector =
                GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (miniProgramFragment != null) {
                            if (miniProgramFragment?.isAtTop() == true) {
                                miniProgramFragment?.scrollBottomSmooth()
                                if (isImmersiveTheme()) {
                                    //overallXScroll无法得知，简单显示背景
                                    overallXScroll = ScreenUtil.getScreenHeight2(activity)
                                    if (hasX5InView()) {
                                        homeBlur = false
                                    }
                                    toolbar_im_bg?.visibility = View.VISIBLE
                                    toolbar_im_bg?.setBlurRadius(
                                        if (homeBlur) toolbar_im_bg?.defRadius ?: 0F else 0F
                                    )
                                }
                            } else {
                                miniProgramFragment?.scrollTopSmooth()
                                if (isImmersiveTheme()) {
                                    overallXScroll = 0
                                    adjustToolbarImBg()
                                }
                            }
                        }
                        return super.onDoubleTap(e)
                    }
                })
        }
        view.setOnTouchListener { v: View?, event: MotionEvent? ->
            gestureDetector?.onTouchEvent(
                event
            )!!
        }
    }

    private fun adjustToolbarImBg() {
        if (hasX5InView()) {
            homeBlur = false
        }
        if (overallXScroll <= 0) {   //设置标题的背景颜色
            toolbar_im_bg!!.visibility = View.GONE
            toolbar_im_bg!!.setBlurRadius(0f)
        } else if (overallXScroll <= toolbarHeight) { //滑动距离小于banner图的高度时，设置背景和字体颜色颜色透明度渐变
            toolbar_im_bg!!.visibility = View.VISIBLE
            toolbar_im_bg!!.setBlurRadius(if (homeBlur) toolbar_im_bg!!.defRadius else 0F)
        } else {
            toolbar_im_bg!!.visibility = View.VISIBLE
            toolbar_im_bg!!.setBlurRadius(if (homeBlur) toolbar_im_bg!!.defRadius else 0F)
        }
    }

    private fun hasX5InView(): Boolean {
        return if (miniProgramFragment?.getWebViewHolder() != null && miniProgramFragment!!.getWebViewHolder()?.webView != null
        ) {
            //没加载地址暂时不管
            StringUtil.isNotEmpty(miniProgramFragment!!.getWebViewHolder()?.webView?.url)
        } else false
    }

    private fun isNight(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            PreferenceMgr.getBoolean(context, "forceDark", true)
        } else false
    }

    private fun showNav() {
        try {
            AndroidBarUtils.isNavigationBarExist(this) { show, h ->
                if (show && h > 0) {
                    val frameLayout =
                        findView<FrameLayout>(R.id.body)
                    frameLayout.setPadding(0, 0, 0, h)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                    window.navigationBarColor = getNavBarColor()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isNight()) {
                        window.decorView.systemUiVisibility = window.decorView
                            .systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getNavBarColor(): Int {
        return resources.getColor(R.color.film_nav_bg)
    }

    fun showImmersive() {
        val fullTheme = isFullTheme()
        val body = findView<FrameLayout>(R.id.body)
        body.background = null
        //ToolBar、自定义的TitleBar 重叠问题以及适配刘海屏
        if (!fullTheme) {
            AndroidBarUtils.setTranslucentStatusBar2(this, true)
            setBarPaddingTopForFrameLayout(this, toolbarIm!!)
            showNav()
        } else {
            if (isReadTheme()) {
                val textConfig: TextConfig = SettingConfig.initTextConfig(context)
                background = textConfig.background
                updateBg()
            }
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            NotchScreenManager.getInstance().setDisplayInNotch(this)
            if (!isGameTheme()) {
                NotchScreenManager.getInstance().getNotchInfo(
                    this
                ) { notchScreenInfo: NotchScreenInfo ->
                    if (notchScreenInfo.hasNotch) {
                        var notchHeight = 0
                        for (rect in notchScreenInfo.notchRects) {
                            Timber.i("notch screen Rect =  %s", rect.toShortString())
                            if (notchHeight < rect.height()) {
                                notchHeight = rect.height()
                            }
                        }
                        if (notchHeight > 0) {
                            val body1 =
                                findView<FrameLayout>(R.id.body)
                            body1.setPadding(0, notchHeight, 0, 0)
                        }
                    }
                }
            } else {
                val view_game_close = findView<View>(R.id.view_game_close)
                view_game_close.visibility = View.VISIBLE
                view_game_close.setOnClickListener { v: View? ->
                    XPopup.Builder(context)
                        .atView(v)
                        .asAttachList(
                            arrayOf("退出页面"), null
                        ) { position: Int, text: String? ->
                            when (text) {
//                                "收藏页面" -> onOptionsItemSelected(myMenu.findItem(R.id.collect))
                                "退出页面" -> finish()
                            }
                        }
                        .show()
                }
            }
        }
        val toolbar = findView<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE
        if (!fullTheme) {
            toolbarIm?.visibility = View.VISIBLE
        }
        setSupportActionBar(toolbarIm)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        toolbarIm?.setTitleTextColor(resources.getColor(R.color.blackText2))
        toolbarIm?.setNavigationIcon(R.drawable.ico_action_back_black)
    }

    private fun setBarPaddingTopForFrameLayout(context: Activity, view: View) {
        try {
            val statusBarHeight = AndroidBarUtils.getStatusBarHeight(context)
            val layoutParams = view.layoutParams as FrameLayout.LayoutParams
            layoutParams.setMargins(
                layoutParams.leftMargin,
                layoutParams.topMargin + statusBarHeight,
                layoutParams.rightMargin,
                layoutParams.bottomMargin
            )
            view.layoutParams = layoutParams
            toolbarHeight = DisplayUtil.dpToPx(getContext(), 44) + statusBarHeight
            val layoutParams1 = toolbar_im_bg!!.layoutParams as FrameLayout.LayoutParams
            layoutParams1.height = toolbarHeight
            toolbar_im_bg!!.layoutParams = layoutParams1
            toolbar_im_bg!!.setPlaceholderId(R.drawable.shape_half_trans)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mini_program_options, menu)
        myMenu = menu
        return true
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
            R.id.about ->
                if (item.title == "保存规则") {
                    miniProgramFragment?.clickSaveRuleBtn()
                } else if (item.title == "加到主页") {
                    MiniProgramOfficer.addToShortcut(context, ruleDTO!!)
                } else {
                    ToastMgr.shortBottomCenter(context, "还没写")
                }
            R.id.showRule -> {
                val popup = FileDetailPopup(this, "页面规则", getDetailData())
                    .withClickListener(object : FileDetailAdapter.OnClickListener {
                        override fun click(text: String) {
                            if (text.startsWith("页面链接")) {
                                ClipboardUtil.copyToClipboard(context, ruleDTO?.url, false)
                                ToastMgr.shortCenter(context, "页面链接已复制到剪贴板")
                            }
                        }

                        override fun longClick(view: View?, text: String) {

                        }
                    })
                XPopup.Builder(context)
                    .asCustom(popup)
                    .show()
            }
            R.id.showLogs -> {
                XPopup.Builder(activity)
                    .enableDrag(false)
                    .autoOpenSoftInput(false)
                    .moveUpToKeyboard(false)
                    .asCustom(LogsPopup(activity))
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getDetailData(): Array<String> {
        return arrayOf(
            "页面链接：" + ruleDTO?.url,
            "显示样式：" + ruleDTO?.col_type,
            "UA标识：" + ruleDTO?.ua,
            "解析规则：" + ruleDTO?.rule,
        )
    }

    override fun onBackPressed() {
        if (miniProgramFragment?.onBackPressed() == true) {
            //被fragment消费了
            return
        }
        super.onBackPressed()
    }

    companion object {
        fun startPage(context: Context, ruleDTO: RuleDTO) {
            val intent = Intent(context, MiniProgramActivity::class.java)
            val fileName = UUIDUtil.genUUID()
            DataTransferUtils.putCache(ruleDTO, fileName)
            intent.putExtra("rule", fileName)
            context.startActivity(intent)
        }
    }

    override fun hideLoading() {
        loadingPopupView?.dismiss()
    }

    override fun showLoading(text: String) {
        if (loadingPopupView == null) {
            loadingPopupView = XPopup.Builder(activity).asLoading(text)
        } else {
            loadingPopupView?.setTitle(text)
        }
        loadingPopupView?.show()
    }

    override fun onResume() {
        super.onResume()
        isOnPause = false
        miniProgramFragment?.showLastClickItem()
    }

    override fun onPause() {
        super.onPause()
        isOnPause = true
        if (globalLoadingView != null && globalLoadingView?.isShow == true) {
            globalLoadingView?.dismiss()
        }
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        if (ruleServiceStarted) {
            Application.application.stopRuleForegroundService()
        }
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSetTitle(event: SetPageTitleEvent) {
        if (isOnPause || isFinishing) {
            return
        }
        if (event.title == null) {
            return
        }
        title = event.title
        //需要更新一下历史记录的标题
        intent.putExtra("title", event.title)
        HistoryMemoryService.updatePage(ruleDTO!!, pageTitle, event.title)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSetPic(event: SetPagePicEvent) {
        if (isOnPause || isFinishing) {
            return
        }
        if (event.url == null) {
            return
        }
        intent.putExtra("picUrl", event.url)
        HistoryMemoryService.updatePic(ruleDTO!!, pageTitle, event.url)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSetParams(event: SetPageParamsEvent) {
        if (isOnPause || isFinishing) {
            return
        }
        if (event.params == null || ruleDTO == null || miniProgramFragment == null) {
            return
        }
        ruleDTO?.params = event.params
        miniProgramFragment?.updateParams(event.params)
        HistoryMemoryService.updateParams(ruleDTO!!, pageTitle, event.params)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoading(event: LoadingEvent) {
        if (isOnPause) {
            return
        }
        if (event.isShow) {
            if (globalLoadingView != null && globalLoadingView?.isShow == true) {
                globalLoadingView!!.setTitle(event.text)
                return
            }
            globalLoadingView = XPopup.Builder(context)
                .asLoading(event.text)
            globalLoadingView!!.show()
        } else if (globalLoadingView != null) {
            globalLoadingView?.dismiss()
            globalLoadingView = null
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        touchX = ev.rawX
        touchY = ev.rawY
        return super.dispatchTouchEvent(ev)
    }

    fun getTouchArea(): ClickArea {
        val manager = windowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        val height = outMetrics.heightPixels
        val width = outMetrics.widthPixels
        return when {
            touchY < height.toFloat() / 3 -> {
                ClickArea.TOP
            }
            touchY > height.toFloat() * 2 / 3 -> {
                ClickArea.BOTTOM
            }
            else -> {
                if (touchX < width.toFloat() / 4) {
                    return ClickArea.CENTER_LEFT
                } else if (touchX > width.toFloat() * 3 / 4) {
                    return ClickArea.CENTER_RIGHT
                }
                ClickArea.CENTER
            }
        }
    }

    private fun isImmersiveTheme(): Boolean {
        return (ruleDTO != null && StringUtil.isNotEmpty(ruleDTO!!.url)
                && ruleDTO!!.url!!.contains("#immersiveTheme#"))
    }

    private fun isReadTheme(): Boolean {
        return (ruleDTO != null && StringUtil.isNotEmpty(ruleDTO!!.url)
                && ruleDTO!!.url!!.contains("#readTheme#"))
    }

    private fun isGameTheme(): Boolean {
        return (ruleDTO != null && StringUtil.isNotEmpty(ruleDTO!!.url)
                && ruleDTO!!.url!!.contains("#gameTheme#"))
    }

    private fun isFullTheme(): Boolean {
        return (ruleDTO != null && StringUtil.isNotEmpty(ruleDTO!!.url)
                && ruleDTO!!.url!!.contains("#fullTheme#")) || isReadTheme() || isGameTheme()
    }

    private fun isRequireBackgroundService(): Boolean {
        return (ruleDTO != null && StringUtil.isNotEmpty(ruleDTO!!.url)
                && ruleDTO!!.url!!.contains("#background#"))
    }

    private fun updateBg() {
        if (background!!.startsWith("/") || background!!.startsWith("file://") || background!!.startsWith(
                "http"
            )
        ) {
            Glide.with(context)
                .asDrawable()
                .load(background)
                .into(object : CustomTarget<Drawable?>() {
                    override fun onLoadStarted(placeholder: Drawable?) {}

                    override fun onLoadCleared(placeholder: Drawable?) {
                        try {
                            if (placeholder is GifDrawable) {
                                placeholder.stop()
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        try {
                            if (resource is GifDrawable) {
                                resource.setLoopCount(GifDrawable.LOOP_FOREVER)
                                resource.start()
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        findView<View>(R.id.viewBg).background = resource
                    }
                })
            loadDominantColor(context, background!!) {
                window.navigationBarColor = it
            }
            return
        }
        val color = Color.parseColor(background)
        findView<View>(R.id.viewBg).setBackgroundColor(color)
        window.navigationBarColor = color
    }

    fun refreshBackground(bg: String) {
        if (isReadTheme() && !StringUtils.equals(background, bg)) {
            background = bg
            updateBg()
        }
    }


    fun getParentData(): AutoPageData? {
        if (!parentDataLoaded) {
            parentDataLoaded = true
            parentData = DataTransferUtils.loadTemp<AutoPageData>()
        }
        return parentData
    }

    fun setParentData(parentData: AutoPageData) {
        this.parentData = parentData
    }

    fun background() {
        if (intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0) {
            //是新开的窗口
            moveTaskToBack(true)
            ToastMgr.shortBottomCenter(context, "已后台，通过系统多任务界面可以回到当前窗口")
        } else {
            ToastMgr.shortBottomCenter(context, "当前页面不支持退到后台")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEXT_CONFIG_IMG_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data!!.data
                UriUtils.getFilePathFromURI(context, uri, object : UriUtils.LoadListener {
                    override fun success(s: String) {
                        val a = File(s)
                        if (!isFinishing && a.exists()) {
                            runOnUiThread {
                                val zipFilePath =
                                    UriUtils.getRootDir(context) + File.separator + "images" + File.separator + a.name
                                val dir =
                                    File(UriUtils.getRootDir(context) + File.separator + "images")
                                if (!dir.exists()) {
                                    dir.mkdirs()
                                }
                                FileUtil.copy(a, File(zipFilePath))
                                a.delete()
                                background = zipFilePath
                                val textConfig = SettingConfig.getTextConfig(context)
                                textConfig.background = background
                                SettingConfig.updateTextConfig(context, textConfig)
                                updateBg()
                            }
                        }
                    }

                    override fun failed(msg: String) {
                        if (!isFinishing) {
                            runOnUiThread {
                                ToastMgr.shortBottomCenter(
                                    context,
                                    "出错：$msg"
                                )
                            }
                        }
                    }
                })
            }
        } else {
            if (resultCode == RESULT_OK && data?.getBooleanExtra("refreshPage", false) == true) {
                isOnPause = false
                val scrollTop = data.getBooleanExtra("scrollTop", false)
                EventBus.getDefault().post(OnRefreshPageEvent(scrollTop))
                return
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (miniProgramFragment != null && miniProgramFragment?.onKeyDown(
                keyCode,
                event
            ) == true
        ) {
            //被fragment消费了
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun finishWhenRestore(): Boolean {
        return isReadTheme()
    }
}