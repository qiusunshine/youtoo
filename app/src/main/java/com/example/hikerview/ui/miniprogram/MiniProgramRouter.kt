package com.example.hikerview.ui.miniprogram

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.annimon.stream.function.Consumer
import com.example.hikerview.constants.ArticleColTypeEnum
import com.example.hikerview.service.http.CodeUtil
import com.example.hikerview.service.http.CodeUtil.OnCodeGetListener
import com.example.hikerview.service.parser.HttpParser
import com.example.hikerview.service.parser.JSEngine
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.browser.util.UUIDUtil
import com.example.hikerview.ui.home.ArticleListRuleEditActivity
import com.example.hikerview.ui.home.model.ArticleListRule
import com.example.hikerview.ui.miniprogram.data.RuleDTO
import com.example.hikerview.ui.miniprogram.data.ViewDTO
import com.example.hikerview.utils.*
import com.example.hikerview.utils.encrypt.AesUtil
import com.lxj.xpopup.XPopup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.regex.Pattern

/**
 * 作者：By 15968
 * 日期：On 2022/7/3
 * 时间：At 12:02
 */
object MiniProgramRouter {
    var data: MutableList<RuleDTO> = ArrayList()
    var lastOverrideTime: Long = 0
    var lastOverrideUrl: String = ""
    const val AES_KEY = "hk123kh"

    /**
     * 迁移数据
     */
    fun migrateFiles() {
        migrateFile("mini-program.json")
        migrateFile("mini-program-store.json")
        migrateFile("mini-program-history.json")
        migrateFile("mini-program-view.json")
    }

