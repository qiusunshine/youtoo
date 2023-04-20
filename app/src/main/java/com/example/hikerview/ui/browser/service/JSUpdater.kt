package com.example.hikerview.ui.browser.service

import android.util.Base64
import com.alibaba.fastjson.JSON
import com.example.hikerview.model.BigTextDO
import com.example.hikerview.service.parser.HttpHelper
import com.example.hikerview.ui.ActivityManager
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.browser.model.JSManager
import com.example.hikerview.ui.browser.model.JSUpdateDTO
import com.example.hikerview.utils.*
import com.lxj.xpopup.XPopup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.adblockplus.libadblockplus.android.Utils
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.regex.Pattern

/**
 * 作者：By 15968
 * 日期：On 2023/1/8
 * 时间：At 11:36
 */

private val versionPattern = Pattern.compile("\\/\\/\\s*@version\\s+(.+)[\\s\\n]+")

fun saveGreasyUpdateInfo(fileName: String, js: String?, url: String?) {
    if (url.isNullOrEmpty()) {
        if (js.isNullOrEmpty()) {
            return
        }
        val u = findUpdateUrl(js)
        if (!u.isNullOrEmpty()) {
            saveGreasyUpdateUrl(fileName, u)
        }
    } else {
        saveGreasyUpdateUrl(fileName, url)
    }
}

fun findUpdateUrl(js: String): String? {
    val updateUrlPattern = Pattern.compile("\\/\\/\\s*@updateURL\\s+(.+)[\\s\\n]+")
    val m = updateUrlPattern.matcher(js)
    val u = if (m.find()) m.group(1) else null
    return if (!u.isNullOrEmpty()) {
        u
    } else {
        findDownloadUrl(js)
    }
}

fun findDownloadUrl(js: String): String? {
    val downloadUrlPattern = Pattern.compile("\\/\\/\\s*@downloadURL\\s+(.+)[\\s\\n]+")
    val m = downloadUrlPattern.matcher(js)
    return if (m.find()) m.group(1) else null
}

fun findVersion(js: String): String? {
    val m = versionPattern.matcher(js)
    return if (m.find()) m.group(1) else null
}

private fun saveGreasyUpdateUrl(fileName: String, url: String) {
    ThreadTool.async {
        val updateDTO = getGreasyUpdateDTO(fileName)
        updateDTO.url = url
        updateDTO.ct = 0L
        saveGreasyUpdateDTO(fileName, updateDTO)
    }
}

private fun saveGreasyUpdateDTO(fileName: String, dto: JSUpdateDTO) {
    val rule = Utils.escapeJavaScriptString(JSManager.getNameFromFileName(fileName))
    BigTextDO.setItem(rule, "_update0_", JSON.toJSONString(dto))
}

fun clearGreasyUpdateDTO(fileName: String) {
    val rule = Utils.escapeJavaScriptString(JSManager.getNameFromFileName(fileName))
    BigTextDO.removeItem(rule, "_update0_")
}

