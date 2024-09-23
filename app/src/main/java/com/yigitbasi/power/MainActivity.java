package com.yigitbasi.power;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "YGTBS";
    private PowerConnectionReceiver receiver = null;

    @SuppressLint("SetTextI18n")
    private void updatePowerStatus() {
        try {
            boolean isPowerConnected = isPowerConnected(this);
            Log.d(TAG,"Updating power " + isPowerConnected);
            TextView powerStatusView = findViewById(R.id.power_status_view);
            SimpleDateFormat fmt =
                    new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
            if (isPowerConnected) {
                powerStatusView.setText("Power Status: Connected");
                receiver.sendTelegramMessage(this, this.getString(R.string.power_connected, fmt.format(new Date())));
            } else {
                powerStatusView.setText("Power Status: Disconnected");
                receiver.sendTelegramMessage(this, this.getString(R.string.power_disconnected, fmt.format(new Date())));
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            registerReceiver(receiver, intentFilter);
        } catch (Exception e) {
            Log.e(TAG,"Error Updating power ", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            if(!hasCameraPermissionGranted()) {
                requestCameraPermission();
            }
            /*if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS},
                        0);
            }*/
        else {
            initView();
            }

        Log.d(TAG, "MainActivity started");
    }

    private void initView() {
        receiver = new PowerConnectionReceiver();

        TextView powerStatusTextView = findViewById(R.id.power_status_view);

        Context context = getApplicationContext();

        if(Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(MainActivity.this, MyService.class); // Build the intent for the service
            context.startService(intent);
            Log.d(TAG, "My service starting...");
        }
        updatePowerStatus();

        powerStatusTextView.setOnClickListener(v -> {
            updatePowerStatus();
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /*try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TelegramMessageReceiver());
        } catch (TelegramApiException e) {
            Log.e(TAG,"Error creating telegram bot ", e);
        }*/

        Button pushButton = (Button) findViewById(R.id.pushButton);
        pushButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ComponentName deviceAdmin = new ComponentName(MainActivity.this, DeviceAdminReceiver.class);
                DevicePolicyManager manager =
                        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if(manager.isAdminActive(deviceAdmin)) {
                    Log.d(TAG, "Rebooting...");
                    manager.reboot(deviceAdmin);
                } else {
                    Log.d(TAG, "No admin app rights!!!...");
                }
            }
        });

        try {
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, MainActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            Log.e(TAG, "Error on component settings", e);
        }
    }

    public static boolean isPowerConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    private boolean hasCameraPermissionGranted(){
        return  ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)) {
                int REQUEST_CODE = 101;
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                myIntent.setData(Uri.parse("package:" + getPackageName()));
                Log.d(TAG, "Wait start for overlay");
                startActivityForResult(myIntent, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(permsRequestCode,permissions, grantResults);
        Log.v(TAG, "onRequestPermissionsResult Result.");
        switch (permsRequestCode) {

            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView();
                } else {
                    //Toast.makeText(this, "Please Grant Permissions other wise app will close.!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "OnActivity Result.");
        super.onActivityResult(requestCode, resultCode, data);
        //check if received result code
        //  is equal our requested code for draw permission
        int REQUEST_CODE = 101;
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Overlay enabled...");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS},
                        0);
            }
        }
    }
}