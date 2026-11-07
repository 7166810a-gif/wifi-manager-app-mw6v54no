package com.generated.app;

import android.content.Context;
import android.net.wifi.WifiManager;

public class ApplicationContextHelper {
    public static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
}
