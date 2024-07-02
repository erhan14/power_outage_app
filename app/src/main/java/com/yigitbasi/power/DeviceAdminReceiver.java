package com.yigitbasi.power;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
    void showToast(Context context, String msg) {
        String status = context.getString(R.string.admin_receiver_status, msg);
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, context.getString(R.string.admin_receiver_status_enabled));
    }

}
