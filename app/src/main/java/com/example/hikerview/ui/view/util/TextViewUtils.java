package com.example.hikerview.ui.view.util;

import android.graphics.Color;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.example.hikerview.ui.Application;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.WebUtil;
import com.zzhoujay.richtext.LinkHolder;
import com.zzhoujay.richtext.spans.LongClickableURLSpan;

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
                    textView.setText(getClickableHtml(textView, mContent));
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
                textView.setText(getClickableHtml(textView, mContent));
            }
        } catch (Exception e) {
            textView.setText(title);
        }
    }

    /**
     * 格式化超链接文本内容并设置点击处理
     */
    private static CharSequence getClickableHtml(TextView textView, Spanned spannedHtml) {
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            setLinkClickable(clickableHtmlBuilder, span);
        }
        if (urls.length > 0) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        return clickableHtmlBuilder;
    }

    /**
     * 设置点击超链接对应的处理内容
     */
    private static void setLinkClickable(final SpannableStringBuilder clickableHtmlBuilder, final URLSpan urlSpan) {
        int start = clickableHtmlBuilder.getSpanStart(urlSpan);
        int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
        clickableHtmlBuilder.removeSpan(urlSpan);
        LinkHolder linkHolder = new LinkHolder(urlSpan.getURL());
        LongClickableURLSpan longClickableURLSpan = new LongClickableURLSpan(linkHolder, url -> {
            WebUtil.goWeb(Application.application.getHomeActivity(), url);
            return true;
        }, null);
        clickableHtmlBuilder.setSpan(longClickableURLSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                        if (last <= index + key.length()) {
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
                    if (last <= index + key.length()) {
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