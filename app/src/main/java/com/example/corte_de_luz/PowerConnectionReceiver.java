package com.example.corte_de_luz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Toast.makeText(context, "Power Connected", Toast.LENGTH_SHORT).show();
            sendTelegramMessage(context, context.getString(R.string.power_connected));
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            Toast.makeText(context, "Power Disconnected", Toast.LENGTH_SHORT).show();
            sendTelegramMessage(context, context.getString(R.string.power_disconnected));
        }
    }

    private void sendTelegramMessage(Context context, String message) {
        String chatId = "";
        String token = "";
        String url = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text=" + message;

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, "Mensaje enviado", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error al enviar mensaje: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }
}
