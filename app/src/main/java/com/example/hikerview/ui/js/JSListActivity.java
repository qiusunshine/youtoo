package com.example.hikerview.ui.js;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.android.material.snackbar.Snackbar;
import com.example.hikerview.R;
import com.example.hikerview.constants.RemotePlayConfig;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.ui.base.BaseSlideActivity;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.js.model.JsRule;
import com.example.hikerview.ui.rules.service.RuleImporterManager;
import com.example.hikerview.ui.view.CustomRecyclerViewPopup;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.ui.webdlan.RemoteServerManager;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.WebUtil;
import com.lxj.xpopup.XPopup;
import com.yanzhenjie.andserver.Server;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.hikerview.ui.view.colorDialog.PromptDialog.DIALOG_TYPE_WARNING;

/**
 * ?????????By 15968
 * ?????????On 2019/10/5
 * ?????????At 10:09
 */
public class JSListActivity extends BaseSlideActivity {
    private RecyclerView recyclerView;
    private JSListAdapter adapter;
    private List<JsRule> rules = new ArrayList<>();
    private EditText search_edit;
    private ImageView search_clear;
    private List<JsRule> allRules = new ArrayList<>();
    private boolean isSorting;
    private Button js_list_add;
    protected Map<String, Integer> orderMap = new HashMap<>();

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activit_js_list;
    }

    @Override
    protected View getBackgroundView() {
        return findView(R.id.js_list_window);
    }

    @Override
    protected void initView2() {
        recyclerView = findView(R.id.js_list_recycler_view);
        adapter = new JSListAdapter(getContext(), rules);
        adapter.setOnItemClickListener(onItemClickListener);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //???????????????
        int marginTop = MyStatusBarUtil.getStatusBarHeight(getContext()) + DisplayUtil.dpToPx(getContext(), 86);
        View bg = findView(R.id.js_list_bg);
        findView(R.id.js_list_window).setOnClickListener(view -> finish());
        js_list_add = findView(R.id.js_list_add);
        js_list_add.setOnClickListener(v -> {
            if ("??????".equals(js_list_add.getText().toString())) {
                startActivity(new Intent(getContext(), JSEditActivity.class));
            } else {
                if (rules.size() != allRules.size()) {
                    ToastMgr.shortCenter(getContext(), "???????????????????????????????????????????????????");
                    return;
                }
                js_list_add.setText("??????");
                for (int i = 0; i < rules.size(); i++) {
                    orderMap.put(rules.get(i).getName(), i + 1);
                }
                BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.JS_LIST_ORDER_KEY).findFirst(BigTextDO.class);
                if (bigTextDO == null) {
                    bigTextDO = new BigTextDO();
                    bigTextDO.setKey(BigTextDO.JS_LIST_ORDER_KEY);
                }
                bigTextDO.setValue(JSON.toJSONString(orderMap));
                bigTextDO.save();
                ToastMgr.shortCenter(getContext(), "?????????????????????");
                isSorting = false;
            }
        });
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bg.getLayoutParams();
        layoutParams.topMargin = marginTop;
        bg.setLayoutParams(layoutParams);
        findView(R.id.js_list_store).setOnClickListener(this::showStores);
    }

    private void showStores(View view) {
        int jsPluginMsg = PreferenceMgr.getInt(getContext(), "version", "jsPluginMsg", 0);
        int now = 1;
        showStoresNow(view);
        if (jsPluginMsg < now) {
            PreferenceMgr.put(getContext(), "version", "jsPluginMsg", now);
            new XPopup.Builder(getContext())
                    .asConfirm("????????????", "???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", () -> {

                    }).show();
        }
    }

    private void showStoresNow(View view) {
        new XPopup.Builder(getContext())
                .atView(view)
                .asAttachList(new String[]{"Via?????????", "Greasy Fork"}, null,
                        (position, text) -> {
                            switch (text) {
                                case "Via?????????":
                                    WebUtil.goWeb(getContext(), "https://tool.whgpc.com/Tools/viaPLUG/#/tabBar/plugin");
                                    break;
                                case "Greasy Fork":
                                    WebUtil.goWeb(getContext(), "https://greasyfork.org/zh-CN");
                                    break;
                            }
                            finish();
                        })
                .show();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        search_edit = findView(R.id.search_edit);
        search_clear = findView(R.id.search_clear);
        search_clear.setOnClickListener(v -> search_edit.setText(""));
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    return;
                }
                String key = s.toString();
                if (StringUtil.isEmpty(key)) {
                    search_clear.setVisibility(View.INVISIBLE);
                } else {
                    search_clear.setVisibility(View.VISIBLE);
                }
                rules.clear();
                rules.addAll(Stream.of(allRules)
                        .filter(bookmark -> StringUtil.isEmpty(key)
                                || (StringUtil.isNotEmpty(bookmark.getName()) && bookmark.getName().contains(key)))
                        .collect(Collectors.toList()));
                adapter.notifyDataSetChanged();
            }
        });
        touchHelper.attachToRecyclerView(recyclerView);
    }

    private JSListAdapter.OnItemClickListener onItemClickListener = new JSListAdapter.OnItemClickListener() {
        @Override
        public void onClick(View view, int position) {
            Intent jsIntent = new Intent(getContext(), JSEditActivity.class);
            jsIntent.putExtra("dom", rules.get(position).getName());
            startActivity(jsIntent);
        }

        @Override
        public void onLongClick(View view, int position) {
            if (isSorting) {
                return;
            }
            String[] titles = rules.get(position).isEnable() ?
                    new String[]{"????????????", "Web??????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "??????????????????", "??????????????????2", "??????????????????5", "??????????????????6", "??????????????????"} :
                    new String[]{"????????????", "Web??????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "??????????????????", "??????????????????2", "??????????????????5", "??????????????????6", "??????????????????"};
            CustomRecyclerViewPopup popup = new CustomRecyclerViewPopup(getContext())
                    .withTitle("???????????????")
                    .height(0.6f)
                    .with(titles, 2, new CustomRecyclerViewPopup.ClickListener() {
                        @Override
                        public void click(String text, int pos) {
                            switch (text) {
                                case "Web??????":
                                    RemotePlayConfig.playerPath = RemotePlayConfig.WEBS;
                                    RemoteServerManager.instance().destroyServer();
//                                    Snackbar.make(recyclerView, "?????????????????????????????????", Snackbar.LENGTH_SHORT).show();
                                    String playUrl = RemoteServerManager.instance().getServerUrl(getContext());
                                    if (TextUtils.isEmpty(playUrl)) {
                                        Snackbar.make(recyclerView, "?????????????????????????????????IP???", Snackbar.LENGTH_LONG).show();
                                        return;
                                    }
                                    playUrl = playUrl + "/ruleEdit#/js?name=" + rules.get(position).getName();
                                    ClipboardUtil.copyToClipboard(getContext(), playUrl, true);
                                    Snackbar.make(recyclerView, "??????????????????????????????WiFi??????????????????????????????" + playUrl, Snackbar.LENGTH_LONG).show();
                                    try {
                                        RemoteServerManager.instance().startServer(getContext(), new Server.ServerListener() {
                                            @Override
                                            public void onStarted() {

                                            }

                                            @Override
                                            public void onStopped() {

                                            }

                                            @Override
                                            public void onException(Exception e) {
                                                Snackbar.make(recyclerView, "???????????????" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        Snackbar.make(recyclerView, "???????????????" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                case "????????????":
                                    JSManager.instance(getContext()).enableJsByFileName(rules.get(position).getName(), false);
                                    refreshData();
                                    ToastMgr.shortBottomCenter(getContext(), "???????????????");
                                    break;
                                case "????????????":
                                    ShareUtil.findChooserToDeal(getContext(), "file://" + JSManager.instance(getContext()).getFilePathByName(rules.get(position).getName()));
                                    break;
                                case "????????????":
                                    if (StringUtil.isNotEmpty(search_edit.getText().toString())) {
                                        ToastMgr.shortCenter(getContext(), "??????????????????????????????????????????");
                                        break;
                                    }
                                    new XPopup.Builder(getContext())
                                            .asConfirm("????????????", "????????????????????????????????????????????????????????????????????????????????????" +
                                                            "??????????????????????????????????????????????????????????????????????????????????????????????????????" +
                                                            "?????????????????????eval(fetch('hiker://files/rules/js/g-parse-list.js',{}))",
                                                    () -> {
                                                        isSorting = true;
                                                        js_list_add.setText("????????????");
                                                    }).show();
                                    break;
                                case "????????????":
                                    JSManager.instance(getContext()).enableJsByFileName(rules.get(position).getName(), true);
                                    refreshData();
                                    ToastMgr.shortBottomCenter(getContext(), "???????????????");
                                    break;
                                case "????????????":
                                    new XPopup.Builder(getContext())
                                            .asConfirm("????????????", "??????????????????????????????", () -> {
                                                JSManager.instance(getContext()).enableAllJs(true);
                                                refreshData();
                                                ToastMgr.shortBottomCenter(getContext(), "?????????????????????");
                                            }).show();
                                    break;
                                case "????????????":
                                    new XPopup.Builder(getContext())
                                            .asConfirm("????????????", "??????????????????????????????", () -> {
                                                JSManager.instance(getContext()).enableAllJs(false);
                                                refreshData();
                                                ToastMgr.shortBottomCenter(getContext(), "?????????????????????");
                                            }).show();
                                    break;
                                case "????????????":
                                    Intent jsIntent = new Intent(getContext(), JSEditActivity.class);
                                    jsIntent.putExtra("dom", rules.get(position).getName());
                                    startActivity(jsIntent);
                                    break;
                                case "????????????":
                                    new PromptDialog(getContext())
                                            .setDialogType(DIALOG_TYPE_WARNING)
                                            .setTitleText("????????????")
                                            .setContentText("?????????????????????" + rules.get(position).getName() + "??????JS?????????")
                                            .setPositiveListener("????????????", dialog -> {
                                                dialog.dismiss();
                                                boolean ok = JSManager.instance(getContext()).deleteJs(rules.get(position).getName());
                                                if (ok) {
                                                    ToastMgr.shortBottomCenter(getContext(), "???????????????");
                                                } else {
                                                    ToastMgr.shortBottomCenter(getContext(), "???????????????");
                                                }
                                                refreshData();
                                            }).show();
                                    break;
                                case "????????????":
                                    String nowJs1 = JSManager.instance(getContext()).getJsByFileName(rules.get(position).getName());
                                    if (TextUtils.isEmpty(nowJs1)) {
                                        ToastMgr.shortBottomCenter(getContext(), "?????????????????????");
                                        return;
                                    }
                                    if (nowJs1.length() > 10240) {
                                        ToastMgr.shortBottomCenter(getContext(), "?????????????????????????????????????????????");
                                        ShareUtil.findChooserToSend(getContext(), JSManager.getJsDirPath() + File.separator + rules.get(position).getName() + ".js");
                                    } else {
                                        try {
                                            nowJs1 = new String(Base64.encode(nowJs1.getBytes(), Base64.NO_WRAP));
                                            nowJs1 = StringUtil.replaceLineBlank(nowJs1);
                                            AutoImportHelper.shareWithCommand(getContext(), rules.get(position).getName() + "@base64://" + nowJs1, AutoImportHelper.JS_URL);
                                        } catch (Exception e) {
                                            DebugUtil.showErrorMsg(JSListActivity.this, getContext(), "?????????????????????", e.getMessage(), "", e);
                                            ShareUtil.findChooserToSend(getContext(), JSManager.getJsDirPath() + File.separator + rules.get(position).getName() + ".js");
                                        }
                                    }
                                    break;
                                case "??????????????????":
                                    String paste1 = getSharePaste(position);
                                    if (StringUtil.isNotEmpty(paste1)) {
                                        RuleImporterManager.share(RuleImporterManager.Importer.Num1, JSListActivity.this, paste1, rules.get(position).getName(), "?????????");
                                    }
                                    break;
                                case "??????????????????2":
                                    String paste2 = getSharePaste(position);
                                    if (StringUtil.isNotEmpty(paste2)) {
                                        RuleImporterManager.share(RuleImporterManager.Importer.Num2, JSListActivity.this, paste2, rules.get(position).getName(), "?????????");
                                    }
                                    break;
                                case "??????????????????4":
                                    String paste4 = getSharePaste(position);
                                    if (StringUtil.isNotEmpty(paste4)) {
                                        RuleImporterManager.share(RuleImporterManager.Importer.Num4, JSListActivity.this, paste4, rules.get(position).getName(), "?????????");
                                    }
                                    break;
                                case "??????????????????5":
                                    String paste5 = getSharePaste(position);
                                    if (StringUtil.isNotEmpty(paste5)) {
                                        RuleImporterManager.share(RuleImporterManager.Importer.Num5, JSListActivity.this, paste5, rules.get(position).getName(), "?????????");
                                    }
                                    break;
                                case "??????????????????6":
                                    String paste6 = getSharePaste(position);
                                    if (StringUtil.isNotEmpty(paste6)) {
                                        RuleImporterManager.share(RuleImporterManager.Importer.Num6, JSListActivity.this, paste6, rules.get(position).getName(), "?????????");
                                    }
                                    break;
                                case "??????????????????":
                                    String fileName = rules.get(position).getName();
                                    boolean pageEnd = JSManager.getPageEndByFileName(fileName);
                                    String loadTime = pageEnd ? "???????????????????????????" : "??????????????????????????????????????????";
                                    String otherTime = pageEnd ? "??????????????????????????????????????????" : "???????????????????????????";
                                    new XPopup.Builder(getContext())
                                            .asConfirm("??????????????????", "???????????????????????????" + loadTime + "??????????????????" + otherTime, () -> {
                                                boolean enable = rules.get(position).isEnable();
                                                String newName = JSManager.revertPageEndByFileName(fileName);
                                                JSManager.instance(getContext()).updateJs(newName, JSManager.instance(getContext()).getJsByFileName(fileName));
                                                JSManager.instance(getContext()).enableJsByFileName(newName, enable);
                                                JSManager.instance(getContext()).deleteJs(fileName);
                                                ToastMgr.shortBottomCenter(getContext(), "????????????" + otherTime);
                                                refreshData();
                                            }).show();
                                    break;
                            }
                        }

                        @Override
                        public void onLongClick(String url, int position) {
                        }
                    });
            new XPopup.Builder(getContext())
                    .asCustom(popup)
                    .show();
        }
    };

    private String getSharePaste(int position) {
        String nowJs = JSManager.instance(getContext()).getJsByFileName(rules.get(position).getName());
        if (TextUtils.isEmpty(nowJs)) {
            ToastMgr.shortBottomCenter(getContext(), "?????????????????????");
            return null;
        }
        nowJs = new String(Base64.encode(nowJs.getBytes(), Base64.NO_WRAP));
        nowJs = StringUtil.replaceLineBlank(nowJs);
        String ru = rules.get(position).getName() + "@base64://" + nowJs;
        String shareRulePrefix = PreferenceMgr.getString(getContext(), "shareRulePrefix", "");
        return AutoImportHelper.getCommand(shareRulePrefix, ru, AutoImportHelper.JS_URL);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (adapter != null) {
            allRules.clear();
            allRules.addAll(JSManager.instance(getContext()).listAllJsFileNames());
            orderMap.clear();
            BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.JS_LIST_ORDER_KEY).findFirst(BigTextDO.class);
            if (bigTextDO != null && !allRules.isEmpty()) {
                String value = bigTextDO.getValue();
                if (StringUtil.isNotEmpty(value)) {
                    orderMap = JSON.parseObject(value, new TypeReference<Map<String, Integer>>() {
                    });
                    for (JsRule allRule : allRules) {
                        if (orderMap.containsKey(allRule.getName())) {
                            Integer order = orderMap.get(allRule.getName());
                            allRule.setOrder(order == null ? 0 : order);
                        } else {
                            allRule.setOrder(Integer.MAX_VALUE);
                        }
                    }
                    Collections.sort(allRules);
                }
            }
            rules.clear();
            String key = search_edit.getText().toString();
            rules.addAll(Stream.of(allRules)
                    .filter(bookmark -> StringUtil.isEmpty(key)
                            || (StringUtil.isNotEmpty(bookmark.getName()) && bookmark.getName().contains(key)))
                    .collect(Collectors.toList()));
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onBackPressed() {
        if (!"??????".equals(js_list_add.getText().toString())) {
            new XPopup.Builder(getContext())
                    .asConfirm("????????????", "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????",
                            this::finish).show();
            return;
        }
        super.onBackPressed();
    }


    private ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlag = 0;
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            return makeMovementFlags(dragFlag, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(rules, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(rules, i, i - 1);
                }
            }
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //???????????????????????????
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return isSorting;
        }

        /**
         * ????????????Item?????????????????????
         * ????????????
         * @param viewHolder
         * @param actionState
         */
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                viewHolder.itemView.setBackgroundColor(getContext().getResources().getColor(R.color.gray_rice));
                //????????????????????????//??????70??????
                try {
                    Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                    if (vib != null) {
                        vib.vibrate(70);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        /**
         * ?????????????????????????????????
         * @param recyclerView
         * @param viewHolder
         */
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
//            viewHolder.itemView.setBackgroundColor(0);
            adapter.notifyDataSetChanged();
        }
    });

}
