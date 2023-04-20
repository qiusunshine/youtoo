package com.example.hikerview.ui.browser.webview;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.example.hikerview.ui.browser.view.BaseWebViewActivity;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2020/4/4
 * 时间：At 16:09
 */
public class MultiWindowManager {
    private static final String TAG = "MultiWindowManager";

    private volatile static MultiWindowManager sInstance;

    public List<HorizontalWebView> getWebViewList() {
        Timber.d("MultiWindowManager: getWebViewList: %s", webViewList.size());
        return webViewList;
    }

    public List<HorizontalWebView> getUsedWebViewList() {
        Timber.d("MultiWindowManager: getWebViewList: %s", webViewList.size());
        List<HorizontalWebView> list = new ArrayList<>();
        for (HorizontalWebView webView : webViewList) {
            boolean isParent = false;
            for (HorizontalWebView check : webViewList) {
                if (webView == check.getParentWebView()) {
                    isParent = true;
                    break;
                }
            }
            //不是任何一个WebView的父组件说明才是在用的
            if (!isParent) {
                list.add(webView);
            }
        }
        return list;
    }

    private List<HorizontalWebView> webViewList = new ArrayList<>();

    public void setActivity(BaseWebViewActivity activity) {
        this.activity = activity;
    }

    private BaseWebViewActivity activity;
    private HorizontalWebView.OnUrlLoadListener onUrlLoadListener;

    private MultiWindowManager() {
    }

    public static MultiWindowManager instance(Activity activity) {
        if (sInstance == null) {
            synchronized (MultiWindowManager.class) {
                if (sInstance == null) {
                    sInstance = new MultiWindowManager();
                }
            }
        }
        if (activity instanceof BaseWebViewActivity) {
            InternalContext.getInstance().setBaseContext(activity);
            sInstance.activity = (BaseWebViewActivity) activity;
        }
        return sInstance;
    }

    public static MultiWindowManager instance() {
        if (sInstance == null) {
            synchronized (MultiWindowManager.class) {
                if (sInstance == null) {
                    sInstance = new MultiWindowManager();
                }
            }
        }
        return sInstance;
    }

    public HorizontalWebView getCurrentWebView() {
        for (HorizontalWebView webView : webViewList) {
            if (webView.isUsed()) {
                return webView;
            }
        }
        return null;
    }

    public HorizontalWebView initBaseWebview(boolean notNeedLoad) {
        if (webViewList.size() > 0) {
            for (int i = 0; i < webViewList.size(); i++) {
                HorizontalWebView horizontalWebView = webViewList.get(i);
                if (horizontalWebView.isUsed()) {
                    if (!notNeedLoad) {
                        //销毁之前的，新建一个新的来加载
                        Log.d(TAG, "initBaseWebview: noload=true");
                        webViewList.remove(i);
                        try {
                            horizontalWebView.destroy();
                        } catch (Throwable ignored) {
                        }
                        HorizontalWebView webView = new HorizontalWebView(InternalContext.getInstance().getMutableContext());
                        webView.setUsed(true);
                        initWebView(webView);
                        webViewList.add(i, webView);
                        return webView;
                    } else {
                        Timber.d("initBaseWebview: noload=false, color=#%06X", horizontalWebView.getStatusBarColor());
                        //直接使用之前得
                        initWebView(horizontalWebView);
                        return horizontalWebView;
                    }
                }
            }
        }
        //新建一个
        HorizontalWebView webView = new HorizontalWebView(InternalContext.getInstance().getMutableContext());
        webView.setUsed(true);
        initWebView(webView);
        webViewList.add(webView);
        return webView;
    }

    public HorizontalWebView addWebView(String url) {
        return addWebView(url, true);
    }

    public HorizontalWebView addWebView(String url, boolean showNow) {
        return addWebView(url, showNow, null);
    }

