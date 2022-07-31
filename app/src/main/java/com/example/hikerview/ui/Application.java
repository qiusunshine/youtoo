package com.example.hikerview.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.example.hikerview.BuildConfig;
import com.example.hikerview.R;
import com.example.hikerview.constants.TimeConstants;
import com.example.hikerview.service.http.ContentTypeAfterInterceptor;
import com.example.hikerview.service.http.ContentTypePreInterceptor;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.dlan.DlanForegroundService;
import com.example.hikerview.ui.download.DownloadForegroundService;
import com.example.hikerview.ui.video.MusicForegroundService;
import com.example.hikerview.utils.CrashHandler;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.NotifyManagerUtils;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.DBCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.model.HttpHeaders;
import com.wanjian.cockroach.Cockroach;
import com.wanjian.cockroach.ExceptionHandler;
import com.zzhoujay.richtext.RichText;

import org.litepal.LitePal;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.brotli.BrotliInterceptor;
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptor;
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;
import ren.yale.android.cachewebviewlib.config.CacheExtensionConfig;
import timber.log.Timber;

/**
 * 作者：By hdy
 * 日期：On 2017/10/7
 * 时间：At 21:50
 */

public class Application extends android.app.Application {
    private static final String TAG = "Application";
    public static Application application = null;
    private static boolean hasMainActivity = false;
    private Activity homeActivity;
    public static long start;
    private static CookieJarImpl cookieJar;
    public static CookieJarImpl getCookieJar() {
        return cookieJar;
    }

    public static boolean hasMainActivity() {
        return hasMainActivity && application != null && application.getHomeActivity() != null;
    }

    public static void setHasMainActivity(boolean hasMainActivity) {
        Application.hasMainActivity = hasMainActivity;
    }

