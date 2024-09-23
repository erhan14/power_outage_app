package com.yigitbasi.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public final class NetworkService extends JobIntentService {

    // this is an intent service - runs on its own thread - otherwise it would
    // deadlock as I am using it. Moreover it holds a wakelock and woken up by
    // an AlarmManager's Receiver - works reliably
    private BroadcastReceiver mConnectionReceiver = null;
    private volatile static CountDownLatch latch;

    private static final String TAG = NetworkService.class.getSimpleName();
    public static final String RECEIVER = "receiver";
    private static final String ACTION_DOWNLOAD = "action.DOWNLOAD_DATA";
    static final int DOWNLOAD_JOB_ID = 1099;

    private int errors = 0;

    @Override
    public void onCreate() {
        if(mConnectionReceiver==null) {
            mConnectionReceiver = new WifiConnectionMonitor();
            startMonitoringConnection();
        }
    }

    @Override
    public void onDestroy() {
        stopMonitoringConnection();
        mConnectionReceiver = null;
    }

    public static void enqueueWork(Context context) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_DOWNLOAD);
        enqueueWork(context, NetworkService.class, DOWNLOAD_JOB_ID, intent);
        Log.d(TAG, "Job enqueued");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onHandleWork() called with: intent = [" + intent + "]");
        WifiManager.WifiLock _wifiLock = null;
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        try {
            boolean failedToConnect = true;
            if (wm != null && wm.isWifiEnabled()) {// Don't want to enable it myself
                _wifiLock = wm.createWifiLock(
                        /* WifiManager.WIFI_MODE_FULL_HIGH_PERF */0x3, this.getClass()
                                .getName() + ".WIFI_LOCK");
                _wifiLock.acquire();
                Log.d(TAG, "Lock acquired");
                failedToConnect = !wakeWifiUp();
            }
            if (failedToConnect) {
                if (_wifiLock != null) _wifiLock.release();
                Log.e(TAG, "No connection !");
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "IOException sending data ", e);
        } finally {
            if (_wifiLock != null) _wifiLock.release();
        }
    }

    private boolean wakeWifiUp() {
        ConnectivityManager _androidConnectivityMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = _androidConnectivityMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager _wifiManager = (WifiManager)
                getSystemService(Context.WIFI_SERVICE);
        final int wifiState = _wifiManager.getWifiState();
        if (!_wifiManager.isWifiEnabled()
                || wifiState == WifiManager.WIFI_STATE_DISABLED
                || wifiState == WifiManager.WIFI_STATE_DISABLING) {
            // Make sure the Wi-Fi is enabled, required for some devices when
            // enable WiFi does not occur immediately
            Log.d(TAG, "!_wifiManager.isWifiEnabled()");
            _wifiManager.setWifiEnabled(true);
            // do not enable if not enabled ! FIXME
        }
        Log.d(TAG, "Wifi state " + wifiState + " connecting: " + wifiInfo.isConnectedOrConnecting());
        if (!wifiInfo.isConnectedOrConnecting()) {
            Log.d(TAG, "Wifi is NOT Connected Or Connecting - "
                    + "wake it up and wait till is up");
            try {
                //latch = new CountDownLatch(1);
                //Log.d(TAG, "I wait");
                _wifiManager.startScan();
                //latch.await();
                //Log.d(TAG, "Woke up");
                return true; // made it
            } catch (Exception e) {
                Log.e(TAG, "Interrupted while waiting for connection", e);
                return false;
            } finally {
                //stopMonitoringConnection();
            }
        }
        errors = 0;
        return true;
    }

    static void downTheLatch() {
        latch.countDown();
    }

    private synchronized void startMonitoringConnection() {
        IntentFilter aFilter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        aFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        aFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mConnectionReceiver, aFilter);
    }

    private synchronized void stopMonitoringConnection() {
        unregisterReceiver(mConnectionReceiver);
    }


    private final class WifiConnectionMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent in) {
            String action = in.getAction();
            Log.d(TAG, "Wifi event " + action);
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = in
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, networkInfo + "");
                if (networkInfo.isConnected()) {
                    Log.d(TAG, "Wifi is connected!");
                    //NetworkService.downTheLatch(); // HERE THE SERVICE IS WOKEN!
                    errors = 0;
                } else {
                    final Handler handler = new Handler();
                    errors++;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            WifiManager _wifiManager = (WifiManager)
                                    getSystemService(Context.WIFI_SERVICE);
                            _wifiManager.startScan();
                        }
                    }, 30000);
                }
            }//Search the scan results for saved WiFi APs.
            else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                ConnectivityManager _androidConnectivityMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiInfo = _androidConnectivityMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                WifiManager _wifiManager = (WifiManager)
                        getSystemService(Context.WIFI_SERVICE);
                boolean foundMatch = false;
                try {
                    if (!wifiInfo.isConnectedOrConnecting()) {
                        Log.d(TAG, "Not connected!");
                        Map<String, Integer> savedNetworks = new HashMap<String, Integer>();

                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            for (WifiConfiguration config : _wifiManager.getConfiguredNetworks()) {
                                String escapedSSID = config.SSID.replaceAll("\"", "");
                                savedNetworks.put(escapedSSID, config.networkId);
                                //Log.d(TAG, "SavedConfig:" + escapedSSID + "!");
                            }
                        }
                        List<ScanResult> scanResults = _wifiManager.getScanResults();
                        for (ScanResult ap : scanResults) {
                            //Log.d(TAG, "ScanResult:" + ap.SSID + "!");
                            Integer networkId = savedNetworks.get(ap.SSID);
                            if (networkId != null) {
                                savedNetworks.remove(ap.SSID);
                                Log.d(TAG, "Try reconnect to " + ap.SSID + "!");
                                _wifiManager.enableNetwork(networkId, false);
                                foundMatch = true;
                            }
                        }
                        if(foundMatch) {
                            _wifiManager.reassociate();
                            _wifiManager.reconnect();
                        } else {
                            Log.d(TAG, "No SSID found!");
                            errors++;
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(errors>1) {
                                        try {
                                            Log.d(TAG, "Disabling wifi!");
                                            _wifiManager.disconnect();
                                            // Brute force methods required for some devices
                                            _wifiManager.setWifiEnabled(false);
                                            try {Thread.sleep(1000);}catch (Exception ignored){}
                                            _wifiManager.setWifiEnabled(true);
                                            try {Thread.sleep(1000);}catch (Exception ignored){}
                                            _wifiManager.disconnect();
                                            _wifiManager.startScan();
                                            _wifiManager.reassociate();
                                            _wifiManager.reconnect();
                                        } catch (SecurityException e) {
                                            // Catching exception which should not occur on most
                                            // devices. OS bug details at :
                                            // https://code.google.com/p/android/issues/detail?id=22036
                                        }
                                        errors = 0;
                                    }
                                    _wifiManager.startScan();
                                }
                            }, 30000);
                        }
                    } else {
                        errors = 0;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "exception on wifi check", e);
                }
            }
        }
    }
}