package com.example.hikerview.ui.js;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.hikerview.R;
import com.example.hikerview.ui.base.BaseStatusActivity;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.js.editor.CodePane;
import com.example.hikerview.ui.js.editor.PreformEdit;
import com.example.hikerview.ui.view.ZoomCodePaneView;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringFindUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.WebUtil;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.example.hikerview.ui.view.colorDialog.PromptDialog.DIALOG_TYPE_WARNING;

/**
 * 作者：By 15968
 * 日期：On 2019/10/9
 * 时间：At 20:22
 */
public class HomeHtmlEditActivity extends BaseStatusActivity {
    private static final String TAG = "HomeHtmlEditActivity";
    private CodePane codePane;
    private EditText domEditView;
    private PreformEdit preformEdit;
    private TextView searchInfo;
    private EditText search_edit;
    private StringFindUtil.SearchFindResult findResult = new StringFindUtil.SearchFindResult();

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_file_edit);
    }

    @Override
    protected void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        codePane = findView(R.id.js_edit_code_pane);
        preformEdit = new PreformEdit(codePane.getCodeText());
        ZoomCodePaneView zoomCodePaneView = findView(R.id.js_edit_code_pane_bg);
        float zoomScale = 0.05f;// 缩放比例
        zoomCodePaneView.init(codePane, zoomScale);
        domEditView = findView(R.id.js_edit_dom);


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
                ((InputMethodManager) Objects.requireNonNull(HomeHtmlEditActivity.this.getSystemService(INPUT_METHOD_SERVICE)))
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
                ((InputMethodManager) Objects.requireNonNull(HomeHtmlEditActivity.this.getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(codePane.getCodeText().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            findAllAsync(content);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        //现在dom已经是fileName（无文件格式后缀）
        String file = getIntent().getStringExtra("file");
        if (!TextUtils.isEmpty(file)) {
            if ("home".equals(file)) {
                domEditView.setText("主页");
                file = WebUtil.getLocalHomePath(getContext());
            } else {
                domEditView.setText(file);
            }
            if (new File(file).exists()) {
                String text = FileUtil.fileToString(file);
                codePane.getCodeText().setText(text);
            }
            domEditView.setFocusable(false);
        } else {
            ToastMgr.shortBottomCenter(getContext(), "文件路径为空");
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.js_edit_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.js_edit_save:
                String dom = domEditView.getText().toString();
                if (TextUtils.isEmpty(dom)) {
                    ToastMgr.shortBottomCenter(getContext(), "文件路径不能为空！");
                    break;
                }
                if (dom.equals("主页")) {
                    dom = WebUtil.getLocalHomePath(getContext());
                }
                String js = codePane.getCodeText().getText().toString();
                String notice = "确定保存吗？保存后原文件内容将会被覆盖无法恢复";
                String finalDom = dom;
                new PromptDialog(getContext())
                        .setDialogType(DIALOG_TYPE_WARNING)
                        .setTitleText("温馨提示")
                        .setContentText(notice)
                        .setPositiveListener("确定", dialog -> {
                            dialog.dismiss();
                            try {
                                String path = getIntent().getStringExtra("file");
                                if ("home".equals(path)) {
                                    WebUtil.saveLocalHomeContent(getContext(), js);
                                    Log.d(TAG, "onOptionsItemSelected: ======" + finalDom);
                                    PreferenceMgr.put(getContext(), "home", "html");
                                    ToastMgr.shortBottomCenter(getContext(), "首页网页文件已保存");
                                    WebUtil.goLocalHome(getContext());
                                } else {
                                    FileUtil.stringToFile(js, finalDom);
                                    ToastMgr.shortBottomCenter(getContext(), "文件已保存");
                                }
                            } catch (IOException e) {
                                DebugUtil.showErrorMsg(HomeHtmlEditActivity.this, getContext(), "写入文件失败", e.getMessage(), "500", e);
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
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
}
