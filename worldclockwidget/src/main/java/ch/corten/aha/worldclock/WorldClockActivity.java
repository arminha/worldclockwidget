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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import ch.corten.aha.widget.DigitalClock;
import ch.corten.aha.widget.PauseListener;
import ch.corten.aha.widget.PauseSource;
import ch.corten.aha.worldclock.provider.WorldClock;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks.MoveTarget;

public class WorldClockActivity extends SherlockFragmentActivity {
    private static final boolean IS_GINGERBREAD = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;

    private static final int WEATHER_UPDATE_INTERVAL = 900000; // 15 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            ClockListFragment list = new ClockListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class ClockListFragment extends SherlockListFragment implements
    LoaderManager.LoaderCallbacks<Cursor>, PauseSource {

        private CursorAdapter mAdapter;
        private ActionMode mMode;
        private OnSharedPreferenceChangeListener mSpChange;
        private boolean mAutoSortClocks;
        private final List<PauseListener> mListeners = new ArrayList<PauseListener>();

        private static final String[] CLOCKS_PROJECTION = {
            Clocks._ID,
            Clocks.TIMEZONE_ID,
            Clocks.CITY,
            Clocks.AREA,
            Clocks.TIME_DIFF,
            Clocks.TEMPERATURE,
            Clocks.HUMIDITY,
            Clocks.WIND_DIRECTION,
            Clocks.WIND_SPEED,
            Clocks.WEATHER_CONDITION,
            Clocks.CONDITION_CODE,
            Clocks.LATITUDE,
            Clocks.LONGITUDE
        };

        private static final String CAB = "cab";

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText(getText(R.string.no_clock_defined));
            setHasOptionsMenu(true);

            mAdapter = new ClockCursorAdapter(getActivity(), R.layout.world_clock_item, null, this);
            setListAdapter(mAdapter);

            setListShown(false);

            ListView listView = getListView();
            setupCabOld(listView);
            registerPreferenceChanged();

            if (savedInstanceState != null) {
                // Restore contextual action bar state
                CharSequence cab = savedInstanceState.getCharSequence(CAB);
                if (cab != null) {
                    mMode = getSherlockActivity().startActionMode(new ModeCallback());
                    mMode.setTitle(cab);
                    mMode.invalidate();
                }
            }

            getLoaderManager().initLoader(0, null, this);
            Clocks.updateOrder(getActivity());
            updateWeather(false);
        }

