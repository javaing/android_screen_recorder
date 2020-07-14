package com.arttseng.screenrecorder;

import android.app.Application;
import android.util.ArrayMap;
import android.util.Log;


import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class MyApplication extends Application {

    private static Map<String, Object> dataMap = new ArrayMap<>();
    public static void putData(String key, Object value) {
        dataMap.put(key, value);
    }
    public static Object getData(String key) {
        return dataMap.get(key);
    }

    private static MyApplication _instance;
    public static MyApplication get() {
        return _instance;
    }

    private static OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;

        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
//        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
//            @Override
//            public void onViewInitFinished(boolean arg0) {
//                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
//                Log.e("myApplication", " onViewInitFinished is " + arg0);
//            }
//
//            @Override
//            public void onCoreInitFinished() {
//            }
//        };
//        //x5内核初始化接口
//        QbSdk.initX5Environment(this, cb);

    }
//    Interceptor UserAgentInterc = new Interceptor() {
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//            Request request = chain.request()
//                    .newBuilder()
//                    .removeHeader("User-Agent")//移除旧的
//                    .addHeader("User-Agent",  Tools.getSTUserAgent() )//添加真正的头部
//                    .build();
//            return chain.proceed(request);
//        }
//    };

    public final OkHttpClient getOkHttpClient() {
        if(okHttpClient==null) {
            if(BuildConfig.DEBUG) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.level(HttpLoggingInterceptor.Level.BODY);
                okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .retryOnConnectionFailure(true)
                        //.addInterceptor(UserAgentInterc)
                        .connectTimeout(80, TimeUnit.SECONDS)
                        .readTimeout(80, TimeUnit.SECONDS)
                        .build();
            } else {
                okHttpClient = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        //.addInterceptor(UserAgentInterc)
                        .connectTimeout(80, TimeUnit.SECONDS)
                        .readTimeout(80, TimeUnit.SECONDS)
                        .build();
            }
        }
        return okHttpClient;
    }
}