    public int mzNightModeUseOf() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return 1;
        }
        return 2;
    }

    @Override
    public void onCreate() {
        start = System.currentTimeMillis();
        super.onCreate();
        application = this;
        registerActivityLifecycleCallbacks(ActivityManager.getInstance());
        //OKGO配置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(BrotliInterceptor.INSTANCE);
        builder.addInterceptor(ContentTypePreInterceptor.INSTANCE);
        builder.addNetworkInterceptor(ContentTypeAfterInterceptor.INSTANCE);
        builder.readTimeout(TimeConstants.HTTP_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.writeTimeout(TimeConstants.HTTP_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.connectTimeout(TimeConstants.HTTP_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //方法一：信任所有证书,不安全有风险
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, HttpsUtils.UnSafeTrustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
        LitePal.initialize(this);
        LitePal.getDatabase().disableWriteAheadLogging();
        //cookiejar
        try {
            //vivo的分身会崩溃，非分身和其它系统正常
            cookieJar = new CookieJarImpl(new DBCookieStore(getContext()));
            builder.cookieJar(cookieJar);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.put("charset", "UTF-8");
        //无法使用，因为一旦全局用了这个就无法自定义cookie了，okhttp在拦截器里面加载cookieJar的时是直接set的
//        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));
        OkHttpClient okHttpClient = builder.build();
        okHttpClient.dispatcher().setMaxRequestsPerHost(16);
        OkGo.getInstance().init(this).setOkHttpClient(okHttpClient)
                .setRetryCount(1)
                .addCommonHeaders(headers);
        LitePal.initialize(this);
        installCrashHandler();
        initCacheWeb();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
//        Timber.d("consume: app  onCreate %s", (System.currentTimeMillis() - start));
        RichText.initCacheDir(this);
        // 写 ActivityLifecycle 调试 Task 信息用
        // Timber.tag("Application").d(activity.getLocalClassName() + "#TaskId#" + activity.getTaskId());
    }

    private void installCrashHandler() {
        CrashHandler.getInstance().initDefaultHandler(getContext());
        Cockroach.install(this, new ExceptionHandler() {
            @Override
            protected void onUncaughtExceptionHappened(Thread thread, Throwable throwable) {
                Timber.e(throwable, "--->onUncaughtExceptionHappened:" + thread + "<---");
                String fileName = CrashHandler.getInstance().saveCatchInfo2File(throwable);
                new Handler(Looper.getMainLooper()).post(() -> {
                    ToastMgr.shortBottomCenter(getApplicationContext(), "检测到异常崩溃信息，已记录崩溃日志");
                });
            }

            @Override
            protected void onBandageExceptionHappened(Throwable throwable) {
                String fileName = CrashHandler.getInstance().saveCatchInfo2File(throwable);
                new Handler(Looper.getMainLooper()).post(() -> {
                    ToastMgr.shortBottomCenter(getApplicationContext(), "检测到异常崩溃信息，已记录崩溃日志");
                });
            }

            @Override
            protected void onEnterSafeMode() {

            }

            @Override
            protected void onMayBeBlackScreen(Throwable e) {
                Thread thread = Looper.getMainLooper().getThread();
                CrashHandler.getInstance().crashMySelf(thread, e);
            }

        });
    }

    private void initCacheWeb() {
        String homeIp = getResources().getString(R.string.home_ip);
        HeavyTaskUtil.executeNewTask(() -> {
            WebViewCacheInterceptor.Builder builder2 = new WebViewCacheInterceptor.Builder(this);
            CacheExtensionConfig extension = new CacheExtensionConfig();
            builder2.setDebug(false);
            builder2.setCacheSize(1024 * 1024 * 300);
            //删除缓存后缀
            extension.removeExtension("html").removeExtension("htm")
                    .removeExtension("js").removeExtension("css").removeExtension("txt")
                    .removeExtension("gif").removeExtension("bmp");
            builder2.setCacheExtensionConfig(extension);
            builder2.setResourceInterceptor(url -> {
                if (StringUtil.isEmpty(url)) {
                    return false;
                }
                return url.contains(".oss-cn-hangzhou.aliyuncs.com") ||
                        (url.contains(homeIp) && UrlDetector.isImage(url));
            });
            WebViewCacheInterceptorInst.getInstance().init(builder2);
        });
    }

    public static Context getContext() {
        return application;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(base);
    }

    public void startDlanForegroundService(Context context) {
        try {
            stopDlanForegroundService();
            NotifyManagerUtils.Companion.openNotification(context, () -> {
                Intent intent = new Intent(Application.application, DlanForegroundService.class);
                startService(intent);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopDlanForegroundService() {
        try {
            Intent intent = new Intent(Application.application, DlanForegroundService.class);
            stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDownloadForegroundService() {
        NotifyManagerUtils.Companion.openNotification(getContext(), () -> {
            Intent intent = new Intent(Application.application, DownloadForegroundService.class);
            startService(intent);
        });
    }

    public void stopDownloadForegroundService() {
        try {
            Intent intent = new Intent(Application.application, DownloadForegroundService.class);
            stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startMusicForegroundService(Context context) {
        try {
            NotifyManagerUtils.Companion.openNotification(context, () -> {
                Intent intent = new Intent(Application.application, MusicForegroundService.class);
                startService(intent);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusicForegroundService() {
        try {
            Intent intent = new Intent(Application.application, MusicForegroundService.class);
            stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Activity getHomeActivity() {
        if (homeActivity != null && homeActivity.isFinishing()) {
            return null;
        }
        return homeActivity;
    }

    public void setHomeActivity(Activity homeActivity) {
        this.homeActivity = homeActivity;
    }

//    @Override
//    public String getPackageName() {
//        try {
//            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//            for (StackTraceElement element : stackTrace) {
//                if ("org.chromium.base.BuildInfo".equalsIgnoreCase(element.getClassName())) {
//                    if ("getAll".equalsIgnoreCase(element.getMethodName())) {
//                        return "com.tencent.qq";
//                    }
//                    break;
//                }
//            }
//        } catch (Exception e) { }
//        return super.getPackageName();
//    }
}
