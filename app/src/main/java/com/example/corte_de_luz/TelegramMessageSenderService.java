package com.example.corte_de_luz;

import android.app.IntentService;
import android.content.Intent;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public class TelegramMessageSenderService extends IntentService {

    private static final String TELEGRAM_BOT_TOKEN = "";
    private static final String CHAT_ID = "";

    public TelegramMessageSenderService() {
        super("TelegramMessageSenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra("message");

        TelegramBot bot = new TelegramBot(TELEGRAM_BOT_TOKEN);
        bot.execute(new SendMessage(CHAT_ID, message));
    }
}