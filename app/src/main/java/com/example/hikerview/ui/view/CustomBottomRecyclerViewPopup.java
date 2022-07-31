package com.example.hikerview.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.R;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class CustomBottomRecyclerViewPopup extends BottomPopupView {
    private List<String> data;
    private CustomCenterRecyclerViewPopup.ClickListener clickListener;
    private String title;
    private int span = 2;
    private Consumer<RecyclerView> onCreateCallback;
    private boolean dismissWhenClick = true;
    private float height = .69f;
    private GridLayoutManager gridLayoutManager;
    private View.OnClickListener menuClick;

    public CustomBottomRecyclerViewPopup with(String[] data, int span, CustomCenterRecyclerViewPopup.ClickListener clickListener) {
        this.data = new ArrayList<>(Arrays.asList(data));
        this.clickListener = clickListener;
        this.span = span;
        return this;
    }

    public CustomBottomRecyclerViewPopup with(List<String> data, int span, CustomCenterRecyclerViewPopup.ClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
        this.span = span;
        return this;
    }

    public CustomBottomRecyclerViewPopup withTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomBottomRecyclerViewPopup withMenu(View.OnClickListener menuClick) {
        this.menuClick = menuClick;
        return this;
    }

    public CustomBottomRecyclerViewPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_custom_center_recycler_view;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        Window window = dialog.getWindow();
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 延伸显示区域到刘海
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
            // 设置页面全屏显示
            final View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (StringUtil.isNotEmpty(title)) {
            TextView titleView = findViewById(R.id.title);
            titleView.setText(title);
        }
        if (menuClick != null) {
            View menu = findViewById(R.id.menu_icon);
            menu.setVisibility(VISIBLE);
            menu.setOnClickListener(menuClick);
        }
        gridLayoutManager = new GridLayoutManager(getContext(), span);
        recyclerView.setLayoutManager(gridLayoutManager);
        CustomRecyclerViewAdapter adapter = new CustomRecyclerViewAdapter(getContext(), data, new CustomRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (dismissWhenClick) {
                    dismissWith(() -> clickListener.click(data.get(position), position));
                } else {
                    clickListener.click(data.get(position), position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (dismissWhenClick) {
                    dismissWith(() -> {
                        clickListener.onLongClick(data.get(position), position);
                    });
                } else {
                    clickListener.onLongClick(data.get(position), position);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        if (onCreateCallback != null) {
            onCreateCallback.accept(recyclerView);
        }
    }

    public void changeSpanCount(int span) {
        this.span = span;
        gridLayoutManager.setSpanCount(span);
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * (height > 0 ? height : .85f));
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * height);
    }

    @Override
    protected int getMaxWidth() {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            if (activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    && activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                //横屏，挖孔屏也占满
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                if (wm == null) return -1;
                Point point = new Point();
                wm.getDefaultDisplay().getRealSize(point);
                return Math.max(point.x, point.y);
            }
        }
        return super.getMaxWidth();
    }

    public Consumer<RecyclerView> getOnCreateCallback() {
        return onCreateCallback;
    }

    public CustomBottomRecyclerViewPopup withOnCreateCallback(Consumer<RecyclerView> onCreateCallback) {
        this.onCreateCallback = onCreateCallback;
        return this;
    }

    public boolean isDismissWhenClick() {
        return dismissWhenClick;
    }

    public CustomBottomRecyclerViewPopup dismissWhenClick(boolean dismissWhenClick) {
        this.dismissWhenClick = dismissWhenClick;
        return this;
    }

    public CustomBottomRecyclerViewPopup withHeight(float height) {
        this.height = height;
        return this;
    }
}
