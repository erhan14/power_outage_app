import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class App extends BroadcastReceiver {
    private TelegramBot telegramBot;

    public App(String botToken) {
        telegramBot = new TelegramBot(botToken);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
            telegramBot.sendMessage("chat_id", "El cargador se ha desconectado");
        } else if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
            telegramBot.sendMessage("chat_id", "El cargador se ha vuelto a conectar");
        }
    }
}