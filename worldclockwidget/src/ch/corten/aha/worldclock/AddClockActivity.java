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

import java.util.Comparator;
import java.util.TimeZone;

import ch.corten.aha.widget.FilterableArrayAdapter;
import ch.corten.aha.worldclock.provider.WorldClock;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class AddClockActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FragmentManager fm = getFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            TimeZoneListFragment list = new TimeZoneListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }
    
    public static class TimeZoneListFragment extends ListFragment implements
            OnQueryTextListener {
        private ArrayAdapter<TimeZoneInfo> mAdapter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            
            mAdapter = new TimeZoneArrayAdapter(getActivity(),
                    TimeZoneInfo.getAllTimeZones());
            mAdapter.sort(new CityComparer());
            setListAdapter(mAdapter);
        }

        @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            MenuItem item = menu.add("Search");
            item.setIcon(android.R.drawable.ic_menu_search);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            SearchView sv = new SearchView(getActivity());
            sv.setOnQueryTextListener(this);
            item.setActionView(sv);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            TimeZoneInfo timeZone = mAdapter.getItem(position);
            WorldClock.Clocks.addClock(getActivity(), timeZone);
            getActivity().setResult(1);
            getActivity().finish();
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (!TextUtils.isEmpty(newText)) {
                mAdapter.getFilter().filter(newText);
            } else {
                mAdapter.getFilter().filter(null);
            }
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }
    }

    private static class TimeZoneArrayAdapter extends
            FilterableArrayAdapter<TimeZoneInfo> {

        public TimeZoneArrayAdapter(Context context, TimeZoneInfo[] objects) {
            super(context, R.layout.time_zone_item, R.id.city_text, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.time_zone_item, null);
            } else {
                view = convertView;
            }
            bindView(position, view);
            return view;
        }

        private void bindView(int position, View view) {
            TimeZoneInfo item = getItem(position);
            TextView cityText = (TextView) view.findViewById(R.id.city_text);
            cityText.setText(item.getCity());
            TextView areaText = (TextView) view.findViewById(R.id.area_text);
            areaText.setText(item.getArea());
            TextView timeDiffText = (TextView) view.findViewById(R.id.time_diff_text);
            TimeZone tz = item.getTimeZone();
            timeDiffText.setText(TimeZoneInfo.getTimeDifferenceString(tz));
            TextView timeZoneDescText = (TextView) view.findViewById(R.id.timezone_desc_text);
            timeZoneDescText.setText(TimeZoneInfo.getDescription(tz));
        }

        @Override
        protected boolean match(String prefixString, TimeZoneInfo value) {
            return value.getCity().toLowerCase().contains(prefixString);
        }
    }
    
    private static class CityComparer implements Comparator<TimeZoneInfo> {
        @Override
        public int compare(TimeZoneInfo lhs, TimeZoneInfo rhs) {
            return lhs.getCity().compareTo(rhs.getCity());
        }
    }

}
