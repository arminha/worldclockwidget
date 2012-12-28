package ch.corten.aha.worldclock;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public abstract class AbstractWeatherWidgetProvider extends ClockWidgetProvider {

    private static final List<Class<? extends AppWidgetProvider>> WEATHER_WIDGET_PROVIDERS = new ArrayList<Class<? extends AppWidgetProvider>>(); 

    protected static void registerWeatherWidget(Class<? extends AbstractWeatherWidgetProvider> provider) {
        registerClockWidget(provider);
        WEATHER_WIDGET_PROVIDERS.add(provider);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        enableUpdateService(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (!isAnyWidgetActive(context, WEATHER_WIDGET_PROVIDERS)) {
            disableUpdateService(context);
        }
    }

    /**
     * enable weather update service
     */
    private static void enableUpdateService(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                AlarmManager.INTERVAL_HOUR, createWeatherUpdateIntent(context));
    }
    
    /**
     * disable weather update service
     */
    private static void disableUpdateService(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(createWeatherUpdateIntent(context));
    }

    private static PendingIntent createWeatherUpdateIntent(Context context) {
        Intent service = new Intent(context, UpdateWeatherService.class);
        service.putExtra(UpdateWeatherService.BACKGROUND_UPDATE, true);
        return PendingIntent.getService(context, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
