package com.example.hikerview.ui.js;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.ui.base.BaseStatusActivity;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.js.editor.CodePane;
import com.example.hikerview.ui.js.editor.PreformEdit;
import com.example.hikerview.ui.js.model.ViaJsPlugin;
import com.example.hikerview.ui.view.ZoomCodePaneView;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.utils.StringFindUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.example.hikerview.ui.view.colorDialog.PromptDialog.DIALOG_TYPE_WARNING;

/**
 * 作者：By 15968
 * 日期：On 2019/10/9
 * 时间：At 20:22
 */
public class JSEditActivity extends BaseStatusActivity {
    private static final String TAG = "JSEditActivity";
    private CodePane codePane;
    private EditText domEditView;
    private PreformEdit preformEdit;
    private String defaultJs = "";
    private TextView searchInfo;
    private EditText search_edit, js_edit_name;
    private StringFindUtil.SearchFindResult findResult = new StringFindUtil.SearchFindResult();

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_js_edit);
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
        js_edit_name = findView(R.id.js_edit_name);

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
                ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE)))
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
                ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE)))
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
        //现在dom已经是fileName（无文件格式后缀）
        String dom = getIntent().getStringExtra("dom");
        String jsPrefix = "";
        String viaJsId = "";
        if (getIntent().getBooleanExtra("via", false)) {
            String viaJs = getIntent().getStringExtra("viaJs");
            if (!TextUtils.isEmpty(viaJs)) {
                Log.d(TAG, "initData: viaJs ： " + viaJs);
                ViaJsPlugin viaJsPlugin = JSON.parseObject(viaJs, ViaJsPlugin.class);
                if (!TextUtils.isEmpty(viaJsPlugin.getCode())) {
                    String code = new String(Base64.decode(viaJsPlugin.getCode(), Base64.NO_WRAP));
                    viaJsId = "//==========via-plugin:" + viaJsPlugin.getId() + "==========\n";
                    jsPrefix = viaJsId +
                            "//==========" + viaJsPlugin.getName() + "==========\n" +
                            code +
                            "\n" + viaJsId;
                    dom = viaJsPlugin.getUrl();
                    Log.d(TAG, "initData: viaJsPlugin.getUrl:" + dom);
                    if ("*".equals(dom)) {
                        dom = "global";
                    }
                    String[] content = jsPrefix.split("@name: ");
                    if (content.length > 1) {
                        String title = content[1].split("\n")[0];
                        if (StringUtil.isNotEmpty(title)) {
                            dom = dom + "_" + title;
                        }
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(dom)) {
            dom = StringUtil.getDom(dom);
            String[] names = dom.split("_");
            String title = names.length <= 1 ? names[0] : StringUtil.arrayToString(names, 1, "_");
            domEditView.setText(names[0]);
            if (names.length > 1) {
                js_edit_name.setText(title);
            }
            if (JSManager.instance(getContext()).hasJs(dom)) {
                String nowJs = JSManager.instance(getContext()).getJsByFileName(dom);
                if (nowJs == null) {
                    nowJs = "";
                }
                if (TextUtils.isEmpty(viaJsId)) {
                    preformEdit.setDefaultText(nowJs);
                } else {
                    if (!nowJs.contains(viaJsId)) {
                        ToastMgr.shortBottomCenter(getContext(), "检测到via插件！");
                        preformEdit.setDefaultText(jsPrefix + nowJs);
                    } else {
//                        Log.d(TAG, "initData: + nowJs : " + nowJs + ",viaJsId》" + viaJsId);
                        String[] strings = StringUtils.splitByWholeSeparatorPreserveAllTokens(nowJs, viaJsId);
                        Log.d(TAG, "initData: strings ; " + strings.length);
                        if (strings.length == 3) {
                            ToastMgr.shortBottomCenter(getContext(), "检测到via插件更新！");
                            preformEdit.setDefaultText(strings[0] + jsPrefix + strings[2]);
                        } else {
                            ToastMgr.shortBottomCenter(getContext(), "via插件识别失败！");
                            preformEdit.setDefaultText(nowJs);
                        }
                    }
                }
            } else {
                preformEdit.setDefaultText("".equals(jsPrefix) ? defaultJs : jsPrefix);
            }
        } else {
            preformEdit.setDefaultText("".equals(jsPrefix) ? defaultJs : jsPrefix);
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
                String dom = StringUtil.getDom(domEditView.getText().toString());
                if (StringUtil.isEmpty(dom)) {
                    dom = "global";
                    domEditView.setText(dom);
                } else if (dom.contains("*")) {
                    dom = dom.replace("*", "global");
                    domEditView.setText(dom);
                }
                String desc = js_edit_name.getText().toString();
                if (StringUtil.isNotEmpty(desc)) {
                    dom = dom + "_" + desc;
                }
                String js = codePane.getCodeText().getText().toString();
                if (TextUtils.isEmpty(js)) {
                    deleteJs(dom);
                } else {
                    String notice = JSManager.instance(getContext()).hasJs(dom) ? "确定更新该域名（该JS文件）下的插件？更新后将无法找回旧版！" : "确定保存该插件？保存后将立即生效，不要保存无用插件！";
                    String finalDom = dom;
                    new PromptDialog(getContext())
                            .setDialogType(DIALOG_TYPE_WARNING)
                            .setTitleText("温馨提示")
                            .setContentText(notice)
                            .setPositiveListener("确定", dialog -> {
                                dialog.dismiss();
                                boolean ok = JSManager.instance(getContext()).updateJs(finalDom, js);
                                if (ok) {
                                    ToastMgr.shortBottomCenter(getContext(), "保存成功！");
                                } else {
                                    ToastMgr.shortBottomCenter(getContext(), "保存失败！");
                                }
                            }).show();
                }
                break;
//            case R.id.js_edit_delete:
//                String dom2 = StringUtil.getDom(domEditView.getTextNoDelay().toString());
//                if (TextUtils.isEmpty(dom2)) {
//                    ToastMgr.shortBottomCenter(getContext(), "网站域名不能为空！");
//                    break;
//                }
//                deleteJs(dom2);
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteJs(String dom) {
        new PromptDialog(getContext())
                .setDialogType(DIALOG_TYPE_WARNING)
                .setTitleText("温馨提示")
                .setContentText("您是否想要删除" + dom + "下的JS插件？")
                .setPositiveListener("确定删除", dialog -> {
                    dialog.dismiss();
                    boolean ok = JSManager.instance(getContext()).deleteJs(dom);
                    if (ok) {
                        ToastMgr.shortBottomCenter(getContext(), "删除成功！");
                        preformEdit.setDefaultText(defaultJs);
                    } else {
                        ToastMgr.shortBottomCenter(getContext(), "删除失败！");
                    }
                }).show();
    }
}
