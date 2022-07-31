package com.example.hikerview.utils;

import android.graphics.Color;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

/**
 * 作者：By 15968
 * 日期：On 2021/5/26
 * 时间：At 20:01
 */

public class TextViewUtils {

    public static void setSpanText(TextView textView, String title) {
        if (StringUtil.isEmpty(title)) {
            textView.setText("");
            return;
        }
        try {
            String[] s1 = title.split("““");
            if (s1.length == 1) {
                s1 = title.split("‘‘");
                if (s1.length == 1) {
                    textView.setText(title);
                } else {
                    StringBuilder builder = new StringBuilder("<font>" + s1[0] + "</font>");
                    for (int i = 1; i < s1.length; i++) {
                        String[] s2 = s1[i].split("’’");
                        builder.append("<font color=#f0983c>").append(s2[0]).append("</font>");
                        if (s2.length > 1) {
                            builder.append("<font>").append(s2[1]).append("</font>");
                        }
                    }
                    Spanned mContent = Html.fromHtml(StringUtil.convertBlankToTagP(builder.toString()));
                    textView.setText(mContent);
                }
            } else {
                StringBuilder builder = new StringBuilder("<font>" + s1[0] + "</font>");
                for (int i = 1; i < s1.length; i++) {
                    String[] s2 = s1[i].split("””");
                    builder.append("<font color=#FF0000>").append(s2[0]).append("</font>");
                    if (s2.length > 1) {
                        builder.append("<font>").append(s2[1]).append("</font>");
                    }
                }
                Spanned mContent = Html.fromHtml(StringUtil.convertBlankToTagP(builder.toString()));
                textView.setText(mContent);
            }
        } catch (Exception e) {
            textView.setText(title);
        }
    }

    public static void setBackgroundSpanText(TextView textView, String title) {
        if (StringUtil.isEmpty(title)) {
            textView.setText("");
            return;
        }
        try {
            String[] s1 = title.split("““");
            if (s1.length == 1) {
                s1 = title.split("‘‘");
                if (s1.length == 1) {
                    textView.setText(title);
                } else {
                    SpannableStringBuilder builder = new SpannableStringBuilder(title);
                    String key = "‘‘";
                    String close = "’’";
                    for (int index = title.indexOf(key); index >= 0 && index <= title.lastIndexOf(key); index = title.indexOf(key, index + 1)) {
                        int last = title.indexOf(close, index);
                        if(last <= index + key.length()){
                            continue;
                        }
                        builder.setSpan(new BackgroundColorSpan(Color.GREEN), index + key.length(), last, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                    textView.setText(builder);
                }
            } else {
                SpannableStringBuilder builder = new SpannableStringBuilder(title);
                String key = "““";
                String close = "””";
                for (int index = title.indexOf(key); index >= 0 && index <= title.lastIndexOf(key); index = title.indexOf(key, index + 1)) {
                    int last = title.indexOf(close, index);
                    if(last <= index + key.length()){
                        continue;
                    }
                    builder.setSpan(new BackgroundColorSpan(Color.RED), index + key.length(), last, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                textView.setText(builder);
            }
        } catch (Exception e) {
            textView.setText(title);
        }
    }
} 