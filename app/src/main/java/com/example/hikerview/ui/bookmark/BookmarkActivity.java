package com.example.hikerview.ui.bookmark;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.event.BookmarkRefreshEvent;
import com.example.hikerview.event.OnUrlChangeEvent;
import com.example.hikerview.event.web.OnBookmarkUpdateEvent;
import com.example.hikerview.event.web.OnShortcutUpdateEvent;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.model.Bookmark;
import com.example.hikerview.ui.base.BaseActivity;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.bookmark.model.BookmarkModel;
import com.example.hikerview.ui.bookmark.service.ChromeParser;
import com.example.hikerview.ui.browser.model.AdUrlBlocker;
import com.example.hikerview.ui.browser.model.Shortcut;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.browser.webview.MultiWindowManager;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.ui.view.DialogBuilder;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.ui.view.ZLoadingDialog.ZLoadingDialog;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.ui.view.popup.SimpleHintPopupWindow;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.RandomUtil;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.UriUtils;
import com.example.hikerview.utils.WebUtil;
import com.lxj.xpopup.XPopup;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * ?????????By hdy
 * ?????????On 2018/6/17
 * ?????????At 11:19
 */

public class BookmarkActivity extends BaseActivity implements BaseCallback<Bookmark> {
    private RecyclerView recyclerView;
    private List<Bookmark> list = new ArrayList<>();
    private List<Bookmark> showList = new ArrayList<>();
    private BookmarkAdapter adapter;
    private BookmarkModel bookmarkModel = new BookmarkModel();
    private ZLoadingDialog loadingDialog;
    private String groupSelected;
    private static final String TAG = "BookmarkActivity";
    private EditText search_edit;
    private ImageView search_clear;
    private View sort_ok_bg, sort_ok_btn;
    private boolean isSorting = false, isSelecting = false, isSelectingAll = false;
    protected Map<String, Integer> orderMap = new HashMap<>();
    private BookmarkEditPopup popup;
    public static final int[] colors = {0xFF6354EF, 0xFF717171, 0xFF62A6FB, 0xFFFF6877, 0xFFFE9700, 0xFF2196F3, 0xFF01BFA5};
    private boolean showAdd = false;
    private TextView selectDel;

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activity_bookmark;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookmark_options, menu);
        return true;
    }

    @Override
    protected void initView() {
        try {
            setSupportActionBar(findView(R.id.home_toolbar));
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                generateShowList(key);
                Collections.sort(showList);
                adapter.notifyDataSetChanged();
            }
        });
        recyclerView = findView(R.id.home_recy);
        loadingDialog = DialogBuilder.createLoadingDialog(getContext(), false);
        sort_ok_bg = findView(R.id.sort_ok_bg);
        sort_ok_btn = findView(R.id.sort_ok_btn);
        sort_ok_btn.setOnClickListener(v -> {
            saveRulesPosOrder();
            ToastMgr.shortCenter(getContext(), "?????????????????????");
        });
        View select_bg = findView(R.id.select_bg);
        select_bg.findViewById(R.id.select_all).setOnClickListener(v -> {
            boolean select = !isSelectingAll;
            for (Bookmark bookmark : showList) {
                bookmark.setSelected(select);
            }
            adapter.notifyDataSetChanged();
            isSelectingAll = !isSelectingAll;
        });
        selectDel = select_bg.findViewById(R.id.select_del);
        selectDel.setOnClickListener(v -> batchDelete());
        select_bg.findViewById(R.id.select_move).setOnClickListener(v -> batchMove());
        select_bg.findViewById(R.id.select_done).setOnClickListener(v -> {
            for (Bookmark bookmark : showList) {
                bookmark.setSelected(false);
                bookmark.setSelecting(false);
            }
            isSelectingAll = false;
            isSelecting = false;
            findView(R.id.select_bg).setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        });
    }

    private void batchDelete() {
        List<Bookmark> bookmarks = Stream.of(showList).filter(Bookmark::isSelected).toList();
        if (CollectionUtil.isEmpty(bookmarks)) {
            ToastMgr.shortBottomCenter(getContext(), "???????????????????????????");
            return;
        }
        new XPopup.Builder(getContext())
                .asConfirm("????????????", "?????????????????????????????????????????????????????????", () -> {
                    for (Bookmark bookmark : bookmarks) {
                        if (bookmark.isDir()) {
                            ChromeParser.BookmarkGroupNode groupNode = ChromeParser.toGroupNode(list, bookmark);
                            BookmarkModel.deleteByGroupNode(groupNode);
                        } else {
                            boolean isFile = StringUtil.isNotEmpty(bookmark.getUrl()) && bookmark.getUrl().startsWith("file://");
                            if (isFile) {
                                new File(bookmark.getUrl().replace("file://", "")).delete();
                            }
                            bookmarkModel.delete(getContext(), bookmark);
                        }
                        list.remove(bookmark);
                        showList.remove(bookmark);
                    }
                    adapter.notifyDataSetChanged();
                }).show();
    }

    private void batchMove() {
        List<Bookmark> bookmarks = Stream.of(showList).filter(Bookmark::isSelected).toList();
        if (CollectionUtil.isEmpty(bookmarks)) {
            ToastMgr.shortBottomCenter(getContext(), "???????????????????????????");
            return;
        }
        List<String> groupPaths = getGroupPaths();
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.isDir()) {
                //?????????1/?????????2
                String nowPath = BookmarkModel.getGroupPath(bookmark);
                if (nowPath.isEmpty()) {
                    nowPath = "/";
                }
                String finalNowPath = nowPath;
                groupPaths = Stream.of(groupPaths).filter(it -> !it.contains(finalNowPath)).toList();
            }
        }

        if (CollectionUtil.isEmpty(groupPaths)) {
            ToastMgr.shortCenter(getContext(), "??????????????????????????????");
            return;
        }

        String[] s = new String[groupPaths.size()];
        CustomCenterRecyclerViewPopup popup = new CustomCenterRecyclerViewPopup(getContext())
                .withTitle("???????????????")
                .with(groupPaths.toArray(s), 1, new CustomCenterRecyclerViewPopup.ClickListener() {
                    @Override
                    public void click(String url, int position) {
                        for (Bookmark bookmark : bookmarks) {
                            bookmark.setParentId(findDirIdByPath(url));
                            bookmark.save();
                        }
                        bookmarkModel.process("all", BookmarkActivity.this);
                    }

                    @Override
                    public void onLongClick(String url, int position) {

                    }
                });
        new XPopup.Builder(getContext())
                .asCustom(popup)
                .show();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        groupSelected = PreferenceMgr.getString(getContext(), "bookmarkSelectedGroup", "");
        if (StringUtil.isNotEmpty(groupSelected)) {
            String[] ts = groupSelected.split("/");
            setTitle(ts[ts.length - 1]);
        } else {
            setTitle("????????????");
        }
        adapter = new BookmarkAdapter(getContext(), showList);
        adapter.setOnItemClickListener(new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (isSelecting) {
                    showList.get(position).setSelected(!showList.get(position).isSelected());
                    adapter.notifyItemChanged(position);
                    return;
                }
                chooseClickOption(showList.get(position));
            }

            @Override
            public void onLongClick(View view, int position) {
                if (!isSorting && !isSelecting) {
                    if (showList.get(position).isDir()) {
                        manageGroup(showList.get(position), view, position);
                    } else {
                        chooseLongClickOption(showList.get(position), position, view);
                    }
                }
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        touchHelper.attachToRecyclerView(recyclerView);
        String webs = getIntent().getStringExtra("webs");
        if (!TextUtils.isEmpty(webs)) {
            showAdd = true;
        }
//        loadUrlFromClipboard();
        if (getIntent().getBooleanExtra("offline_pages", false)) {
            search_edit.setText("????????????");
        }
        bookmarkModel.process("all", this);
    }

    private void chooseLongClickOption(Bookmark bookmark, int position, View view) {
        String[] ops;
        if (StringUtil.isNotEmpty(bookmark.getUrl()) && bookmark.getUrl().startsWith("file://")) {
            ops = new String[]{"????????????", "???????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????"};
        } else {
            ops = new String[]{"????????????", "???????????????", "????????????", "????????????", "????????????", "????????????", "????????????"};
        }
        new SimpleHintPopupWindow(this, ops, s -> {
            switch (s) {
                case "???????????????":
                    EventBus.getDefault().post(new OnUrlChangeEvent(bookmark.getUrl(), true, false));
                    finish();
                    break;
                case "????????????":
                    EventBus.getDefault().post(new OnUrlChangeEvent(bookmark.getUrl(), true, true));
                    ToastMgr.shortBottomCenter(getContext(), "??????????????????");
                    break;
                case "????????????":
                    ShareUtil.findChooserToSend(getContext(), bookmark.getUrl());
                    break;
                case "????????????":
                    sortRules();
                    break;
                case "????????????":
                    batchSelect();
                    break;
                case "????????????":
                    boolean isFile = StringUtil.isNotEmpty(showList.get(position).getUrl()) && showList.get(position).getUrl().startsWith("file://");
                    new XPopup.Builder(getContext())
                            .asConfirm("????????????", "??????????????????" + showList.get(position).getTitle() + "???" + (isFile ? "???????????????" : "") + "??????", () -> {
                                if (isFile) {
                                    new File(showList.get(position).getUrl().replace("file://", "")).delete();
                                }
                                bookmarkModel.delete(getContext(), showList.get(position));
                                list.remove(showList.get(position));
                                showList.remove(position);
                                adapter.notifyDataSetChanged();
                                ToastMgr.shortBottomCenter(getContext(), "????????????");
                            }).show();
                    break;
                case "????????????":
                    addToShortcut(showList.get(position));
                    break;
                case "????????????":
                    String text1 = showList.get(position).getTitle() + "???" + showList.get(position).getUrl();
                    String groupPath = BookmarkModel.getGroupPath(bookmark);
                    if (StringUtil.isNotEmpty(groupPath)) {
                        text1 = text1 + "???" + groupPath;
                    }
                    addBookmark(text1, showList.get(position).getId());
                    break;
            }
        }).showPopupWindowCenter(view);
    }

    private void batchSelect() {
        isSelecting = true;
        isSelectingAll = false;
        for (Bookmark bookmark1 : showList) {
            bookmark1.setSelecting(true);
        }
        adapter.notifyDataSetChanged();
        findView(R.id.select_bg).setVisibility(View.VISIBLE);
    }

    private void addToShortcut(Bookmark bookmark) {
        addToShortcut(getContext(), bookmark);
    }

    private static void addToShortcut(Context context, Bookmark bookmark) {
        String icon = "color://" + colors[RandomUtil.getRandom(0, colors.length)];
        Shortcut shortcut = new Shortcut(bookmark.getTitle(), bookmark.getUrl(), icon);
        List<Shortcut> shortcuts = Shortcut.toList(BigTextDO.getShortcuts(context));
//        if (shortcuts.size() >= 20) {
//            ToastMgr.shortBottomCenter(context, "????????????????????????20?????????????????????");
//            return;
//        }
        for (Shortcut shortcut1 : shortcuts) {
            if (StringUtils.equals(shortcut1.getUrl(), shortcut.getUrl())) {
                ToastMgr.shortBottomCenter(context, "?????????????????????????????????");
                return;
            }
        }
        shortcuts.add(shortcut);
        BigTextDO.updateShortcuts(context, Shortcut.toStr(shortcuts));
        ToastMgr.shortBottomCenter(context, "?????????????????????");
        EventBus.getDefault().post(new OnShortcutUpdateEvent());
    }

    private void sortRules() {
        if (isSelecting) {
            ToastMgr.shortCenter(getContext(), "????????????????????????????????????");
            return;
        }
        sort_ok_bg.setVisibility(View.VISIBLE);
        isSorting = true;
        ToastMgr.shortCenter(getContext(), "?????????????????????");
    }

    private void chooseClickOption(Bookmark bookmark) {
        if (bookmark.isDir()) {
            //???????????????
            setTitle(bookmark.getTitle());
            groupSelected = BookmarkModel.getGroupPath(bookmark);
            String key = search_edit.getText().toString();
            generateShowList(key);
            Collections.sort(showList);
            adapter.notifyDataSetChanged();
            PreferenceMgr.put(getContext(), "bookmarkSelectedGroup", groupSelected);
            return;
        }
        WebUtil.goWeb(getContext(), bookmark.getUrl());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add_bookmark:
                addBookmark(null, -1);
                break;
            case R.id.action_add_group:
                new XPopup.Builder(getContext())
                        .asInputConfirm("???????????????", null, text -> {
                            if (StringUtil.isNotEmpty(text)) {
                                if (text.contains("/")) {
                                    ToastMgr.shortCenter(getContext(), "???????????????????????????/??????");
                                    return;
                                }
                                Bookmark bookmark = new Bookmark();
                                bookmark.setDir(true);
                                bookmark.setTitle(text);
                                if (StringUtil.isNotEmpty(groupSelected)) {
                                    bookmark.setParentId(findDirIdByPath(groupSelected));
                                }
                                bookmark.save();
                                bookmarkModel.process("all", this);
                            }
                        }).show();
                break;
            case R.id.action_sort_bookmark:
                sortRules();
                break;
            case R.id.action_import_html:
                importRuleFromHtml();
                break;
            case R.id.action_export_html:
                exportRuleToHtml();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportRuleToHtml() {
        if (CollectionUtil.isEmpty(list)) {
            ToastMgr.shortCenter(getContext(), "??????????????????");
            return;
        }
        String path = getShareFilePath("share-bookmarks.html");
        if (StringUtil.isNotEmpty(path)) {
            HeavyTaskUtil.executeNewTask(() -> {
                try {
                    ChromeParser.exportToFile(list, path);
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            ToastMgr.shortBottomCenter(getContext(), "???????????????????????????????????????");
                            ShareUtil.findChooserToSend(getContext(), "file://" + path);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            ToastMgr.shortBottomCenter(getContext(), "?????????" + e.getMessage());
                        });
                    }
                }
            });
        }
    }

    private String getShareFilePath(String fileName) {
        String fileDirPath = UriUtils.getRootDir(getContext()) + File.separator + "share";
        File dir = new File(fileDirPath);
        if (!dir.exists()) {
            boolean ok = dir.mkdir();
            if (!ok) {
                ToastMgr.shortBottomCenter(getContext(), "????????????" + fileDirPath + "??????");
                return null;
            }
        }
        File ruleFile = new File(fileDirPath + File.separator + fileName);
        if (ruleFile.exists()) {
            boolean ok = ruleFile.delete();
            if (!ok) {
                ToastMgr.shortBottomCenter(getContext(), "????????????" + ruleFile.getAbsolutePath() + "??????");
                return null;
            }
        }
        return ruleFile.getAbsolutePath();
    }

    private void importRuleFromHtml() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType(???image/*???);//????????????
        //intent.setType(???audio/*???); //????????????
        //intent.setType(???video/*???); //???????????? ???mp4 3gp ???android????????????????????????
        //intent.setType(???video/*;image/*???);//???????????????????????????
        intent.setType("text/html");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    private void manageGroup(Bookmark bookmark, View view, int position) {
        new SimpleHintPopupWindow(this,
                new String[]{"??????????????????", "???????????????", "???????????????", "????????????"}, s -> {
            switch (s) {
                case "??????????????????":
                    new XPopup.Builder(getContext()).asInputConfirm("??????????????????", null, bookmark.getTitle(), "????????????????????????????????????",
                            text1 -> {
                                if (TextUtils.isEmpty(text1)) {
                                    ToastMgr.shortBottomCenter(getContext(), "????????????");
                                    return;
                                }
                                if (text1.contains("/")) {
                                    ToastMgr.shortCenter(getContext(), "???????????????????????????/??????");
                                    return;
                                }
                                bookmark.setTitle(text1);
                                bookmark.save();
                                adapter.notifyItemChanged(position);
                            })
                            .show();
                    break;
                case "???????????????":
                    new XPopup.Builder(getContext())
                            .asConfirm("????????????", "????????????????????????????????????????????????????????????????????????????????????", () -> {
                                ChromeParser.BookmarkGroupNode groupNode = ChromeParser.toGroupNode(list, bookmark);
                                BookmarkModel.deleteByGroupNode(groupNode);
                                bookmarkModel.process("all", this);
                            })
                            .show();
                    break;
                case "????????????":
                    batchSelect();
                    break;
                case "???????????????":
                    //?????????1????????????1/?????????2????????????1/?????????3????????????1/?????????2/?????????4???/
                    List<String> groupPaths = getGroupPaths();
                    //?????????1/?????????2
                    String nowPath = BookmarkModel.getGroupPath(bookmark);
                    if (nowPath.isEmpty()) {
                        nowPath = "/";
                    }
                    String finalNowPath = nowPath;
                    //?????????1????????????1/?????????3???/
                    groupPaths = Stream.of(groupPaths).filter(it -> !it.contains(finalNowPath)).toList();
                    Collections.sort(groupPaths);
                    if (CollectionUtil.isEmpty(groupPaths)) {
                        ToastMgr.shortCenter(getContext(), "??????????????????????????????");
                        break;
                    }
                    CustomCenterRecyclerViewPopup popup = new CustomCenterRecyclerViewPopup(getContext())
                            .withTitle("?????????????????????")
                            .with(groupPaths, 1, new CustomCenterRecyclerViewPopup.ClickListener() {
                                @Override
                                public void click(String url, int position) {
                                    bookmark.setParentId(findDirIdByPath(url));
                                    bookmark.save();
                                    bookmarkModel.process("all", BookmarkActivity.this);
                                }

                                @Override
                                public void onLongClick(String url, int position) {

                                }
                            });
                    new XPopup.Builder(getContext())
                            .asCustom(popup)
                            .show();
                    break;
            }
        }).showPopupWindowCenter(view);
    }

    private List<String> getGroupPaths() {
        return getGroupPaths(list);
    }

    private static List<String> getGroupPaths(@Nullable List<Bookmark> list) {
        if (CollectionUtil.isEmpty(list)) {
            list = LitePal.findAll(Bookmark.class);
            BookmarkModel.initBookmarkParent(list);
        }
        List<String> paths = Stream.of(list).filter(Bookmark::isDir).map(it -> {
            String path = BookmarkModel.getGroupPath(it);
            if (path.isEmpty()) {
                return "/";
            } else {
                return path;
            }
        }).distinct().toList();
        paths.add(0, "/");
        return paths;
    }

    private long findDirIdByPath(String path) {
        Bookmark dir = findDirByPath(list, path);
        if (dir == null) {
            return -1;
        } else {
            return dir.getId();
        }
    }

    private static long findDirIdByPath(List<Bookmark> list, String path) {
        Bookmark dir = findDirByPath(list, path);
        if (dir == null) {
            return -1;
        } else {
            return dir.getId();
        }
    }

    private static Bookmark findDirByPath(@Nullable List<Bookmark> list, String path) {
        if ("/".equals(path) || StringUtil.isEmpty(path)) {
            return null;
        }
        if (CollectionUtil.isEmpty(list)) {
            list = LitePal.findAll(Bookmark.class);
            BookmarkModel.initBookmarkParent(list);
        }
        String[] paths = path.split("/");
        List<Bookmark> dirs = Stream.of(list).filter(Bookmark::isDir).toList();
        Bookmark parent = null;
        for (String s : paths) {
            boolean find = false;
            for (Bookmark dir : dirs) {
                if (parent == null) {
                    //???????????????????????????
                    if (dir.getParent() == null && s.equals(dir.getTitle())) {
                        parent = dir;
                        find = true;
                    }
                } else {
                    //?????????????????????????????????+?????????
                    if (dir.getParent() != null && parent.getId() == dir.getParent().getId() && s.equals(dir.getTitle())) {
                        parent = dir;
                        find = true;
                    }
                }
            }
            if (parent == null) {
                //????????????????????????????????????????????????????????????
                return null;
            }
            if (!find) {
                //???????????????????????????
                return null;
            }
        }
        return parent;
    }

    private void deleteAllBookmarks() {
        new PromptDialog(getContext())
                .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                .setAnimationEnable(true)
                .setTitleText("????????????")
                .setContentText("???????????????????????????????????????????????????????????????????????????")
                .setPositiveListener("????????????", dialog -> {
                    bookmarkModel.deleteAll(getContext());
                    bookmarkModel.process("all", this);
                    ToastMgr.shortBottomCenter(getContext(), "???????????????????????????");
                    dialog.dismiss();
                }).show();
    }

    private void addBookmark(@Nullable String addWeb, long id) {
        String title = null, url = null, groupPath = null;
        if (StringUtil.isNotEmpty(addWeb)) {
            String[] ads = addWeb.split("?????????????????????");
            if (ads.length == 2) {
                addWeb = ads[0];
                if (!TextUtils.isEmpty(ads[1])) {
                    String[] urls = ads[1].split("##");
                    HeavyTaskUtil.executeNewTask(() -> AdUrlBlocker.instance().addUrls(Arrays.asList(urls)));
                }
            }
            String[] detail = addWeb.split("???");
            if (detail.length == 2) {
                title = detail[0];
                url = detail[1];
            } else if (detail.length == 3) {
                title = detail[0];
                url = detail[1];
                groupPath = detail[2];
            }
        } else {
            if (StringUtil.isNotEmpty(groupSelected)) {
                groupPath = groupSelected;
            }
        }
        List<String> groups = getGroupPaths();
        popup = new BookmarkEditPopup(getContext())
                .showShortcut(StringUtil.isNotEmpty(addWeb) && id == -1)
                .bind(StringUtil.isNotEmpty(addWeb) ? "????????????" : "????????????",
                        title, url, groupPath, groups, (title1, url1, group1, addShortCut) -> {
                            if (StringUtil.isEmpty(group1)) {
                                group1 = "";
                            }
                            if (TextUtils.isEmpty(title1) || TextUtils.isEmpty(url1)) {
                                ToastMgr.shortBottomCenter(getContext(), "?????????????????????");
                            } else {
                                if (!url1.startsWith("http") && !url1.startsWith("file://")) {
                                    url1 = "http://" + url1;
                                }
                                if (id == -1) {
                                    Bookmark bookmark = new Bookmark();
                                    Bookmark bookmark1 = LitePal.where("url = ?", url1).findFirst(Bookmark.class);
                                    if (bookmark1 != null) {
                                        bookmark = bookmark1;
                                    }
                                    bookmark.setTitle(title1);
                                    bookmark.setUrl(url1);
                                    bookmark.setOrder(list.size());
                                    bookmark.setParentId(findDirIdByPath(group1));
                                    bookmarkModel.add(getContext(), bookmark);
                                    bookmarkModel.process("all", BookmarkActivity.this);
                                    ToastMgr.shortBottomCenter(getContext(), "????????????");
                                    if (addShortCut) {
                                        addToShortcut(bookmark);
                                    } else {
                                        EventBus.getDefault().post(new OnBookmarkUpdateEvent());
                                    }
                                } else {
                                    Bookmark bookmark = LitePal.find(Bookmark.class, id);
                                    if (bookmark == null) {
                                        bookmark = new Bookmark();
                                    }
                                    bookmark.setTitle(title1);
                                    bookmark.setUrl(url1);
                                    bookmark.setParentId(findDirIdByPath(group1));
                                    bookmark.save();
                                    ToastMgr.shortBottomCenter(getContext(), "????????????");
                                    bookmarkModel.process("all", BookmarkActivity.this);
                                    if (addShortCut) {
                                        addToShortcut(bookmark);
                                    } else {
                                        EventBus.getDefault().post(new OnBookmarkUpdateEvent());
                                    }
                                }
                            }
                        }
                );
        new XPopup.Builder(getContext())
                .asCustom(popup)
                .show();
    }

    public static void addBookmark(Activity context, String title, String url) {
        List<Bookmark> list = LitePal.findAll(Bookmark.class);
        BookmarkModel.initBookmarkParent(list);
        List<String> groups = getGroupPaths(list);
        BookmarkEditPopup popup = new BookmarkEditPopup(context)
                .showShortcut(true)
                .bind("????????????",
                        title, url, null, groups, (title1, url1, group1, addShortCut) -> {
                            if (StringUtil.isEmpty(group1)) {
                                group1 = "";
                            }
                            if (TextUtils.isEmpty(title1) || TextUtils.isEmpty(url1)) {
                                ToastMgr.shortBottomCenter(context, "?????????????????????");
                            } else {
                                if (!url1.startsWith("http") && !url1.startsWith("file://")) {
                                    url1 = "http://" + url1;
                                }
                                Bookmark bookmark = new Bookmark();
                                Bookmark bookmark1 = LitePal.where("url = ?", url1).findFirst(Bookmark.class);
                                if (bookmark1 != null) {
                                    bookmark = bookmark1;
                                } else {
                                    HorizontalWebView webView = MultiWindowManager.instance(context).getCurrentWebView();
                                    if (webView.getWebViewHelper() != null && webView.getWebViewHelper().getRequestHeaderMap() != null) {
                                        for (String u : webView.getWebViewHelper().getRequestHeaderMap().keySet()) {
                                            if (u.contains(".ico") || u.contains("/favicon")) {
                                                bookmark.setIcon(u);
                                                break;
                                            }
                                        }
                                    }
                                }
                                bookmark.setTitle(title1);
                                bookmark.setUrl(url1);
                                bookmark.setOrder(list.size());
                                bookmark.setParentId(findDirIdByPath(list, group1));
                                BookmarkModel.add(context, bookmark);
                                ToastMgr.shortBottomCenter(context, "????????????");
                                if (addShortCut) {
                                    addToShortcut(context, bookmark);
                                } else {
                                    EventBus.getDefault().post(new OnBookmarkUpdateEvent());
                                }
                            }
                        }
                );
        new XPopup.Builder(context)
                .asCustom(popup)
                .show();
    }

    @Override
    public void bindArrayToView(String actionType, List<Bookmark> data) {
        if (isSelecting) {
            for (Bookmark bookmark1 : showList) {
                bookmark1.setSelecting(true);
            }
        }
        runOnUiThread(() -> {
            if ("all".equals(actionType)) {
                list.clear();
                list.addAll(data);
                if (popup != null && popup.isShow()) {
                    List<String> groups = Stream.of(list).map(Bookmark::getGroup).filter(StringUtil::isNotEmpty).distinct().collect(Collectors.toList());
                    popup.updateGroups(groups);
                }
                String key = search_edit.getText().toString();
                generateShowList(key);
                Timber.d("bindArrayToView: %s", list.size());
                Timber.d("bindArrayToView: %s", showList.size());
                loadOrderMap();
                adapter.notifyDataSetChanged();
                if (showAdd) {
                    String webs = getIntent().getStringExtra("webs");
                    if (!TextUtils.isEmpty(webs)) {
                        addBookmark(webs, -1);
                        showAdd = false;
                    }
                }
            }
        });
    }

    @Override
    public void bindObjectToView(String actionType, Bookmark data) {

    }

    @Override
    public void error(String title, String msg, String code, Exception e) {
        runOnUiThread(() -> {
            loading(false);
            DebugUtil.showErrorMsg(this, getContext(), title, msg, code, e);
        });
    }

    @Override
    public void loading(boolean isLoading) {
        if (loadingDialog != null) {
            if (isLoading) {
                loadingDialog.show();
            } else {
                loadingDialog.dismiss();
            }
        }
    }

    private void generateShowList(String key) {
        showList.clear();
        String lKey = StringUtil.isEmpty(key) ? key : key.toLowerCase();
        if ("????????????".equals(key)) {
            lKey = "offline_pages";
        }
        List<Bookmark> bookmarks = new ArrayList<>();
        for (Bookmark bookmark : list) {
            if (StringUtil.isNotEmpty(lKey)) {
                //??????????????????????????????
                if (bookmark.isDir()) {
                    continue;
                }
                boolean ok = bookmark.getTitle().toLowerCase().contains(lKey)
                        || (StringUtil.isNotEmpty(bookmark.getUrl()) && bookmark.getUrl().toLowerCase().contains(lKey));
                if (!ok) {
                    continue;
                }
                bookmarks.add(bookmark);
            } else {
                //??????????????????
                String groupPath = BookmarkModel.getGroupPath(bookmark);
                if (StringUtil.isNotEmpty(groupSelected)) {
                    if (!bookmark.isDir() && groupSelected.equals(groupPath)) {
                        //??????
                        bookmarks.add(bookmark);
                    } else if (bookmark.isDir() && groupPath.contains(groupSelected)) {
                        //????????????????????????????????????
                        if (groupPath.split("/").length == groupSelected.split("/").length + 1) {
                            bookmarks.add(bookmark);
                        }
                    }
                } else {
                    if (!bookmark.isDir() && groupPath.isEmpty()) {
                        //?????????????????????
                        bookmarks.add(bookmark);
                    } else if (bookmark.isDir() && bookmark.getParent() == null) {
                        //??????????????????
                        bookmarks.add(bookmark);
                    }
                }
            }
        }
        showList.addAll(bookmarks);
    }


    private synchronized void loadOrderMap() {
        loadOrderMapValue();
        if (!orderMap.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isDir()) {
                    String key = "dir:" + list.get(i).getTitle();
                    if (orderMap.containsKey(key)) {
                        list.get(i).setOrder(orderMap.get(key));
                    }
                } else {
                    if (orderMap.containsKey(list.get(i).getUrl())) {
                        list.get(i).setOrder(orderMap.get(list.get(i).getUrl()));
                    }
                }
            }
        }
        Collections.sort(list);
        Collections.sort(showList);
    }

    private synchronized void loadOrderMapValue() {
        orderMap.clear();
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.BOOKMARK_ORDER_KEY).findFirst(BigTextDO.class);
        if (bigTextDO != null) {
            String value = bigTextDO.getValue();
//            Log.d(TAG, "loadOrderMap: " + value);
            if (StringUtil.isNotEmpty(value)) {
                orderMap = JSON.parseObject(value, new TypeReference<Map<String, Integer>>() {
                });
            }
        }
    }

    private synchronized void saveOrderMap() {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.BOOKMARK_ORDER_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(BigTextDO.BOOKMARK_ORDER_KEY);
        }
//        Log.d(TAG, "saveOrderMap: " + orderMap.toString());
        bigTextDO.setValue(JSON.toJSONString(orderMap));
//        Log.d(TAG, "saveOrderMap: " + bigTextDO.getValue());
        bigTextDO.save();
    }


    private void saveRulesPosOrder() {
        for (int i = 0; i < showList.size(); i++) {
            if (showList.get(i).isDir()) {
                orderMap.put("dir:" + showList.get(i).getTitle(), i + 1);
            } else {
                orderMap.put(showList.get(i).getUrl(), i + 1);
            }
            showList.get(i).setOrder(i + 1);
        }
        saveOrderMap();
        sort_ok_bg.setVisibility(View.GONE);
        isSorting = false;
        Collections.sort(list);
        Collections.sort(showList);
        adapter.notifyDataSetChanged();
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
                    Collections.swap(showList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(showList, i, i - 1);
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
                    if (isFinishing()) {
                        return;
                    }
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

    @Override
    public void finish() {
        if (isSorting) {
            new XPopup.Builder(getContext()).asConfirm("????????????", "?????????????????????????????????????????????????????????????????????????????????", () -> {
                isSorting = false;
                finish();
            }).show();
            return;
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBookmarkRefresh(BookmarkRefreshEvent event) {
        bookmarkModel.process("all", this);
    }

    @Override
    public void onBackPressed() {
        if (isSelecting) {
            findViewById(R.id.select_done).performClick();
            return;
        }
        if (StringUtil.isNotEmpty(groupSelected)) {
            if (isSorting) {
                ToastMgr.shortBottomCenter(getContext(), "?????????????????????????????????");
                return;
            }
            String[] groups = groupSelected.split("/");
            if (groups.length > 1) {
                setTitle(groups[groups.length - 2]);
                groupSelected = StringUtil.arrayToString(groups, 0, groups.length - 1, "/");
            } else {
                setTitle("????????????");
                groupSelected = "";
            }
            generateShowList(search_edit.getText().toString());
            Collections.sort(showList);
            adapter.notifyDataSetChanged();
            PreferenceMgr.put(getContext(), "bookmarkSelectedGroup", groupSelected);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            UriUtils.getFilePathFromURI(getContext(), uri, new UriUtils.LoadListener() {
                @Override
                public void success(String s) {
                    if (!isFinishing()) {
                        List<Bookmark> bookmarks = ChromeParser.parse(s);
                        new File(s).delete();
                        if (CollectionUtil.isEmpty(bookmarks)) {
                            runOnUiThread(() -> {
                                ToastMgr.shortBottomCenter(getContext(), "???????????????????????????????????????");
                            });
                        } else {
                            int count = BookmarkModel.addByList(getContext(), bookmarks);
                            runOnUiThread(() -> {
                                bookmarkModel.process("all", BookmarkActivity.this);
                                ToastMgr.shortBottomCenter(getContext(), "????????????" + count + "?????????");
                                EventBus.getDefault().post(new OnBookmarkUpdateEvent());
                            });
                        }
                    }
                }

                @Override
                public void failed(String msg) {
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            ToastMgr.shortBottomCenter(getContext(), "?????????" + msg);
                        });
                    }
                }
            });
        }
    }
}
