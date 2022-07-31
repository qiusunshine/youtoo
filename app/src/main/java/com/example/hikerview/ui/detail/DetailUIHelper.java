package com.example.hikerview.ui.detail;

import android.app.Activity;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.example.hikerview.R;
import com.example.hikerview.event.home.OnRefreshWebViewEvent;
import com.example.hikerview.model.ViewCollection;
import com.example.hikerview.model.ViewCollectionExtraData;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.detail.model.InputExtra;
import com.example.hikerview.ui.detail.model.SelectExtra;
import com.example.hikerview.ui.home.FilmListActivity;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.home.model.BaseResultItem;
import com.example.hikerview.ui.miniprogram.MiniProgramRouter;
import com.example.hikerview.ui.thunder.ThunderManager;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.ui.view.CustomRecyclerViewPopup;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ThreadTool;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static com.example.hikerview.utils.StringUtil.SCHEME_CONFIRM;
import static com.example.hikerview.utils.StringUtil.SCHEME_INPUT;

/**
 * 作者：By 15968
 * 日期：On 2021/1/16
 * 时间：At 13:33
 */

public class DetailUIHelper {


    public static String getCodeAndHeader(String originalUrl, String[] lazyRule) {
        String codeAndHeader = "";
        String[] ars = originalUrl.split(";");
        if (ars.length > 1) {
            codeAndHeader = ";" + StringUtil.arrayToString(ars, 1, ars.length, ";");
            codeAndHeader = codeAndHeader.replace(";post", ";get").replace(";POST", ";get");
        }
        if (lazyRule.length > 1) {
            if (StringUtil.isEmpty(codeAndHeader)) {
                codeAndHeader = ";get";
            }
        }
        return codeAndHeader;
    }

    public static boolean dealUrlSimply(Activity activity, String url) {
        return dealUrlSimply(activity, null, null, url, null);
    }

