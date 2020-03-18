package com.yunti.clickread;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Formatter;

import androidx.fragment.app.Fragment;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {

    private static final int STRING_BUILDER_SIZE = 256;
    private static final String EMPTY = "";

    public static <O> List<O> subList(List<O> all, int start, int end) {
        List<O> result = new ArrayList<>();
        for (int n = start; n < end; n++) {
            result.add(all.get(n));
        }
        return result;
    }

    public static String fileSize(Long sizeBytes, Fragment fragment) {
        if (sizeBytes == null) {
            return null;
        }
        return fileSize(sizeBytes, fragment.getContext());
    }

    public static String fileSize(Long sizeBytes, Context context) {
        if (sizeBytes == null) {
            return null;
        }

        String fileSize = Formatter.formatFileSize(context, sizeBytes);
        if (!TextUtils.isEmpty(fileSize)) {
            fileSize = fileSizeConvert(fileSize);
        }
        return fileSize;
    }

    public static String fen2Yuan(String fen) throws NumberFormatException {
        if (TextUtils.isEmpty(fen) || "null".equals(fen)) {
            return "";
        }
        try {
            return fen2Yuan(Float.valueOf(fen));
        } catch (Exception e) {
            //ignore
        }
        return "";
    }


    public static String format(String format, Object... args) {
        return String.format(Locale.CHINA, format, args);
    }

    public static long getDirFileCount(String dir) {
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            return file.list().length;
        }
        return 0;
    }

    private static String fen2Yuan(float fen) {
        float yuan = fen / 100;
        return String.format(Locale.CHINA, "%.2f", yuan);
    }

    private static String fileSizeConvert(String fileSize) {
        Map<String, String> fileSizeMap = new HashMap<>();
        fileSizeMap.put("吉字节", "GB");
        fileSizeMap.put("兆字节", "MB");
        fileSizeMap.put("千字节", "KB");
        for (Map.Entry<String, String> entry : fileSizeMap.entrySet()) {
            fileSize = fileSize.replace(entry.getKey(), entry.getValue());
        }
        return fileSize;
    }


    static String fromParameterMap(Map<String, Object> parameterMap) {
        List<String> concatenatedList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            concatenatedList.add(entry.getKey() + "=" + entry.getValue());
        }
        return StringUtils.join(concatenatedList, "&");
    }

}
