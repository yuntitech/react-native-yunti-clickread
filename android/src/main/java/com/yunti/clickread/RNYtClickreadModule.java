
package com.yunti.clickread;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.yt.ytdeep.client.dto.ClickReadTrackinfo;
import com.yunti.clickread.activity.ClickReadActivity;

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

    public static ClickReadActivity activity;

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
        RNYtClickreadModule.activity = null;
        super.onCatalystInstanceDestroy();
        LocalBroadcastManager.getInstance(getReactApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public String getName() {
        return NAME;
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

    public static void push(Context context, String screen, Long bizId, int bizType) {
        Bundle params = new Bundle();
        params.putString("screen", screen);
        params.putLong("bizId", bizId);
        params.putInt("bizType", bizType);
        push(context, params);
    }

    public static void push(Context context, Bundle params) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "push");
        intent.putExtras(params);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void pop(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "pop");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void download(Context context, ClickReadDTO clickReadDTO) {
        if (context == null || clickReadDTO == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "download");
        intent.putExtra("clickReadJSON", JSON.toJSONString(clickReadDTO));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void showVideo(Context context, long crId, ClickReadTrackinfo track) {
        if (context == null || track == null) {
            return;
        }
        Intent intent = new Intent(RNYtClickreadModule.NAME);
        intent.putExtra("action", "video");
        intent.putExtra("crId", crId);
        intent.putExtra("trackJSON", JSON.toJSONString(track));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}