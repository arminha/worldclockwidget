package ch.corten.aha.worldclock;

import android.app.IntentService;
import android.content.Intent;

public class WorldClockWidgetService extends IntentService {

    public WorldClockWidgetService() {
        super("WorldClockWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WorldClockWidgetProvider.updateTime(this);
    }
}
