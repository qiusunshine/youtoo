package com.example.hikerview.ui.rules.service;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.function.Consumer;
import com.example.hikerview.event.OnArticleListRuleChangedEvent;
import com.example.hikerview.event.OnSubRefreshEvent;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.home.model.ArticleListRuleJO;
import com.example.hikerview.ui.rules.ImportSubscribeCheckPopup;
import com.example.hikerview.ui.rules.model.SubscribeRecord;
import com.example.hikerview.utils.FilterUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lzy.okgo.OkGo;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 作者：By 15968
 * 日期：On 2021/3/3
 * 时间：At 23:50
 */

public class HomeRulesSubService {

    public static List<SubscribeRecord> getSubRecords() {
        String text = BigTextDO.getHomeSub();
        if (StringUtil.isEmpty(text)) {
            return new ArrayList<>();
        }
        return JSON.parseArray(text, SubscribeRecord.class);
    }

    public static void addSubWithPopup(Context context, String titleDefault, String urlDefault, Consumer<Boolean> finishListener) {
        ImportSubscribeCheckPopup inputPopup = new ImportSubscribeCheckPopup(context)
                .bind("新建规则订阅", "订阅名称", titleDefault, "订阅地址", urlDefault, "是否立即启用", true,
                        (title, code, checked, addAndUpdate) -> {
                    boolean ok = HomeRulesSubService.addSubRecords(context, title, code, checked, addAndUpdate);
                    finishListener.accept(ok);
                });
        new XPopup.Builder(context)
                .asCustom(inputPopup)
                .show();
    }

    private static boolean addSubRecords(Context context, String title, String url, boolean useNow, boolean addAndUpdate) {
        if (StringUtil.isEmpty(title)) {
            ToastMgr.shortBottomCenter(context, "订阅名称不能为空");
            return false;
        }
        if (StringUtil.isEmpty(url)) {
            ToastMgr.shortBottomCenter(context, "订阅地址不能为空");
            return false;
        }
        if (!url.toLowerCase().startsWith("http")) {
            ToastMgr.shortBottomCenter(context, "订阅地址格式有误，仅支持网络地址");
            return false;
        }
        List<SubscribeRecord> records = getSubRecords();
        for (SubscribeRecord record : records) {
            if (title.equals(record.getTitle())) {
                ToastMgr.shortBottomCenter(context, "订阅名称已存在");
                return false;
            }
            if (url.equals(record.getUrl())) {
                ToastMgr.shortBottomCenter(context, "订阅地址已存在");
                return false;
            }
        }
        SubscribeRecord record = new SubscribeRecord();
        record.setTitle(title);
        record.setUrl(url);
        record.setCreateDate(new Date());
        record.setUse(useNow);
        record.setOnlyUpdate(!addAndUpdate);
        records.add(record);
        BigTextDO.updateHomeSub(JSON.toJSONString(records));
        checkUpdateAsync(record);
        return true;
    }

    public static void removeSubRecords(String title) {
        List<SubscribeRecord> records = getSubRecords();
        for (SubscribeRecord record : records) {
            if (title.equals(record.getTitle())) {
                records.remove(record);
                BigTextDO.updateHomeSub(JSON.toJSONString(records));
                return;
            }
        }
    }

    public static void updateSubRecords(SubscribeRecord subscribeRecord) {
        List<SubscribeRecord> records = getSubRecords();
        for (int i = 0; i < records.size(); i++) {
            SubscribeRecord record = records.get(i);
            if (subscribeRecord.getTitle().equals(record.getTitle())) {
                records.remove(i);
                records.add(i, subscribeRecord);
                BigTextDO.updateHomeSub(JSON.toJSONString(records));
                return;
            }
        }
    }

    public static void batchCheckUpdate() {
        HeavyTaskUtil.executeNewTask(() -> {
            List<SubscribeRecord> records = getSubRecords();
            if (CollectionUtil.isNotEmpty(records)) {
                int count = 0;
                for (SubscribeRecord record : records) {
                    count = count + checkUpdate(record);
                }
                if (count > 0) {
                    EventBus.getDefault().post(new OnArticleListRuleChangedEvent(count));
                }
                if (EventBus.getDefault().hasSubscriberForEvent(OnSubRefreshEvent.class)) {
                    EventBus.getDefault().post(new OnSubRefreshEvent());
                }
            }
        });
    }

