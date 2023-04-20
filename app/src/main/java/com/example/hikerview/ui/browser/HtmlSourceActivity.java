package com.example.hikerview.ui.browser;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.hikerview.R;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.base.BaseStatusActivity;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringFindUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.ViewTool;
import com.hiker.editor.editor.jsc.CodeTextView;
import com.hiker.editor.editor.jsc.CodeTextViewPane;
import com.hiker.editor.editor.jsc.PreformTextView;
import com.hiker.editor.editor.jsc.ZoomCodeTextPaneView;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Objects;

import timber.log.Timber;

import static android.view.View.VISIBLE;

/**
 * 作者：By 15968
 * 日期：On 2019/10/9
 * <p>
 * 时间：At 20:22
 */
public class HtmlSourceActivity extends BaseStatusActivity {
    private static final String TAG = "HomeHtmlEditActivity";
    private CodeTextViewPane codePane;
    private PreformTextView preformEdit;
    private EditText domEditView;
    private TextView searchInfo;
    private EditText search_edit;
    private View js_edit_code_bg;
    private TextView js_edit_code_text;
    private ScrollView js_edit_code_scroll;
    private StringFindUtil.SearchFindResult findResult = new StringFindUtil.SearchFindResult();
    private boolean isLoading = false;
    private String codeText = "";

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_html_source);
    }

    @Override
    protected void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        codePane = findView(R.id.js_edit_code_pane);
        preformEdit = new PreformTextView(codePane.getCodeText());
        ZoomCodeTextPaneView zoomCodePaneView = findView(R.id.js_edit_code_pane_bg);
        float zoomScale = 0.05f;// 缩放比例
        zoomCodePaneView.init(codePane, zoomScale);
        domEditView = findView(R.id.js_edit_dom);
        js_edit_code_bg = findViewById(R.id.js_edit_code_bg);
        js_edit_code_text = findViewById(R.id.js_edit_code_text);
        js_edit_code_scroll = findViewById(R.id.js_edit_code_scroll);

        //搜索
        searchInfo = findView(R.id.search_count);
        search_edit = findView(R.id.search_edit);
        View search_close = findView(R.id.search_close);
        View search_forward = findView(R.id.search_forward);
        View search_back = findView(R.id.search_back);
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
                ((InputMethodManager) Objects.requireNonNull(getContext().getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(getTextView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            search_edit.setText("");
            String content = search_edit.getText().toString();
            findAllAsync(content);
        });
        findView(R.id.search_ok).setOnClickListener(v -> {
            String content = search_edit.getText().toString();
            try {
                ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(getTextView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            findAllAsync(content);
        });
    }

    private void findAllAsync(String find) {
        StringFindUtil.findAllAsync(findResult, getText(), find, findResult1 -> {
            if (isFinishing()) {
                return;
            }
            runOnUiThread(() -> {
                if (isFinishing()) {
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

    @Override
    protected void initData(Bundle savedInstanceState) {
        //现在dom已经是fileName（无文件格式后缀）
        String file = getIntent().getStringExtra("url");
        domEditView.setHint("网址");

        ViewTool.setOnEnterClickListener(domEditView, s -> {
            if (!isLoading) {
                loadUrl(s, false);
            }
        });
        loadUrl(file);
    }

    private void loadUrl(String url) {
        loadUrl(url, true);
    }

    private void loadUrl(String url, boolean updateDomText) {
        if (isLoading) {
            ToastMgr.shortBottomCenter(getContext(), "正在加载中，请稍候重试");
            return;
        }
        if (updateDomText) {
            domEditView.setText(url);
        }
        isLoading = true;
        try {
            ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE)))
                    .hideSoftInputFromWindow(getTextView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String code = getIntent().getStringExtra("code");
        if (StringUtil.isNotEmpty(code)) {
            setText(code);
            isLoading = false;
            return;
        }
//        codePane.getCodeText().setKeyListener(null);
//        codePane.getCodeText().setTextIsSelectable(true);
        setText("加载中");
        HttpParser.parseSearchUrlForHtml(url, new HttpParser.OnSearchCallBack() {
            @Override
            public void onSuccess(String url, String s) {
                if (isFinishing()) {
                    return;
                }
                try {
                    if (StringUtil.isEmpty(s)) {
                        setText(s);
                    } else {
                        String k = StringUtil.trimBlanks(s);
                        if (k.contains("<body") || k.contains("<html")) {
                            Document doc = Jsoup.parse(s);
                            String s1 = doc.toString();
                            setText(s1);
                        } else if (isJsCode(k) || ((k.startsWith("[") && k.endsWith("]")) || (k.startsWith("{") && k.endsWith("}")))) {
                            HeavyTaskUtil.executeNewTask(() -> {
                                String result = JSEngine.getInstance().evalJS("var window = {}; eval(fetch('hiker://assets/beautify.js')); window.js_beautify(input)", k);
                                if (!isFinishing()) {
                                    runOnUiThread(() -> {
                                        String html = "undefined".equals(result) ? s : result;
                                        setText(html);
                                    });
                                }
                            });
                        } else {
                            setText(s);
                        }
                    }
                } catch (Exception e) {
                    setText(s);
                } finally {
                    isLoading = false;
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                isLoading = false;
                DebugUtil.showErrorMsg(HtmlSourceActivity.this, "获取网页源码失败", new Exception(msg));
            }
        });
    }

    private boolean isJsCode(String code) {
        if (code == null || code.length() <= 0) {
            return false;
        }
        String[] tags = new String[]{"if ", "function ", "var ", "let ", "for ", ".push", "const ", "fetch(", "setResult(", "return "};
        int count = 0;
        for (String tag : tags) {
            if (code.contains(tag)) {
                count++;
            }
            if (count >= 1) {
                //匹配到两个则认为是JS
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.source_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String file = domEditView.getText().toString();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.utf8:
                if (!StringUtil.isEmpty(file)) {
                    loadUrl(file);
                }
                break;
            case R.id.gbk:
                if (!StringUtil.isEmpty(file)) {
                    loadUrl(StringUtils.replace(file, ";UTF-8", ";GBK", 1));
                }
                break;
            case R.id.gb2312:
                if (!StringUtil.isEmpty(file)) {
                    loadUrl(StringUtils.replace(file, ";UTF-8", ";GB2312", 1));
                }
                break;
            case R.id.wrap_mode:
                if (js_edit_code_bg.getVisibility() == VISIBLE) {
                    js_edit_code_bg.setVisibility(View.GONE);
                    codePane.getCodeText().setText(js_edit_code_text.getText().toString());
                    codePane.setVisibility(VISIBLE);
                } else {
                    js_edit_code_bg.setVisibility(VISIBLE);
                    codePane.setVisibility(View.GONE);
                    js_edit_code_text.setText(codePane.getCodeText().getText().toString());
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
