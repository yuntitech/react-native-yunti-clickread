package com.yunti.util;


import java.util.HashMap;

/**
 * Created by hezhisu on 2017/12/21.
 */

public class ResourceUrlUtils {

    public static HashMap<String, String> hostMap = new HashMap<>();

    public static String getTrackUrl(Long resId, String resIdSign) {
        return getHost()
                + String.format("/resourceservice/mediaplay.do?resId=%1$s&resIdSign=%2$s&mediaType=%3$s", resId, resIdSign, 3);
    }

    public static String getImageUrl(Long resId, String resIdSign) {
        return getHost()
                + String.format("/resourceservice/mediaplay.do?resId=%1$s&resIdSign=%2$s&mediaType=%3$s", resId, resIdSign, 4);
    }


    private static String getHost() {
        return "http://app.bookln.cn";
    }

}

