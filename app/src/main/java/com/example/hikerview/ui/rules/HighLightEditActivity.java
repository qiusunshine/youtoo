package com.example.hikerview.ui.rules;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.base.BaseStatusActivity;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.home.model.RuleTemplate;
import com.example.hikerview.ui.js.editor.CodePane;
import com.example.hikerview.ui.js.editor.PreformEdit;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.ui.view.ZoomCodePaneView;
import com.example.hikerview.ui.view.popup.KeyboardToolPop;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.ScreenUtil;
import com.example.hikerview.utils.StringFindUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.UriUtils;
import com.lxj.xpopup.XPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 作者：By 15968
 * 日期：On 2019/10/9
 * 时间：At 20:22
 */
public class HighLightEditActivity extends BaseStatusActivity {
    private CodePane codePane;
    private PreformEdit preformEdit;
    private TextView searchInfo;
    private EditText search_edit;
    private View bg;
    private PopupWindow mSoftKeyboardTool;
    private boolean mIsSoftKeyBoardShowing = false;
    private boolean showTool = true;
    private List<RuleTemplate> ruleTemplates;
    private StringFindUtil.SearchFindResult findResult = new StringFindUtil.SearchFindResult();

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_highlight_edit);
    }

    @Override
    protected void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        bg = findView(R.id.bg);
        codePane = findView(R.id.js_edit_code_pane);
        preformEdit = new PreformEdit(codePane.getCodeText());
        ZoomCodePaneView zoomCodePaneView = findView(R.id.js_edit_code_pane_bg);
        float zoomScale = 0.05f;// 缩放比例
        zoomCodePaneView.init(codePane, zoomScale);

        setTitle("正在编辑：" + getIntent().getStringExtra("title"));


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
                ((InputMethodManager) Objects.requireNonNull(HighLightEditActivity.this.getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(codePane.getCodeText().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
                ((InputMethodManager) Objects.requireNonNull(HighLightEditActivity.this.getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(codePane.getCodeText().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            findAllAsync(content);
        });
        showTool = PreferenceMgr.getBoolean(getContext(), "showTool", true);
        mSoftKeyboardTool = new KeyboardToolPop(this, text -> {
            if ("我的模板".equals(text)) {
                showMyTemplates();
                return;
            } else if ("隐藏".equals(text)) {
                showTool = false;
                PreferenceMgr.put(getContext(), "showTool", false);
                new XPopup.Builder(getContext())
                        .asConfirm("温馨提示", "已隐藏工具栏，如果希望再次开启，在右上角菜单里面开启", () -> {
                        }).show();
                return;
            }
            insertTextToEditText(text);
        });
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new KeyboardOnGlobalChangeListener());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.high_light_options, menu);
        return true;
    }

    private void findAllAsync(String find) {
        StringFindUtil.findAllAsync(findResult, codePane.getCodeText().getText().toString(), find, findResult1 -> {
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
        if (codePane.getCodeText().getEditableText() == null) {
            return;
        }
        try {
            search_edit.clearFocus();
            codePane.getCodeText().requestFocus();
            if (StringUtil.isEmpty(findResult.getFindKey())) {
                codePane.getCodeText().setSelectBackgroundColor(0x33ffffff);
//                codePane.getCodeText().setselect(0, 0);
                Selection.setSelection(codePane.getCodeText().getEditableText(), 0, 0);
                searchInfo.setText("0/0");
                return;
            }
            searchInfo.setText(CollectionUtil.isNotEmpty(findResult.getIndexList()) ? String.format("%d/%d", (findResult.getSelectPos() + 1), findResult.getIndexList().size()) : "0/0");
            if (CollectionUtil.isEmpty(findResult.getIndexList())) {
                codePane.getCodeText().setSelectBackgroundColor(0x33ffffff);
                Selection.setSelection(codePane.getCodeText().getEditableText(), 0, 0);
//                codePane.getCodeText().setSelection(0, 0);
                return;
            }
            codePane.getCodeText().setSelectBackgroundColor(getResources().getColor(R.color.greenAction));
            int start = findResult.getIndexList().get(findResult.getSelectPos());
            if (start < 0) {
                return;
            }
            Selection.setSelection(codePane.getCodeText().getEditableText(), start, start + findResult.getFindKey().length());
//            codePane.getCodeText().setSelection(start, start + findResult.getFindKey().length());
            Layout layout = codePane.getCodeText().getLayout();
            Rect rect = new Rect();
            int line = layout.getLineForOffset(start);
            layout.getLineBounds(line > 0 ? line - 1 : line, rect);
            codePane.smoothScrollTo(rect.left > 50 ? rect.left - 50 : 0, rect.bottom);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        preformEdit.setDefaultText(SettingConfig.highlightRule);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.al_rule_show_tool:
                new XPopup.Builder(getContext())
                        .asConfirm("快捷工具栏设置", "快捷工具栏是在输入法键盘上悬浮的工具栏，当前快捷工具栏处于" + (showTool ? "显示" : "隐藏") + "状态",
                                "隐藏", "显示", () -> {
                                    PreferenceMgr.put(getContext(), "showTool", true);
                                    showTool = true;
                                    ToastMgr.shortCenter(getContext(), "已设置为显示快捷工具栏");
                                }, () -> {
                                    PreferenceMgr.put(getContext(), "showTool", false);
                                    showTool = false;
                                    ToastMgr.shortCenter(getContext(), "已设置为隐藏快捷工具栏");
                                }, false).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        SettingConfig.highlightRule = codePane.getCodeText().getText() == null ? "" : codePane.getCodeText().getText().toString();
        setResult(-1);
        super.finish();
    }


    private void insertTextToEditText(String txt) {
        try {
            if (TextUtils.isEmpty(txt)) return;
            View view = getWindow().getDecorView().findFocus();
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                int start = editText.getSelectionStart();
                int end = editText.getSelectionEnd();
                Editable edit = editText.getEditableText();//获取EditText的文字
                if (start < 0 || start >= edit.length()) {
                    edit.append(txt);
                } else {
                    edit.replace(start, end, txt);//光标所在位置插入文字
                }
            }
        } catch (Exception e) {
            ToastMgr.shortCenter(getContext(), "插入内容失败");
        }
    }

    private void showKeyboardTopPopupWindow(int x, int y) {
//        View view = getWindow().getDecorView().findFocus();
//        if (view instanceof EditText) {
//            view.postDelayed(() -> {
//                int[] location = new int[2];
//                view.getLocationOnScreen(location);
//                int screenHeight = ScreenUtil.getScreenHeight(ArticleListRuleEditActivity.this);
//                Timber.d("showKeyboardTopPopupWindow: screenHeight=" + screenHeight + ", y=" + y + ", bottom_bar=" + bottom_bar.getHeight()
//                        + ", location[1]=" + location[1] + ", view.getHeight()=" + view.getHeight());
//                int scrollY = screenHeight - y - bottom_bar.getHeight() - location[1] - view.getHeight();
//                Timber.d("showKeyboardTopPopupWindow: scrollY=%s", scrollY);
//                int top = ((View) view.getParent().getParent()).getTop();
//                Timber.d("showKeyboardTopPopupWindow: getTop=%s", top);
//                if (scrollY < 0) {
//                    scroll_view.scrollBy(0, scrollY);
//                }
//            }, 300);
//        }
        if (!showTool) {
            return;
        }
        try {
            View view = getWindow().getDecorView().findFocus();
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                if (editText == search_edit) {
                    //搜索的编辑框，不显示
                    return;
                }
            }
        } catch (Exception e) {
        }
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            updateKeyboardTopPopupWindow(x, y); //可能是输入法切换了输入模式，高度会变化（比如切换为语音输入）
            return;
        }
        if (mSoftKeyboardTool != null) {
            mSoftKeyboardTool.showAtLocation(bg, Gravity.BOTTOM, x, y);
        }
    }

    private void updateKeyboardTopPopupWindow(int x, int y) {
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            mSoftKeyboardTool.update(x, y, mSoftKeyboardTool.getWidth(), mSoftKeyboardTool.getHeight());
        }
    }

    private void closePopupWindow() {
        if (mSoftKeyboardTool != null && mSoftKeyboardTool.isShowing()) {
            mSoftKeyboardTool.dismiss();
        }
    }

    private class KeyboardOnGlobalChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            Rect rect = new Rect();
            // 获取当前页面窗口的显示范围
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = ScreenUtil.getScreenHeight(HighLightEditActivity.this);
            int keyboardHeight = screenHeight - rect.bottom; // 输入法的高度
            boolean preShowing = mIsSoftKeyBoardShowing;
            if (Math.abs(keyboardHeight) > screenHeight / 5) {
                mIsSoftKeyBoardShowing = true; // 超过屏幕五分之一则表示弹出了输入法
                showKeyboardTopPopupWindow(ScreenUtil.getScreenWidth(HighLightEditActivity.this) / 2, keyboardHeight);
//                bg.setPadding(0, 0, 0, keyboardHeight + 100);
            } else {
                if (preShowing) {
                    closePopupWindow();
                }
                mIsSoftKeyBoardShowing = false;
//                bg.setPadding(0, 0, 0, 0);
            }
        }
    }


    private String getRuleTemplatePath() {
        String rulesPath = UriUtils.getRootDir(Application.getContext()) + File.separator + "rules";
        File dir = new File(rulesPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return rulesPath + File.separator + "rule_template.json";
    }

    private void showMyTemplates() {
        String rule_template = getRuleTemplatePath();
        File file = new File(rule_template);
        if (!file.exists()) {
            ToastMgr.shortCenter(getContext(), "当前不存在模板，请在编辑规则界面添加模板");
        } else {
            String rules = FileUtil.fileToString(rule_template);
            if (StringUtil.isEmpty(rules)) {
                ToastMgr.shortCenter(getContext(), "当前不存在模板，请在编辑规则界面添加模板");
            } else {
                List<RuleTemplate> articleListRules = JSON.parseArray(rules, RuleTemplate.class);
                if (CollectionUtil.isEmpty(articleListRules)) {
                    ToastMgr.shortCenter(getContext(), "当前不存在模板，请在编辑规则界面添加模板");
                    return;
                }
                ruleTemplates = new ArrayList<>();
                List<String> templates = new ArrayList<>();
                for (int i = 0; i < articleListRules.size(); i++) {
                    String title = articleListRules.get(i).getTitle();
                    if (StringUtil.isEmpty(title)) {
                        title = "模板" + i;
                    }
                    if ("codeBlock".equals(articleListRules.get(i).getTemplateType())) {
                        templates.add(title);
                        ruleTemplates.add(articleListRules.get(i));
                    }
                }
                if (CollectionUtil.isEmpty(articleListRules)) {
                    ToastMgr.shortCenter(getContext(), "当前不存在代码块模板，请在编辑规则界面添加");
                    return;
                }
                CustomCenterRecyclerViewPopup popup = new CustomCenterRecyclerViewPopup(getContext())
                        .withTitle("选择模板")
                        .with(templates, 1, new CustomCenterRecyclerViewPopup.ClickListener() {
                            @Override
                            public void click(String text, int position) {
                                RuleTemplate articleListRule = ruleTemplates.get(position);
                                if ("codeBlock".equals(articleListRule.getTemplateType())) {
                                    insertTextToEditText(articleListRule.getContent());
                                }
                            }

                            @Override
                            public void onLongClick(String text, int position) {

                            }
                        });
                new XPopup.Builder(getContext())
                        .asCustom(popup)
                        .show();
            }
        }
    }

}
