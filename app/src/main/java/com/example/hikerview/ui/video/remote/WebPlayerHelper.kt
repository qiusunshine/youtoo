package com.example.hikerview.ui.video.remote

import android.text.TextUtils
import android.view.View
import com.example.hikerview.constants.PreferenceConstant
import com.example.hikerview.constants.RemotePlayConfig
import com.example.hikerview.service.parser.HttpParser
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.video.PlayerChooser
import com.example.hikerview.ui.video.model.PlayData
import com.example.hikerview.ui.webdlan.LocalServerParser
import com.example.hikerview.ui.webdlan.RemoteServerManager
import com.example.hikerview.utils.ClipboardUtil
import com.example.hikerview.utils.PreferenceMgr
import com.google.android.material.snackbar.Snackbar
import com.yanzhenjie.andserver.Server.ServerListener

/**
 * 作者：By 15968
 * 日期：On 2021/10/21
 * 时间：At 20:26
 */
object WebPlayerHelper {

    fun start(view: View, playData: PlayData, url: String, showToast: Boolean, forRedirect: Boolean, generator: WebPlayGenerator) {
        Snackbar.make(view, "努力为您加载中，请稍候", Snackbar.LENGTH_SHORT).show()
        try {
            if (RemotePlayConfig.playerPath == RemotePlayConfig.WEBS) {
                RemotePlayConfig.playerPath = RemotePlayConfig.D_PLAYER_PATH
                RemoteServerManager.instance().destroyServer()
            }
            RemoteServerManager.instance().startServer(view.context, object : ServerListener {
                override fun onStarted() {
                    val videoUrl = LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(url))
                    var playUrl = RemoteServerManager.instance().getServerUrl(view.context)
                    if (forRedirect) {
                        playUrl = "$playUrl/redirectPlayUrl"
                    }
                    val urlDTO = generator.generate(playUrl)
                    urlDTO.apply {
                        setUrl(videoUrl)
                        headers = HttpParser.getHeaders(url)
                        danmu = playData.danmu
                        subtitle = playData.subtitle
                    }
                    RemoteServerManager.instance().urlDTO = urlDTO
                    if (TextUtils.isEmpty(playUrl)) {
                        Snackbar.make(view, "出现错误：链接为空！", Snackbar.LENGTH_LONG).show()
                        return
                    }
                    val dlanCopyUrl = PreferenceMgr.getBoolean(view.context, PreferenceConstant.FILE_SETTING_CONFIG, "dlanCopyUrl", true)
                    if (dlanCopyUrl) {
                        ClipboardUtil.copyToClipboard(view.context, playUrl, showToast)
                    }
                    if (forRedirect) {
                        val text = if (dlanCopyUrl) "已复制链接，请在电脑上用PotPlayer等第三方播放器打开：$playUrl" else "链接: $playUrl，请在电脑上用PotPlayer等第三方播放器打开"
                        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
                    } else {
                        val text = if (dlanCopyUrl) "已复制链接，请在同一WiFi下的电脑或者电视上打开：$playUrl" else "链接: $playUrl，请在同一WiFi下的电脑或者电视上打开"
                        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onStopped() {}
                override fun onException(e: Exception) {
                    Snackbar.make(view, "出现错误：" + e.message, Snackbar.LENGTH_LONG).show()
                }
            })
        } catch (e: Exception) {
            Snackbar.make(view, "出现错误：" + e.message, Snackbar.LENGTH_LONG).show()
        }
    }
}