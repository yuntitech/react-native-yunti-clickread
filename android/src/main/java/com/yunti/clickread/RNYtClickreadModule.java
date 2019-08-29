
package com.yunti.clickread;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.facebook.react.ReactApplication;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.JavaOnlyArray;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactnativecommunity.asyncstorage.AsyncStorageModule;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yunti.clickread.activity.ClickReadActivity;
import com.yunti.clickread.dialog.BottomOptionDialog;

import java.util.List;

public class RNYtClickreadModule extends ReactContextBaseJavaModule {

    private static final String NAME = "RNYtClickread";
    private Class mClass;
    private final ReactApplicationContext reactContext;
    private DeviceEventManagerModule.RCTDeviceEventEmitter mDeviceEventEmitter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NAME.equals(intent.getAction()) && intent.getExtras() != null) {
                mDeviceEventEmitter.emit("nativeConnector",
                        Arguments.fromBundle(intent.getExtras()));
            }
        }
    };


    public RNYtClickreadModule(ReactApplicationContext reactContext, Class clazz) {
        super(reactContext);
        this.reactContext = reactContext;
        mClass = clazz;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.mDeviceEventEmitter
                = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        LocalBroadcastManager.getInstance(getReactApplicationContext())
                .registerReceiver(mBroadcastReceiver, new IntentFilter(NAME));
    }

    @Override
    public void onCatalystInstanceDestroy() {
        LocalBroadcastManager.getInstance(getReactApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
        super.onCatalystInstanceDestroy();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void buySuccess(ReadableMap params) {
        Intent intent = new Intent(ClickReadActivity.NAME);
        intent.putExtra("action", "buySuccess");
        Bundle bundle = Arguments.toBundle(params);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        LocalBroadcastManager.getInstance(getReactApplicationContext()).sendBroadcast(intent);
    }

    @ReactMethod
    public void openClickReadActivity(ReadableMap params) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, mClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            Bundle bundle = Arguments.toBundle(params);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            activity.startActivity(intent);
        }
    }

    @ReactMethod
    public void notifyDownloadStatusChanged() {
        Intent intent = new Intent(ClickReadActivity.NAME);
        intent.putExtra("action", "notifyDownloadStatusChanged");
        LocalBroadcastManager.getInstance(getReactApplicationContext()).sendBroadcast(intent);
    }

    @ReactMethod
    public void notifyDownloadTotalFileCount(double count) {
        Intent intent = new Intent(ClickReadActivity.NAME);
        intent.putExtra("action", "notifyDownloadTotalFileCount");
        intent.putExtra("totalFileCount", Double.valueOf(count).longValue());
        LocalBroadcastManager.getInstance(getReactApplicationContext()).sendBroadcast(intent);
    }

    @ReactMethod
    public void userHasChanged(ReadableMap params) {
        FetchInfo.setHostAndApiCommonParameters(Arguments.toBundle(params));
        Intent intent = new Intent(ClickReadActivity.NAME);
        intent.putExtra("action", "userHasChanged");
        LocalBroadcastManager.getInstance(getReactApplicationContext()).sendBroadcast(intent);
    }

    public static void pushOrderHomeScreen(ClickReadDTO clickReadDTO, Context cxt) {
        if (cxt == null || clickReadDTO == null) {
            return;
        }
        Long clickReadId = clickReadDTO.getId();
        RNYtClickreadModule.push(cxt,
                "cn.bookln.ConfirmOrderHomeScreen", clickReadId, 8);
    }

    public static void pushLoginScreen(Context cxt) {
        if (cxt == null) {
            return;
        }
        Bundle params = new Bundle();
        params.putString("screen", "cn.bookln.LoginScreen");
        params.putString("componentType", "screen");
        RNYtClickreadModule.push(cxt, params);
    }

    private static void startActivity(Context cxt) {
        try {
            Intent intent = new Intent();
            Class<?> clazz = Class.forName("com.reactnativenavigation.controllers.NavigationActivity");
            intent.setClass(cxt, clazz);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            cxt.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void push(Context cxt, String screen, Long bizId, int bizType) {
        Bundle params = new Bundle();
        params.putString("screen", screen);
        params.putLong("bizId", bizId);
        params.putInt("bizType", bizType);
        push(cxt, params);
    }

    public static void push(Context cxt, Bundle params) {
        if (cxt == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "push");
        intent.putExtras(params);
        LocalBroadcastManager.getInstance(cxt.getApplicationContext()).sendBroadcast(intent);
        startActivity(cxt);
    }

    public static void pop(Context cxt) {
        if (cxt == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "pop");
        LocalBroadcastManager.getInstance(cxt.getApplicationContext()).sendBroadcast(intent);
    }

    public static void download(Context cxt, ClickReadDTO clickReadDTO) {
        if (cxt == null || clickReadDTO == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "download");
        intent.putExtra("clickReadJSON", JSON.toJSONString(clickReadDTO));
        LocalBroadcastManager.getInstance(cxt.getApplicationContext()).sendBroadcast(intent);
    }

    public static void pauseDownload(Context cxt, long crId) {
        if (cxt == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "pauseDownload");
        intent.putExtra("crId", crId);
        LocalBroadcastManager.getInstance(cxt.getApplicationContext()).sendBroadcast(intent);
    }

    public static void removeDownload(Context cxt, long crId) {
        if (cxt == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "removeDownload");
        intent.putExtra("crId", crId);
        LocalBroadcastManager.getInstance(cxt.getApplicationContext()).sendBroadcast(intent);
    }

    public static void showVideo(Context cxt, long crId, ClickReadTrackinfo track) {
        if (cxt == null || track == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "video");
        intent.putExtra("crId", crId);
        intent.putExtra("trackJSON", JSON.toJSONString(track));
        LocalBroadcastManager.getInstance(cxt.getApplicationContext()).sendBroadcast(intent);
    }

    public static void joinBookShelfSuccess(Context cxt) {
        if (cxt == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "joinBookShelfSuccess");
        LocalBroadcastManager.getInstance(cxt.getApplicationContext()).sendBroadcast(intent);
    }

    public static void showToast(Context context, int message) {
        if (context == null) {
            return;
        }
        showToast(context, context.getString(message));
    }

    public static void showToast(Context context, String message) {
        if (context == null) {
            return;
        }
        context = context.getApplicationContext();
        Toast toast = Toast.makeText(context,
                message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void alert(Fragment fragment,
                             MaterialDialog.SingleButtonCallback positiveCallback, int content) {
        if (fragment == null) {
            return;
        }
        alert(fragment, positiveCallback, fragment.getString(content), "确定");
    }

    public static void guestAlert(Fragment fragment) {
        alert(fragment,
                (dialog, which) -> RNYtClickreadModule.pushLoginScreen(fragment.getContext()),
                "您需要登录后使用该功能", "登录");
    }

    public static void alert(Fragment fragment,
                             MaterialDialog.SingleButtonCallback positiveCallback, String content,
                             String positiveText) {
        Activity activity = fragment.getActivity();
        if (activity != null && !activity.isFinishing()) {
            new MaterialDialog.Builder(activity)
                    .title("提示")
                    .content(content)
                    .positiveText(positiveText)
                    .negativeText("取消")
                    .onPositive(positiveCallback)
                    .show();
        }
    }

    public static void getStorageItem(Context context,
                                      String key, final Callback callback, Fragment fragment) {
        JavaOnlyArray params = new JavaOnlyArray();
        params.pushString(key);
        getStorageItem(context, params, callback, fragment);
    }

    public static void setStorageItem(Context context, String key, String value) {
        JavaOnlyArray params = new JavaOnlyArray();
        JavaOnlyArray keyValueArray = new JavaOnlyArray();
        keyValueArray.pushString(key);
        keyValueArray.pushString(value);
        params.pushArray(keyValueArray);
        setStorageItem(context, params);
    }

    private static void getStorageItem(Context context,
                                       final ReadableArray keys, final Callback callback,
                                       Fragment fragment) {
        if (context == null || callback == null) {
            return;
        }
        context = context.getApplicationContext();
        if (context instanceof ReactApplication) {
            ReactContext reactContext = ((ReactApplication) context).
                    getReactNativeHost().getReactInstanceManager().getCurrentReactContext();
            if (reactContext != null) {
                AsyncStorageModule storageModule = reactContext.getNativeModule(AsyncStorageModule.class);
                if (storageModule != null) {
                    storageModule.multiGet(keys, args -> {
                        List<Object> argsList = Arguments.fromJavaArgs(args).toArrayList();
                        List<Object> resultList = (List<Object>) argsList.get(1);
                        if (resultList != null) {
                            resultList = (List<Object>) resultList.get(0);
                            Object value = resultList.get(1);
                            runOnUiThread(fragment, () ->
                                    callback.resolve(value != null ? value.toString() : null));

                        } else {
                            resultList = (List<Object>) argsList.get(0);
                            Object error = resultList.get(0);
                            runOnUiThread(fragment, () ->
                                    callback.reject(error != null ? error.toString() : null));
                        }
                    });
                }
            }
        }
    }

    private static void setStorageItem(Context context,
                                       final ReadableArray keyValueArray) {
        if (context == null) {
            return;
        }
        context = context.getApplicationContext();
        if (context instanceof ReactApplication) {
            ReactContext reactContext = ((ReactApplication) context).
                    getReactNativeHost().getReactInstanceManager().getCurrentReactContext();
            if (reactContext != null) {
                AsyncStorageModule storageModule = reactContext.getNativeModule(AsyncStorageModule.class);
                if (storageModule != null) {
                    storageModule.multiSet(keyValueArray, args -> {
                    });
                }
            }
        }
    }

    public static void showBottomSheet(int[] options, BottomSheetCallback firstCallback,
                                       Fragment fragment) {
        showBottomSheet(options, firstCallback, null, fragment);
    }

    public static void showBottomSheet(int[] options, BottomSheetCallback firstCallback,
                                       BottomSheetCallback secondCallback,
                                       Fragment fragment) {
        if (fragment == null
                || fragment.getActivity() == null
                || fragment.getActivity().isFinishing()
                || options.length == 0
        ) {
            return;
        }
        BottomOptionDialog dialog = new BottomOptionDialog(fragment.getActivity());
        dialog.setOptions(options);
        dialog.addOnOptionListener(position -> {
            switch (position) {
                case 0:
                    firstCallback.onClick();
                    break;
                case 1:
                    if (secondCallback != null) {
                        secondCallback.onClick();
                    }
                    break;
            }
        });
        dialog.show();
    }

    private static boolean checkValid(Fragment fragment) {
        return fragment != null
                && fragment.getActivity() != null
                && !fragment.getActivity().isFinishing();
    }

    private static void runOnUiThread(Fragment fragment, Runnable runnable) {
        if (fragment.getActivity() != null) {
            fragment.getActivity().runOnUiThread(runnable);
        }
    }

    public interface BottomSheetCallback {
        void onClick();
    }

    public interface Callback {
        void reject(String error);

        void resolve(String result);
    }
}