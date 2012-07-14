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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.TimeZone;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;

import ch.corten.aha.worldclock.R.string;
import ch.corten.aha.worldclock.provider.WorldClock;
import ch.corten.aha.worldclock.provider.WorldClock.Cities;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class AddClockActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_city);
        
        FragmentManager fm = getSupportFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            TimeZoneListFragment listFragment = new TimeZoneListFragment();
            fm.beginTransaction().add(android.R.id.content, listFragment).commit();
        }
    }
    
    @Override
    public boolean onSearchRequested() {
        FragmentManager fm = getSupportFragmentManager();
        TimeZoneListFragment fragment = (TimeZoneListFragment) fm
                .findFragmentById(android.R.id.content);
        fragment.startSearch();
        return true;
    }
    
    public static class TimeZoneListFragment extends SherlockListFragment implements
            LoaderManager.LoaderCallbacks<Cursor> {
        private CursorAdapter mAdapter;
        private View mSearchView;
        private MenuItem mSearchItem;
        private String mCurFilter;

        private static final String[] CITY_PROJECTION = {
            Cities._ID,
            Cities.NAME,
            Cities.COUNTRY_CODE,
            Cities.TIMEZONE_ID
            };
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            
            mAdapter = new ResourceCursorAdapter(getActivity(), R.layout.time_zone_item, null, 0) {
                
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView cityText = (TextView) view.findViewById(R.id.city_text);
                    cityText.setText(cursor.getString(cursor.getColumnIndex(Cities.NAME)));
                    TextView areaText = (TextView) view.findViewById(R.id.area_text);
                    String countryCode = cursor.getString(cursor.getColumnIndex(Cities.COUNTRY_CODE));
                    areaText.setText(getCountryName(countryCode));
                    TextView timeDiffText = (TextView) view.findViewById(R.id.time_diff_text);
                    TimeZone tz = TimeZone.getTimeZone(cursor.getString(cursor.getColumnIndex(Cities.TIMEZONE_ID)));
                    timeDiffText.setText(TimeZoneInfo.getTimeDifferenceString(tz));
                    TextView timeZoneDescText = (TextView) view.findViewById(R.id.timezone_desc_text);
                    timeZoneDescText.setText(TimeZoneInfo.getDescription(tz));
                }
            };
            setListAdapter(mAdapter);
            setListShown(false);
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selection = null;
            if (!TextUtils.isEmpty(mCurFilter)) {
                selection = Cities.ASCII_NAME + " like '%" + mCurFilter + "%' or " 
                        + Cities.NAME + " like '%" + mCurFilter + "%'"; 
            }
            return new CursorLoader(getActivity(), Cities.CONTENT_URI,
                    CITY_PROJECTION, selection, null, null);
        }
        
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.changeCursor(null);
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
        
        private static boolean isHoneycomb() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        }
        
        public void startSearch() {
            if (isHoneycomb()) {
                Class<? extends View> svClass = mSearchView.getClass();
                try {
                    Method method = svClass.getMethod("setIconified", boolean.class);
                    method.invoke(mSearchView, false);
                    mSearchView.requestFocus();
                } catch (Exception e) {
                    Log.e("AddClockActivity", e.getMessage(), e);
                }
            } else {
                mSearchItem.expandActionView();
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            inflater.inflate(R.menu.timezone_list, menu);
            mSearchItem = menu.findItem(R.id.menu_search);
            mSearchView = SearchViewCompat.newSearchView(getActivity());
            if (mSearchView != null) {
                mSearchItem.setActionView(mSearchView);
                SearchViewCompat.setOnQueryTextListener(mSearchView, new OnQueryTextListenerCompat() {
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return TimeZoneListFragment.this.onQueryTextChange(newText);
                    }
                    
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return TimeZoneListFragment.this.onQueryTextSubmit(query);
                    }
                });
            } else { // pre Honeycomb create our own search view
                mSearchItem.setActionView(R.layout.collapsible_search_view);
                mSearchView = mSearchItem.getActionView();
                mSearchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                final EditText editText = (EditText) mSearchView;
                mSearchItem.setOnActionExpandListener(new OnActionExpandListener() {
                    
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }
                    
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        editText.setText("");
                        return true;
                    }
                });
                editText.addTextChangedListener(new TextWatcher() {
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                    
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                            int after) {
                    }
                    
                    @Override
                    public void afterTextChanged(Editable s) {
                        onQueryTextChange(s.toString());
                    }
                });
            }
        }

        private static final String[] ADD_CITY_PROJECTION = {
            Cities.NAME,
            Cities.LATITUDE,
            Cities.LONGITUDE,
            Cities.COUNTRY_CODE,
            Cities.TIMEZONE_ID
        };
        
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            Uri uri = ContentUris.withAppendedId(Cities.CONTENT_URI, id);
            Cursor c = getActivity().getContentResolver().query(uri,
                    ADD_CITY_PROJECTION, null, null, null);
            try {
                c.moveToNext();
                String timeZoneId = c.getString(c.getColumnIndex(Cities.TIMEZONE_ID));
                String city = c.getString(c.getColumnIndex(Cities.NAME));
                String countryCode = c.getString(c.getColumnIndex(Cities.COUNTRY_CODE));
                String country = getCountryName(countryCode);
                int timeDiff = TimeZoneInfo.getTimeDifference(TimeZone.getTimeZone(timeZoneId));
                double latitude = c.getDouble(c.getColumnIndex(Cities.LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Cities.LONGITUDE));
                WorldClock.Clocks.addClock(getActivity(), timeZoneId, city,
                        country, timeDiff, latitude, longitude);
                returnResult(1);
            } finally {
                c.close();
            }
        }
        
        private String getCountryName(String isoCode) {
            try {
                Class<string> c = R.string.class;
                Field field = c.getField("country_" + isoCode);
                int id = field.getInt(null);
                return getResources().getString(id);
            } catch (Exception e) {
                Log.e("AddClockActivity", "failure to get country string id", e);
                return isoCode;
            }
        }
        
        private void returnResult(int resultCode) {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }

        public boolean onQueryTextChange(String newText) {
            mCurFilter = newText;
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }

        public boolean onQueryTextSubmit(String query) {
            return false;
        }
    }
}
