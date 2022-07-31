package com.example.hikerview.ui.browser.view;

/**
 * 作者：By 15968
 * 日期：On 2020/4/4
 * 时间：At 15:58
 */
public class WebViewClient extends android.webkit.WebViewClient {
//    private static final String TAG = "WebViewClient";
//    private BaseWebViewActivity activity;
//    private int openAppCount;
//    private String lastDom;
//    private long pageTimestamp;
//
//    public WebViewClient(BaseWebViewActivity activity){
//        this.activity = activity;
//    }
//
//    @Override
//    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//        handler.proceed();
//    }
//
//    @Override
//    public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
//        openAppCount = 0;
//        lastProgress = 0;
//        pageTimestamp = System.currentTimeMillis();
//        pageUrl = s;
//        hasSetAppBarColor = false;
//        hasLoadJsOnProgress = false;
//        if (isToastShow) {
//            toastView.animate().scaleY(0).scaleX(0).setDuration(300).start();
//            isToastShow = false;
//            new Handler().postDelayed(() -> {
//                if (!isToastShow) {
//                    toastView.setVisibility(View.INVISIBLE);
//                }
//            }, 270);
//        }
//        String dom = StringUtil.getDom(s);
//        if (dom != null && !dom.equals(lastDom)) {
////                Log.d(TAG, "onPageStarted: setUserAgentString===>" + s);
//            lastDom = dom;
//            String ua = UAModel.getAdjustUa(s);
////                Log.d(TAG, "onPageStarted: setUserAgentString(UAModel.getAdjustUa())==>" + ua);
//            if (!TextUtils.isEmpty(ua)) {
//                webViewT.getSettings().setUserAgentString(ua);
//            } else {
//                if (!TextUtils.isEmpty(UAModel.getUseUa())) {
////                        Log.d(TAG, "onPageStarted: setUserAgentString(UAModel.getUseUa())");
//                    webViewT.getSettings().setUserAgentString(UAModel.getUseUa());
//                } else {
//                    webViewT.getSettings().setUserAgentString(systemUA);
//                }
//            }
//        }
//        super.onPageStarted(webView, s, bitmap);
//        bottomBarXiuTanBg.setVisibility(View.GONE);
//        bottomBarXiuTan.setText("0");
//        hasAutoPlay = false;
//        if (blockImg) {
//            webViewT.getSettings().setBlockNetworkImage(true);
//        }
//        hasDismissXiuTan = false;
//        DetectorManager.getInstance().startDetect();
//        //检测剪贴板
//        checkClip(s, pageTimestamp);
//        showSearchEngine();
//        if (!isXiuTan) {
////                HeavyTaskUtil.saveHistory(getContext(), "网页浏览", "", s, webView.getTitle());
//        }
//        if (!s.equals(startUrl) && home_bottom_bg != null && home_bottom_bg.getVisibility() != GONE) {
//            home_bottom_bg.setVisibility(GONE);
//        }
//    }
//
//    @Override
//    public void onPageFinished(WebView webView, String s) {
//        if (progressView1 != null && progressView1.getVisibility() == VISIBLE) {
//            progressView1.setVisibility(View.INVISIBLE);
//            ((Animatable) progressView1.getDrawable()).stop();
//        }
//        loadAllJs(s);
//        if (!isXiuTan) {
//            HeavyTaskUtil.saveHistory(getContext(), "网页浏览", "", s, webView.getTitle());
//        }
//        String title1 = webView.getTitle();
//        if (!TextUtils.isEmpty(title1)) {
//            bottomTitleView.setText(title1);
//        }
//        super.onPageFinished(webView, s);
//    }
//
//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
////            Log.d(TAG, "shouldInterceptRequest: request:" + request.getUrl().toString());
//        long id = AdUrlBlocker.instance().shouldBlock(lastDom, request.getUrl().toString());
//        if (id >= 0) {
//            DetectedMediaResult mediaResult = new DetectedMediaResult(request.getUrl().toString());
//            mediaResult.setMediaType(new Media(Media.BLOCK, id + ""));
//            DetectorManager.getInstance().addMediaResult(mediaResult);
//            return new WebResourceResponse(null, null, null);
//        }
//        DetectorManager.getInstance().addTask(new VideoTask(request.getRequestHeaders(), request.getMethod(), request.getUrl().toString(), request.getUrl().toString()));
//        return WebViewCacheInterceptorInst.getInstance().interceptRequest(request);
//    }
//
//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//        final String url = request.getUrl().toString();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Log.d(TAG, "shouldOverrideUrlLoading: " + url + ", isredirect:" + request.isRedirect());
//        }
//
//        if (url.startsWith("http")) {
//            if (UrlDetector.isImage(url)) {
//                new XPopup.Builder(activity)
//                        .asImageViewer(null, url, new PopImageLoaderNoView(activity.getWebView().getUrl()))
//                        .show();
//            }
//            if (Build.VERSION.SDK_INT < 26) {
//                view.loadUrl(url);
//                return true;
//            }
//            return false;
//        } else {
//            if (url.startsWith("hiker://")) {
//                String route = url.replace("hiker://", "");
//                switch (route) {
//                    case "search":
//                        onClick(bottomTitleView);
//                        return true;
//                    case "download":
//                        Intent intent = new Intent(activity, DownloadRecordsActivity.class);
//                        intent.putExtra("downloaded", true);
//                        activity.startActivity(intent);
//                        return true;
//                    case "home":
//                        activity.finish();
//                        return true;
//                    case "bookmark":
//                        activity.startActivityForResult(new Intent(activity, BookmarkActivity.class), 101);
//                        return true;
//                    case "collection":
//                        activity.startActivityForResult(new Intent(activity, CollectionListActivity.class), 101);
//                        return true;
//                    case "history":
//                        activity.startActivityForResult(new Intent(activity, HistoryListActivity.class), 101);
//                        return true;
//                }
//            } else if (url.equals("folder://")) {
//                activity.startActivityForResult(new Intent(activity, BookmarkActivity.class), 101);
//                return true;
//            } else if (url.equals("history://")) {
//                activity.startActivityForResult(new Intent(activity, HistoryListActivity.class), 101);
//                return true;
//            } else if (url.equals("download://")) {
//                Intent intent = new Intent(activity, DownloadRecordsActivity.class);
//                intent.putExtra("downloaded", true);
//                activity.startActivity(intent);
//                return true;
//            }
//            if (openAppCount < 3) {
//                Snackbar.make(getSnackBarBg(), "是否允许网页打开外部应用？", Snackbar.LENGTH_LONG)
//                        .setAction("允许", v -> ShareUtil.findChooserToDeal(activity, url)).show();
//                openAppCount++;
//            }
//            return true;
//        }
//    }
}
