package com.example.hikerview.ui.bookmark.model;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.model.Bookmark;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseModel;
import com.example.hikerview.ui.bookmark.service.ChromeParser;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2019/10/3
 * 时间：At 22:13
 */
public class BookmarkModel extends BaseModel<Bookmark> {
    @Override
    public void process(String actionType, BaseCallback<Bookmark> baseCallback) {
        try {
            HeavyTaskUtil.executeNewTask(() -> {
                migrate(Application.application);
                LitePal.findAllAsync(Bookmark.class).listen(list -> {
                    if (CollectionUtil.isEmpty(list)) {
                        baseCallback.bindArrayToView(actionType, new ArrayList<>());
                    } else {
                        initBookmarkParent(list);
                        baseCallback.bindArrayToView(actionType, list);
                    }
                });
            });
        } catch (Exception e) {
            baseCallback.error(e.getMessage(), e.getMessage(), e.getMessage(), e);
        }
    }

    public static void initBookmarkParent(List<Bookmark> list) {
        Map<Long, Bookmark> map = new HashMap<>();
        for (Bookmark bookmark : list) {
            map.put(bookmark.getId(), bookmark);
        }
        for (Bookmark bookmark : list) {
            if (bookmark.getParentId() > 0) {
                bookmark.setParent(map.get(bookmark.getParentId()));
            }
            bookmark.setDirTag(bookmark.isDir());
        }
    }

    public static void add(Context context, Bookmark bookmark) {
        List<Bookmark> bookmarks = null;
        try {
            bookmarks = LitePal.where("url = ?", bookmark.getUrl()).limit(1).find(Bookmark.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(bookmarks)) {
            bookmarks.get(0).setTitle(bookmark.getTitle());
            bookmarks.get(0).setOrder(bookmark.getOrder());
            bookmarks.get(0).setParentId(bookmark.getParentId());
            bookmarks.get(0).save();
        } else {
            bookmark.save();
        }
    }

    public void delete(Context context, Bookmark bookmark) {
        List<Bookmark> bookmarks = null;
        try {
            bookmarks = LitePal.where("url = ?", bookmark.getUrl()).limit(1).find(Bookmark.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(bookmarks)) {
            bookmarks.get(0).delete();
        }
    }

    public void deleteAll(Context context) {
        try {
            LitePal.deleteAll(Bookmark.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int addByList(Context context, List<Bookmark> bookmarks){
        int count = 0;
        for (Bookmark bookmark : bookmarks) {
            if (StringUtil.isEmpty(bookmark.getUrl())) {
                continue;
            }
            if (StringUtil.isNotEmpty(bookmark.getGroup())) {
                String[] groups = bookmark.getGroup().split("@@@");
                long parentId = -1;
                for (String group : groups) {
                    group = group.replace("/", "-");
                    Bookmark dir;
                    if (parentId > 0) {
                        dir = LitePal.where("title = ? and dir = 1 and parentId = ?", group, String.valueOf(parentId)).findFirst(Bookmark.class);
                    } else {
                        dir = LitePal.where("title = ? and dir = 1 and parentId <= 0", group).findFirst(Bookmark.class);
                    }
                    if (dir == null) {
                        dir = new Bookmark();
                        dir.setTitle(group);
                        if (parentId > 0) {
                            dir.setParentId(parentId);
                        }
                        dir.setDir(true);
                        dir.save();
                    }
                    parentId = dir.getId();
                }
                if (parentId > 0) {
                    bookmark.setParentId(parentId);
                }
            }
            BookmarkModel.add(context, bookmark);
            count++;
        }
        return count;
    }

    public static void deleteByGroupNode(ChromeParser.BookmarkGroupNode groupNode) {
        if (CollectionUtil.isNotEmpty(groupNode.bookmarks)) {
            for (Bookmark bookmark : groupNode.bookmarks) {
                bookmark.delete();
            }
        }
        if (groupNode.id > 0) {
            LitePal.delete(Bookmark.class, groupNode.id);
        }
        if (CollectionUtil.isNotEmpty(groupNode.childGroups)) {
            for (ChromeParser.BookmarkGroupNode childGroup : groupNode.childGroups) {
                deleteByGroupNode(childGroup);
            }
        }
    }

    public static List<Bookmark> sort(List<Bookmark> bookmarks) {
        if (CollectionUtil.isEmpty(bookmarks)) {
            return bookmarks;
        }
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.BOOKMARK_ORDER_KEY).findFirst(BigTextDO.class);
        if (bigTextDO != null) {
            String value = bigTextDO.getValue();
            if (StringUtil.isNotEmpty(value)) {
                Map<String, Integer> orderMap = JSON.parseObject(value, new TypeReference<Map<String, Integer>>() {
                });
                if (!orderMap.isEmpty()) {
                    for (int i = 0; i < bookmarks.size(); i++) {
                        if (orderMap.containsKey(bookmarks.get(i).getUrl())) {
                            bookmarks.get(i).setOrder(orderMap.get(bookmarks.get(i).getUrl()));
                        }
                    }
                }
            }
        }
        Collections.sort(bookmarks);
        return bookmarks;
    }

    public static void migrate(Context context) {
        String key = "migrate-bookmark";
        int oldVersion = PreferenceMgr.getInt(context, key, 0);
        int newVersion = 1;
        if (newVersion > oldVersion && oldVersion == 0) {
            //oldVersion == 0才做迁移
            PreferenceMgr.put(context, key, newVersion);
            List<Bookmark> bookmarks = LitePal.findAll(Bookmark.class);
            if (CollectionUtil.isNotEmpty(bookmarks)) {
                List<String> groups = Stream.of(bookmarks).map(Bookmark::getGroup).filter(StringUtil::isNotEmpty).distinct().collect(Collectors.toList());
                for (String group : groups) {
                    group = group.replace("/", "-");
                    Bookmark dir = new Bookmark();
                    dir.setDir(true);
                    dir.setTitle(group);
                    Bookmark exist = LitePal.where("title = ? and dir = 1", group).findFirst(Bookmark.class);
                    if (exist != null) {
                        //有一个存在，说明就迁移初始化过了
                        return;
                    }
                    dir.save();
                }
                List<Bookmark> dirs = LitePal.where("dir = ?", "1").find(Bookmark.class);
                Map<String, Long> dirMap = new HashMap<>();
                for (Bookmark dir : dirs) {
                    dirMap.put(dir.getTitle(), dir.getId());
                }
                for (Bookmark bookmark : bookmarks) {
                    if (StringUtil.isNotEmpty(bookmark.getGroup()) && dirMap.containsKey(bookmark.getGroup())) {
                        Long id = dirMap.get(bookmark.getGroup());
                        if (id != null) {
                            bookmark.setParentId(id);
                            bookmark.save();
                        }
                    }
                }
            }
        }
    }

    public static String getGroupPath(Bookmark bookmark) {
        String groupPath = "";
        if (bookmark.isDir()) {
            groupPath = bookmark.getTitle();
        }
        Bookmark parent = bookmark.getParent();
        while (parent != null) {
            if (parent.isDir()) {
                if (groupPath.isEmpty()) {
                    groupPath = parent.getTitle();
                } else {
                    groupPath = parent.getTitle() + "/" + groupPath;
                }
            }
            parent = parent.getParent();
        }
        return groupPath;
    }
}
