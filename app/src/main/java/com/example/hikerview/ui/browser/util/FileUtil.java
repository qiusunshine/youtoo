package com.example.hikerview.ui.browser.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

/**
 * @author fisher
 * @description 文件工具类
 */

public class FileUtil {

    /**
     * 根据传入的文件全路径，返回文件所在路径
     *
     * @param fullPath 文件全路径
     * @return 文件所在路径
     */
    public static String getDir(String fullPath) {
        int iPos1 = fullPath.lastIndexOf("/");
        int iPos2 = fullPath.lastIndexOf("\\");
        iPos1 = (iPos1 > iPos2 ? iPos1 : iPos2);
        return fullPath.substring(0, iPos1 + 1);
    }

    /**
     * 根据传入的文件全路径，返回文件全名（包括后缀名）
     *
     * @param fullPath 文件全路径
     * @return 文件全名（包括后缀名）
     */
    public static String getFileName(String fullPath) {
        int iPos1 = fullPath.lastIndexOf("/");
        int iPos2 = fullPath.lastIndexOf("\\");
        iPos1 = (iPos1 > iPos2 ? iPos1 : iPos2);
        return fullPath.substring(iPos1 + 1);
    }

    /**
     * 获得文件名fileName中的后缀名
     *
     * @param fileName 源文件名
     * @return String 后缀名
     */
    public static String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1,
                fileName.length());
    }

    /**
     * 根据传入的文件全名（包括后缀名）或者文件全路径返回文件名（没有后缀名）
     *
     * @param fullPath 文件全名（包括后缀名）或者文件全路径
     * @return 文件名（没有后缀名）
     */
    public static String getPureFileName(String fullPath) {
        String fileFullName = getFileName(fullPath);
        return fileFullName.substring(0, fileFullName.lastIndexOf("."));
    }

    /**
     * 转换文件路径中的\\为/
     *
     * @param filePath 要转换的文件路径
     * @return String
     */
    public static String wrapFilePath(String filePath) {
        filePath.replace('\\', '/');
        if (filePath.charAt(filePath.length() - 1) != '/') {
            filePath += "/";
        }
        return filePath;
    }

    public static String getFormatedFileSize(long size) {
        String result;
        if (size == 0) {
            return "0B";
        }
        DecimalFormat df = new DecimalFormat("#.00");
        if (size < 1024) {
            result = df.format((double) size) + "B";
        } else if (size < 1048576) {
            result = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            result = df.format((double) size / 1048576) + "MB";
        } else {
            result = df.format((double) size / 1073741824) + "GB";
        }
        return result;
    }

    public static String getExtension(String fileName) {
        if (fileName.lastIndexOf(".") < 1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    public static String getName(String fileName) {
        if (fileName.lastIndexOf(".") < 1) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static long getFolderSize(File file) {
        long size = 0;
        File[] fileList = file.listFiles();
        for (File aFileList : fileList) {
            if (aFileList.isDirectory()) {
                size = size + getFolderSize(aFileList);
            } else {
                size = size + aFileList.length();
            }
        }
        return size;
    }

    public static String fileNameFilter(String fileName) {
        Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|.]");
        return fileName == null ? null : FilePattern.matcher(fileName).replaceAll("");
    }
}
