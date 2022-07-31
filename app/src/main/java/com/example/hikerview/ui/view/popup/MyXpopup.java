package com.example.hikerview.ui.view.popup;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;

import com.lxj.xpopup.core.ImageViewerPopupView;
import com.lxj.xpopup.core.PopupInfo;
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener;
import com.lxj.xpopup.interfaces.XPopupImageLoader;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/12/12
 * 时间：At 19:14
 */

public class MyXpopup {

    private Context context;
    private final PopupInfo popupInfo = new PopupInfo();

    public MyXpopup Builder(Context context) {
        this.context = context;
        return this;
    }

    /**
     * 大图浏览类型弹窗，单张图片使用场景
     *
     * @param srcView 源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
     * @return
     */
    public MyImageViewerPopupView asImageViewer(ImageView srcView, Object url, XPopupImageLoader imageLoader) {
        ImageViewerPopupView popupView = new MyImageViewerPopupView(this.context)
                .setSingleSrcView(srcView, url)
                .setXPopupImageLoader(imageLoader);
        popupView.popupInfo = this.popupInfo;
        return (MyImageViewerPopupView) popupView;
    }

    /**
     * 大图浏览类型弹窗，单张图片使用场景
     *
     * @param srcView           源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
     * @param url               资源id，url或者文件路径
     * @param isInfinite        是否需要无限滚动，默认为false
     * @param placeholderColor  占位View的填充色，默认为-1
     * @param placeholderStroke 占位View的边框色，默认为-1
     * @param placeholderRadius 占位View的圆角大小，默认为-1
     * @param isShowSaveBtn     是否显示保存按钮，默认显示
     * @param bgColor           背景颜色
     * @return
     */
    public MyImageViewerPopupView asImageViewer(ImageView srcView, Object url, boolean isInfinite, int placeholderColor, int placeholderStroke, int placeholderRadius,
                                                boolean isShowSaveBtn, int bgColor, XPopupImageLoader imageLoader) {
        ImageViewerPopupView popupView = new MyImageViewerPopupView(this.context)
                .setSingleSrcView(srcView, url)
                .isInfinite(isInfinite)
                .setPlaceholderColor(placeholderColor)
                .setPlaceholderStrokeColor(placeholderStroke)
                .setPlaceholderRadius(placeholderRadius)
                .isShowSaveButton(isShowSaveBtn)
                .setBgColor(bgColor)
                .setXPopupImageLoader(imageLoader);
        popupView.popupInfo = this.popupInfo;
        return (MyImageViewerPopupView) popupView;
    }

    /**
     * 大图浏览类型弹窗，多张图片使用场景
     *
     * @param srcView               源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
     * @param currentPosition       指定显示图片的位置
     * @param urls                  图片url集合
     * @param srcViewUpdateListener 当滑动ViewPager切换图片后，需要更新srcView，此时会执行该回调，你需要调用updateSrcView方法。
     * @return
     */
    public MyImageViewerPopupView asImageViewer(ImageView srcView, int currentPosition, List<Object> urls,
                                                OnSrcViewUpdateListener srcViewUpdateListener, XPopupImageLoader imageLoader) {
        return asImageViewer(srcView, currentPosition, urls, false, true, -1, -1, -1, true,
                Color.rgb(32, 36, 46), srcViewUpdateListener, imageLoader);
    }

    /**
     * 大图浏览类型弹窗，多张图片使用场景
     *
     * @param srcView               源View，就是你点击的那个ImageView，弹窗消失的时候需回到该位置。如果实在没有这个View，可以传空，但是动画变的非常僵硬，适用于在Webview点击场景
     * @param currentPosition       指定显示图片的位置
     * @param urls                  图片url集合
     * @param isInfinite            是否需要无限滚动，默认为false
     * @param isShowPlaceHolder     是否显示默认的占位View，默认为false
     * @param placeholderColor      占位View的填充色，默认为-1
     * @param placeholderStroke     占位View的边框色，默认为-1
     * @param placeholderRadius     占位View的圆角大小，默认为-1
     * @param isShowSaveBtn         是否显示保存按钮，默认显示
     * @param srcViewUpdateListener 当滑动ViewPager切换图片后，需要更新srcView，此时会执行该回调，你需要调用updateSrcView方法。
     * @return
     */
    public MyImageViewerPopupView asImageViewer(ImageView srcView, int currentPosition, List<Object> urls,
                                                boolean isInfinite, boolean isShowPlaceHolder,
                                                int placeholderColor, int placeholderStroke, int placeholderRadius, boolean isShowSaveBtn,
                                                int bgColor, OnSrcViewUpdateListener srcViewUpdateListener, XPopupImageLoader imageLoader) {
        ImageViewerPopupView popupView = new MyImageViewerPopupView(this.context)
                .setSrcView(srcView, currentPosition)
                .setImageUrls(urls)
                .isInfinite(isInfinite)
                .isShowPlaceholder(isShowPlaceHolder)
                .setPlaceholderColor(placeholderColor)
                .setPlaceholderStrokeColor(placeholderStroke)
                .setPlaceholderRadius(placeholderRadius)
                .isShowSaveButton(isShowSaveBtn)
                .setBgColor(bgColor)
                .setSrcViewUpdateListener(srcViewUpdateListener)
                .setXPopupImageLoader(imageLoader);
        popupView.popupInfo = this.popupInfo;
        return (MyImageViewerPopupView) popupView;
    }
} 