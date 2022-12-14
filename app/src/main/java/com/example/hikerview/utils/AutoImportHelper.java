package com.example.hikerview.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.bean.MovieInfoUse;
import com.example.hikerview.event.BookmarkRefreshEvent;
import com.example.hikerview.event.LoadingDismissEvent;
import com.example.hikerview.event.OnArticleListRuleChangedEvent;
import com.example.hikerview.event.ToastMessage;
import com.example.hikerview.model.Bookmark;
import com.example.hikerview.model.SearchEngineDO;
import com.example.hikerview.service.http.CharsetStringCallback;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.ui.bookmark.BookmarkActivity;
import com.example.hikerview.ui.bookmark.model.BookmarkModel;
import com.example.hikerview.ui.browser.model.AdBlockModel;
import com.example.hikerview.ui.browser.model.AdUrlBlocker;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.ArticleListRuleEditActivity;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.home.model.ArticleListRuleJO;
import com.example.hikerview.ui.home.model.NetCutResponse;
import com.example.hikerview.ui.home.model.PastemeResponse;
import com.example.hikerview.ui.js.AdUrlListActivity;
import com.example.hikerview.ui.miniprogram.data.RuleDTO;
import com.example.hikerview.ui.rules.service.RuleImporterManager;
import com.example.hikerview.ui.search.engine.SearchEngineMagActivity;
import com.example.hikerview.ui.search.model.SearchRuleJO;
import com.example.hikerview.ui.setting.MoreSettingActivity;
import com.example.hikerview.ui.setting.office.MiniProgramOfficer;
import com.example.hikerview.ui.setting.utils.FastPlayImportUtil;
import com.example.hikerview.ui.setting.utils.XTDialogRulesImportUtil;
import com.example.hikerview.ui.view.DialogBuilder;
import com.example.hikerview.ui.view.ZLoadingDialog.ZLoadingDialog;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.ui.view.dialog.GlobalDialogActivity;
import com.example.hikerview.ui.view.popup.ConfirmPopup;
import com.example.hikerview.ui.view.popup.ImportRulesPopup;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * ?????????By 15968
 * ?????????On 2019/10/19
 * ?????????At 10:29
 */
public class AutoImportHelper {
    private static final String TAG = "AutoImportHelper";

    public static final String BOOKMARK = "bookmark";
    public static final String FAST_PLAY_URLS = "fast_play_urls";
    public static final String XT_DIALOG_RULES = "xt_dialog_rules";
    public static final String HOME_RULE = "home_rule";
    public static final String HOME_RULE_V2 = "home_rule_v2";
    public static final String HOME_RULE_URL = "home_rule_url";
    public static final String JS_URL = "js_url";
    public static final String AD_URL_RULES = "ad_url_rule";
    public static final String SEARCH_ENGINE = "search_engine_v2";
    public static final String SEARCH_ENGINE_v3 = "search_engine_v3";
    public static final String SEARCH_ENGINE_URL = "search_engine_url";
    public static final String BOOKMARK_URL = "bookmark_url";
    public static final String FILE_URL = "file_url";
    public static final String AD_SUBSCRIBE = "ad_subscribe_url";
    public static final String AD_BLOCK_RULES = "ad_block_url";
    public static final String PAGE_DETAIL_RULES = "page_detail";
    public static final String PUBLISH_CODE = "publish";
    public static final String HOME_SUB = "home_sub";
    public static final String MINI_PROGRAM = "mini-program";
    private static final String SOURCE = "???????????????????????????????????????????????????????????????APP??????????????????????????????????????????source???";
    private static String shareRule = "";


    public static String getCommand(String shareRulePrefix, String shareText, String type) {
        String text = "????????????????????????????????????????????????";
        switch (type) {
            case BOOKMARK:
                text = text + "????????????";
                break;
            case FAST_PLAY_URLS:
                text = text + "?????????????????????";
                break;
            case XT_DIALOG_RULES:
                text = text + "?????????????????????";
                break;
            case BOOKMARK_URL:
                text = text + "????????????";
                break;
            case JS_URL:
                text = text + "????????????";
                break;
            case AD_URL_RULES:
                text = text + "??????????????????";
                break;
            case AD_BLOCK_RULES:
                text = text + "??????????????????";
                break;
            case FILE_URL:
                text = text + "????????????";
                break;
            case AD_SUBSCRIBE:
                text = text + "??????????????????";
                break;
            case PAGE_DETAIL_RULES:
                text = text + "??????????????????";
                break;
            case SEARCH_ENGINE_v3:
                text = text + "????????????";
                break;
            case MINI_PROGRAM:
                text = "??????????????????????????????????????????????????????????????????";
                break;
        }
        if (StringUtil.isNotEmpty(shareRulePrefix)) {
            text = text + "???" + shareRulePrefix;
        }
        return text + "???" + type + "???" + shareText;
    }

    public static void shareWithCommand(Context context, String shareText, String type) {
        String shareRulePrefix = PreferenceMgr.getString(context, "shareRulePrefix", "");
        if (FilterUtil.hasFilterWord(shareText)) {
            ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            return;
        }
        String command = getCommand(shareRulePrefix, shareText, type);
        setShareRule(command);
        ClipboardUtil.copyToClipboardForce(context, command, false);
        ToastMgr.shortBottomCenter(context, "???????????????????????????");
    }

