package com.example.hikerview.ui.bookmark.service;

import androidx.annotation.Nullable;

import com.annimon.stream.Stream;
import com.example.hikerview.model.Bookmark;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.StringUtil;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2021/6/6
 * 时间：At 12:30
 */

public class ChromeParser {

    public static List<Bookmark> parse(String path) {
        List<Bookmark> bookmarks = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE en-export SYSTEM \"http://xml.evernote.com/pub/evernote-export3.dtd\">\n" +
                "<en-export export-date=\"20130730T205637Z\" application=\"Evernote\" version=\"Evernote Mac\">");
        try {
            Document document = Jsoup.parse(new File(path), "UTF-8");
            Elements elements = document.select("a");
            for (Element el : elements) {
                Bookmark note = parseNote(el);
                bookmarks.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookmarks;
    }

    private static Bookmark parseNote(Element el) {
        Bookmark resultNote = new Bookmark();
        resultNote.setTitle("Empty");
        if (!el.text().isEmpty()) {
            String escaped = StringEscapeUtils.escapeHtml4(el.text());
            if (escaped.length() > 250) { // title must be 250 chars max
                resultNote.setTitle(escaped.subSequence(0, 249).toString());
            } else {
                resultNote.setTitle(escaped);
            }
        }

        String href = el.attr("href");
        if (!href.isEmpty()) {
            resultNote.setUrl(href);
        }
        String creationDate = el.attr("ADD_DATE");
        if (!creationDate.isEmpty()) {
//            resultNote.setCreationDate(creationDate);
        }
        Element parent = el.parent().parent().parent();
        String tag = parent.child(0).text();
        if (!tag.isEmpty()) {
            String group = StringEscapeUtils.escapeHtml4(tag);
            List<String> groups = new ArrayList<>();
            groups.add(group);
            findGrandGroups(parent, groups);
            if (groups.size() == 1 && "Bookmarks".equals(group)) {
                return resultNote;
            }
            resultNote.setGroup(CollectionUtil.listToString(groups, "@@@"));
        }
        return resultNote;
    }

    private static void findGrandGroups(Element parent, List<String> groups) {
        try {
            if (parent.parent() == null || parent.parent().parent() == null) {
                return;
            }
            Element grandParent = parent.parent().parent();
            Element h3 = grandParent.child(0);
            if ("h3".equalsIgnoreCase(h3.tagName())) {
                String tag = h3.text();
                if (StringUtil.isNotEmpty(tag)) {
                    groups.add(0, tag);
                    findGrandGroups(grandParent, groups);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class BookmarkGroupNode {
        public long id;
        String name;
        public List<Bookmark> bookmarks;
        public List<BookmarkGroupNode> childGroups;
    }

    private static void makeSureIdMap(Map<Long, BookmarkGroupNode> groupNodeMap, Bookmark bookmark, BookmarkGroupNode childDir) {
        if (bookmark.isDir()) {
            if (!groupNodeMap.containsKey(bookmark.getId())) {
                BookmarkGroupNode groupNode = new BookmarkGroupNode();
                groupNode.name = bookmark.getTitle();
                groupNode.id = bookmark.getId();
                groupNode.childGroups = new ArrayList<>();
                if (childDir != null) {
                    groupNode.childGroups.add(childDir);
                }
                groupNodeMap.put(bookmark.getId(), groupNode);
                if (bookmark.getParent() != null) {
                    BookmarkGroupNode parentNode = groupNodeMap.get(bookmark.getParentId());
                    if (parentNode != null) {
                        parentNode.childGroups.add(groupNode);
                    }
                }
            } else if (childDir != null) {
                BookmarkGroupNode groupNode = groupNodeMap.get(bookmark.getId());
                if (groupNode != null) {
                    groupNode.childGroups.add(childDir);
                }
            }
        }
        if (bookmark.getParent() != null && !groupNodeMap.containsKey(bookmark.getParent().getId())) {
            makeSureIdMap(groupNodeMap, bookmark.getParent(), groupNodeMap.get(bookmark.getId()));
        }
    }

    public static BookmarkGroupNode toGroupNode(List<Bookmark> bookmarks, @Nullable Bookmark top) {
        BookmarkGroupNode root = new BookmarkGroupNode();
        root.name = "书签栏";
        root.childGroups = new ArrayList<>();
        root.bookmarks = new ArrayList<>();
        Map<Long, BookmarkGroupNode> groupNodeMap = new HashMap<>();
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.isDir()) {
                //文件夹
                makeSureIdMap(groupNodeMap, bookmark, null);
                if (bookmark.getParent() == null) {
                    //顶级文件夹
                    root.childGroups.add(groupNodeMap.get(bookmark.getId()));
                }
            } else if (bookmark.getParent() != null) {
                //有文件夹的书签
                makeSureIdMap(groupNodeMap, bookmark, null);
                BookmarkGroupNode groupNode = groupNodeMap.get(bookmark.getParentId());
                if (groupNode != null) {
                    if (groupNode.bookmarks == null) {
                        groupNode.bookmarks = new ArrayList<>();
                    }
                    groupNode.bookmarks.add(bookmark);
                }
            } else {
                //顶级书签
                root.bookmarks.add(bookmark);
            }
        }
        if (top == null) {
            return root;
        } else {
            return groupNodeMap.get(top.getId());
        }
    }

    private static String buildHtml(BookmarkGroupNode groupNode) {
        String gHead = "<DT><H3>" + groupNode.name + "</H3>\n" + "<DL>\n";
        String gFoot = "\n</DL>\n</DT>";
        List<String> groups = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(groupNode.childGroups)) {
            groups = Stream.of(groupNode.childGroups).map(ChromeParser::buildHtml).toList();
        }
        List<String> list;
        if (CollectionUtil.isNotEmpty(groupNode.bookmarks)) {
            list = Stream.of(groupNode.bookmarks).map(b -> String.format("<DT><A HREF=\"%s\">%s</A></DT>", b.getUrl(), b.getTitle()))
                    .toList();
        } else {
            list = new ArrayList<>();
        }
        return gHead + CollectionUtil.listToString(groups, "\n") + CollectionUtil.listToString(list, "\n") + gFoot;
    }

    public static void exportToFile(List<Bookmark> bookmarks, String filePath) throws IOException {
        String head = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                "<TITLE>Bookmarks</TITLE>\n" +
                "<H1>Bookmarks</H1>\n" +
                "<DL>\n";
        String foot = "\n</DL>";
        BookmarkGroupNode root = toGroupNode(bookmarks, null);
        List<String> contentList = new ArrayList<>();
        for (BookmarkGroupNode childGroup : root.childGroups) {
            contentList.add(buildHtml(childGroup));
        }
        if (CollectionUtil.isNotEmpty(root.bookmarks)) {
            contentList.addAll(Stream.of(root.bookmarks).map(b -> String.format("<DT><A HREF=\"%s\">%s</A></DT>", b.getUrl(), b.getTitle()))
                    .toList());
        }
        String fileContent = head + CollectionUtil.listToString(contentList, "\n") + foot;
        FileUtil.stringToFile(fileContent, filePath);
    }
} 