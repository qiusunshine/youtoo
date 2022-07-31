package com.example.hikerview.ui.view.popup;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.utils.FilesInAppUtil;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class AlertImgPopup extends BottomPopupView {
    private String fileName;

    public AlertImgPopup(@NonNull Context context) {
        super(context);
    }

    public AlertImgPopup with(String fileName) {
        this.fileName = fileName;
        return this;
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_alert_img;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        ImageView imageView = findViewById(R.id.img_view);
        Bitmap bitmap = FilesInAppUtil.getImageFromAssetsFile(getContext(), fileName);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        findViewById(R.id.item_video).setOnClickListener(v -> dismiss());
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }
}
