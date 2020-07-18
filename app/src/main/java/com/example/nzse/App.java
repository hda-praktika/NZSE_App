package com.example.nzse;

import android.app.Application;
import android.content.Context;
import android.renderscript.RenderScript;

public class App extends Application {
    private static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        return sInstance.getApplicationContext();
    }

    public static RenderScript getRenderScript() {
        return sInstance.getOrCreateRenderScript();
    }

    // ---------------------------------

    private RenderScript mRenderScript;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    private synchronized RenderScript getOrCreateRenderScript() {
        if(mRenderScript == null) {
            mRenderScript = RenderScript.create(getContext());
        }
        return mRenderScript;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if(mRenderScript != null) {
            mRenderScript.destroy();
        }
    }
}
