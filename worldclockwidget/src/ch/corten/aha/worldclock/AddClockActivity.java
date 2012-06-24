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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TimeZone;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;

import ch.corten.aha.widget.FilterableArrayAdapter;
import ch.corten.aha.worldclock.provider.WorldClock;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class AddClockActivity extends SherlockFragmentActivity {

    private TimeZoneListFragment mListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_clock);
        
        FragmentManager fm = getSupportFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            mListFragment = new TimeZoneListFragment();
            fm.beginTransaction().add(android.R.id.content, mListFragment).commit();
        }
    }
    
    @Override
    public boolean onSearchRequested() {
        mListFragment.startSearch();
        return true;
    }
    
    public static class TimeZoneListFragment extends SherlockListFragment {
        private ArrayAdapter<TimeZoneInfo> mAdapter;
        private View mSearchView;
        private MenuItem mSearchItem;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            
            mAdapter = new TimeZoneArrayAdapter(getActivity(),
                    TimeZoneInfo.getAllTimeZones());
            mAdapter.sort(new CityComparer());
            setListAdapter(mAdapter);
        }

        private void setupCab() {
//            ListView listView = getListView();
//            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//            listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
//                
//                @Override
//                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                    return false;
//                }
//                
//                @Override
//                public void onDestroyActionMode(ActionMode mode) {
//                }
//                
//                @Override
//                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                    MenuInflater menuInflater = mode.getMenuInflater();
//                    menuInflater.inflate(R.menu.timezone_list_context, menu);
//                    return true;
//                }
//                
//                @Override
//                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                    switch (item.getItemId()) {
//                    case R.id.menu_add:
//                        addSelected();
//                        mode.finish();
//                        return true;
//                    default:
//                        return false;
//                    }
//                }
//                
//                @Override
//                public void onItemCheckedStateChanged(ActionMode mode, int position,
//                        long id, boolean checked) {
//                    int count = getListView().getCheckedItemCount();
//                    if (count > 0) {
//                        CharSequence format = getResources().getText(R.string.n_selcted_format);
//                        mode.setTitle(MessageFormat.format(format.toString(), count));
//                    } else {
//                        mode.setTitle("");
//                    }
//                }
//            });
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

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            final TimeZoneInfo timeZone = mAdapter.getItem(position);
            WorldClock.Clocks.addClock(getActivity(), timeZone);
            returnResult(1);
        }
        
        private void addSelected() {
            final SparseBooleanArray positions = getListView().getCheckedItemPositions();
            final ArrayList<TimeZoneInfo> timeZones = new ArrayList<TimeZoneInfo>();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                if (positions.get(i)) {
                    timeZones.add(mAdapter.getItem(i));
                }
            }
            final Context context = getActivity();
            for (TimeZoneInfo timeZone : timeZones) {
                WorldClock.Clocks.addClock(context, timeZone);
            }
            returnResult(timeZones.size());
        }

        private void returnResult(int resultCode) {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }

        public boolean onQueryTextChange(String newText) {
            if (!TextUtils.isEmpty(newText)) {
                mAdapter.getFilter().filter(newText);
            } else {
                mAdapter.getFilter().filter(null);
            }
            return true;
        }

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
