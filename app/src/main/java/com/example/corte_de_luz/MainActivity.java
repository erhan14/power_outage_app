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

        TextView powerStatusTextView = findViewById(R.id.power_status_view);

        updatePowerStatus();

        powerStatusTextView.setOnClickListener(v -> {
            updatePowerStatus();
        });

    }

    private boolean isPowerConnected(MainActivity context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }
}