    public static boolean dealUrlSimply(Activity activity, Object rule, @Nullable BaseResultItem resultItem,
                                        String url, Consumer<String> reDealConsumer) {
        if (StringUtil.isEmpty(url)) {
            ToastMgr.shortBottomCenter(activity, "链接为空！");
            return true;
        }
        if (url.startsWith("code://")) {
            url = StringUtils.replaceOnce(url, "code://", "");
            url = url.trim();
            if (url.length() > 6 && "海阔视界".equals(url.substring(1, 5))) {
                url = url.substring(1);
            }
            AutoImportHelper.checkText(activity, url);
            return true;
        }
        Timber.d("dealWithUrl: %s", url);
        String lowUrl = url.toLowerCase();
        if (lowUrl.startsWith("toast://")) {
            String title = StringUtils.replaceOnceIgnoreCase(url, "toast://", "");
            ToastMgr.shortBottomCenter(activity, title);
            return true;
        } else if ("hiker://empty".equals(lowUrl)) {
            return true;
        } else if (url.startsWith("x5WebView://")) {
            String title = StringUtils.replaceOnceIgnoreCase(url, "x5WebView://", "");
            EventBus.getDefault().post(new OnRefreshWebViewEvent(title));
            return true;
        } else if (url.startsWith(SCHEME_INPUT)) {
            String title = StringUtils.replaceOnceIgnoreCase(url, SCHEME_INPUT, "");
            InputExtra extra;
            if (title.startsWith("{") && title.endsWith("}") && (title.contains("\"js\"") || title.contains("'js'"))) {
                extra = JSON.parseObject(title, InputExtra.class);
            } else {
                String[] jsRules = title.split("\\.js:");
                String jsRule = StringUtil.arrayToString(jsRules, 1, ".js:");
                String[] titles = jsRules[0].split("////");
                String content = titles.length > 1 ? titles[titles.length - 1] : null;
                String input = StringUtil.arrayToString(titles, 0, titles.length - 1, "////");
                extra = new InputExtra();
                extra.setJs(jsRule);
                extra.setValue(input);
                extra.setHint(content);
            }
            if ("{{clipboard}}".equals(extra.getValue()) && activity.getWindow() != null) {
                ClipboardUtil.getText(activity, activity.getWindow().getDecorView(), text -> {
                    showInput(activity, rule, resultItem, extra.getHint(), text, extra.getJs(), reDealConsumer);
                }, 200);
            } else {
                showInput(activity, rule, resultItem, extra.getHint(), extra.getValue(), extra.getJs(), reDealConsumer);
            }
            return true;
        } else if (url.startsWith(SCHEME_CONFIRM)) {
            String title = StringUtils.replaceOnceIgnoreCase(url, SCHEME_CONFIRM, "");
            String[] jsRules = title.split("\\.js:");
            if (jsRules.length < 2) {
                ToastMgr.shortBottomCenter(activity, "规则有误：" + url);
                return true;
            }
            if (activity == null) {
                return true;
            }
            String jsRule = StringUtil.arrayToString(jsRules, 1, ".js:");
            new XPopup.Builder(activity)
                    .asConfirm("小程序提示", jsRules[0], () -> {
                        HeavyTaskUtil.executeNewTask(() -> {
                            String result = JSEngine.getInstance().evalJS(JSEngine.getMyRule(rule) + jsRule, "");
                            if (activity != null && !activity.isFinishing()) {
                                activity.runOnUiThread(() -> {
                                    if (StringUtil.isNotEmpty(result) && !"undefined".equalsIgnoreCase(result)) {
                                        if (reDealConsumer != null) {
                                            reDealConsumer.accept(result);
                                        } else {
                                            dealUrlSimply(activity, rule, resultItem, result, null);
                                        }
                                    }
                                });
                            }
                        });
                    }).show();
            return true;
        } else if (url.startsWith(StringUtil.SCHEME_SELECT)) {
            String title = StringUtils.replaceOnceIgnoreCase(url, StringUtil.SCHEME_SELECT, "");
            try {
                SelectExtra extra;
                if (title.startsWith("{") && title.endsWith("}") && title.contains("options")) {
                    extra = JSON.parseObject(title, SelectExtra.class);
                } else {
                    String[] jsRules = title.split("\\.js:");
                    String jsRule = StringUtil.arrayToString(jsRules, 1, ".js:");
                    extra = new SelectExtra();
                    extra.setOptions(new ArrayList<>(Arrays.asList(jsRules[0].split("&&"))));
                    extra.setJs(jsRule);
                }
                CustomCenterRecyclerViewPopup popup = new CustomCenterRecyclerViewPopup(activity)
                        .withTitle(StringUtil.isEmpty(extra.getTitle()) ? "请选择" : extra.getTitle())
                        .with(extra.getOptions(), extra.getCol() > 0 ? extra.getCol() : 3, new CustomCenterRecyclerViewPopup.ClickListener() {
                            @Override
                            public void click(String text, int position) {
                                if (activity.isFinishing()) {
                                    return;
                                }
                                HeavyTaskUtil.executeNewTask(() -> {
                                    String result = JSEngine.getInstance().evalJS(JSEngine.getMyRule(rule) + extra.getJs(), text);
                                    if (!activity.isFinishing()) {
                                        activity.runOnUiThread(() -> {
                                            if (StringUtil.isNotEmpty(result) && !"undefined".equalsIgnoreCase(result)) {
                                                if (reDealConsumer != null) {
                                                    reDealConsumer.accept(result);
                                                } else {
                                                    dealUrlSimply(activity, rule, resultItem, result, null);
                                                }
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onLongClick(String url, int position) {
                            }
                        });
                new XPopup.Builder(activity)
                        .asCustom(popup)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
                if (activity != null) {
                    ToastMgr.shortCenter(activity, "出错：" + e.getMessage());
                }
            }
            return true;
        } else if (url.startsWith(StringUtil.SCHEME_COPY)) {
            String title = StringUtils.replaceOnceIgnoreCase(url, StringUtil.SCHEME_COPY, "");
            String[] jsRules = title.split("\\.js:");
            if (jsRules.length < 2) {
                ClipboardUtil.copyToClipboardForce(activity, title);
                return true;
            }
            ClipboardUtil.copyToClipboardForce(activity, jsRules[0]);
            HeavyTaskUtil.executeNewTask(() -> {
                String result = JSEngine.getInstance().evalJS(JSEngine.getMyRule(rule) + jsRules[1], jsRules[0]);
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(() -> {
                        //不允许copy://text.js:'copy://'
                        if (StringUtil.isNotEmpty(result) && !"undefined".equalsIgnoreCase(result) && !StringUtil.SCHEME_COPY.startsWith(result)) {
                            if (reDealConsumer != null) {
                                reDealConsumer.accept(result);
                            } else {
                                dealUrlSimply(activity, rule, resultItem, result, null);
                            }
                        }
                    });
                }
            });
            return true;
        } else if (lowUrl.startsWith("rule://") || url.startsWith("海阔视界")) {
            //可导入的规则
            if (url.startsWith("海阔视界")) {
                AutoImportHelper.checkAutoText(activity, url);
                return true;
            }
            try {
                String realRule = StringUtils.replaceOnceIgnoreCase(url, "rule://", "");
                String decodeStr = new String(Base64.decode(StringUtil.replaceLineBlank(realRule), Base64.NO_WRAP));
                if (StringUtil.isEmpty(decodeStr) || !AutoImportHelper.checkAutoText(activity, decodeStr)) {
                    ToastMgr.shortBottomCenter(activity, "获取规则口令失败");
                }
            } catch (Exception e) {
                ToastMgr.shortBottomCenter(activity, "解析规则口令失败：" + e.getMessage());
            }
            return true;
        } else if (url.startsWith("hiker://search") && resultItem != null) {
            //使用规则代理
            String ruleJs = resultItem.getBaseExtra().getRules();
            if (StringUtil.isNotEmpty(ruleJs)) {
                String[] s = url.split("search\\?");
                if (s.length > 1) {
                    String[] params = s[1].split("&");
                    String key = null, rule0 = null, group = null, simple = null;
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length < 2) {
                            continue;
                        }
                        String key1 = keyValue[0];
                        String value = StringUtil.arrayToString(keyValue, 1, "=");
                        if ("s".equals(key1)) {
                            key = value;
                        } else if ("rule".equals(key1)) {
                            rule0 = value;
                        } else if ("simple".equals(key1)) {
                            simple = value;
                        } else if ("ruleGroup".equals(key1) || "group".equals(key1)) {
                            group = value;
                        }
                    }
                    startSearchByDelegateRules(activity, key, rule, ruleJs);
                }
                return true;
            }
        } else if (!url.contains("@lazyRule=") && !url.contains("@rule=")) {
            if (url.startsWith("magnet:?")) {
                ThunderManager.INSTANCE.startDownloadMagnet(activity, url);
                return true;
            } else if (url.split(";")[0].endsWith(".torrent")) {
                ThunderManager.INSTANCE.startParseTorrent(activity, url);
                return true;
            } else if (ThunderManager.INSTANCE.isFTPOrEd2k(url)) {
//                交给后面处理
//                ThunderManager.INSTANCE.startParseFTPOrEd2k(activity, url);
                return false;
            } else if (StringUtil.isCannotHandleScheme(url)) {
                ShareUtil.findChooserToDeal(activity, url);
                return true;
            }
        }
        return false;
    }

    /**
     * 规则代理
     *
     * @param activity
     * @param key
     * @param rules
     */
    private static void startSearchByDelegateRules(Activity activity, String key, Object rule, String rules) {
        if (StringUtil.isEmpty(rules)) {
            return;
        }
        HeavyTaskUtil.executeNewTask(() -> {
            String result = JSEngine.getInstance().evalJS(JSEngine.getMyRule(rule) + rules, key, false);
            if (StringUtil.isNotEmpty(result) && !"undefined".equalsIgnoreCase(result) && !"[]".equals(result)) {
                List<ArticleListRule> rules1 = JSON.parseArray(result, ArticleListRule.class);
                if (CollectionUtil.isNotEmpty(rules1)) {
                    ThreadTool.INSTANCE.runOnUI(() -> {
                        if (rules1.size() == 1) {
                            MiniProgramRouter.INSTANCE.startMiniProgramBySearch(activity, key, rules1.get(0));
                        } else {
                            CustomRecyclerViewPopup popup = new CustomRecyclerViewPopup(activity)
                                    .withTitle("请选择搜索的小程序")
                                    .height(0.75f)
                                    .withDismissAfterClick(false)
                                    .with(Stream.of(rules1).map(ArticleListRule::getTitle).toList(), 2, new CustomRecyclerViewPopup.ClickListener() {
                                        @Override
                                        public void click(String url, int position) {
                                            MiniProgramRouter.INSTANCE.startMiniProgramBySearch(activity, key, rules1.get(position));
                                        }

                                        @Override
                                        public void onLongClick(String url, int position) {

                                        }
                                    });
                            new XPopup.Builder(activity)
                                    .asCustom(popup)
                                    .show();
                        }

                    });
                }
            } else if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(() -> ToastMgr.shortBottomCenter(activity, "获取规则列表失败"));
            }
        });
    }

    private static void showInput(Activity activity, Object rule, BaseResultItem resultItem, String content, String input, String jsRule, Consumer<String> reDealConsumer) {
        new XPopup.Builder(activity)
                .asInputConfirm("请输入", content, input, null, text -> {
                    HeavyTaskUtil.executeNewTask(() -> {
                        String result = JSEngine.getInstance().evalJS(JSEngine.getMyRule(rule) + jsRule, text);
                        if (activity != null && !activity.isFinishing()) {
                            activity.runOnUiThread(() -> {
                                if (StringUtil.isNotEmpty(result) && !"undefined".equalsIgnoreCase(result)) {
                                    if (reDealConsumer != null) {
                                        reDealConsumer.accept(result);
                                    } else {
                                        dealUrlSimply(activity, rule, resultItem, result, null);
                                    }
                                }
                            });
                        }
                    });
                }, null, R.layout.xpopup_confirm_input).show();
    }

    public static String getItemTitle(Activity activity, String title) {
        try {
            title = getTitleText(title);
            String tit = getActivityTitle(activity);
            return (StringUtil.isEmpty(tit) ? "" : (tit + "-")) + title;
        } catch (Exception e) {
            return getActivityTitle(activity);
        }
    }

    public static String getTitleText(String title) {
        return getTitleText(title, true);
    }

    public static String getTitleText(String title, boolean limitLength) {
        try {
            if (StringUtil.isEmpty(title)) {
                title = "";
            }
            String trimTitle = StringUtil.trimBlanks(title);
            if (trimTitle.startsWith("““””") || trimTitle.startsWith("‘‘’’")) {
                trimTitle = StringUtils.replaceOnce(trimTitle, "““””", "");
                trimTitle = StringUtils.replaceOnce(trimTitle, "‘‘’’", "");
                Document document = Jsoup.parse(trimTitle);
                title = document.text();
            } else if (trimTitle.startsWith("<") && trimTitle.contains("</")) {
                Document document = Jsoup.parse(trimTitle);
                title = document.text();
            }
            if (limitLength && title.length() > 25) {
                title = title.substring(0, 25) + "...";
            }
            return title.replace("-", "_");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }


    public static String getActivityTitle(Activity activity) {
        String tit = activity == null ? "" : activity.getIntent().getStringExtra("title");
        if (StringUtil.isEmpty(tit)) {
            return "";
        }
        return tit;
    }

    public static ViewCollection getViewCollection(Activity activity, String pageUrl) {
        if (activity instanceof FilmListActivity) {
            String title = activity.getIntent().getStringExtra("title");
            List<ViewCollection> collections = null;
            try {
                collections = LitePal.where("CUrl = ? and MTitle = ?", pageUrl, title).limit(1).find(ViewCollection.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!CollectionUtil.isEmpty(collections)) {
                return collections.get(0);
            }
        }
        return null;
    }

    public static String getViewCollectionExtraData(Activity activity, String pageUrl) {
        if (activity instanceof FilmListActivity) {
            ViewCollectionExtraData extraData = null;
            try {
                ViewCollection collection = getViewCollection(activity, pageUrl);
                extraData = ViewCollectionExtraData.extraDataFromJson(collection.getExtraData());
                // TODO 有点疑惑这里为何要判断？
                if (extraData.isCustomJump() || extraData.isCustomPlayer()) {
                    return ViewCollectionExtraData.extraDataToJson(extraData);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static ViewCollection findViewCollectionById(long id) {
        List<ViewCollection> mCollections = null;
        try {
            mCollections = LitePal.where("id = ?", String.valueOf(id)).limit(1).find(ViewCollection.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CollectionUtil.isEmpty(mCollections)) {
            return null;
        } else {
            return mCollections.get(0);
        }
    }
}
