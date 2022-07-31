package com.example.hikerview.ui.rules.utils;

import android.app.Activity;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.event.ShowToastMessageEvent;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.rules.PublishCodeEditActivity;
import com.example.hikerview.ui.rules.model.AccountPwd;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.XPopup;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/1/2
 * 时间：At 15:10
 */

public class PublishHelper {

    public static void publishRule(Activity context, Object rule) {
        String jsCode = getPublishCode();
        if (StringUtil.isEmpty(jsCode)) {
            //未设置云仓库规则
            showPublishGuide(context);
        } else {
            //后台提交云仓库
            HeavyTaskUtil.executeNewTask(() -> {
                try {
                    String resp = JSEngine.getInstance().parsePublishRule(rule, jsCode, getPublishAccount());
                    if ("success".equals(resp)) {
                        EventBus.getDefault().post(new ShowToastMessageEvent("提交云仓库完成"));
                    } else if (StringUtil.isNotEmpty(resp) && (resp.contains("success") || resp.contains("成功") || resp.contains("完成"))) {
                        EventBus.getDefault().post(new ShowToastMessageEvent(resp));
                    } else {
                        EventBus.getDefault().post(new ShowToastMessageEvent("提交云仓库失败：" + resp));
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    EventBus.getDefault().post(new ShowToastMessageEvent("提交云仓库失败：" + e.getMessage()));
                }
            });
        }
    }

    public static void showPublishGuide(Activity activity) {
        new XPopup.Builder(activity)
                .asConfirm("提交云仓库说明", "当前未设置提交云仓库规则。开发者可设置提交云仓库的JS规则，在规则里面使用MY_RULE变量获取提交的规则详情，" +
                        "使用my_rule变量获取规则字符串，然后提交到云仓库最后返回一个包含‘成功’或‘success’或‘完成’的字符串即可", "取消", "设置规则", () -> {
                    activity.startActivity(new Intent(activity, PublishCodeEditActivity.class));
                }, () -> {

                }, false, 0).show();
    }

    public static void savePublishCode(String code) {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.PUBLISH_CODE_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(BigTextDO.PUBLISH_CODE_KEY);
        }
        bigTextDO.setValue(code);
        bigTextDO.save();
    }

    public static String getPublishCode() {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.PUBLISH_CODE_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            return "";
        } else {
            return bigTextDO.getValue();
        }
    }

    public static AccountPwd getPublishAccount() {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.PUBLISH_ACCOUNT_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            return new AccountPwd();
        } else {
            AccountPwd accountPwd = JSON.parseObject(bigTextDO.getValue(), AccountPwd.class);
            if (accountPwd == null) {
                return new AccountPwd();
            } else {
                return accountPwd;
            }
        }
    }

    public static void savePublishAccount(AccountPwd accountPwd) {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.PUBLISH_ACCOUNT_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(BigTextDO.PUBLISH_ACCOUNT_KEY);
        }
        bigTextDO.setValue(JSON.toJSONString(accountPwd));
        bigTextDO.save();
    }
}
