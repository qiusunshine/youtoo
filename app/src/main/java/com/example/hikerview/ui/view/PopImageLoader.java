package com.example.hikerview.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.utils.GlideUtil;
import com.lxj.xpopup.core.ImageViewerPopupView;
import com.lxj.xpopup.interfaces.XPopupImageLoader;
import com.lxj.xpopup.photoview.PhotoView;

import java.io.File;

/**
 * 作者：By hdy
 * 日期：On 2019/6/14
 * 时间：At 22:55
 */
public class PopImageLoader implements XPopupImageLoader {
    private ImageView lastView;
    private String baseUrl;
    private int selectPos;

    public PopImageLoader(ImageView lastView, String baseUrl) {
        this.lastView = lastView;
        this.baseUrl = baseUrl;
    }

    @Override
    public View loadImage(int position, @NonNull Object uri, @NonNull ImageViewerPopupView popupView, @NonNull PhotoView snapshot, @NonNull ProgressBar progressBar) {
        ImageView imageView = buildPhotoView(popupView, snapshot, position);
        if (uri instanceof String) {
            String url = UrlDetector.clearTag((String) uri);
            uri = GlideUtil.getGlideUrl(baseUrl, url);
        }
        RequestOptions options = new RequestOptions().skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        if (position == selectPos && lastView != null) {
            options = options.placeholder(lastView.getDrawable());
        } else {
            options = options.placeholder(imageView.getContext().getResources().getDrawable(R.drawable.ic_loading_bg));
        }
        Glide.with(imageView)
                .asDrawable()
                .load(uri)
                .apply(options)
                .into(imageView);
        return imageView;
    }

    //必须实现这个方法，返回uri对应的缓存文件，可参照下面的实现，内部保存图片会用到。如果你不需要保存图片这个功能，可以返回null。
    @Override
    public File getImageFile(@NonNull Context context, @NonNull Object uri) {
        try {
            if (uri instanceof String) {
                String url = UrlDetector.clearTag((String) uri);
                uri = GlideUtil.getGlideUrl(baseUrl, url);
            }
            return Glide.with(context).downloadOnly().load(uri).submit().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void loadSnapshot(@NonNull Object uri, @NonNull final PhotoView snapshot, @Nullable final ImageView srcView) {
        if (uri instanceof String) {
            String url = UrlDetector.clearTag((String) uri);
            uri = GlideUtil.getGlideUrl(baseUrl, url);
        }
        if (srcView != null && srcView.getDrawable() != null) {
            try {
                snapshot.setImageDrawable(srcView.getDrawable().getConstantState().newDrawable());
            } catch (Exception e) {
            }
        } else {
            Glide.with(snapshot).load(uri).into(snapshot);
        }
    }

    public int getSelectPos() {
        return selectPos;
    }

    public void setSelectPos(int selectPos) {
        this.selectPos = selectPos;
    }

    private PhotoView buildPhotoView(final ImageViewerPopupView popupView, final PhotoView snapshotView, final int realPosition) {
        final PhotoView photoView = new PhotoView(popupView.getContext());
        photoView.setOnMatrixChangeListener(rect -> {
            if (snapshotView != null) {
                Matrix matrix = new Matrix();
                photoView.getSuppMatrix(matrix);
                snapshotView.setSuppMatrix(matrix);
            }
        });
        photoView.setOnClickListener(v -> popupView.dismiss());
        if (popupView.longPressListener != null) {
            photoView.setOnLongClickListener(v -> {
                popupView.longPressListener.onLongPressed(popupView, realPosition);
                return false;
            });
        }
        return photoView;
    }

}