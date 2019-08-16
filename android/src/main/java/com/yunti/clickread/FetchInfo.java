package com.yunti.clickread;

import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.yunti.util.MD5Util;

import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;

public class FetchInfo {

    static String HOST = "https://app.bookln.cn";
    public static long USER_ID = 25;
    static Map<String, Object> apiCommonParameters;

    static {
        apiCommonParameters = new HashMap<>();
        apiCommonParameters.put("_appn", "miniApp");
        apiCommonParameters.put("_appv", 81);
        apiCommonParameters.put("_appt", "2");
        apiCommonParameters.put("_channel", "wxMinApp");
//        apiCommonParameters.put("_channel", "yuntiguanwang");
    }

    public static FetchInfoParams queryByBookId(Long bookId) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("bookId", bookId);
        return new FetchInfoParams("/clickreadservice/querybybookid.do",
                buildFormBody("/clickreadservice/querybybookid.do", dataMap));
    }

    public static FetchInfoParams playV3(Long resId, String resIdSign) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("resId", resId);
        dataMap.put("resIdSign", resIdSign);
        dataMap.put("act", "play");
        return new FetchInfoParams("/resourceservice/play/v3.do",
                buildFormBody("/resourceservice/play/v3.do", dataMap));
    }


    public static class FetchInfoParams {
        private FormBody mFormBody;
        private String mAction;

        FetchInfoParams(String action, FormBody formBody) {
            mAction = action;
            mFormBody = formBody;
        }

        public String getUrl() {
            return HOST + mAction;
        }

        public FormBody getFormBody() {
            return mFormBody;
        }
    }

    private static FormBody buildFormBody(String action, Map<String, Object> dataMap) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> parameter : apiCommonParameters.entrySet()) {
            if (parameter.getKey() == null || parameter.getValue() == null) {
                throw new IllegalArgumentException("parameter cannot be null");
            }
            String name = parameter.getKey().trim();
            String value = parameter.getValue().toString().trim();
            builder.add(name, value);
        }
        builder.add("_data", JSON.toJSONString(dataMap));
        builder.add("_sign", sign(action, dataMap));
        return builder.build();

    }


    public static void setHostAndApiCommonParameters(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        String host = bundle.getString("host");
        if (host != null) {
            HOST = host;
        }
        long userId = getLongValue(bundle, "userId");
        if (userId > 0) {
            USER_ID = userId;
        }
        Bundle apiCommonBundle = bundle.getBundle("apiCommonParameters");
        if (apiCommonBundle != null) {
            apiCommonParameters.put("_appv", getLongValue(apiCommonBundle, "_appv"));
            apiCommonParameters.put("_appt", apiCommonBundle.getString("_appt"));
            apiCommonParameters.put("_templateid", getLongValue(apiCommonBundle, "_templateid"));
            apiCommonParameters.put("_appid", apiCommonBundle.getString("_appid"));
            apiCommonParameters.put("_userId", getLongValue(apiCommonBundle, "_userId"));

        }
    }


    private static String sign(String action, Map<String, Object> dataMap) {
        String signParam = "00000";
        String tid = (String) apiCommonParameters.get("_tid");
        if (!TextUtils.isEmpty(tid)) {
            signParam = "" + tid.charAt(1) +
                    tid.charAt(3) +
                    tid.charAt(6) +
                    tid.charAt(2) +
                    tid.charAt(4)
            ;
        }
        signParam = signParam + apiCommonParameters.get("_appv") + "153158E" + action;
        signParam = MD5Util.MD5(signParam);
        if (dataMap != null) {
            signParam = signParam + JSON.toJSONString(dataMap);
        }
        String md5Val = MD5Util.MD5(signParam);
        String sourceSign = md5Val.substring(10, 16);
        return sourceSign;
    }

    private static long getLongValue(Bundle bundle, String key) {
        Double value = bundle.getDouble(key);
        return value.longValue();
    }

}
