package com.yigitbasi.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SimpleDateFormat fmt =
                new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
        Log.w(MainActivity.TAG, "Power status changed");
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, "Fiş Takılı", Toast.LENGTH_SHORT).show();
            sendTelegramMessage(context, context.getString(R.string.power_connected, fmt.format(new Date())));
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, "Elektrik Kesildi", Toast.LENGTH_SHORT).show();
            sendTelegramMessage(context, context.getString(R.string.power_disconnected, fmt.format(new Date())));
        }else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(MainActivity.TAG, "Receive boot completed broadcast");
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }

    public void sendTelegramMessage(Context context, String message) {
        String url = "https://api.telegram.org/bot" + TelegramMessageSenderService.TELEGRAM_TOKEN + "/sendMessage?chat_id=" + TelegramMessageSenderService.TELEGRAM_CHAT_ID + "&text=" + message;

        //RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, "Mesaj gönderildi", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(MainActivity.TAG, "Error sending telegram mes: " + error.getMessage());
                final Handler handler = new Handler(Looper.getMainLooper());

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendTelegramMessage(context, message);
                    }
                }, 1000);
            }
        });
        MyToolbox.getInstance(context).addToRequestQueue(stringRequest);
        //queue.add(stringRequest);
    }
}
