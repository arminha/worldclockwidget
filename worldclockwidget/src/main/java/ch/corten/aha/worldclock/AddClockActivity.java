/*
 * Copyright (C) 2012 - 2014  Armin Häberling
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

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

import org.joda.time.DateTimeZone;

import ch.corten.aha.worldclock.provider.WorldClock;
import ch.corten.aha.worldclock.provider.WorldClock.Cities;

public class AddClockActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_city);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        private SearchView mSearchView;
        private String mCurFilter;

        private static final String[] CITY_PROJECTION = {
            Cities._ID,
            Cities.NAME,
            Cities.COUNTRY,
            Cities.TIMEZONE_ID
        };

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);

            // use constructor available in gingerbread
            mAdapter = new ResourceCursorAdapter(getActivity(), R.layout.time_zone_item, null) {

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    BindHelper.bindText(view, cursor, R.id.city_text, Cities.NAME);
                    BindHelper.bindText(view, cursor, R.id.area_text, Cities.COUNTRY);
                    TextView timeDiffText = (TextView) view.findViewById(R.id.time_diff_text);
                    DateTimeZone tz = DateTimeZone.forID(cursor.getString(cursor.getColumnIndex(Cities.TIMEZONE_ID)));
                    timeDiffText.setText(TimeZoneInfo.getTimeDifferenceString(tz));
                    TextView timeZoneDescText = (TextView) view.findViewById(R.id.timezone_desc_text);
                    timeZoneDescText.setText(TimeZoneInfo.getDescription(tz));
                }
            };
            setListAdapter(mAdapter);
            setListShown(false);
            getListView().setFastScrollEnabled(true);
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selection = null;
            String[] selectionArgs = null;
            if (!TextUtils.isEmpty(mCurFilter)) {
                String search = "%" + mCurFilter + "%";
                selection = Cities.ASCII_NAME + " like ? or "
                        + Cities.NAME + " like ? or "
                        + Cities.COUNTRY + " like ?";
                selectionArgs = new String[] {search, search, search};
            }
            return new CursorLoader(getActivity(), Cities.CONTENT_URI,
                    CITY_PROJECTION, selection, selectionArgs, null);
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

        public void startSearch() {
            mSearchView.setIconified(false);
            mSearchView.requestFocus();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            inflater.inflate(R.menu.timezone_list, menu);
            MenuItem searchItem = menu.findItem(R.id.menu_search);
            mSearchView = (SearchView) searchItem.getActionView();
            mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    return TimeZoneListFragment.this.onQueryTextChange(newText);
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return TimeZoneListFragment.this.onQueryTextSubmit(query);
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private static final String[] ADD_CITY_PROJECTION = {
            Cities.NAME,
            Cities.LATITUDE,
            Cities.LONGITUDE,
            Cities.COUNTRY,
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
                String country = c.getString(c.getColumnIndex(Cities.COUNTRY));
                int timeDiff = TimeZoneInfo.getTimeDifference(DateTimeZone.forID(timeZoneId));
                double latitude = c.getDouble(c.getColumnIndex(Cities.LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(Cities.LONGITUDE));
                WorldClock.Clocks.addClock(getActivity(), timeZoneId, city,
                        country, timeDiff, latitude, longitude);
                returnResult(1);
            } finally {
                if (c != null) {
                    c.close();
                }
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
