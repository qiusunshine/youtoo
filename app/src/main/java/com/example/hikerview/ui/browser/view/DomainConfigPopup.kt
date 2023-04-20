package com.example.hikerview.ui.browser.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.hikerview.R
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.browser.model.UAModel
import com.example.hikerview.ui.browser.service.DomainConfigService
import com.example.hikerview.utils.PreferenceMgr
import com.example.hikerview.utils.ToastMgr
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.suke.widget.SwitchButton

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
class DomainConfigPopup : BottomPopupView {
    private var dom: String = ""

    constructor(context: Context) : super(context) {}
    constructor(activity: Activity, dom: String?) : super(activity) {
        this.dom = dom ?: ""
    }

    // 返回自定义弹窗的布局
    override fun getImplLayoutId(): Int {
        return R.layout.view_domain_config_popup
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    override fun onCreate() {
        super.onCreate()
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = "网站配置($dom)"
        val disableAdBlock = findViewById<SwitchButton>(R.id.disableAdBlock)
        val disableForceBlock = findViewById<SwitchButton>(R.id.disableForceBlock)
        val disableJsPlugin = findViewById<SwitchButton>(R.id.disableJsPlugin)
        val disableXiuTan = findViewById<SwitchButton>(R.id.disableXiuTan)
        val disableFloatVideo = findViewById<SwitchButton>(R.id.disableFloatVideo)
        val downloadStrongPrompt = findViewById<SwitchButton>(R.id.downloadStrongPrompt)
        val downloadNoConfirm = findViewById<SwitchButton>(R.id.downloadNoConfirm)
        val openAppSetting = findViewById<View>(R.id.openAppSetting)
        val disableGetLocation = findViewById<SwitchButton>(R.id.disableGetLocation)
        val disableForceNewWindow = findViewById<SwitchButton>(R.id.disableForceNewWindow)
        val disablePageCache = findViewById<SwitchButton>(R.id.disablePageCache)
        val customUa = findViewById<RelativeLayout>(R.id.customUa)
        val domainConfig = DomainConfigService.getConfig(dom)
        domainConfig.dom = dom

        disableAdBlock.isChecked = domainConfig.disableAdBlock == true
        disableAdBlock.setOnCheckedChangeListener { _, isChecked ->
            domainConfig.disableAdBlock = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        disableForceBlock.isChecked = domainConfig.disableForceBlock == true
        disableForceBlock.setOnCheckedChangeListener { _, isChecked ->
            if (!PreferenceMgr.getBoolean(Application.application, "forceBlock", false)) {
                ToastMgr.shortBottomCenter(context, "开启强力拦截模式后此选项才生效哦")
            }
            domainConfig.disableForceBlock = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        disableJsPlugin.isChecked = domainConfig.disableJsPlugin == true
        disableJsPlugin.setOnCheckedChangeListener { _, isChecked ->
            domainConfig.disableJsPlugin = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        disableXiuTan.isChecked = domainConfig.disableXiuTan == true
        disableXiuTan.setOnCheckedChangeListener { _, isChecked ->
            domainConfig.disableXiuTan = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        disableFloatVideo.isChecked = domainConfig.disableFloatVideo == true
        disableFloatVideo.setOnCheckedChangeListener { _, isChecked ->
            if (!PreferenceMgr.getBoolean(context, "floatVideo", false)) {
                ToastMgr.shortBottomCenter(context, "开启悬浮嗅探播放后此选项才生效哦")
            }
            domainConfig.disableFloatVideo = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        downloadStrongPrompt.isChecked = domainConfig.downloadStrongPrompt == true
        downloadStrongPrompt.setOnCheckedChangeListener { _, isChecked ->
            domainConfig.downloadStrongPrompt = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        downloadNoConfirm.isChecked = domainConfig.d1 == true
        downloadNoConfirm.setOnCheckedChangeListener { _, isChecked ->
            domainConfig.d1 = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        openAppSetting.setOnClickListener {
            var mode = 1
            when {
                domainConfig.openAppMode == 2 -> {
                    mode = 3
                }
                domainConfig.openAppMode == 1 -> {
                    mode = 2
                }
                domainConfig.disableOpenApp == true -> {
                    mode = 0
                }
            }
            XPopup.Builder(context)
                .asBottomList(
                    null,
                    arrayOf("禁止唤起应用", "允许唤起，提示两次", "允许唤起，总是提示", "直接唤起，无需提示"),
                    null,
                    mode
                ) { position, _ ->
                    when (position) {
                        0 -> {
                            domainConfig.disableOpenApp = true
                            domainConfig.openAppMode = 0
                        }
                        1 -> {
                            domainConfig.disableOpenApp = false
                            domainConfig.openAppMode = 0
                        }
                        2 -> {
                            domainConfig.disableOpenApp = false
                            domainConfig.openAppMode = 1
                        }
                        3 -> {
                            domainConfig.disableOpenApp = false
                            domainConfig.openAppMode = 2
                        }
                    }
                    DomainConfigService.configMap[dom] = domainConfig
                    DomainConfigService.restoreConfig()
                    ToastMgr.shortBottomCenter(context, "设置成功")
                }.show()
        }
        disableGetLocation.isChecked = domainConfig.disableGetLocation == true
        disableGetLocation.setOnCheckedChangeListener { _, isChecked ->
            domainConfig.disableGetLocation = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        disableForceNewWindow.isChecked = domainConfig.disableForceNewWindow == true
        disableForceNewWindow.setOnCheckedChangeListener { _, isChecked ->
            if (!PreferenceMgr.getBoolean(context, "forceNewWindow", false)) {
                ToastMgr.shortBottomCenter(context, "开启强制新窗口打开后此选项才生效哦")
            }
            domainConfig.disableForceNewWindow = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        disablePageCache.isChecked = domainConfig.disablePCache == true
        disablePageCache.setOnCheckedChangeListener { _, isChecked ->
            if (!PreferenceMgr.getBoolean(context, "pageCache", false)) {
                ToastMgr.shortBottomCenter(context, "开启返回不重载后此选项才生效哦")
            }
            domainConfig.disablePCache = isChecked
            DomainConfigService.configMap[dom] = domainConfig
            DomainConfigService.restoreConfig()
        }
        customUa.setOnClickListener {
            UAModel.showUpdateOrAddDialog(context, dom) { ua: String? ->

            }
        }
    }

    override fun show(): BasePopupView {
        return super.show()
    }

    override fun getPopupHeight(): Int {
        return (XPopupUtils.getScreenHeight(context) * .85f).toInt()
    }

    override fun getMaxHeight(): Int {
        return (XPopupUtils.getScreenHeight(context) * .85f).toInt()
    }
}