        private void registerPreferenceChanged() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Context context = getActivity();
            mAutoSortClocks = prefs.getBoolean(context.getString(R.string.auto_sort_clocks_key), true);
            mSpChange = new OnSharedPreferenceChangeListener() {

                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                        String key) {
                    mAutoSortClocks = sharedPreferences.getBoolean(context.getString(R.string.auto_sort_clocks_key), true);
                    getLoaderManager().restartLoader(0, null, ClockListFragment.this);
                    sendWidgetRefresh(context);
                }
            };
            prefs.registerOnSharedPreferenceChangeListener(mSpChange);
        }

        private void unregisterPreferenceChanged() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.unregisterOnSharedPreferenceChangeListener(mSpChange);
            mSpChange = null;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            // save contextual action bar state
            if (mMode != null) {
                outState.putCharSequence(CAB, mMode.getTitle());
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterPreferenceChanged();
        }

        @Override
        public void onPause() {
            super.onPause();
            for (PauseListener listener : mListeners) {
                listener.onPause();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            for (PauseListener listener : mListeners) {
                listener.onResume();
            }
        }

        @Override
        public void addPauseListener(PauseListener listener) {
            mListeners.add(listener);
        }

        @Override
        public void removePauseListener(PauseListener listener) {
            mListeners.remove(listener);
        };

        private void setupCabOld(ListView listView) {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    long[] checked = getListView().getCheckedItemIds();

                    if (checked.length > 0) {
                        if (mMode == null) {
                            mMode = getSherlockActivity().startActionMode(new ModeCallback());
                        }
                        CharSequence format = getResources().getText(R.string.n_selcted_format);
                        mMode.setTitle(MessageFormat.format(format.toString(), checked.length));
                        mMode.invalidate();
                    } else {
                        if (mMode != null) {
                            mMode.finish();
                        }
                    }
                }
            });
        }

        private class ModeCallback implements ActionMode.Callback {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.clock_list_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem editItem = menu.findItem(R.id.menu_edit);
                MenuItem upItem = menu.findItem(R.id.menu_up);
                MenuItem downItem = menu.findItem(R.id.menu_down);
                boolean oneSelected = getListView().getCheckedItemIds().length == 1;
                boolean reorderEnabled = !mAutoSortClocks && oneSelected && getListView().getCount() > 1;
                boolean changed = setVisible(editItem, oneSelected);
                changed = changed || setVisible(upItem, reorderEnabled);
                changed = changed || setVisible(downItem, reorderEnabled);
                if (changed) {
                    // fixes entries in the overflow menu
                    mode.invalidate();
                }
                return changed;
            }

            private boolean setVisible(MenuItem item, boolean visible) {
                if (item.isVisible() == visible) {
                    return false;
                } else {
                    item.setVisible(visible);
                    return true;
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                case R.id.menu_delete:
                    deleteSelectedItems();
                    mode.finish();
                    return true;
                case R.id.menu_edit:
                    editClock();
                    mode.finish();
                    return true;
                case R.id.menu_up:
                    moveSelected(MoveTarget.UP);
                    return true;
                case R.id.menu_down:
                    moveSelected(MoveTarget.DOWN);
                    return true;
                default:
                    return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                for (int i = 0; i < getListAdapter().getCount(); i++) {
                    getListView().setItemChecked(i, false);
                }

                if (mode == mMode) {
                    mMode = null;
                }
            }
        }

        private void moveSelected(MoveTarget target) {
            long id = getListView().getCheckedItemIds()[0];
            WorldClock.Clocks.move(getActivity(), id, target);
            sendWidgetRefresh(getActivity());
        }

        private void deleteSelectedItems() {
            long[] itemIds = getListView().getCheckedItemIds();
            Uri baseUri = Clocks.CONTENT_URI;
            ContentResolver resolver = getActivity().getContentResolver();
            for (long id : itemIds) {
                resolver.delete(ContentUris.withAppendedId(baseUri, id), null, null);
            }
            sendWidgetRefresh(getActivity());
        }

        private static void sendWidgetRefresh(Context context) {
            // send update broadcast to widget
            Intent broadcast = new Intent(ClockWidgetProvider.WIDGET_DATA_CHANGED_ACTION);
            context.sendBroadcast(broadcast);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.clock_list, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.menu_add:
                addClock();
                return true;
            case R.id.menu_refresh:
                updateWeather(true);
                return true;
            case R.id.menu_preferences:
                showPreferences();
                return true;
            default:
                return super.onOptionsItemSelected(item);
            }
        }

        private void showPreferences() {
            Intent i = new Intent(getActivity(), WorldClockPreferenceActivity.class);
            startActivity(i);
        }

        private void updateWeather(boolean immediately) {
            // check if automatic update is enabled
            if (!immediately && !automaticWeatherUpdate()) {
                return;
            }

            Intent service = new Intent(getActivity(), UpdateWeatherService.class);
            int updateInterval;
            if (immediately) {
                updateInterval = 0;
            } else {
                updateInterval = WEATHER_UPDATE_INTERVAL;
            }
            service.putExtra(UpdateWeatherService.WEATHER_DATA_UPDATE_INTERVAL, updateInterval);
            getActivity().startService(service);
        }

        private boolean automaticWeatherUpdate() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean disableUpdate = prefs.getBoolean(getString(R.string.disable_automatic_weather_update_key), false);
            return !disableUpdate;
        }

        private void addClock() {
            Intent intent = new Intent(getActivity(), AddClockActivity.class);
            startActivityForResult(intent, 0);
        }

        private void editClock() {
            long id = getListView().getCheckedItemIds()[0];
            Intent intent = new Intent(getActivity(), EditClockActivity.class);
            intent.putExtra(Clocks._ID, id);
            startActivityForResult(intent, 0);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode > 0) {
                updateWeather(false);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String sortOrder = mAutoSortClocks
                            ? Clocks.TIME_DIFF + " ASC, " + Clocks.CITY + " ASC"
                            : Clocks.ORDER_KEY + " ASC";
            return new CursorLoader(getActivity(), Clocks.CONTENT_URI,
                    CLOCKS_PROJECTION, null, null, sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.changeCursor(data);
            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
            if (mMode != null) {
                if (IS_GINGERBREAD) {
                    // update checked item after move
                    ListView list = getListView();
                    long[] ids = list.getCheckedItemIds();
                    if (ids.length == 1) {
                        long checkedId = ids[0];
                        for (int i = 0; i < list.getCount(); i++) {
                            long id = list.getAdapter().getItemId(i);
                            list.setItemChecked(i, checkedId == id);
                        }
                    }
                }
                mMode.invalidate();
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            mAdapter.changeCursor(null);
        }
    }

    private static final class ClockCursorAdapter extends ResourceCursorAdapter {
        private final Context mContext;
        private final PauseSource mPauseSource;

        @SuppressWarnings("deprecation")
        private ClockCursorAdapter(Context context, int layout, Cursor c, PauseSource pauseSource) {
            // use constructor available in gingerbread
            super(context, layout, c);
            mContext = context;
            mPauseSource = pauseSource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            DigitalClock clock = (DigitalClock) view.findViewById(R.id.time_clock);
            clock.setPauseSource(mPauseSource);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            BindHelper.bindText(view, cursor, R.id.city_text, Clocks.CITY);
            BindHelper.bindText(view, cursor, R.id.area_text, Clocks.AREA);

            String timeZoneId = cursor.getString(cursor.getColumnIndex(Clocks.TIMEZONE_ID));
            DateTimeZone tz = DateTimeZone.forID(timeZoneId);
            java.text.DateFormat df = DateFormat.getDateFormat(context);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            dateText.setText(TimeZoneInfo.formatDate(df, tz, DateTimeUtils.currentTimeMillis()));

            TextView timeDiffText = (TextView) view.findViewById(R.id.time_diff_text);
            timeDiffText.setText(TimeZoneInfo.getTimeDifferenceString(tz));
            DigitalClock clock = (DigitalClock) view.findViewById(R.id.time_clock);
            clock.setTimeZone(tz);

            BindHelper.bindTemperature(context, view, cursor, R.id.temp_text);
            BindHelper.bindText(view, cursor, R.id.condition_text, Clocks.WEATHER_CONDITION);
            ImageView condImage = (ImageView) view.findViewById(R.id.condition_image);
            int condCode = cursor.getInt(cursor.getColumnIndex(Clocks.CONDITION_CODE));
            double lat = cursor.getDouble(cursor.getColumnIndex(Clocks.LATITUDE));
            double lon = cursor.getDouble(cursor.getColumnIndex(Clocks.LONGITUDE));
            condImage.setImageResource(WeatherIcons.getIcon(condCode, lon, lat));

            bindHumidity(view, cursor);
            bindWind(view, cursor);
        }

        private void bindWind(View view, Cursor cursor) {
            String text;
            int index = cursor.getColumnIndex(Clocks.WIND_SPEED);
            if (cursor.isNull(index)) {
                text = "";
            } else {
                double windSpeed = cursor.getDouble(cursor.getColumnIndex(Clocks.WIND_SPEED));
                String speed = BindHelper.getSpeed(mContext, windSpeed);
                String windDirection = cursor.getString(cursor.getColumnIndex(Clocks.WIND_DIRECTION));
                String format = mContext.getText(
                        windDirection != null ? R.string.wind_format : R.string.wind_format_no_direction).toString();
                text = MessageFormat.format(format, windDirection, speed);
            }
            BindHelper.setText(view, R.id.wind_text, text);
        }

        private void bindHumidity(View view, Cursor cursor) {
            String text;
            int index = cursor.getColumnIndex(Clocks.HUMIDITY);
            if (cursor.isNull(index)) {
                text = "";
            } else {
                double humidity = cursor.getDouble(index);
                text = MessageFormat.format("{0}: {1, number,#}%", mContext.getString(R.string.humidity), humidity);
            }
            BindHelper.setText(view, R.id.humidity_text, text);
        }
    }
}
