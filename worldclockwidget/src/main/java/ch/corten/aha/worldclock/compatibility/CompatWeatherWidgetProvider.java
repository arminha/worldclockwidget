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

package ch.corten.aha.worldclock.compatibility;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import ch.corten.aha.worldclock.AbstractWeatherWidgetProvider;

public abstract class CompatWeatherWidgetProvider extends AbstractWeatherWidgetProvider {

    private final int mSize;
    private final int mLayout;

    public static final int LAYOUT_ONE_COLUMN = 1;
    public static final int LAYOUT_TWO_COLUMNS = 2;

    protected CompatWeatherWidgetProvider(int size, int layout) {
        mSize = size;
        mLayout = layout;
    }

    @Override
    protected void onClockTick(Context context) {
        // Get the widget manager and ids for this widget provider, then call the shared
        // clock update method.
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int appWidgetID: ids) {
            updateAppWidget(context, appWidgetManager, appWidgetID);
        }
    }

    @Override
    protected void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Intent service = new Intent(context, CompatWeatherWidgetService.class);
        service.putExtra(CompatWeatherWidgetService.APP_WIDGET_ID, appWidgetId);
        service.putExtra(CompatWeatherWidgetService.LAYOUT, mLayout);
        service.putExtra(CompatWeatherWidgetService.SIZE, mSize);
        context.startService(service);
    }
}
