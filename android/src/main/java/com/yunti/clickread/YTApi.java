package com.yunti.clickread;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqtouch.tool.MD5Util;
import com.facebook.react.modules.network.OkHttpClientProvider;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/*
 * @Author: kangqiang
 * @Date: 2019-08-22 10:38
 * @Last Modified by: kangqiang
 * @Last Modified time: 2019-08-22 10:38
 */
public class YTApi {

    public static final int API_CODE_CACHE = -1;
    public static final int API_CODE_NET = 0;


    public static <T> void loadCacheAndFetch(FetchInfo.FetchInfoParams params, Callback<T> callback,
                                             Fragment fragment) {
        OkHttpClientProvider.getOkHttpClient().dispatcher().executorService().submit(() -> {
            String responseData = getApiCache(params, fragment);
            String ldv = null;
            if (!TextUtils.isEmpty(responseData)) {
                JSONObject responseObject = JSON.parseObject(responseData);
                ldv = responseObject.getString("ldv");
                boolean success = responseObject.getBoolean("success");
                if (success) {
                    String data = responseObject.getString("data");
                    T result = (T) JSON.parseObject(data, params.getClazz());
                    runOnUiThread(() -> callback.onResponse(API_CODE_CACHE, result), fragment);
                } else {
                    String msg = responseObject.getString("msg");
                    runOnUiThread(() -> callback.onFailure(API_CODE_CACHE, msg), fragment);
                }
            } else {
                runOnUiThread(() -> callback.onFailure(API_CODE_CACHE, "empty data "), fragment);
            }
            params.addLdv(ldv);
            fetch(params, callback, fragment);
        });
    }

    public static <T> void loadCache(FetchInfo.FetchInfoParams params, Callback<T> callback,
                                     Fragment fragment) {
        OkHttpClientProvider.getOkHttpClient().dispatcher().executorService().submit(() -> {
            String responseData = getApiCache(params, fragment);
            if (!TextUtils.isEmpty(responseData)) {
                JSONObject responseObject = JSON.parseObject(responseData);
                boolean success = responseObject.getBoolean("success");
                if (success) {
                    String data = responseObject.getString("data");
                    T result = (T) JSON.parseObject(data, params.getClazz());
                    runOnUiThread(() -> callback.onResponse(API_CODE_CACHE, result), fragment);
                } else {
                    String msg = responseObject.getString("msg");
                    runOnUiThread(() -> callback.onFailure(API_CODE_CACHE, msg), fragment);
                }
            } else {
                runOnUiThread(() -> callback.onFailure(API_CODE_CACHE, "empty data "), fragment);
            }
        });

    }

    public static <T> void fetch(FetchInfo.FetchInfoParams params, Callback<T> callback,
                                 Fragment fragment) {
        Request request = new Request.Builder()
                .post(params.getFormBody())
                .url(params.getUrl())
                .build();
        //当前为主线程
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            OkHttpClientProvider.getOkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    onFetchFailure(e, callback, fragment);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    onFetchResponse(response, params, callback, fragment);
                }
            });
        }
        //其他线程
        else {
            try {
                Response response = OkHttpClientProvider.getOkHttpClient().newCall(request).execute();
                onFetchResponse(response, params, callback, fragment);
            } catch (IOException e) {
                e.printStackTrace();
                onFetchFailure(e, callback, fragment);
            }
        }

    }

    private static <T> void onFetchFailure(IOException e, Callback<T> callback, Fragment fragment) {
        if (callback != null) {
            runOnUiThread(() -> callback.onFailure(API_CODE_NET, e.getMessage()), fragment);
        }
    }

    private static <T> void onFetchResponse(Response response,
                                            FetchInfo.FetchInfoParams params,
                                            Callback<T> callback, Fragment fragment) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        String responseData = response.body().string();
        JSONObject responseObject = JSON.parseObject(responseData);
        boolean success = responseObject.getBoolean("success");
        if (success) {
            if (callback != null) {
                String data = responseObject.getString("data");
                T result = (T) JSON.parseObject(data, params.getClazz());
                runOnUiThread(() -> callback.onResponse(API_CODE_NET, result), fragment);
            }
            String ldv = MD5Util.MD5(responseData);
            saveApiCacheIfNeeded(params, responseObject, ldv, fragment);
        } else {
            onFetchFailure(new IOException(responseObject.getString("msg")), callback, fragment);
        }
    }

    private static String getApiCache(FetchInfo.FetchInfoParams infoParams, Fragment fragment) {
        if (fragment == null
                || fragment.getContext() == null
        ) {
            return null;
        }
        Context context = fragment.getContext().getApplicationContext();
        String filePath = getApiCacheFilePath(infoParams, context);
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return FileUtils.readFileToString(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void saveApiCacheIfNeeded(FetchInfo.FetchInfoParams infoParams,
                                             JSONObject responseObject, String ldv, Fragment fragment) {
        if (fragment == null
                || fragment.getContext() == null
        ) {
            return;
        }
        Context context = fragment.getContext().getApplicationContext();
        String dir = getApiCacheFileDir(context);
        try {
            responseObject.put("ldv", ldv);
            FileUtils.forceMkdir(new File(dir));
            String filePath = getApiCacheFilePath(infoParams, context);
            FileUtils.write(new File(filePath), responseObject.toJSONString(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getApiCacheFilePath(FetchInfo.FetchInfoParams infoParams, Context context) {
        return Utils.format("%s/%s", getApiCacheFileDir(context), getApiCacheFileName(infoParams));
    }

    private static String getApiCacheFileName(FetchInfo.FetchInfoParams infoParams) {
        // 服务器地址 + 请求地址 + 请求参数 + 缓存自定义key + 缓存userId
        String params = Utils.format("%s_%s_%s", FetchInfo.HOST, infoParams.getAction(),
                infoParams.getFetchParams());
        return MD5Util.MD5(params).toLowerCase();
    }

    private static String getApiCacheFileDir(Context context) {
        File externalDirectory = context.getExternalFilesDir(null);
        if (externalDirectory != null) {
            String externalDirectoryPath = externalDirectory.getAbsolutePath();
            if (!TextUtils.isEmpty(externalDirectoryPath)) {
                return Utils.format("%s/%s/networking", externalDirectory, context.getPackageName());
            }

        }
        return null;
    }

    private static void runOnUiThread(Runnable runnable, Fragment fragment) {
        if (fragment != null
                && fragment.getActivity() != null
                && !fragment.getActivity().isFinishing()
        ) {
            fragment.getActivity().runOnUiThread(runnable);
        }
    }


    public interface Callback<T> {
        void onFailure(int code, String errorMsg);

        void onResponse(int code, T response);
    }

}