    public HorizontalWebView addWebView(String url, boolean showNow, @Nullable WebView fromWho) {
        if (showNow) {
            for (HorizontalWebView horizontalWebView : webViewList) {
                try {
                    horizontalWebView.setUsed(false);
                    unbindBaseProperties(horizontalWebView);
                    horizontalWebView.stopLoading();
                    horizontalWebView.onPause();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        HorizontalWebView webView = new HorizontalWebView(InternalContext.getInstance().getMutableContext());
        webView.setUsed(showNow);
        initWebView(webView);
        if (StringUtil.isNotEmpty(url)) {
            webView.loadUrl(url);
        }
        if (fromWho != null) {
            for (int i = 0; i < webViewList.size(); i++) {
                if (fromWho == webViewList.get(i)) {
                    webViewList.add(i + 1, webView);
                    break;
                }
            }
        } else {
            webViewList.add(webView);
        }
        return webView;
    }

    /**
     * 开启页面缓存，干掉老的父WebView，降低性能消耗
     *
     * @param webView
     */
    public void checkTooManyParents(HorizontalWebView webView) {
        HorizontalWebView parentView = webView.getParentWebView();
        int count = 0;
        HorizontalWebView last = parentView, survive = null;
        while (parentView != null) {
            count++;
            survive = last;
            last = parentView;
            parentView = parentView.getParentWebView();
        }
        if (count >= 5) {
            //有5个及以上，干掉最后一个
            List<String> parentUrls = last.getParentUrls();
            if (parentUrls == null) {
                parentUrls = new ArrayList<>();
            }
            parentUrls.add(last.getUrl());
            try {
                unbindBaseProperties(last);
                last.stopLoading();
                last.onPause();
                last.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            webViewList.remove(last);
            survive.setParentWebView(null);
            survive.setParentUrls(parentUrls);
        }
    }

    /**
     * 重建WebView
     *
     * @param webView
     * @return
     */
    public HorizontalWebView recreate(HorizontalWebView webView) {
        int pos = -1;
        for (int i = 0; i < webViewList.size(); i++) {
            if (webViewList.get(i) == webView) {
                pos = i;
                break;
            }
        }
        if (pos < 0) {
            return null;
        }
        boolean used = webView.isUsed();
        webView.stopLoading();
        webView.onPause();
        unbindBaseProperties(webView);
        HorizontalWebView parentView = webView.getParentWebView();
        ViewParent parent = webView.getParent();
        try {
            if (parent != null) {
                ((ViewGroup) parent).removeView(webView);
            }
            webView.destroy();
        } catch (Throwable ignored) {
        }
        webView = new HorizontalWebView(InternalContext.getInstance().getMutableContext());
        webView.setUsed(used);
        initWebView(webView);
        webViewList.set(pos, webView);
        if (parent != null) {
            ((ViewGroup) parent).addView(webView);
        }
        while (parentView != null) {
            try {
                unbindBaseProperties(parentView);
                parentView.stopLoading();
                parentView.onPause();
                parentView.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            webViewList.remove(parentView);
            parentView = parentView.getParentWebView();
        }
        return webView;
    }

    public HorizontalWebView selectWindow(int index) {
        List<HorizontalWebView> list = getUsedWebViewList();
        HorizontalWebView webView = list.get(index);
        int pos = webViewList.indexOf(webView);
        for (int i = 0; i < webViewList.size(); i++) {
            try {
                if (pos == i) {
                    if (!webViewList.get(i).isUsed()) {
                        webViewList.get(i).setUsed(true);
                        initWebView(webViewList.get(i));
                    }
                } else {
                    if (webViewList.get(i).isUsed()) {
                        webViewList.get(i).setUsed(false);
                        unbindBaseProperties(webViewList.get(i));
                        webViewList.get(i).stopLoading();
                        webViewList.get(i).onPause();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return webViewList.get(pos);
    }

    private void destroyWebView(HorizontalWebView webView) {
        unbindBaseProperties(webView);
        webView.stopLoading();
        webView.onPause();
        webView.destroy();
        HorizontalWebView parent = webView.getParentWebView();
        while (parent != null) {
            try {
                unbindBaseProperties(parent);
                parent.stopLoading();
                parent.onPause();
                parent.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            parent = parent.getParentWebView();
        }
    }

    public void clearOtherWebView() {
        HorizontalWebView current = null;
        List<HorizontalWebView> list = getUsedWebViewList();
        for (HorizontalWebView webView : list) {
            if (webView.isUsed()) {
                current = webView;
            } else {
                destroyWebView(webView);
            }
        }
        webViewList.clear();
        if (current != null) {
            webViewList.add(current);
            HorizontalWebView parent = current.getParentWebView();
            while (parent != null) {
                webViewList.add(0, parent);
                parent = parent.getParentWebView();
            }
        }
    }

    /**
     * 清除所有窗口
     *
     * @return
     */
    public HorizontalWebView clear() {
        if (webViewList.size() == 0) {
            return addWebView(null);
        } else {
            //先移除没有使用的
            for (Iterator<HorizontalWebView> iterator = webViewList.iterator(); iterator.hasNext(); ) {
                HorizontalWebView webView = iterator.next();
                if (!webView.isUsed()) {
                    unbindBaseProperties(webView);
                    try {
                        if (webView.getParent() != null) {
                            ((ViewGroup) webView.getParent()).removeView(webView);
                        }
                        webView.destroy();
                    } catch (Throwable ignored) {
                    }
                    iterator.remove();
                }
            }
            //再移除最后一个
            if (webViewList.isEmpty()) {
                return addWebView(null);
            } else {
                return removeWebView(0);
            }
        }
    }

    public HorizontalWebView removeWebView(HorizontalWebView horizontalWebView) {
        for (int i = 0; i < webViewList.size(); i++) {
            if (webViewList.get(i) == horizontalWebView) {
                return removeWebView(i);
            }
        }
        return null;
    }

    public int removeByIndex(List<HorizontalWebView> list, int index) {
        if (index < 0 || index >= list.size()) {
            return 0;
        }
        HorizontalWebView webView = list.get(index);
        HorizontalWebView parent = webView.getParentWebView();
        int count = 1;
        while (parent != null) {
            webViewList.remove(parent);
            try {
                unbindBaseProperties(parent);
                if (parent.getParent() != null) {
                    ((ViewGroup) parent.getParent()).removeView(parent);
                }
                parent.stopLoading();
                parent.onPause();
                parent.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            count++;
            parent = parent.getParentWebView();
        }
        webViewList.remove(webView);
        try {
            unbindBaseProperties(webView);
            if (webView.getParent() != null) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
            webView.stopLoading();
            webView.onPause();
//            webView.pauseTimers();
            webView.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public HorizontalWebView removeWindow(int index) {
        List<HorizontalWebView> list = getUsedWebViewList();
        if (index < 0 || index >= list.size()) {
            ToastMgr.shortBottomCenter(activity, "移除窗口失败");
            return null;
        }
        HorizontalWebView webView = list.get(index);
//        int position = webViewList.indexOf(webView);
        if (webView.isUsed()) {
            //移除正在用的
            int newIndex = index - 1;
            if (index == 0) {
                newIndex = index + 1;
            }
            if (newIndex < 0 || newIndex >= list.size()) {
                //只剩一个窗口，不移除，加载首页
                removeByIndex(list, index);
                return addWebView(null);
            } else {
                //使用前一个webview，销毁当前的
                Log.d(TAG, "removeWebView: use last, " + newIndex);
                removeByIndex(list, index);
                if (newIndex > index) {
                    newIndex = newIndex - 1;
                }
                list.get(newIndex).setUsed(true);
                initWebView(list.get(newIndex));
                return list.get(newIndex);
            }
        } else {
            //移除别的窗口，销毁那个webview
            removeByIndex(list, index);
            //返回正在使用的webview
            return null;
        }
    }

    public HorizontalWebView removeWebView(int position) {
        if (position < 0 || position >= webViewList.size()) {
            ToastMgr.shortBottomCenter(activity, "移除窗口失败");
            return null;
        }
        HorizontalWebView webView = webViewList.get(position);
        if (webView.isUsed()) {
            //移除正在用的
            webView.stopLoading();
            webView.onPause();
//            webView.pauseTimers();
            int newPos = position - 1;
            if (position == 0) {
                newPos = position + 1;
            }
            if (newPos < 0 || newPos >= webViewList.size()) {
                //只剩一个窗口，不移除，加载首页
                unbindBaseProperties(webView);
                try {
                    if (webView.getParent() != null) {
                        ((ViewGroup) webView.getParent()).removeView(webView);
                    }
                    webView.destroy();
                } catch (Throwable ignored) {
                }
                webViewList.remove(position);
                return addWebView(null);
            } else {
                //使用前一个webview，销毁当前的
                Log.d(TAG, "removeWebView: use last, " + newPos);
                unbindBaseProperties(webView);
                try {
                    if (webView.getParent() != null) {
                        ((ViewGroup) webView.getParent()).removeView(webView);
                    }
                    webView.destroy();
                } catch (Throwable ignored) {
                }
                webViewList.remove(position);
                if (newPos > position) {
                    newPos = newPos - 1;
                }
                webViewList.get(newPos).setUsed(true);
                initWebView(webViewList.get(newPos));
                return webViewList.get(newPos);
            }
        } else {
            //移除别的窗口，销毁那个webview
            unbindBaseProperties(webView);
            try {
                if (webView.getParent() != null) {
                    ((ViewGroup) webView.getParent()).removeView(webView);
                }
                webView.destroy();
            } catch (Throwable ignored) {
            }
            webViewList.remove(position);
            //返回正在使用的webview
            return null;
        }
    }

    public synchronized void releaseAllWebview() {
        for (HorizontalWebView horizontalWebView : webViewList) {
            horizontalWebView.stopLoading();
            horizontalWebView.onPause();
        }
        webViewList.clear();
    }

    public void startLoadBySecondWebview() {
        //TODO
    }

    private void unbindBaseProperties(HorizontalWebView webView) {
        webView.removeProperties();
    }

    private void initWebView(HorizontalWebView webView) {
        webView.onResume();
        webView.resumeTimers();
        webView.updateWebViewHelper(activity);
        if (onUrlLoadListener != null) {
            webView.setOnUrlLoadListener(onUrlLoadListener);
        }
    }

    public void updateTextZoom(int zoom) {
        for (HorizontalWebView horizontalWebView : webViewList) {
            horizontalWebView.getSettings().setTextZoom(zoom);
        }
    }

    public HorizontalWebView.OnUrlLoadListener getOnUrlLoadListener() {
        return onUrlLoadListener;
    }

    public void setOnUrlLoadListener(HorizontalWebView.OnUrlLoadListener onUrlLoadListener) {
        this.onUrlLoadListener = onUrlLoadListener;
    }
}
