package com.example.hikerview.ui.browser.model;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.hikerview.R;
import com.example.hikerview.listener.UpdateUaListener;
import com.example.hikerview.model.UaRuleDO;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;

import org.litepal.LitePal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2019/10/3
 * 时间：At 21:45
 */
public class UAModel {

    public enum WebUA {
        Android("Android", null),
        PC("PC桌面", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36"),
        IPhone("IPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13E230 Safari/601.1"),
        CustomGlobal("自定义全局", null),
        Custom("自定义该网站", null);

        private final String code;
        private final String content;

        WebUA(String code, String content) {
            this.code = code;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public String getCode() {
            return code;
        }
    }

    private static final String TAG = "UAModel";
    private static String useUa = "";
    private static Map<String, String> uaMap = new HashMap<>();

    public static String getUseUa() {
        return useUa;
    }

    public static void setUseUa(String useUa) {
        UAModel.useUa = useUa;
    }

    public static void deleteFromCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String dom = StringUtil.getDom(url);
        uaMap.put(dom, null);
    }

    public static void updateUa(String url, String ua) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String dom = StringUtil.getDom(url);
        uaMap.put(dom, ua);
        List<UaRuleDO> rules = LitePal.where("dom = ?", dom).find(UaRuleDO.class);
        if (!CollectionUtil.isEmpty(rules)) {
            if (TextUtils.isEmpty(ua)) {
                rules.get(0).delete();
            } else {
                rules.get(0).setUa(ua);
                rules.get(0).save();
            }
        } else if (!TextUtils.isEmpty(ua)) {
            UaRuleDO ruleDO = new UaRuleDO();
            ruleDO.setDom(dom);
            ruleDO.setUa(ua);
            ruleDO.save();
        }
    }

    private static String getAdjustUaNow(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String dom = StringUtil.getDom(url);
//        Log.d(TAG, "getAdjustUa: -->" + dom);
        if (uaMap.containsKey(dom) && uaMap.get(dom) != null) {
//            Log.d(TAG, "getAdjustUa:containsKey -->" + uaMap.get(dom));
            return uaMap.get(dom);
        }
        List<UaRuleDO> rules = LitePal.where("dom = ?", dom).find(UaRuleDO.class);
        if (!CollectionUtil.isEmpty(rules)) {
            String ua = rules.get(0).getUa();
//            Log.d(TAG, "getAdjustUa: !CollectionUtil.isEmpty(rules)-->" + ua);
            uaMap.put(dom, ua);
            return ua;
        } else {
//            Log.d(TAG, "getAdjustUa: CollectionUtil.isEmpty(rules)-->");
            uaMap.put(dom, "");
        }
        return null;
    }

    public static boolean hasAdjustUa(String url) {
        String ua = UAModel.getAdjustUa(url);
        return StringUtil.isNotEmpty(ua);
    }

    public static String[] getUaList() {
        WebUA[] uas = WebUA.values();
        String[] d = new String[uas.length];
        for (int i = 0; i < uas.length; i++) {
            d[i] = uas[i].getCode();
        }
        return d;
    }

    public static String getAdjustUa(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String ua = getAdjustUaNow(url);
        if (StringUtil.isNotEmpty(ua)) {
            return ua;
        }
        String dom = StringUtil.getDom(url);
        String[] doms = dom.split("\\.");
        if (doms.length >= 3) {
            dom = StringUtil.arrayToString(doms, 1, ".");
        }
        return getAdjustUaNow(dom);
    }

    public static void showUpdateOrAddDialog(Context context, String url, @Nullable UpdateUaListener listener) {
        String useUa = TextUtils.isEmpty(url) ? "" : getAdjustUa(url);
        String dom = TextUtils.isEmpty(url) ? "" : StringUtil.getDom(url);
        String[] doms = dom.split("\\.");
        if (doms.length >= 3) {
            dom = StringUtil.arrayToString(doms, 1, ".");
        }
        final View view1 = LayoutInflater.from(context).inflate(R.layout.view_dialog_ua_add, null, false);
        final EditText titleE = view1.findViewById(R.id.web_add_title);
        final EditText urlE = view1.findViewById(R.id.web_add_url);
        titleE.setHint("请输入网址或域名");
        titleE.setText(dom);
        urlE.setHint("请输入UA规则");
        urlE.setText(useUa == null ? "" : useUa);
        new AlertDialog.Builder(context)
                .setTitle("自定义网站UA")
                .setView(view1)
                .setCancelable(true)
                .setPositiveButton("确定", (dialog2, which2) -> {
                    String title = titleE.getText().toString();
                    String ua = urlE.getText().toString();
                    if (TextUtils.isEmpty(title)) {
                        ToastMgr.shortBottomCenter(context, "请输入域名和UA");
                    } else {
                        if ("*".equals(title) || "global".equals(url)) {
                            ToastMgr.shortBottomCenter(context, "全局UA设置请在网页菜单栏里面切换");
                            return;
                        }
                        title = StringUtil.replaceBlank(title);
                        dialog2.dismiss();
                        updateUa(title, ua);
                        ToastMgr.shortBottomCenter(context, "UA保存成功");
                        if (listener != null) {
                            listener.saved(ua);
                        }
                    }
                }).setNegativeButton("取消", (dialog2, which3) -> dialog2.dismiss()).show();
    }
}
