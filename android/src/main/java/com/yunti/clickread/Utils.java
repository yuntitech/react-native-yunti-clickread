package com.yunti.clickread;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Formatter;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.Locale;

public class Utils {


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
        return Formatter.formatFileSize(context, sizeBytes);
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

}
