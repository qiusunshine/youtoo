package com.example.hikerview.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class XiuTanResultPopup extends BottomPopupView {
    private List<DetectedMediaResult> mediaResults;
    private ClickListener clickListener;
    private String title;
    private XiuTanResultAdapter adapter;
    private View.OnClickListener codeIconClickListener;
    private boolean dismissOnClick = true;

    public XiuTanResultPopup with(List<DetectedMediaResult> mediaResults, ClickListener clickListener) {
        this.mediaResults = mediaResults;
        this.clickListener = clickListener;
        return this;
    }

    public XiuTanResultPopup withTitle(String title) {
        this.title = title;
        return this;
    }

    public XiuTanResultPopup withIcon(View.OnClickListener codeIconClickListener) {
        this.codeIconClickListener = codeIconClickListener;
        return this;
    }

    public void updateData(List<DetectedMediaResult> results) {
        if (!isShow()) {
            return;
        }
        mediaResults.clear();
        mediaResults.addAll(results);
        adapter.notifyDataSetChanged();
    }

    public XiuTanResultPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_xiu_tan_result;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (StringUtil.isNotEmpty(title)) {
            TextView titleView = findViewById(R.id.title);
            titleView.setText(title);
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        if (codeIconClickListener != null) {
            View icon = findViewById(R.id.code);
            icon.setVisibility(VISIBLE);
            icon.setOnClickListener(codeIconClickListener);
        }
        adapter = new XiuTanResultAdapter(getContext(), mediaResults, new XiuTanResultAdapter.OnClickListener() {
            @Override
            public void click(int position, String url) {
                if (dismissOnClick) {
                    dismissWith(() -> clickNow(position, url));
                } else {
                    clickNow(position, url);
                }
            }

            private void clickNow(int position, String url) {
                clickListener.click(url, "play");
                try {
                    mediaResults.get(position).setClicked(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void longClick(String url) {
                boolean floatVideo = PreferenceMgr.getBoolean(getContext(), "floatVideo", false);
                new XPopup.Builder(getContext())
                        .asBottomList("选择操作", floatVideo ?
                                new String[]{"悬浮播放", "下载资源", "复制链接"} : new String[]{"下载资源", "复制链接"}, (position1, text1) -> {
                            dismissWith(() -> clickListener.click(url, text1));
                        })
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }

    public boolean isDismissOnClick() {
        return dismissOnClick;
    }

    public XiuTanResultPopup withDismissOnClick(boolean dismissOnClick) {
        this.dismissOnClick = dismissOnClick;
        return this;
    }

    public interface ClickListener {
        void click(String url, String type);
    }
}
