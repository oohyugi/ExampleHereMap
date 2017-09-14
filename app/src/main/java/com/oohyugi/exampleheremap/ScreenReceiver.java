package com.oohyugi.exampleheremap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by oohyugi on 9/14/17.
 */

class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MyLocationService.class);
        context.startService(i);
    }
}
