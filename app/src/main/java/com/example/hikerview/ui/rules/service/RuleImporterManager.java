package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import com.example.hikerview.ui.view.popup.ConfirmPopup;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.XPopup;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/8/5
 * 时间：At 11:42
 */

public class RuleImporterManager {

    public enum Importer {
        Num1("云剪贴板1"),
        Num2("云剪贴板2"),
        Num3("云剪贴板3"),
        Num4("云剪贴板4"),
        Num5("云剪贴板5"),
        Num6("云剪贴板6");

        private final String name;

        Importer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static String[] getImporters() {
        Importer[] importers = Importer.values();
        String[] s = new String[importers.length];
        for (int i = 0; i < importers.length; i++) {
            s[i] = "云剪贴板" + i;
        }
        return s;
    }

    public static String[] getShareImporters() {
        Importer[] importers = Importer.values();
        String[] s = new String[importers.length];
        for (int i = 0; i < importers.length; i++) {
            s[i] = "云剪贴板分享" + i;
        }
        return s;
    }

    public static List<String> getSyncableImporters() {
        List<String> names = new ArrayList<>();
        Importer[] importers = Importer.values();
        for (Importer importer : importers) {
            RuleImporter ruleImporter = getImporter(importer);
            if (ruleImporter != null && ruleImporter.canUseSync()) {
                names.add(importer.getName());
            }
        }
        return names;
    }

    public static String shareSync(String content, String name) {
        if (StringUtil.isEmpty(name)) {
            return "error:云剪贴板名称不能为空";
        }
        Importer[] importers = Importer.values();
        for (Importer importer : importers) {
            if (!name.equals(importer.getName())) {
                continue;
            }
            RuleImporter ruleImporter = getImporter(importer);
            if (ruleImporter != null && ruleImporter.canUseSync()) {
                return ruleImporter.shareSync(content);
            }
        }
        return "error:找不到对应的云剪贴板";
    }

    public static String parseSync(String url) {
        if (StringUtil.isEmpty(url)) {
            return "error:云剪贴板地址不能为空";
        }
        Importer[] importers = Importer.values();
        for (Importer importer : importers) {
            RuleImporter ruleImporter = getImporter(importer);
            if (ruleImporter != null && ruleImporter.canUseSync() && ruleImporter.canParse(url)) {
                return ruleImporter.parseSync(url);
            }
        }
        return "error:找不到可解析的云剪贴板";
    }

    public static void sharePublic(Importer importer, Activity activity, String paste, String title, String rulePrefix) {
        RuleImporter ruleImporter = getImporter(importer);
        if (ruleImporter == null) {
            return;
        }
        ruleImporter.share(activity, paste, title, "", rulePrefix);
    }

    public static void share(Importer importer, Activity activity, String paste, String title, String rulePrefix) {
        RuleImporter ruleImporter = getImporter(importer);
        if (ruleImporter == null) {
            return;
        }
        if (ruleImporter.canSetPwd()) {
            new XPopup.Builder(activity)
                    .asCustom(new ConfirmPopup(activity)
                            .bind("设置访问密码", "为空表示无需密码", new ConfirmPopup.OkListener() {
                                @Override
                                public void ok(String text) {
                                    ruleImporter.share(activity, paste, title, text, rulePrefix);
                                }

                                @Override
                                public void cancel() {
                                    String pwd = StringUtil.genRandomPwd(6, true);
                                    ruleImporter.share(activity, paste, title, pwd, rulePrefix);
                                }
                            }).setBtn("确定", "随机密码")).show();
        } else {
            ruleImporter.share(activity, paste, title, "", rulePrefix);
        }
    }

    public static boolean parse(Activity activity, String text) {
        if (StringUtil.isEmpty(text)) {
            return false;
        }
        for (Importer value : Importer.values()) {
            RuleImporter ruleImporter = getImporter(value);
            if (ruleImporter != null && ruleImporter.canParse(text)) {
                ruleImporter.parse(activity, text);
                return true;
            }
        }
        RuleImporter[] deprecatedImporters = getDeprecatedImporter();
        for (RuleImporter deprecatedImporter : deprecatedImporters) {
            if (deprecatedImporter.canParse(text)) {
                //无法分享的云剪贴板依然尝试获取数据
                deprecatedImporter.parse(activity, text);
                return true;
            }
        }
        return false;
    }

    private static RuleImporter[] getDeprecatedImporter() {
        return new RuleImporter[]{new PastemeImporter(), new UbuntuImporter(), new Pasteme2Importer()};
    }

    private static RuleImporter getImporter(Importer importer) {
        switch (importer) {
            case Num1:
                return new PastebinImporter();
            case Num2:
                return new NetCutImporter();
//            case Num3:
//                return new UbuntuImporter();
//            case Num4:
//                return new Pasteme2Importer();
            case Num5:
                return new CmdImporter();
            case Num6:
                return new BailanImporter();
        }
        return null;
    }

    public static Importer getImporterByName(String name) {
        Importer[] importers = Importer.values();
        for (Importer importer : importers) {
            if (name.equals(importer.getName())) {
                return importer;
            }
        }
        return Importer.Num1;
    }
} 