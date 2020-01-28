package com.element.palm.sample;

import android.app.Application;

import com.element.camera.ElementPalmSDK;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElementPalmSDK.initSDK(this);
    }
}
