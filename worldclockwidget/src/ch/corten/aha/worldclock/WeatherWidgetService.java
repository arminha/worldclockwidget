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

import java.text.DateFormat;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WeatherWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WeatherWidgetViewsFactory(this.getApplicationContext());
    }

    static class WeatherWidgetViewsFactory implements RemoteViewsFactory {
        private Context mContext;
        private Cursor mCursor;
        private DateFormat mTimeFormat;

        public WeatherWidgetViewsFactory(Context context) {
            mContext = context;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            // Refresh the cursor
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = WeatherWidget.getData(mContext);
            setTimeFormat();
        }

        private void setTimeFormat() {
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(mContext);
        }

        private DateFormat getTimeFormat() {
            if (mTimeFormat == null) {
                setTimeFormat();
            }
            return mTimeFormat;
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            if (mCursor == null) {
                return 0;
            } else {
                return mCursor.getCount();
            }
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv;
            if (position == 0) {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.weather_widget_item2);
            } else {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.weather_widget_item);
            }

            if (mCursor.moveToPosition(position)) {
                WeatherWidget.updateItemView(mContext, mCursor, rv, getTimeFormat());
                Intent intent = new Intent();
                rv.setOnClickFillInIntent(R.id.widget_item, intent);
            }
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            // we don't use a special loading view.
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mCursor.getColumnIndex(Clocks._ID));
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
