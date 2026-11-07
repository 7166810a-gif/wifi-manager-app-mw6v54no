package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private WifiManager wifiManager;
    private PreferenceManager preferenceManager;
    private WifiScanReceiver wifiScanReceiver;

    private TextView tvWifiStatus;
    private TextView tvCurrentNetwork;
    private LinearLayout layoutNetworkList;
    private Button btnToggleWifi;
    private Button btnScan;
    private Button btnSettings;

    private List<ScanResult> scanResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiManager = (ApplicationContextHelper.getWifiManager(this));
        preferenceManager = new PreferenceManager(this);

        buildUI();

        wifiScanReceiver = new WifiScanReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);

        checkPermissionsAndScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiScanReceiver != null) {
            unregisterReceiver(wifiScanReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWifiStateUI();
        updateCurrentConnectedNetwork();
    }

    private void buildUI() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(0xFFF5F5F5);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rootLayout.setPadding(32, 32, 32, 32);

        // Top Bar (Title + Settings Button)
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText(R.string.app_name);
        titleView.setTextSize(22);
        titleView.setTextColor(0xFF1976D2);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        titleView.setLayoutParams(titleParams);
        titleView.setGravity(Gravity.RIGHT);
        topBar.addView(titleView);

        btnSettings = new Button(this);
        btnSettings.setText(R.string.settings_title);
        btnSettings.setTextColor(0xFF1976D2);
        btnSettings.setBackgroundResource(R.drawable.bg_button_secondary);
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 120);
        btnSettings.setLayoutParams(settingsParams);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        topBar.addView(btnSettings);

        rootLayout.addView(topBar);

        // Spacer
        View spacer1 = new View(this);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 24));
        rootLayout.addView(spacer1);

        // Status Card
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        statusCard.setBackgroundResource(R.drawable.bg_card);
        statusCard.setPadding(24, 24, 24, 24);

        tvWifiStatus = new TextView(this);
        tvWifiStatus.setText(R.string.wifi_status_off);
        tvWifiStatus.setTextSize(16);
        tvWifiStatus.setTextColor(0xFF333333);
        tvWifiStatus.setGravity(Gravity.RIGHT);
        statusCard.addView(tvWifiStatus);

        tvCurrentNetwork = new TextView(this);
        tvCurrentNetwork.setText(getString(R.string.current_network) + getString(R.string.not_connected));
        tvCurrentNetwork.setTextSize(14);
        tvCurrentNetwork.setTextColor(0xFF666666);
        tvCurrentNetwork.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams currParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        currParams.setMargins(0, 8, 0, 16);
        tvCurrentNetwork.setLayoutParams(currParams);
        statusCard.addView(tvCurrentNetwork);

        LinearLayout actionButtonsLayout = new LinearLayout(this);
        actionButtonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionButtonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        btnToggleWifi = new Button(this);
        btnToggleWifi.setText(R.string.turn_on_wifi);
        btnToggleWifi.setTextColor(0xFFFFFFFF);
        btnToggleWifi.setBackgroundResource(R.drawable.bg_button);
        LinearLayout.LayoutParams btnParams1 = new LinearLayout.LayoutParams(
                0, 120, 1.0f);
        btnParams1.setMargins(8, 0, 8, 0);
        btnToggleWifi.setLayoutParams(btnParams1);
        btnToggleWifi.setOnClickListener(v -> toggleWifiState());
        actionButtonsLayout.addView(btnToggleWifi);

        btnScan = new Button(this);
        btnScan.setText(R.string.scan_networks);
        btnScan.setTextColor(0xFFFFFFFF);
        btnScan.setBackgroundResource(R.drawable.bg_button);
        LinearLayout.LayoutParams btnParams2 = new LinearLayout.LayoutParams(
                0, 120, 1.0f);
        btnParams2.setMargins(8, 0, 8, 0);
        btnScan.setLayoutParams(btnParams2);
        btnScan.setOnClickListener(v -> checkPermissionsAndScan());
        actionButtonsLayout.addView(btnScan);

        statusCard.addView(actionButtonsLayout);
        rootLayout.addView(statusCard);

        // Spacer
        View spacer2 = new View(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 24));
        rootLayout.addView(spacer2);

        // Available Networks Section Title
        TextView tvAvailableTitle = new TextView(this);
        tvAvailableTitle.setText(R.string.available_networks);
        tvAvailableTitle.setTextSize(18);
        tvAvailableTitle.setTextColor(0xFF212121);
        tvAvailableTitle.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams titleParams2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams2.setMargins(0, 0, 0, 12);
        tvAvailableTitle.setLayoutParams(titleParams2);
        rootLayout.addView(tvAvailableTitle);

        // Network List Container
        layoutNetworkList = new LinearLayout(this);
        layoutNetworkList.setOrientation(LinearLayout.VERTICAL);
        layoutNetworkList.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rootLayout.addView(layoutNetworkList);

        scrollView.addView(rootLayout);
        setContentView(scrollView);
    }

    private void toggleWifiState() {
        if (wifiManager == null) return;
        boolean isEnabled = wifiManager.isWifiEnabled();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On Android 10+, direct toggling of WiFi is deprecated or restricted.
            // However, we can use Settings panel intent or try enabling if permitted,
            // but standard API setWifiEnabled works on many manufacturer devices or requires Settings panel.
            // We will attempt setWifiEnabled first, if it fails or returns false, we guide user gracefully.
            try {
                wifiManager.setWifiEnabled(!isEnabled);
            } catch (Exception e) {
                Toast.makeText(this, "נא להדליק Wi-Fi דרך הגדרות המכשיר", Toast.LENGTH_LONG).show();
            }
        } else {
            wifiManager.setWifiEnabled(!isEnabled);
        }
        // Refresh UI state after a slight delay
        rootLayoutPostDelayed();
    }

    private void rootLayoutPostDelayed() {
        layoutNetworkList.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateWifiStateUI();
                updateCurrentConnectedNetwork();
            }
        }, 1000);
    }

    private void checkPermissionsAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                Toast.makeText(this, R.string.wifi_status_off, Toast.LENGTH_SHORT).show();
                wifiManager.setWifiEnabled(true);
            }
            boolean success = wifiManager.startScan();
            if (!success) {
                // Scan might be throttled by OS, but we can still pull last known results
                displayScanResults();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsAndScan();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void displayScanResults() {
        if (wifiManager == null) return;
        try {
            List<ScanResult> results = wifiManager.getScanResults();
            if (results != null) {
                scanResults = results;
            }
        } catch (SecurityException e) {
            scanResults = new ArrayList<>();
        }

        layoutNetworkList.removeAllViews();

        if (scanResults.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(R.string.no_networks_found);
            tvEmpty.setTextSize(14);
            tvEmpty.setTextColor(0xFF757575);
            tvEmpty.setGravity(Gravity.RIGHT);
            tvEmpty.setPadding(16, 16, 16, 16);
            layoutNetworkList.addView(tvEmpty);
            return;
        }

        // Filter unique SSIDs keeping best signal
        List<ScanResult> uniqueNetworks = new ArrayList<>();
        for (ScanResult result : scanResults) {
            if (result.SSID == null || result.SSID.isEmpty()) continue;
            boolean exists = false;
            for (int i = 0; i < uniqueNetworks.size(); i++) {
                if (uniqueNetworks.get(i).SSID.equals(result.SSID)) {
                    exists = true;
                    if (result.level > uniqueNetworks.get(i).level) {
                        uniqueNetworks.set(i, result);
                    }
                    break;
                }
            }
            if (!exists) {
                uniqueNetworks.add(result);
            }
        }

        for (final ScanResult network : uniqueNetworks) {
            final boolean isOpen = !network.capabilities.contains("WEP") && 
                                   !network.capabilities.contains("WPA") && 
                                   !network.capabilities.contains("WPA2") && 
                                   !network.capabilities.contains("WPA3");
            final boolean isFav = preferenceManager.isFavorite(network.SSID);

            // Check auto connect rules
            if (isFav && preferenceManager.isAutoConnectFavorites()) {
                // Auto connect to favorite
                connectToNetwork(network, null);
            } else if (isOpen && preferenceManager.isAutoConnectOpen()) {
                // Auto connect to open network without password prompt
                connectToNetwork(network, null);
            }

            // Network Card Layout
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(24, 24, 24, 24);
            card.setGravity(Gravity.CENTER_VERTICAL);

            // Left side: Action Buttons (Connect / Favorite)
            LinearLayout btnLayout = new LinearLayout(this);
            btnLayout.setOrientation(LinearLayout.VERTICAL);
            btnLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            Button btnConnect = new Button(this);
            btnConnect.setText(R.string.connect);
            btnConnect.setTextColor(0xFFFFFFFF);
            btnConnect.setBackgroundResource(R.drawable.bg_button);
            btnConnect.setTextSize(12);
            LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(220, 90);
            bParams.setMargins(0, 0, 0, 8);
            btnConnect.setLayoutParams(bParams);
            btnConnect.setOnClickListener(v -> handleConnectClick(network, isOpen));
            btnLayout.addView(btnConnect);

            Button btnFav = new Button(this);
            btnFav.setText(isFav ? R.string.remove_favorite : R.string.save_favorite);
            btnFav.setTextColor(0xFF1976D2);
            btnFav.setBackgroundResource(R.drawable.bg_button_secondary);
            btnFav.setTextSize(11);
            btnFav.setLayoutParams(new LinearLayout.LayoutParams(220, 80));
            btnFav.setOnClickListener(v -> {
                if (preferenceManager.isFavorite(network.SSID)) {
                    preferenceManager.removeFavoriteNetwork(network.SSID);
                    Toast.makeText(this, "הוסר מהמועדפות", Toast.LENGTH_SHORT).show();
                } else {
                    preferenceManager.addFavoriteNetwork(network.SSID);
                    Toast.makeText(this, "נוסף למועדפות", Toast.LENGTH_SHORT).show();
                }
                displayScanResults();
            });
            btnLayout.addView(btnFav);

            card.addView(btnLayout);

            // Right side: Network Info (SSID, Security, Signal)
            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            infoParams.setMargins(16, 0, 0, 0);
            infoLayout.setLayoutParams(infoParams);
            infoLayout.setGravity(Gravity.RIGHT);

            TextView tvSsid = new TextView(this);
            tvSsid.setText(network.SSID + (isFav ? " ⭐" : ""));
            tvSsid.setTextSize(16);
            tvSsid.setTextColor(0xFF212121);
            tvSsid.setTypeface(null, android.graphics.Typeface.BOLD);
            tvSsid.setGravity(Gravity.RIGHT);
            infoLayout.addView(tvSsid);

            TextView tvDetails = new TextView(this);
            String securityType = isOpen ? getString(R.string.open_network) : getString(R.string.secured_network);
            tvDetails.setText(securityType + " | עוצמת אות: " + network.level + " dBm");
            tvDetails.setTextSize(13);
            tvDetails.setTextColor(0xFF666666);
            tvDetails.setGravity(Gravity.RIGHT);
            infoLayout.addView(tvDetails);

            card.addView(infoLayout);
            layoutNetworkList.addView(card);
        }
    }

    private void handleConnectClick(final ScanResult network, boolean isOpen) {
        if (isOpen) {
            // Prompt confirmation for open network
            new AlertDialog.Builder(this)
                    .setTitle(R.string.connect)
                    .setMessage("האם להתחבר לרשת הפתוחה " + network.SSID + "?")
                    .setPositiveButton(R.string.ok, (dialog, which) -> connectToNetwork(network, null))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            // Prompt password for secured network
            final EditText input = new EditText(this);
            input.setHint(R.string.password_hint);
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(48, 32, 48, 32);
            container.addView(input);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.password_prompt + network.SSID)
                    .setView(container)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        String password = input.getText().toString();
                        if (!password.isEmpty()) {
                            connectToNetwork(network, password);
                        } else {
                            Toast.makeText(MainActivity.this, "נא להכניס סיסמה", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void connectToNetwork(ScanResult network, String password) {
        Toast.makeText(this, R.string.connecting_to, Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder()
                    .setSsid(network.SSID);
            
            if (password != null && !password.isEmpty()) {
                builder.setWpa2Passphrase(password);
            }

            WifiNetworkSpecifier specifier = builder.build();

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(specifier)
                    .build();

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, R.string.connected_successfully, Toast.LENGTH_SHORT).show();
                            updateCurrentConnectedNetwork();
                        });
                    }

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                        runOnUiThread(() -> 
                            Toast.makeText(MainActivity.this, R.string.connection_failed, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            }
        } else {
            // For older Android versions (< Android 10), use WifiConfiguration API
            android.net.wifi.WifiConfiguration conf = new android.net.wifi.WifiConfiguration();
            conf.SSID = "\"" + network.SSID + "\"";
            
            if (password == null || password.isEmpty()) {
                conf.allowedKeyManagement.set(android.net.wifi.WifiConfiguration.KeyMgmt.NONE);
            } else {
                conf.preSharedKey = "\"" + password + "\"";
            }

            int netId = wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            
            rootLayoutPostDelayed();
            Toast.makeText(this, R.string.connected_successfully, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWifiStateUI() {
        if (wifiManager == null) return;
        boolean isEnabled = wifiManager.isWifiEnabled();
        if (isEnabled) {
            tvWifiStatus.setText(R.string.wifi_status_on);
            tvWifiStatus.setTextColor(0xFF2E7D32);
            btnToggleWifi.setText(R.string.turn_off_wifi);
        } else {
            tvWifiStatus.setText(R.string.wifi_status_off);
            tvWifiStatus.setTextColor(0xFFC62828);
            btnToggleWifi.setText(R.string.turn_on_wifi);
            tvCurrentNetwork.setText(getString(R.string.current_network) + getString(R.string.not_connected));
        }
    }

    private void updateCurrentConnectedNetwork() {
        if (wifiManager == null) return;
        try {
            android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSSID() != null && !wifiInfo.getSSID().equals("<unknown ssid>")) {
                String ssid = wifiInfo.getSSID().replace("\"", "");
                tvCurrentNetwork.setText(getString(R.string.current_network) + ssid);
            } else {
                tvCurrentNetwork.setText(getString(R.string.current_network) + getString(R.string.not_connected));
            }
        } catch (Exception e) {
            tvCurrentNetwork.setText(getString(R.string.current_network) + getString(R.string.not_connected));
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                displayScanResults();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                updateWifiStateUI();
                updateCurrentConnectedNetwork();
            }
        }
    }
}
