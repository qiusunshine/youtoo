package com.example.hikerview.ui;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.caverock.androidsvg.SVG;
import com.example.hikerview.lib.picture.SvgDecoder;
import com.example.hikerview.lib.picture.SvgDrawableTranscoder;
import com.example.hikerview.lib.picture.SvgEncoder;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * 作者：By 15968
 * 日期：On 2019/10/2
 * 时间：At 18:45
 */
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {

        registry.register(SVG.class, PictureDrawable.class, new SvgDrawableTranscoder())
                .append(InputStream.class, SVG.class, new SvgDecoder())
                .append(SVG.class, new SvgEncoder());
    }

    @Override
    public void applyOptions(@NonNull @NotNull Context context, @NonNull @NotNull GlideBuilder builder) {
        int diskCacheSizeBytes = 1024 * 1024 * 512;
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        super.applyOptions(context, builder);
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
