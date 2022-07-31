package com.example.hikerview.ui.browser.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.hikerview.model.AdBlockRule;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;

import org.litepal.LitePal;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2019/10/14
 * 时间：At 19:28
 */
public class AdBlockModel {
    private static final String TAG = "AdBlockModel";
    private static String[] whiteDomainList = new String[]{"haikuoshijie.cn"};

    public static String saveBlockRule(String url, String rule) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String dom = StringUtil.getDom(url);
        List<AdBlockRule> blockRules = null;
        try {
            blockRules = LitePal.where("dom = ?", dom).limit(1).find(AdBlockRule.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String finalRule;
        if (CollectionUtil.isEmpty(blockRules)) {
            AdBlockRule adBlockRule = new AdBlockRule();
            adBlockRule.setDom(dom);
            adBlockRule.setRule(rule);
            adBlockRule.save();
            finalRule = rule;
        } else {
            AdBlockRule blockRule = blockRules.get(0);
            if (("@@" + blockRule.getRule() + "@@").contains("@@" + rule + "@@")) {
                finalRule = blockRule.getRule();
                return getBlockJsByRule(finalRule);
            }
            blockRule.setRule(blockRule.getRule() + "@@" + rule);
            blockRule.save();
            finalRule = blockRule.getRule();
        }
        return getBlockJsByRule(finalRule);
    }

    public static void saveRules(List<AdBlockRule> rules, OnSaveListener listener) {
        if (CollectionUtil.isEmpty(rules)) {
            listener.ok(0);
            return;
        }
        HeavyTaskUtil.executeNewTask(() -> {
            float count = (float) rules.size();
            int progress = 0;
            for (int i = 0; i < rules.size(); i++) {
                saveRule(rules.get(i).getDom(), rules.get(i).getRule());
                int newProgress = (int) ((float) (i + 1) / count * 100);
                if (newProgress > progress) {
                    progress = newProgress;
                    listener.update(progress);
                }
            }
            listener.ok(rules.size());
        });
    }

    public static void saveRule(String url, String rule) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String dom = StringUtil.getDom(url);
        List<AdBlockRule> blockRules = null;
        try {
            blockRules = LitePal.where("dom = ?", dom).limit(1).find(AdBlockRule.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CollectionUtil.isEmpty(blockRules)) {
            AdBlockRule adBlockRule = new AdBlockRule();
            adBlockRule.setDom(dom);
            adBlockRule.setRule(rule);
            adBlockRule.save();
        } else {
            AdBlockRule blockRule = blockRules.get(0);
            if (("@@" + blockRule.getRule() + "@@").contains("@@" + rule + "@@")) {
                return;
            }
            blockRule.setRule(blockRule.getRule() + "@@" + rule);
            blockRule.save();
        }
    }

    public static String getBlockRule(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String dom = StringUtil.getDom(url);
        for (String white : whiteDomainList) {
            if (url.contains(white)) {
                return null;
            }
        }
        List<AdBlockRule> blockRules = null;
        try {
            blockRules = LitePal.where("dom = ?", dom).limit(1).find(AdBlockRule.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CollectionUtil.isEmpty(blockRules)) {
            return AdUrlBlocker.instance().getBlockRules(url);
        }
        return blockRules.get(0).getRule();
    }

    public static String getBlockJs(String url) {
        if (!SettingConfig.shouldBlock()) {
            return null;
        }
        String rule = getBlockRule(url);
        Log.d(TAG, "getBlockJs: " + rule);
        boolean forceBlock = PreferenceMgr.getBoolean(Application.application, "forceBlock", false);
        if (forceBlock) {
            if (StringUtil.isNotEmpty(rule)) {
                rule = rule + "@@" + getForceBlockJS();
            } else {
                rule = getForceBlockJS();
            }
        }
        if (TextUtils.isEmpty(rule)) {
            return null;
        }
        if (!TextUtils.isEmpty(rule)) {
            return getBlockJsByRule(rule);
        }
        return null;
    }

    private static String getForceBlockJS() {
        return "body a[target='_blank'] > img[src$='.gif'],-1";
    }

    public static String getFloatBlockJs(Context context) {
        boolean floatBlock = PreferenceMgr.getBoolean(context, "floatBlock", false);
        if (floatBlock) {
            return "(function(){\n" +
                    "function addCss(styles) {\n" +
                    "          let css;\n" +
                    "          styles = styles.replace(/\\n+\\s*/g, ' ');\n" +
                    "          css = document.createElement('style');\n" +
                    "          if (css.styleSheet) css.styleSheet.cssText = styles;\n" +
                    "          // Support for IE\n" +
                    "          else css.appendChild(document.createTextNode(styles)); // Support for the rest\n" +
                    "          css.type = 'text/css';\n" +
                    "          document.getElementsByTagName('head')[0].appendChild(css);\n" +
                    "        }\n" +
                    "addCss('" + "*{user-select:auto !important}[style*=\"z-index: 999999\"],[style*=\"z-index: 990099\"],[style*=\"z-index: 10000\"],[style*=\"z-index: 214748364\"]{display:none!important; z-index: -1000 !important; visibility: hidden !important; transform: scale(0,0) !important; width: 0px !important; height: 0px !important;}"
                    + "');\n" +
                    "})();";
        }
        return null;
    }

    private static String getBlockJsByRule(String rule) {
        if (TextUtils.isEmpty(rule)) {
            return null;
        }
        if (!TextUtils.isEmpty(rule)) {
            return "(function(){\n" +
                    "try{" +
                    "   eval(request('hiker://files/jquery.min.js'));\n" +
                    "   var jqn = jQuery.noConflict(); \n" +
                    "   function adBlock(rule){\n" +
                    "        //console.log('adBlock start');\n" +
                    "        if(rule.length<=0){\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        var rules = rule.split('@@');\n" +
                    "        for(var i = 0; i<rules.length; i++){\n" +
                    "            adBlockPer(rules[i]);\n" +
                    "        }\n" +
                    "        //console.log('adBlock end');\n" +
                    "    }\n" +
                    "\n" +
                    "    function adBlockPer(rule){\n" +
                    "        //console.log('adBlockPer, ', rule);\n" +
                    "        if(rule.length<=0){\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        var rules = rule.split('&&');\n" +
                    "        for(var i = 0; i<rules.length; i++){\n" +
                    "            rules[i] = getRule(rules[i]);\n" +
                    "        }\n" +
                    "        if(rules.length<2){\n" +
                    "            jqn(rules[0]).css('cssText', 'display:none !important; z-index: -1000 !important; visibility: hidden !important; transform: scale(0,0) !important; width: 0px !important; height: 0px !important;');\n" +
                    "            //console.log('adBlockPer, rules[0], ', rules[0])\n" +
                    "        }else{\n" +
                    "            var rule1 = rules.join(' > ');\n" +
                    "            jqn(rule1).css('cssText', 'display:none !important; z-index: -1000 !important; visibility: hidden !important; transform: scale(0,0) !important; width: 0px !important; height: 0px !important;');\n" +
                    "            //console.log('adBlockPer, rule1, ', rule1)\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    function getRule(rule){\n" +
                    "        var rules = rule.split(',');\n" +
                    "        var count = 0;\n" +
                    "        if(rules.length>1){\n" +
                    "            count = parseInt(rules[1]);\n" +
                    "        }\n" +
                    "        if(rules[0] == 'body' || rules[0].indexOf('#')==0){\n" +
                    "            return rules[0];\n" +
                    "        }\n" +
                    "        if(count < 0){\n" +
                    "            return rules[0];\n" +
                    "        }\n" +
                    "        return rules[0] + ':eq(' + count + ')'\n" +
                    "    }\n" +
                    "    var rule = \"" + rule + "\";\n" +
                    "    var count = 0;\n" +
                    "    var attempt = ()=>{\n" +
                    "    if(count > 30) return;\n" +
                    "    count++;\n" +
                    "    adBlock(rule);\n" +
                    "    setTimeout(attempt, 100);\n" +
                    "};\n" +
                    "    attempt();\n" +
                    "}catch(e){console.log('adBlock: ' + e.toString())}" +
                    "})();";
        }
        return null;
    }

    public interface OnSaveListener {
        void update(int progress);

        void ok(int count);
    }
}
