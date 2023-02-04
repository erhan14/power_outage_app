package com.example.corte_de_luz;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    private void updatePowerStatus() {
        boolean isPowerConnected = isPowerConnected(this);
        TextView powerStatusView = findViewById(R.id.power_status_view);

        if (isPowerConnected) {
            powerStatusView.setText("Power Status: Connected");
        } else {
            powerStatusView.setText("Power Status: Disconnected");
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(new PowerConnectionReceiver(), intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView powerStatusTextView = findViewById(R.id.power_status_text_view);

        updatePowerStatus();

        powerStatusTextView.setOnClickListener(v -> {
            updatePowerStatus();
        });

        startService(new Intent(this, TelegramMessageSenderService.class));

    }

    private boolean isPowerConnected(MainActivity context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return batteryManager.isCharging();
    }
}