package com.example.hikerview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.hikerview.constants.ImageUrlMapEnum;
import com.example.hikerview.ui.setting.model.SettingConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2020/3/15
 * 时间：At 21:52
 */
public class GlideUtil {
    private static final String TAG = "GlideUtil";

    public static Object getGlideUrl(String baseUrl, String url) {
        int drawableId = ImageUrlMapEnum.getIdByUrl(url);
        if (drawableId > 0) {
            return drawableId;
        }
        if (StringUtil.isNotEmpty(url) && url.startsWith("#")) {
            try {
                int color = Color.parseColor(url);
                return new ColorDrawable(color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (StringUtil.isEmpty(baseUrl) || url == null || !url.startsWith("http")) {
            if (StringUtil.isNotEmpty(url) && url.startsWith("hiker://files/")) {
                String fileName = url.replace("hiker://files/", "");
                return "file://" + SettingConfig.rootDir + File.separator + fileName;
            }
            return url;
        } else {
            try {
                if(url.contains("gitee.com") && !url.contains("@Referer=")){
                    url = url + "@Referer=https://gitee.com";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LazyHeaders.Builder builder = new LazyHeaders.Builder();
            String refer = StringUtil.getDom(baseUrl);
            if (baseUrl.startsWith("https")) {
                refer = "https://" + refer + "/";
            } else {
                refer = "http://" + refer + "/";
            }
            String ua = SettingConfig.getGlideUA();

            //检查链接里面是否有自定义cookie
            String[] cookieUrl = url.split("@Cookie=");
            if (cookieUrl.length > 1) {
                url = cookieUrl[0];
                builder.addHeader("Cookie", cookieUrl[1]);
            }

            //检查链接里面是否有自定义UA和referer
            String[] s = url.split("@Referer=");
            if (s.length > 1) {
                if (s[0].contains("@User-Agent=")) {
                    refer = s[1];
                    url = s[0].split("@User-Agent=")[0];
                    ua = s[0].split("@User-Agent=")[1];
                } else if (s[1].contains("@User-Agent=")) {
                    refer = s[1].split("@User-Agent=")[0];
                    url = s[0];
                    ua = s[1].split("@User-Agent=")[1];
                } else {
                    refer = s[1];
                    url = s[0];
                }
            } else {
                if (url.contains("@Referer=")) {
                    url = url.replace("@Referer=", "");
                    refer = "";
                }
                if (url.contains("@User-Agent=")) {
                    ua = url.split("@User-Agent=")[1];
                    url = url.split("@User-Agent=")[0];
                }
            }
//            Log.d(TAG, "getGlideUrl: " + url);
//            Log.d(TAG, "getGlideUrl: " + refer);
//            Log.d(TAG, "getGlideUrl: " + ua);
            if (StringUtils.equals(baseUrl, url) && !url.contains("@Referer=")) {
                refer = "";
            }
            if(StringUtil.isNotEmpty(refer) && !refer.startsWith("http")){
                refer = "";
            }
            if (!StringUtil.isEmpty(ua)) {
                if (StringUtil.isEmpty(refer)) {
                    return new GlideUrl(url, builder.addHeader("User-Agent", ua).build());
                }
                return new GlideUrl(url, builder.addHeader("User-Agent", ua).addHeader("Referer", refer).build());
            } else {
                if (StringUtil.isEmpty(refer)) {
                    return new GlideUrl(url);
                }
                return new GlideUrl(url, builder.addHeader("Referer", refer).build());
            }
        }
    }

    public static void loadFullPic(Context context, ImageView imageView, Object url, RequestOptions options, int width, int height) {
        Glide.with(context)
                .asBitmap()
                .override(width, height)
                .apply(options)
                .load(url)
                .apply(options)
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        if (placeholder != null) {
                            imageView.setPadding(0, 0, 0, DisplayUtil.dpToPx(context, 5));
                            imageView.setImageDrawable(placeholder);
                            imageView.setAdjustViewBounds(true);
                        }
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        if (width == -1 && resource.getWidth() > ScreenUtil.getScreenMin(context)) {
                            int w = ScreenUtil.getScreenMin(context);
                            int h = (int) ((float) resource.getHeight() / (float) resource.getWidth() * w);
                            loadFullPic(context, imageView, url, options, w, h);
                        } else {
                            imageView.setPadding(0, 0, 0, 0);
                            imageView.setImageBitmap(resource);
                            imageView.setAdjustViewBounds(true);
                        }
                    }
                });
    }

    private static int[] smartCompress(Context context, Drawable resource){
        int w = resource.getIntrinsicWidth(), h = resource.getIntrinsicHeight();
        if (resource instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) resource;
            int byteCount = bitmapDrawable.getBitmap().getByteCount();
            Timber.d("Bitmap size: %s", byteCount);
            float radio = (float) ScreenUtil.getScreenMin(context) / w;
            if (w <= ScreenUtil.getScreenMin(context)) {
                //小于屏幕大小
                if (byteCount > 1024 * 1024 * 50) {
                    //超过50Mb，缩小一半，1080P->720P
                    w = (int) ((float) w / 1.5);
                    h = (int) ((float) h / 1.5);
                }
            } else if (byteCount < 1024 * 1024 * 10) {
                //小于10mb不压缩

            } else if (byteCount / radio / radio < 1024 * 1024 * 5) {
                //缩小到屏幕大小占用不超过5mb则以屏幕大小为准
                w = ScreenUtil.getScreenMin(context);
                h = (int) ((float) resource.getIntrinsicHeight() / (float) resource.getIntrinsicWidth() * w);
            } else if (byteCount > 1024 * 1024 * 100) {
                //超过100Mb，缩小到1/20
                w = (int) ((float) w / 4.5);
                h = (int) ((float) h / 4.5);
            } else if (byteCount > 1024 * 1024 * 50) {
                //超过50Mb，缩小到1/10
                w = (int) ((float) w / 3.3);
                h = (int) ((float) h / 3.3);
            } else if (byteCount > 1024 * 1024 * 20) {
                //超过20Mb，缩小到1/4
                w = (int) ((float) w / 2);
                h = (int) ((float) h / 2);
            }
        }
        if (w > ScreenUtil.getScreenMin(context)) {
            //如果依然超过屏幕宽度，则设置为屏幕宽度
            w = ScreenUtil.getScreenMin(context);
            h = (int) ((float) resource.getIntrinsicHeight() / (float) resource.getIntrinsicWidth() * w);
        }
        return new int[]{w, h};
    }

    public static void loadFullPicDrawable(Context context, ImageView imageView, Object url, RequestOptions options) {
        Glide.with(context)
                .asDrawable()
                .apply(options)
                .load(url)
                .apply(options)
                .into(new MyCustomTarget(context, imageView) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        int[] a = smartCompress(context, resource);
                        int w = a[0], h = a[1];
                        if (w != resource.getIntrinsicWidth()) {
                            imageView.setTag(resource.getIntrinsicWidth() + "×" + resource.getIntrinsicHeight() + "/" + w + "×" + h);
                            loadFullPicDrawable(context, imageView, url, options, w, h);
                        } else {
                            try {
                                if (resource instanceof GifDrawable) {
                                    ((GifDrawable) resource).setLoopCount(GifDrawable.LOOP_FOREVER);
                                    ((GifDrawable) resource).start();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            imageView.setTag(resource.getIntrinsicWidth() + "×" + resource.getIntrinsicHeight());
                            imageView.setPadding(0, 0, 0, 0);
                            imageView.setImageDrawable(resource);
                            imageView.setAdjustViewBounds(true);
                        }
                    }
                });
    }

    public static void loadFullPicDrawable(Context context, ImageView imageView, Object url, RequestOptions options, int width, int height) {
        Glide.with(context)
                .asDrawable()
                .override(width, height)
                .apply(options.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(url)
                .apply(options)
                .into(new MyCustomTarget(context, imageView) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        try {
                            if (resource instanceof GifDrawable) {
                                ((GifDrawable) resource).setLoopCount(GifDrawable.LOOP_FOREVER);
                                ((GifDrawable) resource).start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        imageView.setPadding(0, 0, 0, 0);
                        imageView.setImageDrawable(resource);
                        imageView.setAdjustViewBounds(true);
                    }
                });
    }

    public static void loadPicDrawable(Context context, ImageView imageView, Object url, RequestOptions options) {
        Glide.with(context)
                .asDrawable()
                .apply(options)
                .load(url)
                .apply(options)
                .into(new MyCustomTarget(context, imageView) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        if (placeholder != null) {
                            imageView.setImageDrawable(placeholder);
                        }
                    }

                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        int[] a = smartCompress(context, resource);
                        int w = a[0], h = a[1];
                        if (w != resource.getIntrinsicWidth()) {
                            loadPicDrawable(context, imageView, url, options, w, h);
                        } else {
                            try {
                                if (resource instanceof GifDrawable) {
                                    ((GifDrawable) resource).setLoopCount(GifDrawable.LOOP_FOREVER);
                                    ((GifDrawable) resource).start();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            imageView.setImageDrawable(resource);
                        }
                    }
                });
    }

    public static void loadPicDrawable(Context context, ImageView imageView, Object url, RequestOptions options, int width, int height) {
        Glide.with(context)
                .asDrawable()
                .override(width, height)
                .apply(options.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(url)
                .apply(options)
                .into(new MyCustomTarget(context, imageView) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        if (placeholder != null) {
                            imageView.setImageDrawable(placeholder);
                        }
                    }

                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        try {
                            if (resource instanceof GifDrawable) {
                                ((GifDrawable) resource).setLoopCount(GifDrawable.LOOP_FOREVER);
                                ((GifDrawable) resource).start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        imageView.setImageDrawable(resource);
                    }
                });
    }

    private abstract static class MyCustomTarget extends CustomTarget<Drawable> {
        private ImageView imageView;
        private Context context;

        public MyCustomTarget(Context context, ImageView imageView) {
            super();
            this.context = context;
            this.imageView = imageView;
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            if (placeholder != null) {
                imageView.setPadding(0, 0, 0, DisplayUtil.dpToPx(context, 5));
                imageView.setImageDrawable(placeholder);
                imageView.setAdjustViewBounds(true);
            }
        }

        @Override
        public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {
            try {
                if (placeholder instanceof GifDrawable) {
                    ((GifDrawable) placeholder).stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
