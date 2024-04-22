package com.yigitbasi.power;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TelegramMessageSenderService extends IntentService {
    private static final String TAG = TelegramMessageSenderService.class.getSimpleName();

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    public static final String TELEGRAM_TOKEN = "--";

    public static final String TELEGRAM_USERNAME = "--";

    public static final String TELEGRAM_CHAT_ID = "--";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public TelegramMessageSenderService() {
        super("TelegramMessageSenderService");
        client = new OkHttpClient();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        String message = intent.getStringExtra("message");
        if (message == null) return;

        String url = TELEGRAM_API_URL + TELEGRAM_TOKEN + "/sendMessage";
        String json = "{\"chat_id\":\"" + TELEGRAM_CHAT_ID + "\",\"text\":\"" + message + "\"}";

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.e(TAG, "Error sending to Telegram: " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error send Telegram: " + e.getMessage());
        }
    }
}