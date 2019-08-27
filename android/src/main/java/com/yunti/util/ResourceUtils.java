package com.yunti.util;


import android.content.Context;
import android.text.TextUtils;

import com.yunti.clickread.FetchInfo;
import com.yunti.clickread.Utils;

import java.io.File;

/**
 * Created by hezhisu on 2017/12/21.
 */

public class ResourceUtils {

    public static String getAudioFilePath(Long crId, Long resId, long userId, Context context) {
        String filePath = getClickReadFilePath(crId, resId, "audio",
                userId, context);
        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
            return filePath;
        }
        return null;
    }

    public static String getImageUri(Long crId, Long resId, String resIdSign, long userId,
                                     Context context) {
        String filePath = getClickReadFilePath(crId, resId, "image", userId, context);
        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
            return filePath;
        } else {
            return getImageUrl(resId, resIdSign);
        }
    }

    private static String getClickReadFilePath(long crId, long resId, String resType, long userId,
                                               Context context) {
        String crDir = getClickReadDir(context, userId, crId);
        if (crDir != null) {
            String dir = Utils.format("%s/%s", crDir, resType);
            String fileExtension = "";
            switch (resType) {
                case "image":
                case "thumbnail":
                    fileExtension = "jpg";
                    break;
                case "audio":
                    fileExtension = "mp3";
                    break;
                case "video":
                    fileExtension = "mp4";
                    break;
            }
            return Utils.format("%s/%d.%s", dir, resId, fileExtension);
        }
        return null;
    }


    public static String getImageUrl(Long resId, String resIdSign) {
        return FetchInfo.HOST +
                String.format("/resourceservice/mediaplay.do?resId=%1$s&resIdSign=%2$s&mediaType=%3$s",
                        resId, resIdSign, 4);
    }

    private static String getClickReadDir(Context context, long userId, long crId) {
        if (context != null) {
            File file = context.getExternalFilesDir(null);
            if (file != null) {
                String externalDirectory = file.getAbsolutePath();
                return Utils.format("%s/%s/clickRead/u%d/%d", externalDirectory,
                        context.getPackageName(), userId, crId);
            }
        }
        return null;
    }

}

