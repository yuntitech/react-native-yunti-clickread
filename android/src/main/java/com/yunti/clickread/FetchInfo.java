package com.yunti.clickread;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cqtouch.entity.BaseType;
import com.yt.ytdeep.client.dto.BuyResultDTO;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ResPlayDTO;
import com.yunti.util.MD5Util;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.FormBody;

public class FetchInfo {

    public static String HOST = "https://app.bookln.cn";
    public static long USER_ID = 25;
    static Map<String, Object> apiCommonParameters;

    private static final String QUERY_BY_BOOK_ID = "/clickreadservice/querybybookid.do";
    private static final String IS_BUY = "/userorderservice/isbuy/v2.do";
    private static final String ADD_TO_MY_BOOKS = "/userbooksservice/addtomybooks.do";
    private static String yuntiAppToken = "";


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
        private String mLdv;

        FetchInfoParams(String action, Map<String, Object> fetchParams, Class clazz) {
            mAction = action;
            mClass = clazz;
            mFetchParams = JSON.toJSONString(fetchParams);
            mFormBodyBuilder = new FormBody.Builder();
            fetchParams = new HashMap<>();
            addCommonParameters(fetchParams);
            mFormBodyBuilder.add("_data", mFetchParams);
            fetchParams.put("_data", mFetchParams);
            if (this.mLdv != null) {
                fetchParams.put("_ldv", this.mLdv);
            }
            SignV2 signV2 = new SignV2(System.currentTimeMillis(), UUID.randomUUID().toString(),
                    fetchParams);
            if (signV2.getSign() != null) {
                mFormBodyBuilder.add("_timestamp", signV2.getTimestamp());
                mFormBodyBuilder.add("_nonce", signV2.getNonce());
                mFormBodyBuilder.add("_sign", signV2.getSign());
            }
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
                this.mLdv = ldv;
                mFormBodyBuilder.add("_ldv", ldv);
            }
        }

        public String getFetchParams() {
            return mFetchParams;
        }

        private void addCommonParameters(Map<String, Object> fetchParams) {
            for (Map.Entry<String, Object> parameter : apiCommonParameters.entrySet()) {
                if (parameter.getKey() == null || parameter.getValue() == null) {
                    continue;
                }
                String name = parameter.getKey().trim();
                String value = parameter.getValue().toString().trim();
                mFormBodyBuilder.add(name, value);
                fetchParams.put(name, value);
            }
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
            apiCommonParameters.put("_appid", getLongValue(apiCommonBundle, "_appid"));
            apiCommonParameters.put("_userId", getLongValue(apiCommonBundle, "_userId"));
            apiCommonParameters.put("_tid", apiCommonBundle.getString("_tid"));
        }
        yuntiAppToken = bundle.getString("yuntiAppToken");
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
        signParam = MD5Util.MD5(signParam).toLowerCase();
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

    private static final String ALGORITHM = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    private static String signV2(Map<String, Object> parameterMap) {
        if (yuntiAppToken == null) {
            return null;
        }
        parameterMap = new TreeMap<>(parameterMap);
        String concatenatedString = Utils.fromParameterMap(parameterMap);
        String encodedStringToSign = Uri.encode(concatenatedString);
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(yuntiAppToken.getBytes(ENCODING), ALGORITHM));
            byte[] signData = mac.doFinal(encodedStringToSign.getBytes(ENCODING));
            return new String(Base64.encodeBase64(signData));
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            Log.d("calculateSignV2 error", e.getMessage());
            return null;
        }
    }

    static class SignV2 {
        private String timestamp;
        private String nonce;
        private String sign;

        SignV2(long timestamp, String nonce, Map<String, Object> fetchParams) {
            this.timestamp = String.valueOf(timestamp);
            this.nonce = nonce;
            fetchParams.put("_timestamp", this.timestamp);
            fetchParams.put("_nonce", this.nonce);
            this.sign = signV2(fetchParams);
        }

        String getTimestamp() {
            return timestamp;
        }

        String getNonce() {
            return nonce;
        }

        String getSign() {
            return sign;
        }
    }

}
