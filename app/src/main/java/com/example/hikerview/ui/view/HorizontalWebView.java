package com.example.hikerview.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.R;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.ui.browser.view.BaseWebViewActivity;
import com.example.hikerview.ui.browser.view.IVideoWebView;
import com.example.hikerview.ui.browser.webview.WebViewHelper;
import com.example.hikerview.ui.view.colorDialog.util.DisplayUtil;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.ScreenUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.WebUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2020/2/8
 * 时间：At 21:54
 */
public class HorizontalWebView extends WebView implements IVideoWebView {
    private static final String TAG = "HorizontalWebView";
    private float focusX, focusY;
    private boolean used = false;
    private boolean shouldClear = false;
    private int step;
    private onLoadListener onLoadListener;
    private OnUrlLoadListener onUrlLoadListener;
    private String ua;
    private boolean useTranslate;
    private boolean useDevMode;

    public WebViewHelper getWebViewHelper() {
        return webViewHelper;
    }

    public void updateWebViewHelper(BaseWebViewActivity activity) {
        if (webViewHelper == null) {
            webViewHelper = new WebViewHelper(activity, this);
        }
        webViewHelper.updateProperties(activity);
    }

    public void removeProperties() {
        webViewHelper.removeProperties();
    }

    private WebViewHelper webViewHelper;

    public List<DetectedMediaResult> getDetectedMediaResults() {
        return detectedMediaResults;
    }

    public void setDetectedMediaResults(List<DetectedMediaResult> detectedMediaResults) {
        this.detectedMediaResults = detectedMediaResults;
    }

    private List<DetectedMediaResult> detectedMediaResults;

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public void setStatusBarColor(int statusBarColor) {
        this.statusBarColor = statusBarColor;
    }

    private int statusBarColor = Color.parseColor("#ffffff");

    public OnLongClickListener getLongClickListener() {
        return longClickListener;
    }

