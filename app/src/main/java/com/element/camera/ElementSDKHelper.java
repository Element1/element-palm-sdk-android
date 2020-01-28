package com.element.camera;

import android.content.Context;

public class ElementSDKHelper {

    public static boolean isEnrolled(Context var0, String var1) {
        return ProviderUtil.isUserEnrolled(var0, var1, (BaseModelMeta) CameraInternal.META_MAP.get("palm"));
    }

}
