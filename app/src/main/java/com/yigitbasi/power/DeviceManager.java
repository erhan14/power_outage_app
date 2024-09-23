package com.yigitbasi.power;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class DeviceManager
{
    private DevicePolicyManager deviceManger;
    private ActivityManager activityManager;
    private ComponentName compName;

    public DeviceManager(Context context)
    {
        deviceManger =  (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(context, DeviceAdminReceiver.class);
    }

    public void registerAdmin(Context activity)
    {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Administrative Rights need to be granted.");
        activity.startActivity(intent);

    }
}