    public void setLongClickListener(OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    private OnLongClickListener longClickListener;
    private ActionMode mActionMode;

    public HorizontalWebView(Context context) {
        super(context);
        init();
    }

    public HorizontalWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            setForceDarkAllowed(true);
//        }
        try {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean forceDark = PreferenceMgr.getBoolean(getContext(), "forceDark", true);
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES && forceDark) {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        getSettings().setForceDark(WebSettings.FORCE_DARK_ON);
                    }
                }
            } else {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        getSettings().setForceDark(WebSettings.FORCE_DARK_OFF);
                    }
                }
            }
        } catch (Exception e) {

        }
        setBackgroundColor(0);
    }

    public void setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    private MenuItem.OnMenuItemClickListener onMenuItemClickListener;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        focusX = ev.getRawX();
        focusY = ev.getRawY();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getPointerCount() > 1) {
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
//        Log.d(TAG, "onOverScrolled: clampedX=" + clampedX + ", clampedY=" + clampedY + ", scrollX=" + scrollX + ", scrollY=" + scrollY);
        if (clampedX && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
//        evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "clearSelectText.js"), null);
        ActionMode actionMode = super.startActionMode(callback);
        return resolveActionMode(actionMode);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
//        evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "clearSelectText.js"), null);
        ActionMode actionMode = super.startActionMode(callback, type);
        return resolveActionMode(actionMode);
    }

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        ActionMode actionMode = super.startActionModeForChild(originalView, callback);
        return resolveActionMode(actionMode);
    }

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        ActionMode actionMode = super.startActionModeForChild(originalView, callback, type);
        return resolveActionMode(actionMode);
    }

    /**
     * 处理item，处理点击
     *
     * @param actionMode
     */
    private ActionMode resolveActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
        try {
            if (actionMode != null) {
                Menu menu = actionMode.getMenu();
                int sOrder = Menu.NONE;
                int min = sOrder;
                int groupId = Menu.NONE;
                for (int i = 0; i < menu.size(); i++) {
                    if ("拦截元素".equals(menu.getItem(i).getTitle().toString())) {
                        //已经处理过了，直接返回
                        return actionMode;
                    }
                    if (min == Menu.NONE || min > menu.getItem(i).getOrder()) {
                        min = menu.getItem(i).getOrder();
                        groupId = menu.getItem(i).getGroupId();
                    }
                    if (sOrder == Menu.NONE || sOrder > menu.getItem(i).getOrder()) {
                        if (min < menu.getItem(i).getOrder()) {
                            sOrder = menu.getItem(i).getOrder();
                            //                        groupId = menu.getItem(i).getGroupId();
                        }
                    }
                }
                try {
                    List<MenuItem> items = new ArrayList<>();
                    for (int i = 0; i < menu.size(); i++) {
                        items.add(menu.getItem(i));
                    }
                    for (MenuItem item : items) {
                        if ("网页搜索".equals(item.getTitle().toString())) {
                            menu.removeItem(item.getItemId());
                        }
                        if ("搜索".equals(item.getTitle().toString())) {
                            menu.removeItem(item.getItemId());
                        }
                        if ("翻译".equals(item.getTitle().toString())) {
                            menu.removeItem(item.getItemId());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                menu.add("翻译");
                menu.add(groupId, 65535, min, "拦截元素");
                menu.add("搜索");
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    if ("拦截元素".contentEquals(menuItem.getTitle())) {
                        menuItem.setOnMenuItemClickListener(item -> {
                            //                    setOnLongClickListener(longClickListener);
                            onMenuItemClickListener.onMenuItemClick(item);
                            releaseAction();
                            //                    evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "clearSelectText.js"), null);
                            return true;
                        });
                    } else if ("搜索".contentEquals(menuItem.getTitle())) {
                        menuItem.setOnMenuItemClickListener(item -> {
                            evaluateJavascript(getSel("searchBySelect"), null);
                            releaseAction();
                            return true;
                        });
                    } else if ("翻译".contentEquals(menuItem.getTitle())) {
                        menuItem.setOnMenuItemClickListener(item -> {
                            evaluateJavascript(getSel("translate"), null);
                            releaseAction();
                            return true;
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actionMode;
    }

    private String getSel(String method) {
        return "var txt = '';" +
                "if (window.getSelection) {" +
                "txt = window.getSelection().toString();" +
                "} else if (window.document.getSelection) {" +
                "txt = window.document.getSelection().toString();" +
                "} else if (window.document.selection) {" +
                "txt = window.document.selection.createRange().text;" +
                "}\n" +
                "fy_bridge_app." + method + "(txt);";
    }

//    public void freeCopy() {
//        evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "startSelectText.js"), null);
//        new Thread() {
//            public void run() {
//                for (; ; ) {
//                    MotionEvent localMotionEvent3;
//                    try {
//                        long l1 = SystemClock.uptimeMillis();
//                        long l2 = SystemClock.uptimeMillis();
//                        MotionEvent localMotionEvent1 = MotionEvent.obtain(l1, l2, 0, focusX, focusY, 0);
//                        localMotionEvent1.setSource(4098);
//                        MotionEvent localMotionEvent2 = MotionEvent.obtain(l1, l2, 2, focusX, focusY, 0);
//                        localMotionEvent2.setSource(4098);
//                        localMotionEvent3 = MotionEvent.obtain(l1, l2 + 500, 3, focusX, focusY, 0);
//                        localMotionEvent3.setSource(4098);
//                        dispatchTouchEvent(localMotionEvent1);
//                        dispatchTouchEvent(localMotionEvent2);
//                    } catch (NullPointerException localNullPointerException) {
//                        localNullPointerException.printStackTrace();
//                        continue;
//                    }
//                    try {
//                        Thread.sleep(500);
//                        dispatchTouchEvent(localMotionEvent3);
//                        return;
//                    } catch (InterruptedException localInterruptedException) {
//                    }
//                }
//            }
//        }.start();
//    }

    private void releaseAction() {
        if (mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    public float getFocusX() {
        return focusX;
    }

    public void setFocusX(float focusX) {
        this.focusX = focusX;
    }

    public float getFocusY() {
        return focusY;
    }

    public void setFocusY(float focusY) {
        this.focusY = focusY;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isShouldClear() {
        return shouldClear;
    }

    public void setShouldClear(boolean shouldClear) {
        this.shouldClear = shouldClear;
    }

    public Bitmap capturePreview() {
        return capturePreview(this, true);
    }

    public Bitmap capturePreview(View view, boolean whiteBackground) {
        int width = DisplayUtil.dp2px(getContext(), 180);
        int height = DisplayUtil.dp2px(getContext(), 320);

        if (ScreenUtil.isOrientation(WebUtil.getWebViewActivity())) {
            //横屏模式
            int temp = width;
            width = height;
            height = temp;
        }

        if (width > 0 && height > 0) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            int left = view.getScrollX();
            int top = view.getScrollY();
            canvas.translate(-left, -top);
            float scaleX = (float) width / view.getWidth();
            float scaleY = (float) height / view.getHeight();
            canvas.scale(scaleX, scaleY, left, top);
            if (whiteBackground) {
                canvas.drawColor(Color.WHITE);
            }
            view.draw(canvas);
            canvas.setBitmap(null);
            return bitmap;
        }
        return null;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public void goBack() {
        if (onLoadListener != null) {
            onLoadListener.goBack();
        }
        super.goBack();
    }

    @Override
    public void goForward() {
        if (onLoadListener != null) {
            onLoadListener.goForward();
        }
        super.goForward();
    }

    @Override
    public void reload() {
        if (onLoadListener != null) {
            onLoadListener.reload();
        }
        super.reload();
    }


    @Override
    public void loadUrl(String url) {
        if (onUrlLoadListener != null) {
            onUrlLoadListener.load(this);
        }
        String dom = StringUtil.getDom(url);
        if (getResources().getString(R.string.home_domain).equals(dom)) {
            url = url.replace(getResources().getString(R.string.home_domain), getResources().getString(R.string.home_ip));
        }
        Timber.d("loadUrl: %s", url);
        if (onLoadListener != null) {
            onLoadListener.loadUrl(url);
        }
        Map<String, String> additionalHttpHeaders = new HashMap<>();
        additionalHttpHeaders.put("X-Requested-With", "com.android.browser");
        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        if (onUrlLoadListener != null) {
            onUrlLoadListener.load(this);
        }
        String dom = StringUtil.getDom(url);
        if (getResources().getString(R.string.home_domain).equals(dom)) {
            url = url.replace(getResources().getString(R.string.home_domain), getResources().getString(R.string.home_ip));
        }
        Timber.d("additionalHttpHeaders, loadUrl: %s", url);
        if (onLoadListener != null) {
            onLoadListener.loadUrl(url, additionalHttpHeaders);
        }
        super.loadUrl(url, additionalHttpHeaders);
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        Timber.d("loadUrl: data: %s", data);
        if (onLoadListener != null) {
            onLoadListener.loadData(data, mimeType, encoding);
        }
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding,
                                    String historyUrl) {
        if (onLoadListener != null) {
            onLoadListener.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
        }
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void stopLoading() {
        if (onLoadListener != null) {
            onLoadListener.stopLoading();
        }
        super.stopLoading();
    }

    public HorizontalWebView.onLoadListener getOnLoadListener() {
        return onLoadListener;
    }

    public void setOnLoadListener(HorizontalWebView.onLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public OnUrlLoadListener getOnUrlLoadListener() {
        return onUrlLoadListener;
    }

    public void setOnUrlLoadListener(OnUrlLoadListener onUrlLoadListener) {
        this.onUrlLoadListener = onUrlLoadListener;
    }

    public String getUa() {
        return ua;
    }

    public String getUaNonNull() {
        if (StringUtil.isNotEmpty(getUa())) {
            return getUa();
        }
        return getSettings().getUserAgentString();
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public boolean isUseTranslate() {
        return useTranslate;
    }

    public void setUseTranslate(boolean useTranslate) {
        this.useTranslate = useTranslate;
    }

    public boolean isUseDevMode() {
        return useDevMode;
    }

    public void setUseDevMode(boolean useDevMode) {
        this.useDevMode = useDevMode;
    }

    public interface onLoadListener {
        void loadUrl(String url);

        void loadUrl(String url, Map<String, String> additionalHttpHeaders);

        void loadData(String data, String mimeType, String encoding);

        void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl);

        void reload();

        void goBack();

        void goForward();

        void stopLoading();
    }

    public interface OnUrlLoadListener {
        void load(HorizontalWebView webView);
    }

    @Override
    public void postTask(@NotNull Runnable task) {
        post(task);
    }

    @Override
    public void useFastPlay(boolean use) {
        evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "fastPlay.js").replace("{playbackRate}", (use ? "2" : "1")), null);
    }

    @Override
    public void evaluateJS(@NotNull String js, @Nullable Consumer<String> resultCallback) {
        evaluateJavascript(js, s -> {
            if (resultCallback != null) {
                resultCallback.accept(s == null ? "" : s);
            }
        });
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void addJSInterface(@NotNull Object obj, @NotNull String interfaceName) {
        addJavascriptInterface(obj, interfaceName);
    }
}