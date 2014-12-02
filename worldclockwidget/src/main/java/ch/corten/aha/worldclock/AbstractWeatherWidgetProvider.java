/*
 * Copyright (C) 2012  Armin Häberling
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
     * Enable weather update service.
     */
    private static void enableUpdateService(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                AlarmManager.INTERVAL_HOUR, createWeatherUpdateIntent(context));
    }

    /**
     * Disable weather update service.
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
