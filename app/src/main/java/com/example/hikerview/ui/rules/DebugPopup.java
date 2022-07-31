package com.example.hikerview.ui.rules;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.IBinder;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.example.hikerview.R;
import com.example.hikerview.service.parser.CommonParser;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.webview.ArticleWebViewHolder;
import com.example.hikerview.ui.js.editor.CodeTextView;
import com.example.hikerview.ui.js.editor.CodeTextViewPane;
import com.example.hikerview.ui.js.editor.PreformTextView;
import com.example.hikerview.ui.rules.model.DebuggingRule;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.ui.view.ZoomCodeTextPaneView;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringFindUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.DrawerPopupView;
import com.tencent.smtt.sdk.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.example.hikerview.utils.PreferenceMgr.SETTING_CONFIG;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class DebugPopup extends DrawerPopupView implements View.OnClickListener {
    private static final String TAG = "DebugPopup";

    private ZoomCodeTextPaneView zoomCodePaneView;
    private CodeTextViewPane codePane;
    private PreformTextView preformEdit;
    private EditText editText, parseEdit, nodeParseEdit;
    private TextView js_edit_code_text;
    private TextView searchInfo;
    private EditText search_edit;
    private StringFindUtil.SearchFindResult findResult = new StringFindUtil.SearchFindResult();
    private boolean isLoading = false;
    private Activity activity;
    private ImageView wrapBtn, web_debug_icon;
    private View js_edit_code_bg;
    private ScrollView js_edit_code_scroll;
    private String codeText = "";
    private boolean webDebugMode;
    private LinearLayout webDebugContainer;
    private RelativeLayout androidDebug;

    public ArticleWebViewHolder getWebViewHolder() {
        return webViewHolder;
    }

    private ArticleWebViewHolder webViewHolder;

    public DebugPopup(@NonNull Context context) {
        super(context);
    }

    public DebugPopup(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
    }

    public void saveDebuggingRule() {
        DebuggingRule debuggingRule = new DebuggingRule();
        debuggingRule.setUrl(editText.getText().toString());
        debuggingRule.setListRule(parseEdit.getText().toString());
        debuggingRule.setNodeRule(nodeParseEdit.getText().toString());
        debuggingRule.setCode(getText());
        debuggingRule.setScrollX(codePane.getScrollX());
        debuggingRule.setScrollY(codePane.getScrollY());
        debuggingRule.setTextSize(zoomCodePaneView.getTextNowSize());
        debuggingRule.setScale(zoomCodePaneView.getScale());
        debuggingRule.setUseEditText(js_edit_code_bg.getVisibility() == VISIBLE);
        SettingConfig.debuggingRule = JSON.toJSONString(debuggingRule);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_debug_popup;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        zoomCodePaneView = findViewById(R.id.js_edit_code_pane_bg);
        editText = findViewById(R.id.get_html_edit);
        findViewById(R.id.code).setOnClickListener(v -> {
            dismiss();
        });
        parseEdit = findViewById(R.id.parse_edit);
        nodeParseEdit = findViewById(R.id.parse_node_edit);
        codePane = findViewById(R.id.js_edit_code_pane);
        preformEdit = new PreformTextView(codePane.getCodeText());
        js_edit_code_text = findViewById(R.id.js_edit_code_text);
        wrapBtn = findViewById(R.id.wrap_text);
        js_edit_code_bg = findViewById(R.id.js_edit_code_bg);
        js_edit_code_scroll = findViewById(R.id.js_edit_code_scroll);
        web_debug_icon = findViewById(R.id.web_debug_icon);
        webDebugContainer = findViewById(R.id.webDebugContainer);
        webViewHolder = new ArticleWebViewHolder();
        androidDebug = findViewById(R.id.androidDebug);

        findViewById(R.id.notice_icon).setOnClickListener(v -> {
            new XPopup.Builder(getContext())
                    .asConfirm("温馨提示", "列表规则基于源码，只有点击获取源码才会修改列表规则解析的内容。" +
                            "节点规则基于显示的源码，一旦显示的内容修改，其解析的内容就改变，并且因为显示的源码含有列表节点本身，因此规则需要加上列表节点本身（比如列表规则：body&&li，那么节点规则：li&&a&&href）。" +
                            "查找也是基于显示的源码。", () -> {

                    }).show();
        });
        initWebDebug();
        float zoomScale = 0.05f;// 缩放比例
        zoomCodePaneView.init(codePane, zoomScale);
        findViewById(R.id.get_html_btn).setOnClickListener(this);
        findViewById(R.id.parse).setOnClickListener(this);
        findViewById(R.id.parse_node).setOnClickListener(this);
        findViewById(R.id.debug_bg).setOnClickListener(v -> {
        });
        findViewById(R.id.debug_bg).setOnLongClickListener(v -> true);

        //搜索
        searchInfo = findViewById(R.id.search_count);
        search_edit = findViewById(R.id.search_edit);
        View search_close = findViewById(R.id.search_close);
        View search_forward = findViewById(R.id.search_forward);
        View search_back = findViewById(R.id.search_back);
        search_forward.setOnClickListener(v -> {
            if (CollectionUtil.isNotEmpty(findResult.getIndexList()) && findResult.getSelectPos() < findResult.getIndexList().size() - 1) {
                findResult.setSelectPos(findResult.getSelectPos() + 1);
                updateByFindResult(findResult);
            }
        });
        search_back.setOnClickListener(v -> {
            if (CollectionUtil.isNotEmpty(findResult.getIndexList()) && findResult.getSelectPos() > 0) {
                findResult.setSelectPos(findResult.getSelectPos() - 1);
                updateByFindResult(findResult);
            }
        });
        search_close.setOnClickListener(v -> {
            try {
                ((InputMethodManager) Objects.requireNonNull(activity.getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(getCodeWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            search_edit.setText("");
            String content = search_edit.getText().toString();
            findAllAsync(content);
        });
        findViewById(R.id.search_ok).setOnClickListener(v -> {
            String content = search_edit.getText().toString();
            try {
                ((InputMethodManager) Objects.requireNonNull(activity.getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(getCodeWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            findAllAsync(content);
        });


        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (!isLoading) {
                    loadUrl(editText.getText().toString());
                }
                return true;
            }
            return false;
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (!isLoading) {
                    loadUrl(editText.getText().toString());
                }
            }
            return false;
        });

        wrapBtn.setOnClickListener(v -> {
            if (js_edit_code_bg.getVisibility() == VISIBLE) {
                js_edit_code_bg.setVisibility(GONE);
                codePane.getCodeText().setText(js_edit_code_text.getText().toString());
                codePane.setVisibility(VISIBLE);
                Glide.with(getContext())
                        .load(R.drawable.ic_action_wrap_text)
                        .into(wrapBtn);
            } else {
                js_edit_code_bg.setVisibility(VISIBLE);
                codePane.setVisibility(GONE);
                js_edit_code_text.setText(codePane.getCodeText().getText().toString());
                Glide.with(getContext())
                        .load(R.drawable.ic_action_wrap_text_selected)
                        .into(wrapBtn);
            }
        });

        bindDebuggingRule();
    }

    private void initWebDebug() {
        web_debug_icon.setOnClickListener(v -> {
            String[] ops = webDebugMode ? new String[]{"切换回原生开发助手", "设置云助手地址"} : new String[]{"切换到云开发助手", "设置云助手地址"};
            CustomCenterRecyclerViewPopup popup = new CustomCenterRecyclerViewPopup(getContext())
                    .withTitle("云开发助手")
                    .with(ops, 1, new CustomCenterRecyclerViewPopup.ClickListener() {
                        @Override
                        public void click(String url, int position) {
                            switch (url) {
                                case "切换回原生开发助手":
                                    webDebugMode = false;
                                    PreferenceMgr.put(getContext(), SETTING_CONFIG, "webDebugMode", false);
                                    try {
                                        if (webViewHolder.getWebView() != null) {
                                            webViewHolder.getWebView().onPause();
                                            webViewHolder.getWebView().destroy();
                                            webViewHolder.setWebView(null);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    webDebugContainer.removeAllViews();
                                    webDebugContainer.setVisibility(GONE);
                                    androidDebug.setVisibility(VISIBLE);
                                    break;
                                case "设置云助手地址":
                                    setWebDebugUrl();
                                    break;
                                case "切换到云开发助手":
                                    toWebDebugMode();
                                    break;
                            }
                        }

                        @Override
                        public void onLongClick(String url, int position) {

                        }
                    });
            new XPopup.Builder(getContext())
                    .asCustom(popup).show();
        });

        webDebugMode = PreferenceMgr.getBoolean(getContext(), SETTING_CONFIG, "webDebugMode", false);
        if (webDebugMode) {
            toWebDebugMode();
        }
    }

    private void toWebDebugMode() {
        String webDebugUrl1 = PreferenceMgr.getString(getContext(), SETTING_CONFIG, "webDebugUrl", null);
        if (StringUtil.isEmpty(webDebugUrl1)) {
            ToastMgr.shortCenter(getContext(), "请先设置云助手地址哦！");
            setWebDebugUrl();
            return;
        }
        PreferenceMgr.put(getContext(), SETTING_CONFIG, "webDebugMode", true);
        webDebugMode = true;
        try {
            if (webViewHolder.getWebView() != null) {
                webViewHolder.getWebView().onPause();
                webViewHolder.getWebView().destroy();
                webViewHolder.setWebView(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        webDebugContainer.removeAllViews();
        webDebugContainer.setVisibility(VISIBLE);
        androidDebug.setVisibility(GONE);
        WebView webView = new WebView(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(layoutParams);
        webViewHolder.setWebView(webView);
        webViewHolder.initWebView(activity);
        webDebugContainer.addView(webView);
        webView.loadUrl(webDebugUrl1);
    }

    private void setWebDebugUrl() {
        String webDebugUrl = PreferenceMgr.getString(getContext(), SETTING_CONFIG, "webDebugUrl", null);
        new XPopup.Builder(getContext())
                .asInputConfirm("设置云助手地址", null,
                        webDebugUrl, "", text -> {
                            PreferenceMgr.put(getContext(), SETTING_CONFIG, "webDebugUrl", text);
                            ToastMgr.shortCenter(getContext(), "已设置");
                            if (webDebugMode && webViewHolder.getWebView() != null) {
                                webViewHolder.getWebView().loadUrl(text);
                            }
                        }).show();
    }

    private IBinder getCodeWindowToken() {
        return getTextView().getWindowToken();
    }

    private void bindDebuggingRule() {

        if (StringUtil.isNotEmpty(SettingConfig.debuggingRule)) {
            DebuggingRule debuggingRule = JSON.parseObject(SettingConfig.debuggingRule, DebuggingRule.class);
            if (debuggingRule != null) {
                if (StringUtil.isNotEmpty(debuggingRule.getUrl())) {
                    editText.post(() -> editText.setText(debuggingRule.getUrl()));
                }
                if (StringUtil.isNotEmpty(debuggingRule.getListRule())) {
                    parseEdit.post(() -> parseEdit.setText(debuggingRule.getListRule()));
                }
                if (StringUtil.isNotEmpty(debuggingRule.getNodeRule())) {
                    nodeParseEdit.post(() -> nodeParseEdit.setText(debuggingRule.getNodeRule()));
                }
                if (StringUtil.isNotEmpty(debuggingRule.getCode())) {
                    getTextView().post(() -> setText(debuggingRule.getCode()));
                }
                if (debuggingRule.getTextSize() > 0) {
                    getTextView().post(() -> getTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX, debuggingRule.getTextSize()));
                    zoomCodePaneView.setScale(debuggingRule.getScale());
                    zoomCodePaneView.setTextNowSize(debuggingRule.getTextSize());
                }
                if (debuggingRule.getScrollX() > 0 || debuggingRule.getScrollY() > 0) {
                    codePane.postDelayed(() -> codePane.scrollTo(debuggingRule.getScrollX(), debuggingRule.getScrollY()), 400);
                }
                if (debuggingRule.isUseEditText()) {
                    js_edit_code_bg.setVisibility(VISIBLE);
                    codePane.setVisibility(GONE);
                    Glide.with(getContext())
                            .load(R.drawable.ic_action_wrap_text_selected)
                            .into(wrapBtn);
                }
            }
        }
    }


    private void loadUrl(String url) {
        if (isLoading) {
            ToastMgr.shortBottomCenter(getContext(), "正在加载中，请稍候重试");
            return;
        }
        url = url.replace("**", "我").replace("fypage", "1");
        editText.setText(url);
        isLoading = true;
        try {
            ((InputMethodManager) Objects.requireNonNull(activity.getSystemService(INPUT_METHOD_SERVICE)))
                    .hideSoftInputFromWindow(getCodeWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        getTextView().setKeyListener(null);
//        getTextView().setTextIsSelectable(true);
        setText("加载中");
        HttpParser.parseSearchUrlForHtml(url, new HttpParser.OnSearchCallBack() {
            @Override
            public void onSuccess(String url, String s) {
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                try {
                    Document doc = Jsoup.parse(s);
                    if (doc != null) {
                        setText(doc.toString());
                    } else {
                        setText(s);
                    }
                } catch (Exception e) {
                    setText(s);
                } finally {
                    isLoading = false;
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                isLoading = false;
                DebugUtil.showErrorMsg(activity, "获取网页源码失败", new Exception(msg));
            }
        });
    }

//
//    @Override
//    protected int getPopupHeight() {
//        return (int) (XPopupUtils.getScreenHeight(getContext()) * .9f);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_html_btn:
                String text = editText.getText().toString();
                if (TextUtils.equals(text, "")) {
                    ToastMgr.shortCenter(getContext(), "还没输入网址呢！");
                } else {
                    loadUrl(text);
                }
                break;
            case R.id.parse:
                String t = parseEdit.getText().toString();
                if (TextUtils.equals(t, ""))
                    ToastMgr.shortCenter(getContext(), "还没输入文本呢！");
                else {
                    try {
                        parseHtml(t);
                    } catch (Exception e) {
                        ToastMgr.shortCenter(getContext(), e.getMessage());
                    }
                }
                break;
            case R.id.parse_node:
                String nodeRule = nodeParseEdit.getText().toString();
                if (TextUtils.equals(nodeRule, ""))
                    ToastMgr.shortCenter(getContext(), "还没输入文本呢！");
                else {
                    try {
                        parseNodeHtml(nodeRule);
                    } catch (Exception e) {
                        ToastMgr.shortCenter(getContext(), e.getMessage());
                    }
                }
                break;
        }
    }

    private void findAllAsync(String find) {
        StringFindUtil.findAllAsync(findResult, getText(), find, findResult1 -> {
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.runOnUiThread(() -> {
                if (activity.isFinishing()) {
                    return;
                }
                updateByFindResult(findResult1);
            });
        });
    }

    private void updateByFindResult(StringFindUtil.SearchFindResult findResult) {
        Timber.d("updateByFindResult: %s", findResult.getIndexList().size());
        if (getTextView() instanceof CodeTextView && codePane.getCodeText().getEditableText() == null) {
            return;
        }
        try {
            search_edit.clearFocus();
            getTextView().requestFocus();
            Timber.d("updateByFindResult2: %s", findResult.getFindKey());
            if (StringUtil.isEmpty(findResult.getFindKey())) {
                setSelectBackgroundColor(0x33ffffff);
//                getTextView().setselect(0, 0);
                Selection.setSelection(getSpan(0, 0), 0, 0);
                searchInfo.setText("0/0");
                return;
            }
            searchInfo.setText(CollectionUtil.isNotEmpty(findResult.getIndexList()) ? String.format("%d/%d", (findResult.getSelectPos() + 1), findResult.getIndexList().size()) : "0/0");
            if (CollectionUtil.isEmpty(findResult.getIndexList())) {
                setSelectBackgroundColor(0x33ffffff);
                Selection.setSelection(getSpan(0, 0), 0, 0);
//                getTextView().setSelection(0, 0);
                return;
            }
            setSelectBackgroundColor(getResources().getColor(R.color.greenAction));
            int start = findResult.getIndexList().get(findResult.getSelectPos());
            if (start < 0) {
                return;
            }
            Selection.setSelection(getSpan(start, findResult.getFindKey().length()), start, start + findResult.getFindKey().length());
//            getTextView().setSelection(start, start + findResult.getFindKey().length());
            Layout layout = getTextView().getLayout();
            Rect rect = new Rect();
            int line = layout.getLineForOffset(start);
            layout.getLineBounds(line > 0 ? line - 1 : line, rect);
            if (js_edit_code_bg.getVisibility() == VISIBLE) {
                js_edit_code_text.postDelayed(() -> {
                    js_edit_code_scroll.smoothScrollTo(rect.left, rect.bottom);
                }, 200);
            } else {
                codePane.smoothScrollTo(rect.left > 50 ? rect.left - 50 : 0, rect.bottom);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private Spannable getSpan(int start, int length) {
        if (js_edit_code_bg.getVisibility() == VISIBLE) {
            try {
                Spannable spannable = new SpannableString(codeText);
                SpannableStringBuilder builder = new SpannableStringBuilder(codeText);
                builder.setSpan(new BackgroundColorSpan(Color.RED), start, start + length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                js_edit_code_text.setText(builder);
                return spannable;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SpannableString("");
        } else {
            return codePane.getCodeText().getEditableText();
        }
    }

    private TextView getTextView() {
        if (js_edit_code_bg.getVisibility() == VISIBLE) {
            return js_edit_code_text;
        } else {
            return codePane.getCodeText();
        }
    }

    private void setSelectBackgroundColor(int color) {
        if (js_edit_code_bg.getVisibility() == VISIBLE) {

        } else {
            codePane.getCodeText().setSelectBackgroundColor(color);
        }
    }

    private void parseHtml(String nodeRule) {
        try {
            ((InputMethodManager) Objects.requireNonNull(activity.getSystemService(INPUT_METHOD_SERVICE)))
                    .hideSoftInputFromWindow(getCodeWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String text = getText();
        List<String> result = CommonParser.parseDomForList(text, nodeRule);
        setText(StringUtil.listToString(result, "\n\n"));
    }

    private void parseNodeHtml(String nodeRule) {
        try {
            ((InputMethodManager) Objects.requireNonNull(activity.getSystemService(INPUT_METHOD_SERVICE)))
                    .hideSoftInputFromWindow(getCodeWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String text = getText();
        String result = CommonParser.parseDomForUrl(text, nodeRule, "");
        setText(result);
    }

    private String getText() {
        return getTextView().getText().toString();
    }

    private void setText(String text) {
        codeText = text;
        if (getTextView() instanceof CodeTextView) {
            getTextView().setText(text);
        } else {
            getTextView().setText(new SpannableString(text));
        }
    }

}