    public static String getRealRule(String rule) {
        if (TextUtils.isEmpty(rule)) {
            return rule;
        }
        String[] rules = rule.split("???");
        if (rules.length != 3) {
            return rule;
        } else {
            return rules[2];
        }
    }


    public static boolean checkText(Activity context, String text1) {
        if (StringUtil.isEmpty(text1)) {
            return false;
        }
        if (RuleImporterManager.parse(context, text1)) {
            return true;
        }
        return AutoImportHelper.checkAutoText(context, text1);
    }

    public static boolean checkAutoText(Context context, String shareText) {
        try {
            return realCheckAutoText(context, shareText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean realCheckAutoText(Context context, String shareText) {
        shareText = shareText.trim();
        if (shareText.startsWith("??????")) {
            return checkAutoTextByFangYuan(context, shareText);
        }
        final String[] sss = shareText.split("???");
        if (sss.length < 3) {
            return false;
        }
        switch (sss[1]) {
            case BOOKMARK:
                String finalShareText1 = shareText;
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("??????????????????????????????")
                        .setPositiveListener("??????", dialog -> {
                            dialog.dismiss();
                            ClipboardUtil.copyToClipboard(context, "");
                            try {
                                Intent intent = new Intent(context, BookmarkActivity.class);
                                //??????????????????
                                if (finalShareText1.contains("?????????????????????")) {
                                    showWithAdUrlsDialog(context, "????????????????????????????????????????????????????????????????????????????????????", withAdUrls -> {
                                        if (withAdUrls) {
                                            if (sss.length > 4) {
                                                intent.putExtra("webs", sss[2] + "???" + sss[3] + "???" + sss[4]);
                                            } else {
                                                intent.putExtra("webs", sss[2] + "???" + sss[3]);
                                            }
                                            context.startActivity(intent);
                                        } else {
                                            if (sss.length > 4) {
                                                intent.putExtra("webs", sss[2] + "???" + sss[3] + "???" + sss[4].split("?????????????????????")[0]);
                                            } else {
                                                intent.putExtra("webs", sss[2] + "???" + sss[3].split("?????????????????????")[0]);
                                            }
                                            context.startActivity(intent);
                                        }
                                    });
                                } else {
                                    if (sss.length > 4) {
                                        intent.putExtra("webs", sss[2] + "???" + sss[3] + "???" + sss[4]);
                                    } else {
                                        intent.putExtra("webs", sss[2] + "???" + sss[3]);
                                    }
                                    context.startActivity(intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case SEARCH_ENGINE_v3:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("????????????????????????????????????")
                        .setPositiveListener("??????", dialog -> {
                            dialog.dismiss();
                            ClipboardUtil.copyToClipboard(context, "");
                            try {
                                Intent intent = new Intent(context, SearchEngineMagActivity.class);
                                if (sss.length > 4) {
                                    intent.putExtra("webs", sss[2] + "???" + sss[3] + "???" + sss[4]);
                                } else {
                                    intent.putExtra("webs", sss[2] + "???" + sss[3]);
                                }
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case AD_URL_RULES:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("??????????????????????????????????????????")
                        .setPositiveListener("??????", dialog -> {
                            dialog.dismiss();
                            ClipboardUtil.copyToClipboard(context, "");
                            try {
                                Intent intent = new Intent(context, AdUrlListActivity.class);
                                intent.putExtra("data", sss[2]);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case AD_BLOCK_RULES:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("??????????????????????????????????????????")
                        .setPositiveListener("????????????", dialog -> {
                            dialog.dismiss();
                            ClipboardUtil.copyToClipboard(context, "");
                            DialogBuilder.createInputConfirm(context, "??????????????????", sss[2], text1 -> {
                                String[] ss = text1.split("::");
                                if (text1.contains("##") || ss.length != 2 || TextUtils.isEmpty(ss[0]) || StringUtil.isEmpty(ss[1])) {
                                    ToastMgr.shortBottomCenter(context, "????????????");
                                } else {
                                    AdBlockModel.saveRule(ss[0], ss[1]);
                                    ToastMgr.shortBottomCenter(context, "???????????????");
                                }
                            }).show();
                        }).show();
                return true;
            case AD_SUBSCRIBE:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("???????????????????????????????????????????????????????????????")
                        .setPositiveListener("????????????", dialog -> {
                            dialog.dismiss();
                            try {
                                ClipboardUtil.copyToClipboard(context, "");
                                MoreSettingActivity.addAdSubRule(context, sss[2]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case FAST_PLAY_URLS:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("??????????????????????????????????????????????????????????????????")
                        .setPositiveListener("??????", dialog -> {
                            dialog.dismiss();
                            try {
                                if (sss.length != 3) {
                                    ToastMgr.shortBottomCenter(context, "???????????????");
                                    return;
                                }
                                ClipboardUtil.copyToClipboard(context, "");
                                FastPlayImportUtil.importRules(context, sss[2], count -> EventBus.getDefault().post(new ToastMessage("????????????" + count + "?????????")));
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case XT_DIALOG_RULES:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("??????????????????????????????????????????????????????????????????")
                        .setPositiveListener("??????", dialog -> {
                            dialog.dismiss();
                            if (sss.length != 3) {
                                ToastMgr.shortBottomCenter(context, "???????????????");
                                return;
                            }
                            ClipboardUtil.copyToClipboard(context, "");
                            try {
                                XTDialogRulesImportUtil.importRules(context, sss[2], count -> EventBus.getDefault().post(new ToastMessage("????????????" + count + "?????????")));
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case BOOKMARK_URL:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("????????????????????????????????????????????????????????????")
                        .setPositiveListener("????????????", dialog -> {
                            dialog.dismiss();
                            if (sss.length != 3) {
                                ToastMgr.shortBottomCenter(context, "???????????????");
                                return;
                            }
                            ClipboardUtil.copyToClipboard(context, "");
                            importRulesWithDialog(context, data -> {
                                if (StringUtil.isEmpty(sss[2]) || (!sss[2].startsWith("http") && !sss[2].startsWith("file")) && !sss[2].startsWith("hiker://")) {
                                    ToastMgr.shortBottomCenter(context, "???????????????");
                                    return;
                                }
                                if (StringUtil.isNotEmpty(data)) {
                                    BackupUtil.backupDB(context, true);
                                }
                                LitePal.deleteAll(Bookmark.class);
                                importBookmarkRulesByUrl(context, sss[2]);
                            }, data -> {
                                if (StringUtil.isNotEmpty(data)) {
                                    BackupUtil.backupDB(context, true);
                                }
                                importBookmarkRulesByUrl(context, sss[2]);
                            });
                        }).show();
                return true;
            case FILE_URL:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("???????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                        .setPositiveListener("????????????", dialog -> {
                            dialog.dismiss();
                            if (sss.length != 3) {
                                ToastMgr.shortBottomCenter(context, "???????????????");
                                return;
                            }
                            ClipboardUtil.copyToClipboard(context, "");
                            try {
                                importFileByUrl(context, sss[2]);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case JS_URL:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("??????????????????????????????????????????????????????????????????")
                        .setPositiveListener("????????????", dialog -> {
                            dialog.dismiss();
                            try {
                                ClipboardUtil.copyToClipboard(context, "");
                                String[] js = sss[2].split("@");
                                Log.d(TAG, "checkAutoText: sss[2]=" + sss[2] + "???js=" + Arrays.toString(js));
                                if (js.length < 2) {
                                    ToastMgr.shortBottomCenter(context, "???????????????");
                                    return;
                                }
                                if (!JSManager.instance(context).hasJs(js[0])) {
                                    updateJsNow(context, js);
                                } else {
                                    new XPopup.Builder(context)
                                            .asConfirm("????????????", "?????????????????????" + js[0] + "??????????????????????????????????????????????????????",
                                                    () -> updateJsNow(context, js)).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastMgr.shortCenter(context, "?????????" + e.getMessage());
                            }
                        }).show();
                return true;
            case MINI_PROGRAM:
                new PromptDialog(context)
                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                        .setAnimationEnable(true)
                        .setTitleText("????????????")
                        .setContentText("???????????????????????????????????????????????????????????????")
                        .setPositiveListener("????????????", dialog -> {
                            dialog.dismiss();
                            ClipboardUtil.copyToClipboard(context, "");
                            String ruleStr;
                            if (sss.length != 3) {
                                ruleStr = StringUtil.arrayToString(sss, 2, sss.length, "???");
                            } else {
                                ruleStr = sss[2];
                            }
                            importMiniProgram(context, ruleStr);
                        }).show();
                return true;
            default:
                return false;
        }
    }

    private static void importMiniProgram(Context context, String text) {
        try {
            RuleDTO ruleDTO = JSON.parseObject(text, RuleDTO.class);
            if (ruleDTO != null) {
                MiniProgramOfficer.INSTANCE.editMiniProgram(context, ruleDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastMgr.shortBottomCenter(context, "?????????" + e.getMessage());
        }
    }

    private static void importRuleByRuleText(Context context, String ruleStr) {
        String rule = ruleStr;
        if (ruleStr.startsWith("base64://")) {
            try {
                String realRule = ruleStr.split("@")[2];
                rule = new String(Base64.decode(StringUtil.replaceLineBlank(realRule), Base64.NO_WRAP));
            } catch (Exception e) {
                ToastMgr.shortBottomCenter(context, "???????????????" + e.getMessage());
                return;
            }
        }
        if (FilterUtil.hasFilterWord(rule)) {
            ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            return;
        }
        ArticleListRuleJO ruleJO = null;
        try {
            ruleJO = JSON.parseObject(rule, ArticleListRuleJO.class);
        } catch (Exception e) {
            ToastMgr.shortBottomCenter(context, "???????????????" + e.getMessage());
            return;
        }
        if (ruleJO == null || StringUtil.isEmpty(ruleJO.getTitle())) {
            return;
        }
        //??????????????????
        if (StringUtil.isNotEmpty(ruleJO.getAdBlockUrls())) {
            ArticleListRuleJO finalRuleJO = ruleJO;
            String finalRule = rule;
            showWithAdUrlsDialog(context, "??????????????????????????????????????????????????????????????????????????????????????????", withAdUrls -> {
                Timber.d("checkAutoText: withAdUrls=%s", withAdUrls);
                if (withAdUrls) {
                    String[] urls = finalRuleJO.getAdBlockUrls().split("##");
                    HeavyTaskUtil.executeNewTask(() -> AdUrlBlocker.instance().addUrls(Arrays.asList(urls)));
                }
                Intent intent = new Intent(context, ArticleListRuleEditActivity.class);
                intent.putExtra("data", finalRule);
                context.startActivity(intent);
            });
        } else {
            Intent intent = new Intent(context, ArticleListRuleEditActivity.class);
            intent.putExtra("data", rule);
            context.startActivity(intent);
        }
    }

    private static void updateJsNow(Context context, String[] js) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        final ZLoadingDialog loadingDialog = DialogBuilder.createLoadingDialog(context, false);
        loadingDialog.show();
        if (js[1].startsWith("base64://")) {
            if ("base64://".equals(js[1])) {
                loadingDialog.dismiss();
                ToastMgr.shortBottomCenter(context, "???????????????");
                return;
            }
            String decodeStr = null;
            try {
                decodeStr = new String(Base64.decode(StringUtil.replaceLineBlank(js[1]).replace("base64://", ""), Base64.NO_WRAP));
            } catch (Exception e) {
                Log.e(TAG, "checkAutoText: " + e.getMessage(), e);
                ToastMgr.shortBottomCenter(context, "BASE64??????????????????????????????????????????");
                return;
            }
            boolean ok = JSManager.instance(context).updateJs(js[0], decodeStr);
            loadingDialog.dismiss();
            if (context instanceof Activity && ((Activity) context).isFinishing()) {
                return;
            }
            if (ok) {
                ToastMgr.shortBottomCenter(context, js[0] + "?????????????????????????????????");
            } else {
                ToastMgr.shortBottomCenter(context, js[0] + "?????????????????????????????????");
            }
            return;
        }
        CodeUtil.get(js[1], new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                if (context instanceof Activity && ((Activity) context).isFinishing()) {
                    return;
                }
                boolean ok = JSManager.instance(context).updateJs(js[0], s);
                loadingDialog.dismiss();
                if (ok) {
                    ToastMgr.shortBottomCenter(context, js[0] + "?????????????????????????????????");
                } else {
                    ToastMgr.shortBottomCenter(context, js[0] + "?????????????????????????????????");
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                if (context instanceof Activity && ((Activity) context).isFinishing()) {
                    return;
                }
                loadingDialog.dismiss();
                ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            }
        });
    }


    private static void importRulesWithDialog(Context context, OnOkListener deleteImportListener, OnOkListener onlyImportListener) {
        ImportRulesPopup popup = new ImportRulesPopup(context).withListener((backup, delete) -> {
            if (delete) {
                deleteImportListener.ok(backup ? "backup" : null);
            } else {
                onlyImportListener.ok(backup ? "backup" : null);
            }
        });
        new XPopup.Builder(context)
                .asCustom(popup)
                .show();
    }

    private interface OnOkListener {
        void ok(String data);
    }

    private static void importFileByUrl(Context context, String sss) {
        String[] urls = sss.split("@");
        if (urls.length != 2 || StringUtil.isEmpty(urls[0]) || StringUtil.isEmpty(urls[1])
                || (!urls[1].startsWith("http") && !urls[1].startsWith("file"))
                || (!urls[0].startsWith("hiker://files/") && !urls[0].startsWith("file://"))) {
            ToastMgr.shortBottomCenter(context, "???????????????");
            return;
        }
        String filePath = urls[0];
        if (filePath.startsWith("hiker://files/")) {
            String fileName = filePath.replace("hiker://files/", "");
            filePath = UriUtils.getRootDir(context) + File.separator + fileName;
        } else if (filePath.startsWith("file://")) {
            filePath = filePath.replace("file://", "");
        }
        if (!filePath.startsWith(UriUtils.getRootDir(context))) {
            ToastMgr.shortBottomCenter(context, "???????????????");
            return;
        }
        String finalFilePath = filePath;
        CodeUtil.get(urls[1], new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                if (StringUtil.isEmpty(s)) {
                    s = "";
                }
                if (FilterUtil.hasFilterWord(s)) {
                    ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
                    return;
                }
                try {
                    FileUtil.stringToFile(s, finalFilePath);
                    ToastMgr.shortBottomCenter(context, "?????????????????????????????????" + finalFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            }
        });
    }

    private static void importSearchRulesByUrl(Context context, String url) {
        if (StringUtil.isEmpty(url) || (!url.startsWith("http") && !url.startsWith("file")) && !url.startsWith("hiker://")) {
            ToastMgr.shortBottomCenter(context, "???????????????");
            return;
        }
        CodeUtil.get(url, new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                if (StringUtil.isEmpty(s)) {
                    ToastMgr.shortBottomCenter(context, "???????????????????????????");
                    return;
                }
                if (FilterUtil.hasFilterWord(s)) {
                    ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
                    return;
                }
                List<SearchRuleJO> ruleJOList = JSON.parseArray(s, SearchRuleJO.class);
                if (CollectionUtil.isEmpty(ruleJOList)) {
                    ToastMgr.shortBottomCenter(context, "???????????????????????????");
                    return;
                }
                int c = LitePal.count(SearchEngineDO.class);
                if (ruleJOList.size() > 800 || c > 800) {
                    ToastMgr.shortBottomCenter(context, "?????????????????????????????????" + c + "?????????????????????" + ruleJOList.size() + "?????????????????????800??????");
                    return;
                }
                GlobalDialogActivity.startLoading(context, "?????????????????????" + ruleJOList.size() + "?????????");
                HeavyTaskUtil.executeNewTask(() -> {
                    for (int i = 0; i < ruleJOList.size(); i++) {
                        ruleJOList.get(i).toEngineDO(false).save();
                    }
                    EventBus.getDefault().postSticky(new LoadingDismissEvent("???????????????" + ruleJOList.size() + "?????????"));
                });
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            }
        });
    }

    private static void importBookmarkRulesByUrl(Context context, String url) {
        if (StringUtil.isEmpty(url) || (!url.startsWith("http") && !url.startsWith("file")) && !url.startsWith("hiker://")) {
            ToastMgr.shortBottomCenter(context, "???????????????");
            return;
        }
        CodeUtil.get(url, new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                importBookmarkByStr(context, s);
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            }
        });
    }

    public static void importBookmarkByStr(Context context, String s) {
        if (StringUtil.isEmpty(s)) {
            ToastMgr.shortBottomCenter(context, "???????????????????????????");
            return;
        }
        List<Bookmark> ruleJOList = JSON.parseArray(s, Bookmark.class);
        if (CollectionUtil.isEmpty(ruleJOList)) {
            ToastMgr.shortBottomCenter(context, "???????????????????????????");
            return;
        }
        for (Bookmark bookmark : ruleJOList) {
            bookmark.setDir(bookmark.isDirTag());
        }

        GlobalDialogActivity.startLoading(context, "?????????????????????" + ruleJOList.size() + "?????????");
        HeavyTaskUtil.executeNewTask(() -> {
            int count = BookmarkModel.addByList(context, ruleJOList);
            EventBus.getDefault().postSticky(new LoadingDismissEvent("???????????????" + count + "?????????"));
            EventBus.getDefault().post(new BookmarkRefreshEvent());
        });
    }

    private static void importHomeRulesByText(Context context, String s) {
        if (StringUtil.isEmpty(s)) {
            ToastMgr.shortBottomCenter(context, "???????????????????????????");
            return;
        }
        if (FilterUtil.hasFilterWord(s)) {
            ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            return;
        }
        List<ArticleListRuleJO> ruleJOList = JSON.parseArray(s, ArticleListRuleJO.class);
        if (CollectionUtil.isEmpty(ruleJOList)) {
            ToastMgr.shortBottomCenter(context, "???????????????????????????");
            return;
        }
        int c = LitePal.count(ArticleListRule.class);
        if (ruleJOList.size() > 800 || c > 800) {
            ToastMgr.shortBottomCenter(context, "?????????????????????????????????" + c + "?????????????????????" + ruleJOList.size() + "?????????????????????800??????");
            return;
        }
        GlobalDialogActivity.startLoading(context, "?????????????????????" + ruleJOList.size() + "?????????");
        HeavyTaskUtil.executeNewTask(() -> {
            for (int i = 0; i < ruleJOList.size(); i++) {
                List<ArticleListRule> rule = LitePal.where("title = ?", ruleJOList.get(i).getTitle()).find(ArticleListRule.class);
                if (CollectionUtil.isNotEmpty(rule)) {
                    String color = rule.get(0).getTitleColor();
                    String group = rule.get(0).getGroup();
                    rule.get(0).fromJO(ruleJOList.get(i));
                    rule.get(0).setTitleColor(color);
                    rule.get(0).setGroup(group);
                    rule.get(0).save();
                } else {
                    new ArticleListRule().fromJO(ruleJOList.get(i)).save();
                }
            }
            EventBus.getDefault().postSticky(new LoadingDismissEvent("???????????????" + ruleJOList.size() + "?????????"));
            EventBus.getDefault().post(new OnArticleListRuleChangedEvent());
        });
    }


    private static void importHomeRulesByUrl(Context context, String url) {
        if (StringUtil.isEmpty(url) || (!url.startsWith("http") && !url.startsWith("file")) && !url.startsWith("hiker://")) {
            ToastMgr.shortBottomCenter(context, "???????????????");
            return;
        }
        CodeUtil.get(url, new CodeUtil.OnCodeGetListener() {
            @Override
            public void onSuccess(String s) {
                importHomeRulesByText(context, s);
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                ToastMgr.shortBottomCenter(context, "????????????????????????????????????");
            }
        });
    }

    /**
     * ?????????????????????
     *
     * @param context
     * @param shareText
     * @return
     */
    private static boolean checkAutoTextByFangYuan(Context context, String shareText) {
        final String[] sss = shareText.split("???");
        if (sss.length < 2) {
            return false;
        }
        switch (sss[1]) {
            case "source":
                //????????????
                if (sss.length > 3) {
                    new PromptDialog(context)
                            .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                            .setAnimationEnable(true)
                            .setTitleText("????????????")
                            .setContentText("????????????????????????????????????????????????????????????")
                            .setPositiveListener("????????????", dialog -> {
                                dialog.dismiss();
                                importFangYuanSources(context, shareText);
                                ClipboardUtil.copyToClipboard(context, "");
                            }).show();
                    return true;
                }
                if (sss.length != 3) {
                    return false;
                }
                MovieInfoUse movieInfoUse = null;
                try {
                    movieInfoUse = JSON.parseObject(sss[2], MovieInfoUse.class);
                } catch (Exception e) {
                    ToastMgr.shortBottomCenter(context, "???????????????" + e.getMessage());
                    return false;
                }
                if (StringUtil.isEmpty(movieInfoUse.getTitle()) || StringUtil.isEmpty(movieInfoUse.getSearchUrl())) {
                    return false;
                }
                String[] s = movieInfoUse.getTitle().split("???");
                SearchRuleJO searchRuleJO = new SearchRuleJO();
                searchRuleJO.setTitle(movieInfoUse.getTitle());
                if (s.length > 1) {
                    searchRuleJO.setGroup(s[1].split("???")[0]);
                }
                searchRuleJO.setSearch_url(movieInfoUse.getSearchUrl());
                searchRuleJO.setFind_rule(movieInfoUse.getSearchFind());
                String rule = JSON.toJSONString(searchRuleJO);
                String shareRulePrefix = PreferenceMgr.getString(context, "shareRulePrefix", "");
                String command = getCommand(shareRulePrefix, rule, SEARCH_ENGINE);
                return checkAutoText(context, command);
        }
        return false;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param context
     * @param shareText
     * @return
     */
    private static boolean importFangYuanSources(Context context, String shareText) {
        String[] all = shareText.split(SOURCE);
        int k = 0;
        if (TextUtils.isEmpty(all[0])) {
            k = 1;
        }
        if (all.length - k < 2) {
            return false;
        }
        int c = LitePal.count(SearchEngineDO.class);
        if (c > 800) {
            ToastMgr.shortBottomCenter(context, "????????????????????????800??????????????????");
            return false;
        }
        int count = 0;
        for (int i = k; i < all.length; i++) {
            String text = StringUtil.replaceBlank(all[i]);
            MovieInfoUse movieInfoUse = null;
            try {
                movieInfoUse = JSON.parseObject(text, MovieInfoUse.class);
            } catch (Exception e) {
                continue;
            }
            if (StringUtil.isEmpty(movieInfoUse.getTitle()) || StringUtil.isEmpty(movieInfoUse.getSearchUrl())) {
                continue;
            }
            String[] s = movieInfoUse.getTitle().split("???");
            SearchRuleJO searchRuleJO = new SearchRuleJO();
            searchRuleJO.setTitle(movieInfoUse.getTitle());
            if (s.length > 1) {
                searchRuleJO.setGroup(s[1].split("???")[0]);
            }
            searchRuleJO.setSearch_url(movieInfoUse.getSearchUrl());
            searchRuleJO.setFind_rule(movieInfoUse.getSearchFind());
            if (importSearchRule(JSON.toJSONString(searchRuleJO))) {
                count++;
            }
        }
        ClipboardUtil.copyToClipboard(context, "");
        if (count <= 0) {
            return false;
        } else {
            ToastMgr.shortBottomCenter(context, "?????????" + count + "?????????????????????");
            return true;
        }
    }

    /**
     * ????????????????????????
     *
     * @param context
     * @param shareText
     */
    private static void importSearchRules(Context context, String shareText) {
        Log.d(TAG, "importSearchRules: " + shareText);
        int c = LitePal.count(SearchEngineDO.class);
        if (c > 800) {
            ToastMgr.shortBottomCenter(context, "????????????????????????800??????????????????");
            return;
        }
        String shareRulePrefix = PreferenceMgr.getString(context, "shareRulePrefix", "");
        String[] all = shareText.split(getCommand(shareRulePrefix, "", SEARCH_ENGINE));
        int k = 0;
        if (TextUtils.isEmpty(all[0])) {
            k = 1;
        }
        if (all.length - k < 2) {
            return;
        }
        int count = 0;
        for (int i = k; i < all.length; i++) {
            String s = StringUtil.replaceBlank(all[i]);
            if (importSearchRule(s)) {
                count++;
            }
        }
        ClipboardUtil.copyToClipboard(context, "");
        ToastMgr.shortBottomCenter(context, "?????????" + count + "?????????????????????");
    }

    /**
     * ????????????????????????
     *
     * @param rule
     * @return
     */
    private static boolean importSearchRule(String rule) {
        Log.d(TAG, "importSearchRule: " + rule);
        SearchRuleJO searchRuleJO = null;
        try {
            searchRuleJO = JSON.parseObject(rule, SearchRuleJO.class);
        } catch (Exception e) {
            return false;
        }
        if (searchRuleJO == null || StringUtil.isEmpty(searchRuleJO.getTitle())) {
            return false;
        }
        SearchEngineDO engineDO = searchRuleJO.toEngineDO(false);
        engineDO.save();
        //??????????????????
        if (StringUtil.isNotEmpty(searchRuleJO.getAdBlockUrls())) {
            String[] urls = searchRuleJO.getAdBlockUrls().split("##");
            HeavyTaskUtil.executeNewTask(() -> AdUrlBlocker.instance().addUrls(Arrays.asList(urls)));
        }
        return true;
    }

    public static String getShareRule() {
        return shareRule;
    }

    public static void setShareRule(String shareRule) {
        AutoImportHelper.shareRule = shareRule;
    }


    public static void showWithAdUrlsDialog(Context context, String title, OnOkWithAdUrlsListener listener) {
        final View view1 = LayoutInflater.from(context).inflate(R.layout.view_dialog_import_with_block_urls, null, false);
        final TextView titleE = view1.findViewById(R.id.import_rule_title);
        titleE.setText(title);
        titleE.setTag("with");
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("????????????")
                .setView(view1)
                .setCancelable(true)
                .setPositiveButton("??????", (dialog, which) -> {
                    String with = (String) titleE.getTag();
                    Log.d(TAG, "showWithAdUrlsDialog: " + with);
                    listener.ok("with".equals(with));
                    dialog.dismiss();
                }).setNegativeButton("??????", (dialog, which) -> dialog.dismiss()).create();
        Button import_rule_btn = view1.findViewById(R.id.import_rule_btn);
        Button not_import_rule_btn = view1.findViewById(R.id.not_import_rule_btn);
        import_rule_btn.setOnClickListener(v -> {
            titleE.setTag("with");
            import_rule_btn.setBackground(context.getResources().getDrawable(R.drawable.button_layer_red));
            not_import_rule_btn.setBackground(context.getResources().getDrawable(R.drawable.button_layer));
        });
        not_import_rule_btn.setOnClickListener(v -> {
            titleE.setTag("without");
            import_rule_btn.setBackground(context.getResources().getDrawable(R.drawable.button_layer));
            not_import_rule_btn.setBackground(context.getResources().getDrawable(R.drawable.button_layer_red));
        });
        alertDialog.show();
    }

    public static void shareByPasteme(Activity activity, String paste, String title) {
        new XPopup.Builder(activity)
                .asCustom(new ConfirmPopup(activity)
                        .bind("??????????????????", "????????????????????????", new ConfirmPopup.OkListener() {
                            @Override
                            public void ok(String text) {
                                shareByPasteme(activity, paste, title, text, "?????????");
                            }

                            @Override
                            public void cancel() {
                                String pwd = StringUtil.genRandomPwd(6, true);
                                shareByPasteme(activity, paste, title, pwd, "?????????");
                            }
                        }).setBtn("??????", "????????????")).show();
    }

    public static void shareByPasteme(Activity activity, String paste, String title, @Nullable String password, String rulePrefix) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lang", "plain");
            jsonObject.put("content", paste);
            if (StringUtil.isNotEmpty(password)) {
                jsonObject.put("password", password);
            }
            BasePopupView popupView = new XPopup.Builder(activity)
                    .asLoading("????????????????????????")
                    .show();
            PostRequest<String> request = OkGo.post("https://pasteme.cn/_api/backend/");
            request.upJson(jsonObject)
                    .headers(":authority", "pasteme.cn")
                    .headers(":method", "POST")
                    .headers(":path", "/_api/backend/")
                    .headers(":scheme", "https")
                    .headers("referer", "https://pasteme.cn/")
                    .headers("cookie", "")
                    .execute(new CharsetStringCallback("UTF-8") {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<String> res) {
                            String s = res.body();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            if (StringUtil.isEmpty(s)) {
                                activity.runOnUiThread(() -> {
                                    if (popupView != null) {
                                        popupView.dismiss();
                                    }
                                });
                                return;
                            }
                            activity.runOnUiThread(() -> {
                                if (popupView != null) {
                                    popupView.dismiss();
                                }
                                try {
                                    Timber.d("shareByPasteme onSuccess: %s", s);
                                    PastemeResponse response = JSON.parseObject(s, PastemeResponse.class);
                                    if (response == null || StringUtil.isEmpty(response.getKey())) {
                                        ToastMgr.shortCenter(activity, "????????????????????????");
                                    } else {
                                        String url = "https://pasteme.cn/" + response.getKey();
                                        if (StringUtil.isNotEmpty(password)) {
                                            url = url + " " + password;
                                        }
                                        url = url + "\n\n" + rulePrefix + "???" + title;
                                        ClipboardUtil.copyToClipboardForce(activity, url, false);
                                        AutoImportHelper.setShareRule(url);
                                        ToastMgr.shortBottomCenter(activity, "???????????????????????????????????????");
                                    }
                                } catch (Exception e) {
                                    String msg = e.getMessage();
                                    if (msg != null && msg.length() > 20) {
                                        msg = msg.substring(0, 20);
                                    }
                                    ToastMgr.shortCenter(activity, "????????????????????????:" + msg);
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void onError(com.lzy.okgo.model.Response<String> response) {
                            super.onError(response);
                            String msg = response.getException().toString();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }
                            activity.runOnUiThread(() -> {
                                if (popupView != null) {
                                    popupView.dismiss();
                                }
                                ToastMgr.shortCenter(activity, "???????????????????????????" + msg);
                            });
                        }
                    });
        } catch (JSONException e) {
            ToastMgr.shortCenter(activity, "???????????????????????????" + e.getMessage());
        }
    }


    /**
     * ??????pasteme?????????????????????
     *
     * @param url pasteme??????
     */
    public static void checkClipboardByPasteme(Activity activity, String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0];
            String[] urls = url.split(" ");
            if (urls.length == 1) {
                url = url.replace("//pasteme.cn/", "//pasteme.cn/_api/backend/") + "?json=true";
            } else {
                url = urls[0].replace("//pasteme.cn/", "//pasteme.cn/_api/backend/") + "," + urls[1] + "?json=true";
            }
            GetRequest<String> request = OkGo.get(url);
            request.headers(":authority", "pasteme.cn")
                    .headers(":method", "POST")
                    .headers(":path", "/_api/backend/")
                    .headers(":scheme", "https")
                    .headers("referer", "https://pasteme.cn/")
                    .headers("cookie", "")
                    .execute(new CharsetStringCallback("UTF-8") {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<String> res) {
                            String s = res.body();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }
                            if (StringUtil.isEmpty(s)) {
                                return;
                            }
                            activity.runOnUiThread(() -> {
                                Timber.d("checkClipboardByPasteme onSuccess: %s", s);
                                try {
                                    PastemeResponse response = JSON.parseObject(s, PastemeResponse.class);
                                    if (response != null && !StringUtil.isEmpty(response.getContent())) {
                                        try {
                                            checkAutoText(activity, response.getContent());
                                        } catch (Exception ignored) {
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void onError(com.lzy.okgo.model.Response<String> response) {
                            super.onError(response);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareByNetCut(Activity activity, String paste, String title, String rulePrefix) {
        PostRequest<String> request = OkGo.post("http://netcut.cn/api/note/create/");
        String noteName = "hk" + TimeUtil.getSecondTimestamp();
        request.params("note_name", noteName)
                .params("note_content", paste)
                .params("note_pwd", "0")
                .params("expire_time", "31536000")
                .execute(new CharsetStringCallback("UTF-8") {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> res) {
                        String s = res.body();
                        Timber.d("shareByNetCut, response:%s", s);
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }

                        if (StringUtil.isEmpty(s)) {
                            return;
                        }
                        activity.runOnUiThread(() -> {
                            try {
                                NetCutResponse response = JSON.parseObject(s, NetCutResponse.class);
                                if (response == null || response.getData() == null || response.getStatus() != 1) {
                                    ToastMgr.shortCenter(activity, "????????????????????????:" + (response == null ? "null" : response.getError()));
                                } else {
                                    String url = "http://netcut.cn/p/" + response.getData().getNote_id();
                                    url = url + "\n\n" + rulePrefix + "???" + title;
                                    ClipboardUtil.copyToClipboardForce(activity, url, false);
                                    AutoImportHelper.setShareRule(url);
                                    ToastMgr.shortBottomCenter(activity, "???????????????????????????????????????");
                                }
                            } catch (Exception e) {
                                String msg = e.getMessage();
                                if (msg != null && msg.length() > 20) {
                                    msg = msg.substring(0, 20);
                                }
                                ToastMgr.shortCenter(activity, "????????????????????????:" + msg);
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        String msg = response.getException().toString();
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }
                        activity.runOnUiThread(() -> {
                            ToastMgr.shortCenter(activity, "???????????????????????????" + msg);
                        });
                    }
                });
    }


    /**
     * ??????netcut?????????????????????
     *
     * @param url ??????
     */
    public static void checkClipboardByNetCut(Activity activity, String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0];
            String[] urls = url.split(" ");
            String[] noteIdUrl = urls[0].split("/p/");
            if (noteIdUrl.length != 2) {
                return;
            }
            String noteId = noteIdUrl[1];
            url = "http://netcut.cn/api/note/data/?note_id=" + noteId;
            CodeUtil.get(url, new CodeUtil.OnCodeGetListener() {
                @Override
                public void onSuccess(String s) {
                    if (activity.isFinishing()) {
                        return;
                    }
                    if (StringUtil.isEmpty(s)) {
                        return;
                    }
                    activity.runOnUiThread(() -> {
                        NetCutResponse response = JSON.parseObject(s, NetCutResponse.class);
                        if (response != null && response.getData() != null && StringUtil.isNotEmpty(response.getData().getNote_content())) {
                            try {
                                checkAutoText(activity, response.getData().getNote_content());
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }

                @Override
                public void onFailure(int errorCode, String msg) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface OnOkWithAdUrlsListener {
        void ok(boolean withAdUrls);
    }
}
