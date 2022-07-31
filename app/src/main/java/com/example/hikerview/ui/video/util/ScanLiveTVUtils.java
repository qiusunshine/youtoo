package com.example.hikerview.ui.video.util;

import android.text.TextUtils;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.service.http.CharsetStringCallback;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.webdlan.LocalServerParser;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * 作者：By hdy
 * 日期：On 2019/3/30
 * 时间：At 23:33
 */
public class ScanLiveTVUtils {
    public static boolean isScanning = false;
    private static final String PORT = ":12345";
    private static final String HTTP = "http://";
    private static final String PLAY_URL = "/index.html";

    private OkHttpClient okHttpClient;
    private AtomicBoolean hasFound;
    private AtomicInteger counter;
    private Consumer<String> okListener;
    private Consumer<String> failListener;


    /**
     * 扫描局域网内ip，找到对应服务器
     *
     * @return void
     */
    public void scan(Consumer<String> okListener, Consumer<String> failListener) {
        isScanning = true;
        this.okListener = okListener;
        this.failListener = failListener;
        // 本机IP地址-完整
        String mDevAddress = LocalServerParser.getIP(Application.getContext());// 获取本机IP地址
        // 局域网IP地址头,如：192.168.1.
        String mLocAddress = getLocAddrIndex(mDevAddress);// 获取本地ip前缀
        Timber.d("开始扫描设备,本机Ip为：%s", mDevAddress);
        if (TextUtils.isEmpty(mLocAddress)) {
            Timber.e("扫描失败，请检查wifi网络");
            return;
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);

        builder.readTimeout(2000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(2000, TimeUnit.MILLISECONDS);
        builder.connectTimeout(2000, TimeUnit.MILLISECONDS);

        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
        okHttpClient = builder.build();

        hasFound = new AtomicBoolean(false);
        counter = new AtomicInteger(256);

        for (int i = 0; i <= 255; i++) {
            String currentIp = mLocAddress + i;
            HeavyTaskUtil.executeNewTask(new MyRunnable(currentIp));
        }
    }

    /**
     * 获取本机IP前缀
     *
     * @param devAddress // 本机IP地址
     * @return String
     */
    private String getLocAddrIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

    class MyRunnable implements Runnable {
        private String currentIp;

        MyRunnable(String currentIp) {
            this.currentIp = currentIp;
        }


        @Override
        public void run() {
            if (hasFound.get()) {
                Timber.d("ScanDeviceUtil hasFound, stop load");
                return;
            }
            String url = HTTP + currentIp + PORT;
            Timber.d("ScanDeviceUtil start scan url: %s", url);
            OkGo.<String>get(url + PLAY_URL)
                    .client(okHttpClient)
                    .execute(new CharsetStringCallback("UTF-8") {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                            String s = response.body();
                            if (s != null && s.length() > 0) {
                                String deviceName = s;
                                if ("ok".equals(s)) {
                                    deviceName = currentIp;
                                }
                                Timber.d("ScanDeviceUtil 成功获取到连接：%s", url);
                                if (!hasFound.get()) {
                                    hasFound.set(true);
                                    OkGo.cancelAll(okHttpClient);
                                    isScanning = false;
                                    okListener.accept(url);
                                    return;
                                }
                            }
                            int now = counter.decrementAndGet();
                            if (now <= 0) {
                                failListener.accept("");
                            }
                        }

                        @Override
                        public void onError(com.lzy.okgo.model.Response<String> response) {
                            int now = counter.decrementAndGet();
                            if (now <= 0) {
                                isScanning = false;
                                failListener.accept("");
                            }
                            super.onError(response);
                        }
                    });
        }
    }
}
