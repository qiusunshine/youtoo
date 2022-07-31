package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import androidx.annotation.Nullable;

/**
 * 作者：By 15968
 * 日期：On 2021/8/5
 * 时间：At 11:40
 */

public interface RuleImporter {

    void share(Activity activity, String paste, String title, @Nullable String password, String rulePrefix);

    boolean canSetPwd();

    boolean canParse(String text);

    void parse(Activity activity, String url);

    boolean canUseSync();

    String shareSync(String paste);

    String parseSync(String url);
}
