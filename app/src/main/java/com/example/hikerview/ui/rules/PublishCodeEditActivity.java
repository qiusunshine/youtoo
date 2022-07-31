package com.example.hikerview.ui.rules;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.Selection;
import android.util.Base64;
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
import com.example.hikerview.ui.rules.model.AccountPwd;
import com.example.hikerview.ui.rules.utils.PublishHelper;
import com.example.hikerview.ui.view.ZoomCodePaneView;
import com.example.hikerview.ui.view.popup.InputPopup;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.StringFindUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;

import java.util.Objects;

/**
 * 作者：By 15968
 * 日期：On 2019/10/9
 * 时间：At 20:22
 */
public class PublishCodeEditActivity extends BaseStatusActivity {
    private CodePane codePane;
    private PreformEdit preformEdit;
    private TextView searchInfo;
    private EditText search_edit;
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
        codePane = findView(R.id.js_edit_code_pane);
        preformEdit = new PreformEdit(codePane.getCodeText());
        ZoomCodePaneView zoomCodePaneView = findView(R.id.js_edit_code_pane_bg);
        float zoomScale = 0.05f;// 缩放比例
        zoomCodePaneView.init(codePane, zoomScale);


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
                ((InputMethodManager) Objects.requireNonNull(PublishCodeEditActivity.this.getSystemService(INPUT_METHOD_SERVICE)))
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
                ((InputMethodManager) Objects.requireNonNull(PublishCodeEditActivity.this.getSystemService(INPUT_METHOD_SERVICE)))
                        .hideSoftInputFromWindow(codePane.getCodeText().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            findAllAsync(content);
        });
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
        String data = getIntent().getStringExtra("data");
        if (StringUtil.isEmpty(data)) {
            preformEdit.setDefaultText(PublishHelper.getPublishCode());
        } else {
            preformEdit.setDefaultText(data);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.save:
                PublishHelper.savePublishCode(getCode());
                ToastMgr.shortCenter(getContext(), "提交云仓库规则已保存");
                finish();
                break;
            case R.id.share:
                if (StringUtil.isEmpty(getCode())) {
                    ToastMgr.shortCenter(getContext(), "规则不能为空");
                    break;
                }
                String base64 = new String(Base64.encode(getCode().getBytes(), Base64.NO_WRAP));
                AutoImportHelper.shareWithCommand(getContext(), StringUtil.replaceBlank(base64), AutoImportHelper.PUBLISH_CODE);
                break;
            case R.id.account:
                AccountPwd accountPwd = PublishHelper.getPublishAccount();
                InputPopup inputPopup = new InputPopup(getContext())
                        .bind("账号密码管理", "MY_ACCOUNT", accountPwd.getAccount(), "MY_PASSWORD", accountPwd.getPassword(), (title, code) -> {
                            AccountPwd account = new AccountPwd(title, code);
                            PublishHelper.savePublishAccount(account);
                            ToastMgr.shortCenter(getContext(), "账号密码已保存");
                        });
                new XPopup.Builder(getContext())
                        .asCustom(inputPopup)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.publish_code_edit_options, menu);
        return true;
    }

    private String getCode() {
        return codePane.getCodeText().getText() == null ? "" : codePane.getCodeText().getText().toString();
    }

}
