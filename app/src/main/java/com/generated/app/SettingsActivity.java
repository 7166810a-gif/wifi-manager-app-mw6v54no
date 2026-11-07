package com.generated.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferenceManager = new PreferenceManager(this);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setBackgroundColor(0xFFF5F5F5);
        rootLayout.setPadding(32, 32, 32, 32);

        // Header Title
        TextView titleView = new TextView(this);
        titleView.setText(R.string.settings_title);
        titleView.setTextSize(22);
        titleView.setTextColor(0xFF212121);
        titleView.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 32);
        titleView.setLayoutParams(titleParams);
        rootLayout.addView(titleView);

        // Checkbox 1: Auto connect favorites
        final CheckBox cbAutoFav = new CheckBox(this);
        cbAutoFav.setText(R.string.auto_connect_favorites);
        cbAutoFav.setTextSize(16);
        cbAutoFav.setTextColor(0xFF333333);
        cbAutoFav.setChecked(preferenceManager.isAutoConnectFavorites());
        cbAutoFav.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cbParams.setMargins(0, 16, 0, 16);
        cbAutoFav.setLayoutParams(cbParams);
        cbAutoFav.setOnCheckedChangeListener((buttonView, isChecked) -> 
            preferenceManager.setAutoConnectFavorites(isChecked)
        );
        rootLayout.addView(cbAutoFav);

        // Checkbox 2: Auto connect open networks
        final CheckBox cbAutoOpen = new CheckBox(this);
        cbAutoOpen.setText(R.string.auto_connect_open);
        cbAutoOpen.setTextSize(16);
        cbAutoOpen.setTextColor(0xFF333333);
        cbAutoOpen.setChecked(preferenceManager.isAutoConnectOpen());
        cbAutoOpen.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        cbAutoOpen.setLayoutParams(cbParams);
        cbAutoOpen.setOnCheckedChangeListener((buttonView, isChecked) -> 
            preferenceManager.setAutoConnectOpen(isChecked)
        );
        rootLayout.addView(cbAutoOpen);

        setContentView(rootLayout);
    }
}
