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

import java.text.MessageFormat;
import java.util.Date;
import java.util.TimeZone;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import ch.corten.aha.widget.DigitalClock;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import ch.corten.aha.worldclock.weather.WeatherObservation;
import ch.corten.aha.worldclock.weather.WeatherService;
import ch.corten.aha.worldclock.weather.google.GoogleWeatherService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ClockListActivity extends SherlockFragmentActivity {
    
    private static final int WEATHER_TIMEOUT = 900000; // 15 minutes

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
            LoaderManager.LoaderCallbacks<Cursor> {

        private CursorAdapter mAdapter;

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
            Clocks.CONDITION_CODE
            };

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText(getText(R.string.no_clock_defined));
            setHasOptionsMenu(true);

            mAdapter = new ResourceCursorAdapter(getActivity(), R.layout.world_clock_item, null) {

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    BindHelper.bindText(view, cursor, R.id.city_text, Clocks.CITY);
                    BindHelper.bindText(view, cursor, R.id.area_text, Clocks.AREA);
                    
                    String timeZoneId = cursor.getString(cursor.getColumnIndex(Clocks.TIMEZONE_ID));
                    TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
                    java.text.DateFormat df = DateFormat.getDateFormat(context);
                    df.setTimeZone(timeZone);
                    TextView dateText = (TextView) view.findViewById(R.id.date_text);
                    dateText.setText(df.format(new Date()));
                    
                    TextView timeDiffText = (TextView) view.findViewById(R.id.time_diff_text);
                    timeDiffText.setText(TimeZoneInfo.getTimeDifferenceString(timeZone));
                    DigitalClock clock = (DigitalClock) view.findViewById(R.id.time_clock);
                    clock.setTimeZone(timeZone);
                    
                    BindHelper.bindTemperature(context, view, cursor, R.id.temp_text);
                    BindHelper.bindText(view, cursor, R.id.condition_text, Clocks.WEATHER_CONDITION);
                    ImageView condImage = (ImageView) view.findViewById(R.id.condition_image);
                    int condCode = cursor.getInt(cursor.getColumnIndex(Clocks.CONDITION_CODE));
                    condImage.setImageResource(WeatherIcons.getIcon(condCode));
                    
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
                        String speed = BindHelper.getSpeed(getActivity(), windSpeed);
                        String windDirection = cursor.getString(cursor.getColumnIndex(Clocks.WIND_DIRECTION));
                        text = MessageFormat.format(getText(R.string.wind_format).toString(), windDirection, speed);
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
                        text = MessageFormat.format("{0}: {1, number,#}%", getString(R.string.humidity), humidity);
                    }
                    BindHelper.setText(view, R.id.humidity_text, text);
                }
            };
            setListAdapter(mAdapter);

            setListShown(false);
            
            ListView listView = getListView();
            setupCabOld(listView);            
            getLoaderManager().initLoader(0, null, this);
            updateWeather(WEATHER_TIMEOUT);
        }

        private ActionMode mMode;
        
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
                boolean oneSelected = getListView().getCheckedItemIds().length == 1;
                if (editItem.isVisible() == oneSelected) {
                    return false;
                } else {
                    editItem.setVisible(oneSelected);
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
                default:
                    return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                for (int i = 0; i < getListAdapter().getCount(); i++)
                    getListView().setItemChecked(i, false);
     
                if (mode == mMode) {
                    mMode = null;
                }
            }
             
        }
        
        private void deleteSelectedItems() {
            long[] itemIds = getListView().getCheckedItemIds();
            Uri baseUri = Clocks.CONTENT_URI;
            ContentResolver resolver = getActivity().getContentResolver();
            for (long id : itemIds) {
                resolver.delete(ContentUris.withAppendedId(baseUri, id), null, null);
            }
            refreshClocks();
        }

        private void refreshClocks() {
            getLoaderManager().restartLoader(0, null, this);
            // send update broadcast to widget
            Intent broadcast = new Intent(WorldClockAppWidgetProvider.CLOCK_WIDGET_UPDATE);
            getActivity().sendBroadcast(broadcast);
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
                updateWeather(0);
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

        private void updateWeather(int timeout) {
            new UpdateWeatherTask().execute(timeout);
        }
        
        private class UpdateWeatherTask extends AsyncTask<Integer, Integer, Integer> {
            @Override
            protected Integer doInBackground(Integer... params) {
                int updateTimeout = params.length > 0 ? params[0] : 0;
                ContentResolver resolver = getActivity().getContentResolver();
                String[] projection = {
                    Clocks._ID,
                    Clocks.LATITUDE,
                    Clocks.LONGITUDE
                };
                String query = null;
                if (updateTimeout > 0) {
                    query = Clocks.LAST_UPDATE + " < " + (System.currentTimeMillis() - updateTimeout);
                }
                int count = 0;
                WeatherService service = new GoogleWeatherService();
                Cursor c = resolver.query(Clocks.CONTENT_URI, projection, query, null, null);
                while (c.moveToNext()) {
                    double lat = c.getDouble(c.getColumnIndex(Clocks.LATITUDE));
                    double lon = c.getDouble(c.getColumnIndex(Clocks.LONGITUDE));
                    long id = c.getLong(c.getColumnIndex(Clocks._ID));
                    WeatherObservation observation = service.getWeather(lat, lon);
                    if (observation != null && Clocks.updateWeather(getActivity(), id, observation)) {
                        count++;
                    }
                }
                return count;
            }
            
            @Override
            protected void onPostExecute(Integer result) {
                refreshClocks();
            }
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
                updateWeather(WEATHER_TIMEOUT);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), Clocks.CONTENT_URI,
                    CLOCKS_PROJECTION, null, null, Clocks.TIME_DIFF  + " ASC, " + Clocks.CITY + " ASC");
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
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            mAdapter.changeCursor(null);
        }
    }
}
