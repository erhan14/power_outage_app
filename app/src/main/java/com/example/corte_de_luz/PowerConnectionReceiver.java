package com.example.corte_de_luz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, "Power Connected", Toast.LENGTH_SHORT).show();
            Intent serviceIntent = new Intent(context, TelegramMessageSenderService.class);
            serviceIntent.putExtra("message", context.getString(R.string.power_connected));
            context.startService(serviceIntent);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, "Power Disconnected", Toast.LENGTH_SHORT).show();
            Intent serviceIntent = new Intent(context, TelegramMessageSenderService.class);
            serviceIntent.putExtra("message", context.getString(R.string.power_disconnected));
            context.startService(serviceIntent);
        }
    }
}