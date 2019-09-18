package com.yunti.clickread;

import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.cqtouch.entity.BaseType;
import com.yt.ytdeep.client.dto.BuyResultDTO;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ResPlayDTO;
import com.yunti.util.MD5Util;

import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;

public class FetchInfo {

    public static String HOST = "https://app.bookln.cn";
    public static long USER_ID = 25;
    static Map<String, Object> apiCommonParameters;

    private static final String QUERY_BY_BOOK_ID = "/clickreadservice/querybybookid.do";
    private static final String IS_BUY = "/userorderservice/isbuy/v2.do";
    private static final String ADD_TO_MY_BOOKS = "/userbooksservice/addtomybooks.do";


    static {
        apiCommonParameters = new HashMap<>();
        apiCommonParameters.put("_appn", "miniApp");
        apiCommonParameters.put("_appv", 81);
        apiCommonParameters.put("_appt", "2");
        apiCommonParameters.put("_channel", "wxMinApp");
//        apiCommonParameters.put("_channel", "yuntiguanwang");
    }

    public static FetchInfoParams joinBookShelf(long bookId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", bookId);
        return new FetchInfoParams(ADD_TO_MY_BOOKS, params, BaseType.class);
    }

    public static FetchInfoParams isBuy(long crId, int type) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", crId);
        params.put("type", type);
        params.put("user_id", USER_ID);
        return new FetchInfoParams(IS_BUY, params, BuyResultDTO.class);
    }

    public static FetchInfoParams queryByBookId(Long bookId) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("bookId", bookId);
        return new FetchInfoParams(QUERY_BY_BOOK_ID, dataMap, ClickReadDTO.class);
    }

    public static FetchInfoParams playV3(Long resId, String resIdSign) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("resId", resId);
        dataMap.put("resIdSign", resIdSign);
        dataMap.put("act", "play");
        return new FetchInfoParams("/resourceservice/play/v3.do", dataMap, ResPlayDTO.class);
    }


    public static class FetchInfoParams {
        private FormBody.Builder mFormBodyBuilder;
        private String mAction;
        private Class mClass;
        private String mFetchParams;

        FetchInfoParams(String action, Map<String, Object> fetchParams, Class clazz) {
            mAction = action;
            mClass = clazz;
            mFormBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, Object> parameter : apiCommonParameters.entrySet()) {
                if (parameter.getKey() == null || parameter.getValue() == null) {
                    continue;
                }
                String name = parameter.getKey().trim();
                String value = parameter.getValue().toString().trim();
                mFormBodyBuilder.add(name, value);
            }
            mFetchParams = JSON.toJSONString(fetchParams);
            mFormBodyBuilder.add("_data", mFetchParams);
            mFormBodyBuilder.add("_sign", sign(action, fetchParams));
        }

        public String getUrl() {
            return HOST + mAction;
        }

        public String getAction() {
            return mAction;
        }

        public FormBody getFormBody() {
            return mFormBodyBuilder.build();
        }

        public Class getClazz() {
            return mClass;
        }

        public void addLdv(String ldv) {
            if (ldv != null) {
                mFormBodyBuilder.add("_ldv", ldv);
            }
        }

        public String getFetchParams() {
            return mFetchParams;
        }
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
            apiCommonParameters.put("_tid", apiCommonBundle.getString("_tid"));
        }
    }

    public static boolean isGuest() {
        return Long.valueOf(25L).equals(USER_ID);
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
