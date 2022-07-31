package com.example.hikerview.ui.miniprogram

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.annimon.stream.function.Consumer
import com.example.hikerview.service.http.CodeUtil
import com.example.hikerview.service.http.CodeUtil.OnCodeGetListener
import com.example.hikerview.service.parser.HttpParser
import com.example.hikerview.service.parser.JSEngine
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.browser.util.UUIDUtil
import com.example.hikerview.ui.home.model.ArticleListRule
import com.example.hikerview.ui.miniprogram.data.RuleDTO
import com.example.hikerview.ui.miniprogram.data.ViewDTO
import com.example.hikerview.utils.*
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
    const val FILE = "mini-program.json"
    const val VIEW_FILE = "mini-program-view.json"
    const val STORE_FILE = "mini-program-store.json"
    var data: MutableList<RuleDTO> = ArrayList()
    var lastOverrideTime: Long = 0
    var lastOverrideUrl: String = ""

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
        startMiniProgram(context, url, title, ruleDTO)
    }

    fun startMiniProgramBySearch(
        context: Context,
        title: String,
        articleListRule: ArticleListRule
    ) {
        val ruleDTO = toRuleDTO(articleListRule)
        ruleDTO.rule = articleListRule.searchFind
        ruleDTO.url = HttpParser.replaceKey(articleListRule.search_url, title)
        var nextRule = articleListRule.sdetail_find_rule
        var nextColType = articleListRule.sdetail_col_type
        if ("*" == nextRule) {
            nextRule = articleListRule.detail_find_rule
            nextColType = articleListRule.detail_col_type
        }
        ruleDTO.nextRule = nextRule
        ruleDTO.nextColType = nextColType
        if (!articleListRule.preRule.isNullOrEmpty()) {
            //有预处理规则
            if (ruleDTO.rule!!.startsWith("js:")) {
                ruleDTO.rule =
                    "js:\n" + articleListRule.preRule + "\n" + ruleDTO.rule!!.substring(3)
            } else {
                try {
                    JSEngine.getInstance().parsePreRule(articleListRule)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    return
                }
            }
        }
        startMiniProgram(
            context,
            ruleDTO.url!!,
            title,
            ruleDTO
        )
    }

    fun startMiniProgram(
        context: Context,
        url: String,
        title: String?,
        ruleDTO: RuleDTO
    ) {
        val intent = Intent(context, MiniProgramActivity::class.java)
        ruleDTO.url = url
        val fileName = UUIDUtil.genUUID()
        DataTransferUtils.putCache(ruleDTO, fileName)
        intent.putExtra("rule", fileName)
        intent.putExtra("title", title)
        context.startActivity(intent)
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
            MiniProgramRouter.loadConfig(context) {

            }
        }
    }

    /**
     * 从远程加载数据
     */
    suspend fun loadConfig(context: Context, consumer: Consumer<MutableList<RuleDTO>>) {
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
                    saveLocalConfig(context, s)
                    updateData(s)
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
        val path = UriUtils.getRootDir(context) + File.separator + FILE
        File(path).delete()
        data.clear()
    }


    fun getSubscribeUrl(context: Context?): String? {
        return PreferenceMgr.getString(context, "subscribe", "mini-program", "")
    }

    fun updateSubscribeUrl(context: Context?, url: String?) {
        PreferenceMgr.put(context, "subscribe", "mini-program", url)
    }

    private fun loadLocalConfig(context: Context): String {
        val path = UriUtils.getRootDir(context) + File.separator + FILE
        if (File(path).exists()) {
            return FileUtil.fileToString(path)
        }
        return ""
    }

    fun saveLocalConfig(context: Context, str: String) {
        val path = UriUtils.getRootDir(context) + File.separator + FILE
        FileUtil.stringToFile(str, path)
    }

    private fun updateData(text: String) {
        if (text.isEmpty()) {
            data.clear()
            return
        }
        try {
            data = JSON.parseArray(text, RuleDTO::class.java)
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
        val path = UriUtils.getRootDir(context) + File.separator + VIEW_FILE
        if (File(path).exists()) {
            return JSON.parseArray(FileUtil.fileToString(path), ViewDTO::class.java)
        }
        return ArrayList()
    }

    private fun memoryView(context: Context, ruleDTO: RuleDTO, url: String) {
        HeavyTaskUtil.executeNewTask {
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
            val path = UriUtils.getRootDir(context) + File.separator + VIEW_FILE
            FileUtil.stringToFile(JSON.toJSONString(exist), path)
        }
    }

    fun getItem(ruleName: String, key: String, defaultValue: String?): String? {
        val path = UriUtils.getRootDir(Application.getContext()) + File.separator + STORE_FILE
        if (File(path).exists()) {
            val json = JSON.parseObject(FileUtil.fileToString(path))
            if (json.containsKey(ruleName)) {
                val obj = json.getJSONObject(ruleName)
                if (obj.containsKey(key)) {
                    return obj.getString(key)
                }
            }
        }
        return defaultValue
    }

    fun setItem(ruleName: String, key: String, value: String) {
        val path = UriUtils.getRootDir(Application.getContext()) + File.separator + STORE_FILE
        if (File(path).exists()) {
            val json = JSON.parseObject(FileUtil.fileToString(path))
            if (json.containsKey(ruleName)) {
                val obj = json.getJSONObject(ruleName)
                obj[key] = value
                FileUtil.stringToFile(json.toJSONString(), path)
                return
            }
            json[ruleName] = JSONObject()
            json.getJSONObject(ruleName)[key] = value
            FileUtil.stringToFile(json.toJSONString(), path)
            return
        }
        val json = JSONObject()
        json[ruleName] = JSONObject()
        json.getJSONObject(ruleName)[key] = value
        FileUtil.stringToFile(json.toJSONString(), path)
    }

    fun deleteItemByRule(ruleName: String) {
        val path = UriUtils.getRootDir(Application.getContext()) + File.separator + STORE_FILE
        if (File(path).exists()) {
            val json = JSON.parseObject(FileUtil.fileToString(path))
            if (json.containsKey(ruleName)) {
                json.remove(ruleName)
                FileUtil.stringToFile(json.toJSONString(), path)
                return
            }
        }
    }

    fun clearItem(ruleName: String, key: String) {
        val path = UriUtils.getRootDir(Application.getContext()) + File.separator + STORE_FILE
        if (File(path).exists()) {
            val json = JSON.parseObject(FileUtil.fileToString(path))
            if (json.containsKey(ruleName)) {
                val obj = json.getJSONObject(ruleName)
                if (obj.containsKey(key)) {
                    obj.remove(key)
                    FileUtil.stringToFile(json.toJSONString(), path)
                }
            }
        }
    }

    fun toArticleListRule(ruleDTO: RuleDTO): ArticleListRule {
        val articleListRule = ArticleListRule()
        articleListRule.title = ruleDTO.title
        articleListRule.url = ruleDTO.url
        articleListRule.find_rule = ruleDTO.rule
        articleListRule.ua = ruleDTO.ua
        articleListRule.pages = ruleDTO.pages
        articleListRule.params = ruleDTO.params
        return articleListRule
    }

    fun toRuleDTO(articleListRule: ArticleListRule): RuleDTO {
        val ruleDTO = RuleDTO()
        ruleDTO.title = articleListRule.title
        ruleDTO.url = articleListRule.url
        ruleDTO.rule = articleListRule.find_rule
        ruleDTO.ua = articleListRule.ua
        ruleDTO.pages = articleListRule.pages
        ruleDTO.params = articleListRule.params
        ruleDTO.col_type = articleListRule.col_type
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
                            ruleDTO.copy()
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
                ruleDTO.copy()
            )
        }
    }
}