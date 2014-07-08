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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormat;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WeatherWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WeatherWidgetViewsFactory(this.getApplicationContext());
    }

    /**
     * Reads data for the app widget from the
     * {@link ch.corten.aha.worldclock.provider.WorldClockContentProvider}.
     * It synchronizes access to the internal {@link #mCursor} field.
     *
     * <p>Instances of this class are thread-safe.</p>
     */
    static class WeatherWidgetViewsFactory implements RemoteViewsFactory {

        private final ReadWriteLock mCursorLock = new ReentrantReadWriteLock();

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
            Cursor newCursor = WeatherWidget.getData(mContext);
            setCursor(newCursor);
            setTimeFormat();
        }

        private void setCursor(Cursor newCursor) {
            mCursorLock.writeLock().lock();
            try {
                if (mCursor != null) {
                    mCursor.close();
                }
                mCursor = newCursor;
            } finally {
                mCursorLock.writeLock().unlock();
            }
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
            setCursor(null);
        }

        @Override
        public int getCount() {
            mCursorLock.readLock().lock();
            try {
                if (mCursor == null) {
                    return 0;
                } else {
                    return mCursor.getCount();
                }
            } finally {
                mCursorLock.readLock().unlock();
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

            mCursorLock.readLock().lock();
            try {
                if (mCursor != null && mCursor.moveToPosition(position)) {
                    WeatherWidget.updateItemView(mContext, mCursor, rv, getTimeFormat());
                    Intent intent = new Intent();
                    rv.setOnClickFillInIntent(R.id.widget_item, intent);
                }
            } finally {
                mCursorLock.readLock().unlock();
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
            mCursorLock.readLock().lock();
            try {
                if (mCursor != null && mCursor.moveToPosition(position)) {
                    return mCursor.getLong(mCursor.getColumnIndex(Clocks._ID));
                }
            } finally {
                mCursorLock.readLock().unlock();
            }
            return -1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
