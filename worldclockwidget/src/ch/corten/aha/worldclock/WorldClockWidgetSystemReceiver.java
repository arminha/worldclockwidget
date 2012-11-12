package ch.corten.aha.worldclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WorldClockWidgetSystemReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            Intent signal = new Intent(WorldClockWidgetProvider.CLOCK_TICK_ACTION);
            context.sendBroadcast(signal);
        }
    }

}
