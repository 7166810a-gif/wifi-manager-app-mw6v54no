package com.generated.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class PreferenceManager {
    private static final String PREF_NAME = "wifi_app_prefs";
    private static final String KEY_AUTO_FAV = "auto_connect_fav";
    private static final String KEY_AUTO_OPEN = "auto_connect_open";
    private static final String KEY_FAV_NETWORKS = "favorite_networks";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAutoConnectFavorites() {
        return prefs.getBoolean(KEY_AUTO_FAV, true);
    }

    public void setAutoConnectFavorites(boolean value) {
        prefs.edit().putBoolean(KEY_AUTO_FAV, value).apply();
    }

    public boolean isAutoConnectOpen() {
        return prefs.getBoolean(KEY_AUTO_OPEN, false);
    }

    public void setAutoConnectOpen(boolean value) {
        prefs.edit().putBoolean(KEY_AUTO_OPEN, value).apply();
    }

    public Set<String> getFavoriteNetworks() {
        return prefs.getStringSet(KEY_FAV_NETWORKS, new HashSet<String>());
    }

    public void addFavoriteNetwork(String ssid) {
        Set<String> favs = new HashSet<>(getFavoriteNetworks());
        favs.add(ssid);
        prefs.edit().putStringSet(KEY_FAV_NETWORKS, favs).apply();
    }

    public void removeFavoriteNetwork(String ssid) {
        Set<String> favs = new HashSet<>(getFavoriteNetworks());
        favs.remove(ssid);
        prefs.edit().putStringSet(KEY_FAV_NETWORKS, favs).apply();
    }

    public boolean isFavorite(String ssid) {
        return getFavoriteNetworks().contains(ssid);
    }
}
