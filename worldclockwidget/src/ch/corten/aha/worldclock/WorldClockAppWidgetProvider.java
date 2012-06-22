package ch.corten.aha.worldclock;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WorldClockAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WorldClockAppWidgetProvider";
    
    public static final String CLOCK_WIDGET_UPDATE = "ch.corten.aha.worldclock.CLOCK_WIDGET_UPDATE";
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        Log.i(TAG, "Updating widgets " + Arrays.asList(appWidgetIds));
        
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create an Intent to launch ClockListActivity
        Intent intent = new Intent(context, ClockListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.world_clock_widget);
        views.setOnClickPendingIntent(R.id.app_widget, pendingIntent);

        // update view
        updateViews(context, views);
        
        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    static void updateViews(Context context, RemoteViews views) {
        final String city = "Ottawa";
        TimeZoneInfo timeZone = TimeZoneInfo.getCity(city);
        views.setTextViewText(R.id.city_text, city);
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        df.setTimeZone(timeZone.getTimeZone());
        views.setTextViewText(R.id.time_text, df.format(new Date()));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "Widget Provider disabled. Turning off timer");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createClockTickIntent(context));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "Widget Provider enabled.  Starting timer to update widget every second");
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.SECOND, 60);
        calendar.set(Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                60000, createClockTickIntent(context));
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "Received intent " + intent);
        if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
            Log.d(TAG, "Clock update");
            // Get the widget manager and ids for this widget provider, then call the shared
            // clock update method.
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for (int appWidgetID: ids) {
                updateAppWidget(context, appWidgetManager, appWidgetID);
            }
        }
    }

    private PendingIntent createClockTickIntent(Context context) {
        Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

}
