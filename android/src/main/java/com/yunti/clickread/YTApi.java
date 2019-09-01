package com.yunti.clickread;

import android.app.Activity;
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
        Runnable task = () -> {
            String responseData = getApiCache(params, fragment);
            String ldv = null;
            if (!TextUtils.isEmpty(responseData)) {
                JSONObject responseObject = JSON.parseObject(responseData);
                ldv = responseObject.getString("ldv");
                Boolean success = responseObject.getBoolean("success");
                String data = responseObject.getString("data");
                if (Boolean.TRUE.equals(success) && !TextUtils.isEmpty(data)) {
                    T result = (T) JSON.parseObject(data, params.getClazz());
                    onResponseExecute(callback, API_CODE_CACHE, result, fragment);
                } else {
                    String msg = responseObject.getString("msg");
                    onFailureExecute(callback, API_CODE_CACHE, msg, fragment);
                }
            } else {
                onFailureExecute(callback, API_CODE_CACHE, "empty data ", fragment);
            }
            params.addLdv(ldv);
            fetch(params, callback, fragment);
        };
        //当前为主线程
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            OkHttpClientProvider.getOkHttpClient().dispatcher().executorService().submit(task);
        }
        //其他线程
        else {
            task.run();
        }
    }

    public static <T> void loadCache(FetchInfo.FetchInfoParams params, Callback<T> callback,
                                     Fragment fragment) {
        Runnable task = () -> {
            String responseData = getApiCache(params, fragment);
            if (!TextUtils.isEmpty(responseData)) {
                JSONObject responseObject = JSON.parseObject(responseData);
                String ldv = responseObject.getString("ldv");
                Boolean success = responseObject.getBoolean("success");
                String data = responseObject.getString("data");
                if (Boolean.TRUE.equals(success) && !TextUtils.isEmpty(data)) {
                    params.addLdv(ldv);
                    T result = (T) JSON.parseObject(data, params.getClazz());
                    onResponseExecute(callback, API_CODE_CACHE, result, fragment);
                } else {
                    String msg = responseObject.getString("msg");
                    onFailureExecute(callback, API_CODE_CACHE, msg, fragment);
                }
            } else {
                onFailureExecute(callback, API_CODE_CACHE, "empty data ", fragment);
            }
        };
        //当前为主线程
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            OkHttpClientProvider.getOkHttpClient().dispatcher().executorService().submit(task);
        }
        //其他线程
        else {
            task.run();
        }

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
            onFailureExecute(callback, API_CODE_NET, e.getMessage(), fragment);
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
        Boolean success = responseObject.getBoolean("success");
        String data = responseObject.getString("data");
        if (Boolean.TRUE.equals(success) && !TextUtils.isEmpty(data)) {
            if (callback != null) {
                T result = (T) JSON.parseObject(data, params.getClazz());
                onResponseExecute(callback, API_CODE_NET, result, fragment);
            }
            Boolean cacheIsNew = responseObject.getBoolean("cacheIsNew");
            //有新数据
            if (Boolean.FALSE.equals(cacheIsNew)) {
                String ldv = MD5Util.MD5(responseData);
                saveApiCacheIfNeeded(params, responseObject, ldv, fragment);
            }
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

    private static void onFailureExecute(Callback callback, int code, String errorMsg, Fragment fragment) {
        if (callback.runOnUiThread()) {
            runOnUiThread(() -> callback.onFailure(code, errorMsg), fragment);
        } else {
            if (checkActivityValid(fragment)) {
                callback.onFailure(code, errorMsg);
            }
        }
    }

    private static <T> void onResponseExecute(Callback<T> callback, int code, T result, Fragment fragment) {
        if (callback.runOnUiThread()) {
            runOnUiThread(() -> callback.onResponse(code, result), fragment);
        } else {
            if (checkActivityValid(fragment)) {
                callback.onResponse(code, result);
            }
        }
    }

    private static void runOnUiThread(Runnable runnable, Fragment fragment) {
        Activity activity = getSafeActivity(fragment);
        if (activity != null) {
            activity.runOnUiThread(runnable);
        }
    }

    private static boolean checkActivityValid(Fragment fragment) {
        if (fragment == null) {
            return false;
        }
        return fragment.getActivity() != null && !fragment.getActivity().isFinishing();
    }

    private static Activity getSafeActivity(Fragment fragment) {
        if (fragment == null) {
            return null;
        }
        Activity activity = fragment.getActivity();
        return activity != null && !activity.isFinishing() ? activity : null;
    }

    public abstract static class Callback<T> {
        public abstract void onFailure(int code, String errorMsg);

        public abstract void onResponse(int code, T response);

        public boolean runOnUiThread() {
            return true;
        }
    }

}
