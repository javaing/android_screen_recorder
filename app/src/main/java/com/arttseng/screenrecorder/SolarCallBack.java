package com.arttseng.screenrecorder;

import org.json.JSONObject;

public interface SolarCallBack {
    void onOK(String jsonObject);
    void onErr(String errorMsg);
}
