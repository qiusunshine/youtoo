package com.example.hikerview.ui.browser.view;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.browser.service.UpdateEvent;
import com.example.hikerview.utils.FilterUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;


/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class JSUpdatePopup extends BottomPopupView {
    private List<UpdateEvent> list;
    private String title;

    public JSUpdatePopup with(List<UpdateEvent> events) {
        this.list = events;
        return this;
    }

    public JSUpdatePopup withTitle(String title) {
        this.title = title;
        return this;
    }

    public JSUpdatePopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_js_update;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (title != null) {
            TextView titleView = findViewById(R.id.title);
            titleView.setText(title);
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        JSUpdateAdapter adapter = new JSUpdateAdapter(getContext(), list, position -> {
            UpdateEvent event = list.get(position);
            if (StringUtil.isNotEmpty(event.getDownloadUrl())) {
                new XPopup.Builder(getContext())
                        .asConfirm("温馨提示", "确定更新" + event.getFileName() + "到" + event.getNewVersion() + "版本？注意更新后无法恢复旧版本", () -> {
                            LoadingPopupView loadingPopupView = new XPopup.Builder(getContext())
                                    .asLoading("加载中，请稍候");
                            loadingPopupView.show();
                            CodeUtil.get(event.getDownloadUrl(), new CodeUtil.OnCodeGetListener() {
                                @Override
                                public void onSuccess(String s) {
                                    loadingPopupView.dismiss();
                                    if (FilterUtil.hasJSFilterWord(getContext(), event.getDownloadUrl(), s)) {
                                        return;
                                    }
                                    boolean ok = JSManager.instance(getContext()).updateJs(event.getFileName(), s);
                                    if (ok) {
                                        ToastMgr.shortBottomCenter(getContext(), event.getFileName() + "插件保存成功！");
                                    } else {
                                        ToastMgr.shortBottomCenter(getContext(), event.getFileName() + "插件保存失败！");
                                    }
                                }

                                @Override
                                public void onFailure(int errorCode, String msg) {
                                    loadingPopupView.dismiss();
                                    ToastMgr.shortBottomCenter(getContext(), event.getFileName() + "导入失败，网络连接错误！");
                                }
                            });
                        }).show();
            }
        });
        recyclerView.setAdapter(adapter);
        JSManager.instance(getContext()).registerVersionListener((fileName, version) -> {
            for (int i = 0; i < list.size(); i++) {
                UpdateEvent event = list.get(i);
                if (StringUtils.equals(fileName, event.getFileName())) {
                    event.setOldVersion(version);
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        });
        findViewById(R.id.btn_all).setOnClickListener(v -> {
            new XPopup.Builder(getContext())
                    .asConfirm("温馨提示", "确定一键更新" + list.size() + "个脚本插件？注意更新后无法恢复旧版本", () -> {
                        int all = list.size();
                        IntHolder left = new IntHolder();
                        left.value = all;
                        LoadingPopupView loadingPopupView = new XPopup.Builder(getContext())
                                .asLoading("加载中，请稍候 " + left.value + "/" + all);
                        loadingPopupView.show();
                        for (UpdateEvent event : list) {
                            CodeUtil.get(event.getDownloadUrl(), new CodeUtil.OnCodeGetListener() {
                                @Override
                                public void onSuccess(String s) {
                                    left.value--;
                                    if (left.value <= 0) {
                                        loadingPopupView.dismiss();
                                    } else {
                                        loadingPopupView.setTitle("加载中，请稍候 " + left.value + "/" + all);
                                    }
                                    if (FilterUtil.hasJSFilterWord(getContext(), event.getDownloadUrl(), s)) {
                                        return;
                                    }
                                    boolean ok = JSManager.instance(getContext()).updateJs(event.getFileName(), s);
                                    if (ok) {
                                        ToastMgr.shortBottomCenter(getContext(), event.getFileName() + "插件保存成功！");
                                    } else {
                                        ToastMgr.shortBottomCenter(getContext(), event.getFileName() + "插件保存失败！");
                                    }
                                }

                                @Override
                                public void onFailure(int errorCode, String msg) {
                                    left.value--;
                                    if (left.value <= 0) {
                                        loadingPopupView.dismiss();
                                    } else {
                                        loadingPopupView.setTitle("加载中，请稍候 " + left.value + "/" + all);
                                    }
                                    ToastMgr.shortBottomCenter(getContext(), event.getFileName() + "导入失败，网络连接错误！");
                                }
                            });
                        }
                    }).show();
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        JSManager.instance(getContext()).unregisterVersionListener();
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .7f);
    }

    private static class IntHolder {
        int value;
    }
}
