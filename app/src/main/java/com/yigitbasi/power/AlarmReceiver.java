package com.yigitbasi.power;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int PERIOD=30000; // 15 minutes
    private static final int INITIAL_DELAY=1000; // 5 seconds

    private static final String TAG = AlarmReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm Manager running");
        if (intent.getAction() == null) {
            NetworkService.enqueueWork(context);
            scheduleAlarms(context);
        }
    }

    static void scheduleAlarms(Context ctxt) {
        AlarmManager mgr=
                (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(ctxt, AlarmReceiver.class);
        PendingIntent pi= PendingIntent.getBroadcast(ctxt, new Random().nextInt(), i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Depending on the version of Android use different function for setting an Alarm.
        // setAlarmClock() => Android < Marshmallow
        // setExactAndAllowWhileIdle() => Android >= Marshmallow
        try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                mgr.setAlarmClock(
                        new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + PERIOD, pi),
                        pi
                );
            } else {
                mgr.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + PERIOD,
                        pi
                );
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception setting alarm", e);
        }

        //mgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
        //        PERIOD, pi);
        Log.d(TAG, "Alarms scheduled.");

    }

}