fun getGreasyUpdateDTO(fileName: String): JSUpdateDTO {
    val rule = Utils.escapeJavaScriptString(JSManager.getNameFromFileName(fileName))
    val s = BigTextDO.getItem(rule, "_update0_")
    var dto: JSUpdateDTO? = null
    if (StringUtil.isNotEmpty(s)) {
        try {
            dto = JSON.parseObject(s, JSUpdateDTO::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    if (dto == null) {
        dto = JSUpdateDTO()
    }
    return dto
}

fun checkUpdate(dom: String, fileName: String, urgent: Boolean = false) {
    checkUpdate(dom, fileName, urgent) { _, _, _, _, _ ->

    }
}

fun checkUpdate(
    dom: String,
    fileName: String,
    urgent: Boolean = false,
    callback: (oldVersion: String?, newVersion: String?, error: String?, updateDTO: JSUpdateDTO?, downloadUrl: String?) -> Unit
) {
    checkUpdate(dom, fileName, urgent, true, callback)
}

fun checkUpdate(
    dom: String,
    fileName: String,
    urgent: Boolean = false,
    postEvent: Boolean = true,
    callback: (oldVersion: String?, newVersion: String?, error: String?, updateDTO: JSUpdateDTO?, downloadUrl: String?) -> Unit
) {
    ThreadTool.async {
        try {
            val updateDTO = getGreasyUpdateDTO(fileName)
            if (updateDTO.url.isNullOrEmpty()) {
                callback(null, null, "更新地址为空", updateDTO, null)
                return@async
            }
            val now = System.currentTimeMillis()
            if (now - updateDTO.ct <= 3600 * 24 * 1000 && !urgent) {
                //一天内只检查一次
                callback(null, null, "一天内只检查一次", updateDTO, null)
                return@async
            }
            updateDTO.ct = now
            saveGreasyUpdateDTO(fileName, updateDTO)
            var u = updateDTO.url
            if (u.startsWith("https://greasyfork") && !u.endsWith(".js")) {
                u = u.replace(
                    ".+scripts\\/(\\d+-)([^\\/]+).*".toRegex(),
                    "https://greasyfork.org/scripts/$1$2/code/$2.user.js"
                )
            }
            val text = HttpHelper.get(u, HashMap())
            if (text.isNullOrEmpty()) {
                callback(null, null, "访问更新地址获取脚本失败", updateDTO, u)
                return@async
            }
            val jsUrl = Utils.escapeJavaScriptString("hiker://jsfile/$dom/$fileName")
            val js = JSManager.instance(Application.getContext()).getJSFileContent(jsUrl)

            val oldVersion = findVersion(js)
            val downloadUrl0 = findDownloadUrl(text)
            val downloadUrl = if (downloadUrl0.isNullOrEmpty()) {
                findDownloadUrl(js)
            } else downloadUrl0

            val newVersion = findVersion(text)
            Timber.d("jsUpdater: fileName: $fileName, oldVersion: $oldVersion, newVersion: $newVersion")
            callback(oldVersion, newVersion, null, updateDTO, if (downloadUrl.isNullOrEmpty()) u else downloadUrl)
            if (newVersion != oldVersion && !newVersion.isNullOrEmpty() && postEvent) {
                EventBus.getDefault()
                    .post(
                        UpdateEvent(
                            fileName,
                            updateDTO,
                            oldVersion,
                            newVersion,
                            urgent,
                            if (downloadUrl.isNullOrEmpty()) u else downloadUrl
                        )
                    )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun loadGreasyJS(url: String) {
    ThreadTool.async {
        try {
            val text = HttpHelper.get(url, HashMap())
            if (text.isNullOrEmpty() || !text.contains("UserScript==")) {
                return@async
            }
            val namePattern = Pattern.compile("\\/\\/\\s*@name\\s+(.+)[\\s\\n]+")
            val m = namePattern.matcher(text)
            val name = if (m.find()) m.group(1) else null
            if (name.isNullOrEmpty()) {
                return@async
            }
            withContext(Dispatchers.Main) {
                val ctx = ActivityManager.instance.currentActivity
                XPopup.Builder(ctx)
                    .asConfirm("检测到脚本", "检测到可导入的油猴脚本“$name”，确定要导入吗？") {
                        try {
                            val n = StringUtil.replaceBlank(name)
                            val c = String(
                                Base64.encode(
                                    ("$url@_@hiker@_@$text").toByteArray(),
                                    Base64.NO_WRAP
                                )
                            )
                            AutoImportHelper.importJSPlugin(ctx, "global_$n@base64://$c")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ToastMgr.shortCenter(ctx, "出错：" + e.message)
                        }
                    }.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class UpdateEvent(
    val fileName: String,
    val updateDTO: JSUpdateDTO,
    var oldVersion: String?,
    val newVersion: String,
    //是否紧急
    val urgent: Boolean = false,
    val downloadUrl: String
)