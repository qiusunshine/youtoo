package com.example.hikerview.ui;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;
import com.example.hikerview.utils.OkHttpUrlLoader;

import java.io.InputStream;

/**
 * 作者：By 15968
 * 日期：On 2021/8/26
 * 时间：At 10:35
 */

@GlideModule
public final class OkHttpLibraryGlideModule extends LibraryGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, Registry registry) {
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory();
        registry.replace(GlideUrl.class, InputStream.class, factory);
    }
}