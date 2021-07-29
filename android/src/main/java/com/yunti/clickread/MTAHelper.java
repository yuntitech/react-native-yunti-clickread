package com.yunti.clickread;

import android.content.Context;
import android.util.Log;

import com.yt.ytdeep.client.dto.ClickReadDTO;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @Author: kangqiang
 * @Date: 2019-10-23 16:48
 * @Last Modified by: kangqiang
 * @Last Modified time: 2019-10-23 16:48
 */
public class MTAHelper {

    public static MTAObject bl_008 = new MTAObject("bl_008", "点读书-购买弹窗弹出次数");
    public static MTAObject bl_009 = new MTAObject("bl_009", "点读书-购买弹框点击购买次数");
    public static MTAObject bl_010 = new MTAObject("bl_010", "点读书-购买弹框点击取消次数");
    public static MTAObject bl_011 = new MTAObject("bl_011", "点读书-自动播放按钮点击次数");
    /**
     * 点读书-进入阅读页
     */
    public static String xdfsjj_070 = "xdfsjj_070";
    /**
     * 点读书-进入试读结束页
     */
    public static String xdfsjj_071 = "xdfsjj_071";
    /**
     * 点读书-点击立即购买
     */
    public static String xdfsjj_072 = "xdfsjj_072";
    /**
     * 点读书-点击顶栏购买
     */
    public static String xdfsjj_073 = "xdfsjj_073";

    public static void mtaTrackEvent(Context context, MTAObject mtaObject) {
        if (context == null) {
            return;
        }
        try {
            ZhugeSDK.getInstance().track(context, mtaObject.name, mtaObject.toProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mtaTrackEvent(Context context, String eventName, HashMap<String, Object> params) {
        if (context == null) {
            return;
        }
        try {
            ZhugeSDK.getInstance().track(context, eventName, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class MTAObject {
        private String name;
        private String description;
        private Long id;

        public MTAObject setId(ClickReadDTO clickReadDTO) {
            if (clickReadDTO != null) {
                id = clickReadDTO.getId();
            }
            return this;
        }

        private MTAObject(String name, String description) {
            this.name = name;
            this.description = description;
        }

        protected JSONObject toProperties() throws JSONException {
            JSONObject eventObject = new JSONObject();
            if (id != null) {
                eventObject.put("id ", id);
            }
            eventObject.put("eventDescription", description);
            return eventObject;
        }

    }

}
