package com.yigitbasi.power;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.core.impl.UseCaseConfig;
import androidx.camera.core.impl.UseCaseConfig.Builder;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

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

        TextView powerStatusTextView = findViewById(R.id.power_status_view);

        receiver = new PowerConnectionReceiver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!hasCameraPermissionGranted())
                requestCameraPermission();
        }

        Context context = getApplicationContext();
        Intent intent = new Intent(MainActivity.this, MyService.class); // Build the intent for the service
        context.startService(intent);
        Log.d(TAG, "Not service starting...");
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
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                    0);
        }
    }

}