    public static void checkUpdateAsync(SubscribeRecord subscribeRecord) {
        HeavyTaskUtil.executeNewTask(() -> {
            int count = checkUpdate(subscribeRecord);
            if (count > 0) {
                EventBus.getDefault().post(new OnArticleListRuleChangedEvent(count));
            }
            if (EventBus.getDefault().hasSubscriberForEvent(OnSubRefreshEvent.class)) {
                EventBus.getDefault().post(new OnSubRefreshEvent());
            }
        });
    }

    public static int checkUpdate(SubscribeRecord subscribeRecord) {
        if (!subscribeRecord.isUse()) {
            //未启用，不检查更新
            return 0;
        }
        if (subscribeRecord.getErrorCount() > 10) {
            //连续失败大于10次，永远不再检查更新
            return 0;
        }
        if (subscribeRecord.getModifyDate() != null &&
                System.currentTimeMillis() - subscribeRecord.getModifyDate().getTime() < 3600 * 24 * 1000) {
            //一天内只检查更新一次
            return 0;
        }
        List<ArticleListRule> existRules = LitePal.findAll(ArticleListRule.class);
        Map<String, ArticleListRule> existMap = new HashMap<>();
        for (ArticleListRule existRule : existRules) {
            existMap.put(existRule.getTitle(), existRule);
        }
        try {
            try (Response response = OkGo.<String>get(subscribeRecord.getUrl()).execute();
                 ResponseBody body = response.body()) {
                if (body != null) {
                    String s = body.string();
                    if (!StringUtil.isEmpty(s) && !FilterUtil.hasFilterWord(s)) {
                        List<ArticleListRuleJO> ruleJOList = JSON.parseArray(s, ArticleListRuleJO.class);
                        if (CollectionUtil.isNotEmpty(ruleJOList)) {
                            int count = 0;
                            //更新规则总数
                            subscribeRecord.setRulesCount(ruleJOList.size());
                            for (ArticleListRuleJO ruleJO : ruleJOList) {
                                if (!existMap.containsKey(ruleJO.getTitle())) {
                                    if(subscribeRecord.isOnlyUpdate()){
                                        //订阅开启了只更新不新增
                                        continue;
                                    }
                                    //新增规则
                                    count++;
                                    new ArticleListRule().fromJO(ruleJO).save();
                                } else {
                                    //更新
                                    ArticleListRule exist = existMap.get(ruleJO.getTitle());
                                    if (exist.getVersion() >= ruleJO.getVersion()
                                            || !StringUtils.equals(exist.getAuthor(), ruleJO.getAuthor())) {
                                        //版本未增加，或者作者签名不一致，不更新
                                        continue;
                                    }
                                    count++;
                                    String group = exist.getGroup();
                                    String titleColor = exist.getTitleColor();
                                    exist.fromJO(ruleJO);
                                    exist.setGroup(group);
                                    exist.setTitleColor(titleColor);
                                    exist.save();
                                }
                            }
                            subUpdateSuccess(subscribeRecord, count);
                            return count;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            subUpdateError(subscribeRecord);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private static void subUpdateError(SubscribeRecord subscribeRecord) {
        subscribeRecord.setModifyDate(new Date());
        subscribeRecord.setErrorCount(subscribeRecord.getErrorCount() + 1);
        subscribeRecord.setLastUpdateCount(0);
        subscribeRecord.setLastUpdateSuccess(false);
        updateSubRecords(subscribeRecord);
    }

    private static void subUpdateSuccess(SubscribeRecord subscribeRecord, int count) {
        subscribeRecord.setModifyDate(new Date());
        subscribeRecord.setErrorCount(0);
        subscribeRecord.setLastUpdateCount(count);
        subscribeRecord.setLastUpdateSuccess(true);
        updateSubRecords(subscribeRecord);
    }

}