    private fun migrateFile(name: String) {
        try {
            val old =
                UriUtils.getRootDir(Application.getContext()) + File.separator + name
            val oldFile = File(old)
            val path =
                UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + name
            val newFile = File(path)
            if (!newFile.exists() && oldFile.exists()) {
                val json = FileUtil.fileToString(old)
                FileUtil.stringToFile(json, path)
                oldFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun filePath(): String {
        return UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "mini-program.json"
    }

    private fun storePath(): String {
        return UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "mini-program-store.json"
    }

    private fun viewPath(): String {
        return UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "mini-program-view.json"
    }

    fun shouldOverrideUrlLoading(context: Context, url0: String, ua: String): Boolean {
        if (data.isNotEmpty()) {
            var url = url0
            val now = System.currentTimeMillis()
            if (lastOverrideUrl == url && now - lastOverrideTime < 200) {
                //重复发起的忽略
                return false
            }
            lastOverrideUrl = url
            lastOverrideTime = now
            for (ruleDTO in data) {
                if (!ruleDTO.interceptor.isNullOrEmpty()) {
                    try {
                        val rs = ruleDTO.interceptor!!.split(".js:")
                        var interceptor = ruleDTO.interceptor!!
                        if (rs.size > 1) {
                            interceptor = rs[0]
                        }
                        if (!interceptor.contains("*") && !interceptor.contains(
                                "?"
                            ) && url.contains(interceptor)
                        ) {
                            val rule = ruleDTO.copy()
                            //简易匹配
//                            if (!rule.url.isNullOrEmpty() && rule.url?.startsWith("hiker://empty##") == true) {
//                                url = "hiker://empty##$url"
//                            }
                            startMiniProgramFromWeb(rs, context, url, rule.title ?: "", rule)
                            memoryView(context, rule, url)
                            return true
                        } else if (Pattern.matches(interceptor, url)) {
                            val rule = ruleDTO.copy()
                            //正则匹配
//                            if (!rule.url.isNullOrEmpty() && rule.url?.startsWith("hiker://empty##") == true) {
//                                url = "hiker://empty##$url"
//                            }
                            startMiniProgramFromWeb(rs, context, url, rule.title ?: "", rule)
                            memoryView(context, rule, url)
                            return true
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return false
    }

    fun startMiniProgramFromWeb(
        rs: List<String>,
        context: Context,
        u: String,
        title: String,
        ruleDTO: RuleDTO
    ) {
        var url = u
        if (rs.size > 1) {
            url = JSEngine.getInstance().evalJS(JSEngine.getMyRule(ruleDTO) + rs[1], url)
        }
        startMiniProgram(context, url, title, ruleDTO, true)
    }

    fun startMiniProgramBySearch(
        context: Context,
        title: String,
        articleListRule: ArticleListRule
    ) {
        val ruleDTO = toRuleDTO(articleListRule, 2)
        ruleDTO.rule = articleListRule.searchFind
        ruleDTO.col_type = ArticleColTypeEnum.MOVIE_1_VERTICAL_PIC.code
        if (!ruleDTO.rule.isNullOrEmpty() && ruleDTO.rule?.startsWith("js:") != true) {
            val ss = ruleDTO.rule!!.split(";")
            val res = arrayOf("", "", "", "", "")
            res[0] = ss[0]
            res[1] = ss[1]
            if (ss.size < 6 || ss[5].isEmpty() || ss[5] == "*") {
                ruleDTO.col_type = ArticleColTypeEnum.TEXT_1.code
            } else if (ss.size >= 6) {
                res[2] = ss[5]
            }
            if (ss.size >= 4) {
                res[3] = ss[3]
            }
            if (ss.size >= 3) {
                res[4] = ss[2]
            }
            ruleDTO.rule = res.joinToString(";")
        }
        ruleDTO.url = HttpParser.replaceKey(articleListRule.search_url, title)
        startMiniProgram(
            context,
            ruleDTO.url!!,
            title,
            ruleDTO,
            true
        )
    }

    fun startMiniProgram(
        context: Context,
        url: String,
        title: String?,
        ruleDTO: RuleDTO
    ) {
        startMiniProgram(context, url, title, ruleDTO, false)
    }

    fun startMiniProgram(
        context: Context,
        url: String,
        title: String?,
        ruleDTO: RuleDTO,
        needPreParse: Boolean = false
    ) {
        startMiniProgram(context, url, title, ruleDTO, needPreParse, null, null)
    }

    fun startMiniProgram(
        context: Context,
        url: String,
        title: String?,
        ruleDTO: RuleDTO,
        needPreParse: Boolean = false,
        parentTitle: String? = null,
        parentUrl: String? = null
    ) {
        startMiniProgram(context, url, title, ruleDTO, needPreParse, parentTitle, parentUrl, false)
    }

    fun startMiniProgram(
        context: Context,
        url: String,
        title: String?,
        ruleDTO: RuleDTO,
        needPreParse: Boolean = false,
        parentTitle: String? = null,
        parentUrl: String? = null,
        playLast: Boolean = false,
        fromHome: Boolean = false
    ) {
        startMiniProgram(
            context,
            url,
            title,
            ruleDTO,
            needPreParse,
            parentTitle,
            parentUrl,
            playLast,
            fromHome,
            null
        )
    }

    fun startMiniProgram(
        context: Context,
        url: String,
        title: String?,
        ruleDTO: RuleDTO,
        needPreParse: Boolean = false,
        parentTitle: String? = null,
        parentUrl: String? = null,
        playLast: Boolean = false,
        fromHome: Boolean = false,
        picUrl: String? = null
    ) {
        val intent = Intent(context, MiniProgramActivity::class.java)
        ruleDTO.url = url
        val fileName = UUIDUtil.genUUID()
        DataTransferUtils.putCache(ruleDTO, fileName)
        intent.putExtra("rule", fileName)
        intent.putExtra("title", title)
        intent.putExtra("needPreParse", needPreParse)
        intent.putExtra("playLast", playLast)
        intent.putExtra("fromHome", fromHome)
        parentTitle?.let {
            intent.putExtra("parentTitle", it)
        }
        parentUrl?.let {
            intent.putExtra("parentUrl", it)
        }
        if (!picUrl.isNullOrEmpty()) {
            intent.putExtra("picUrl", picUrl)
        }
        if (context is Activity) {
            context.startActivityForResult(intent, 911)
        } else {
            context.startActivity(intent)
        }
    }

    fun startWebPage(context: Context, url: String, title: String) {
        startWebPage(context, url, title, false)
    }

    fun startWebPage(context: Context, url: String, title: String, floatVideo: Boolean = false) {
        val intent = Intent(context, MiniProgramActivity::class.java)
        val ruleDTO = RuleDTO()
        ruleDTO.title = "X5"
        ruleDTO.url = "hiker://empty##$url"
        ruleDTO.rule = "js:" +
                "setResult([{" +
                "url: MY_URL.split('##')[1]," +
                "col_type: 'x5_webview_single'," +
                "desc: 'float&&100%'," +
                "extra: {" +
                "   canBack: true," +
                "   floatVideo: " + floatVideo.toString() + "," +
                "   js: $.toString(() => {\n" +
                "                            try {\n" +
                "                                if (document.title && document.title.length) {\n" +
                "                                    let r = $$$().lazyRule((t) => {\n" +
                "                                        setPageTitle(t);\n" +
                "                                    }, document.title);\n" +
                "                                    fy_bridge_app.parseLazyRule(r);\n" +
                "                                }\n" +
                "                            } catch (e) {\n" +
                "                                fy_bridge_app.log(e.toString());\n" +
                "                            }\n" +
                "                        })" +
                "}" +
                "}]);"
        val fileName = UUIDUtil.genUUID()
        DataTransferUtils.putCache(ruleDTO, fileName)
        intent.putExtra("rule", fileName)
        intent.putExtra("title", title)
        context.startActivity(intent)
    }

    fun loadConfigBackground(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            migrateFiles()
            loadConfig(context) {

            }
        }
    }

    /**
     * 从远程加载数据
     */
    private suspend fun loadConfig(context: Context, consumer: Consumer<MutableList<RuleDTO>>) {
        updateData(loadLocalConfig(context))
        val url = getSubscribeUrl(context)
        if (url.isNullOrEmpty()) {
            return
        }
        val miniProgramEnable =
            PreferenceMgr.getBoolean(context, "subscribe", "miniProgramEnable", true)
        if (!miniProgramEnable) {
            //禁用了远程
            return
        }
        val now = System.currentTimeMillis()
        val lastUpdate = PreferenceMgr.getLong(context, "subscribe", "miniLastUpdateTime", 0)
        if (now - lastUpdate < 86400 * 1000) {
            //一天内只检查一次
            if (data.isEmpty()) {
                updateData(loadLocalConfig(context))
            }
            return
        }
        PreferenceMgr.put(context, "subscribe", "miniLastUpdateTime", now)
        //更新数据
        CodeUtil.get(url, object : OnCodeGetListener {
            override fun onSuccess(s: String) {
                if (TextUtils.isEmpty(s) || !s.contains("rule") || !s.contains("title")) {
                    return
                }
                val now = loadLocalConfig(context)
                if (s != now) {
                    val s2 = mergeRemoteLocal(s, now)
                    saveLocalConfig(context, s2)
                    updateData(s2)
                } else if (data.isEmpty()) {
                    updateData(now)
                }
                consumer.accept(data)
            }

            override fun onFailure(errorCode: Int, msg: String) {
                if (data.isEmpty()) {
                    updateData(loadLocalConfig(context))
                }
                consumer.accept(data)
            }
        })
    }

    private fun mergeRemoteLocal(remote: String, local: String): String {
        val remoteList = if (remote.isEmpty()) ArrayList<RuleDTO>() else JSON.parseArray(
            remote,
            RuleDTO::class.java
        )
        val localList = if (local.isEmpty()) ArrayList<RuleDTO>() else JSON.parseArray(
            local,
            RuleDTO::class.java
        )
        val size = remoteList.size
        val map = HashMap<String, RuleDTO>()
        for (ruleDTO in remoteList) {
            ruleDTO.title?.let {
                map.put(it, ruleDTO)
            }
        }
        for (ruleDTO in localList) {
            if (!map.containsKey(ruleDTO.title)) {
                remoteList.add(0, ruleDTO)
            }
        }
        return if (size == remoteList.size) {
            //没增加
            remote
        } else {
            JSON.toJSONString(remoteList)
        }
    }

    suspend fun reloadConfig(
        context: Context,
        consumer: Consumer<MutableList<RuleDTO>> = Consumer<MutableList<RuleDTO>> { }
    ) {
        PreferenceMgr.remove(context, "subscribe", "miniLastUpdateTime")
        loadConfig(context, consumer)
    }

    fun reloadLocalConfig(context: Context) {
        updateData(loadLocalConfig(context))
    }

    fun clearConfig(context: Context) {
        PreferenceMgr.remove(context, "subscribe", "mini-program")
        PreferenceMgr.remove(context, "subscribe", "miniLastUpdateTime")
        File(filePath()).delete()
        data.clear()
    }


    fun getSubscribeUrl(context: Context?): String? {
        return PreferenceMgr.getString(context, "subscribe", "mini-program", "")
    }

    fun updateSubscribeUrl(context: Context?, url: String?) {
        PreferenceMgr.put(context, "subscribe", "mini-program", url)
    }

    private fun loadLocalConfig(context: Context): String {
        val path = filePath()
        if (File(path).exists()) {
            return FileUtil.fileToString(path)
        }
        return ""
    }

    fun saveLocalConfig(context: Context, str: String) {
        val path = filePath()
        FileUtil.stringToFile(str, path)
    }

    private fun updateData(text: String) {
        if (text.isEmpty()) {
            data.clear()
            return
        }
        try {
            data = JSON.parseArray(text, RuleDTO::class.java)
            val iterator = data.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                if (ArticleListRuleEditActivity.hasBlockDom(it.url) || ArticleListRuleEditActivity.hasBlockDom(
                        it.interceptor
                    )
                ) {
                    iterator.remove()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMemoryView(context: Context, ruleDTO: RuleDTO): String? {
        val exist = loadMemoryList(context)
        for (viewDTO in exist) {
            if (ruleDTO.title.equals(viewDTO.title)) {
                return viewDTO.url
            }
        }
        return null
    }

    private fun loadMemoryList(context: Context): MutableList<ViewDTO> {
        val path = viewPath()
        if (File(path).exists()) {
            return JSON.parseArray(FileUtil.fileToString(path), ViewDTO::class.java)
        }
        return ArrayList()
    }

    private fun memoryView(context: Context, ruleDTO: RuleDTO, url: String) {
        HeavyTaskUtil.executeNewTask {
            val historyCount = PreferenceMgr.getInt(Application.getContext(), "historyCount", 300)
            if (historyCount <= 0) {
                return@executeNewTask
            }
            val exist = loadMemoryList(context)
            for (viewDTO in exist) {
                if (ruleDTO.title.equals(viewDTO.title)) {
                    exist.remove(viewDTO)
                    break
                }
            }
            if (exist.size >= 100) {
                exist.removeAt(0)
            }
            val viewDTO = ViewDTO()
            viewDTO.title = ruleDTO.title
            viewDTO.url = url
            exist.add(viewDTO)
            FileUtil.stringToFile(JSON.toJSONString(exist), viewPath())
        }
    }

    private fun saveStore(json: JSONObject) {
        val t = AesUtil.encrypt(AES_KEY, json.toJSONString())
        FileUtil.stringToFile(t, storePath())
    }

    private fun loadStore(): JSONObject {
        val path = storePath()
        return if (File(path).exists()) {
            val text = FileUtil.fileToString(path)
            if (text.isNullOrEmpty()) {
                JSONObject()
            } else if (text.startsWith("{") && text.endsWith("}")) {
                //老数据，不解密
                JSON.parseObject(text)
            } else {
                //需要解密
                JSON.parseObject(AesUtil.decrypt(AES_KEY, text))
            }
        } else {
            JSONObject()
        }
    }

    private fun isStoreFileExist() = File(storePath()).exists()

    fun getItem(ruleName: String, key: String, defaultValue: String?): String? {
        val json = loadStore()
        if (json.containsKey(ruleName)) {
            val obj = json.getJSONObject(ruleName)
            if (obj.containsKey(key)) {
                return obj.getString(key)
            }
        }
        return defaultValue
    }

    fun setItem(ruleName: String, key: String, value: String) {
        val json = loadStore()
        if (json.containsKey(ruleName)) {
            val obj = json.getJSONObject(ruleName)
            obj[key] = value
            saveStore(json)
            return
        }
        json[ruleName] = JSONObject()
        json.getJSONObject(ruleName)[key] = value
        saveStore(json)
    }

    fun deleteItemByRule(ruleName: String) {
        if (isStoreFileExist()) {
            val json = loadStore()
            if (json.containsKey(ruleName)) {
                json.remove(ruleName)
                saveStore(json)
                return
            }
        }
    }

    fun clearItem(ruleName: String, key: String) {
        if (isStoreFileExist()) {
            val json = loadStore()
            if (json.containsKey(ruleName)) {
                val obj = json.getJSONObject(ruleName)
                if (obj.containsKey(key)) {
                    obj.remove(key)
                    saveStore(json)
                }
            }
        }
    }

    fun toArticleListRule(ruleDTO: RuleDTO, interceptorConvert: Boolean = false): ArticleListRule {
        val articleListRule = ArticleListRule()
        articleListRule.title = ruleDTO.title
        articleListRule.url = ruleDTO.url
        articleListRule.find_rule = ruleDTO.rule
        articleListRule.ua = ruleDTO.ua
        articleListRule.col_type = ruleDTO.col_type
        articleListRule.pages = ruleDTO.pages
        articleListRule.params = ruleDTO.params
        articleListRule.preRule = ruleDTO.preRule
        articleListRule.detail_col_type = ruleDTO.nextColType
        articleListRule.detail_find_rule = ruleDTO.nextRule
        if (interceptorConvert) {
            articleListRule.search_url = ruleDTO.interceptor
        }
        return articleListRule
    }

    fun toRuleDTO(articleListRule: ArticleListRule, withNextRule: Int = 0): RuleDTO {
        val ruleDTO = RuleDTO()
        ruleDTO.title = articleListRule.title
        ruleDTO.url = articleListRule.url
        ruleDTO.rule = articleListRule.find_rule
        ruleDTO.ua = articleListRule.ua
        ruleDTO.pages = articleListRule.pages
        ruleDTO.params = articleListRule.params
        ruleDTO.col_type = articleListRule.col_type
        ruleDTO.preRule = articleListRule.preRule
        if (StringUtil.isNotEmpty(articleListRule.search_url)
            && articleListRule.search_url?.contains(
                "**"
            ) == false
            && StringUtil.isEmpty(articleListRule.searchFind)
        ) {
            ruleDTO.interceptor = articleListRule.search_url
        }
        if (withNextRule == 1) {
            ruleDTO.nextRule = articleListRule.detail_find_rule
            ruleDTO.nextColType = articleListRule.detail_col_type
        } else if (withNextRule == 2) {
            var nextRule = articleListRule.sdetail_find_rule
            var nextColType = articleListRule.sdetail_col_type
            if ("*" == nextRule) {
                nextRule = articleListRule.detail_find_rule
                nextColType = articleListRule.detail_col_type
            }
            ruleDTO.nextRule = nextRule
            ruleDTO.nextColType = nextColType
        }
        return ruleDTO
    }

    fun findArticleListRule(name: String): ArticleListRule? {
        for (datum in data) {
            if (name == datum.title) {
                return toArticleListRule(datum)
            }
        }
        return null
    }

    fun findRuleDTO(name: String): RuleDTO? {
        for (datum in data) {
            if (name == datum.title) {
                return datum
            }
        }
        return null
    }

    fun startRuleHomePage(context: Context, ruleDTO: RuleDTO) {
        if (ruleDTO.url.isNullOrEmpty()) {
            val u = getMemoryView(context, ruleDTO)
            if (u != null) {
                XPopup.Builder(context)
                    .asConfirm("温馨提示", "当前小程序没有主页地址，是否跳转上次访问页面？") {
                        startMiniProgram(
                            context,
                            u,
                            ruleDTO.title ?: "",
                            ruleDTO.copy(),
                            true
                        )
                    }.show()
            } else {
                val home = StringUtil.getHomeUrl(ruleDTO.interceptor)
                if (!home.isNullOrEmpty()) {
                    XPopup.Builder(context)
                        .asConfirm("温馨提示", "当前小程序没有主页地址，是否访问网站首页？") {
                            startWebPage(
                                context,
                                home,
                                ruleDTO.title ?: "",
                                true
                            )
                        }.show()
                } else {
                    ToastMgr.shortBottomCenter(context, "当前小程序没有主页面")
                }
            }
        } else {
            startMiniProgram(
                context,
                ruleDTO.url!!,
                ruleDTO.title ?: "",
                ruleDTO.copy(),
                true,
                null,
                null,
                playLast = false,
                fromHome = true
            )
        }
    }

    fun saveRulesToLocal(context: Context, rules: MutableList<RuleDTO>) {
        if (rules.isEmpty()) {
            return
        }
        val needAddList = ArrayList<RuleDTO>()
        for (rule in rules) {
            if (!replaceRule(rule)) {
                needAddList.add(rule)
            }
        }
        if (needAddList.isNotEmpty()) {
            data.addAll(needAddList)
        }
        saveLocalConfig(context, JSON.toJSONString(data))
    }

    private fun replaceRule(rule: RuleDTO): Boolean {
        for (indexedValue in data.withIndex()) {
            if (rule.title == indexedValue.value.title) {
                data.removeAt(indexedValue.index)
                data.add(indexedValue.index, rule)
                return true
            }
        }
        return false
    }
}