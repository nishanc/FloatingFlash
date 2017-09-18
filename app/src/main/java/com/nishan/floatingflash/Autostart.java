package com.nishan.floatingflash;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * Created by Nishan Chathuranga on 9/17/2017.
 */

public class Autostart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1)
    {
        Intent intent = new Intent(context,FlashService.class);
        context.startService(intent);
        Log.i("Autostart", "started");
    }
}
