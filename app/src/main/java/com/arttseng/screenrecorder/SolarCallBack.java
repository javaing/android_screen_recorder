package com.arttseng.screenrecorder;

import org.json.JSONObject;

public interface SolarCallBack {
    void onOK(JSONObject jsonObject);
    void onErr(String errorMsg);
}
