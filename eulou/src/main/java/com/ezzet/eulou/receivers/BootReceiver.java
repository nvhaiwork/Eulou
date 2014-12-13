package com.ezzet.eulou.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ezzet.eulou.services.EulouService;

// BootReceiver is the broadcast receiver that starts
// monitoring service when boot completed.
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Starts the WhatsAppMonitorService.
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, EulouService.class);
            context.startService(serviceIntent);
        }
    }

}
