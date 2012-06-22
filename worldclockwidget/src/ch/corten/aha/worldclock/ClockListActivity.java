package ch.corten.aha.worldclock;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import ch.corten.aha.widget.DigitalClock;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class ClockListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        FragmentManager fm = getFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            ClockListFragment list = new ClockListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class ClockListFragment extends ListFragment implements
            LoaderManager.LoaderCallbacks<Cursor> {

        private static final String TAG = "ClockListFragment";
        
        private CursorAdapter mAdapter;

        private static final String[] CLOCKS_PROJECTION = {
            Clocks._ID,
            Clocks.TIMEZONE_ID,
            Clocks.CITY,
            Clocks.AREA,
            Clocks.TIME_DIFF
            };

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText(getText(R.string.add_clock));
            setHasOptionsMenu(true);

            mAdapter = new ResourceCursorAdapter(getActivity(), R.layout.world_clock_item, null) {

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView cityText = (TextView) view.findViewById(R.id.city_text);
                    cityText.setText(cursor.getString(cursor.getColumnIndex(Clocks.CITY)));
                    
                    TextView areaText = (TextView) view.findViewById(R.id.area_text);
                    areaText.setText(cursor.getString(cursor.getColumnIndex(Clocks.AREA)));
                    
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
                }
            };
            setListAdapter(mAdapter);

            setListShown(false);
            
            ListView listView = getListView();
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
                
                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    MenuItem editItem = menu.findItem(R.id.menu_edit);
                    boolean oneSelected = getListView().getCheckedItemCount() == 1;
                    if (editItem.isVisible() == oneSelected) {
                        return false;
                    } else {
                        editItem.setVisible(oneSelected);
                        return true;
                    }
                }
                
                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }
                
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.clock_list_context, menu);
                    return true;
                }
                
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                    case R.id.menu_delete:
                        deleteSelectedItems();
                        mode.finish();
                        return true;
                    case R.id.menu_edit:
                        // TODO call edit intend
                        mode.finish();
                        return true;
                    default:
                        return false;
                    }
                }
                
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position,
                        long id, boolean checked) {
                    int count = getListView().getCheckedItemCount();
                    if (count > 0) {
                        CharSequence format = getResources().getText(R.string.n_selcted_format);
                        mode.setTitle(MessageFormat.format(format.toString(), count));
                    } else {
                        mode.setTitle("");
                    }
                    mode.invalidate();
                }
            });            
            getLoaderManager().initLoader(0, null, this);
        }

        private void deleteSelectedItems() {
            long[] itemIds = getListView().getCheckedItemIds();
            Uri baseUri = Clocks.CONTENT_URI;
            ContentResolver resolver = getActivity().getContentResolver();
            for (long id : itemIds) {
                Log.d(TAG, "delete item " + id);
                resolver.delete(ContentUris.withAppendedId(baseUri, id), null, null);
            }
            getLoaderManager().restartLoader(0, null, this);
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
            default:
                return super.onOptionsItemSelected(item);
            }
        }

        private void addClock() {
            TimeZoneInfo[] allTimeZones = TimeZoneInfo.getAllTimeZones();
            int n = new Random(System.currentTimeMillis()).nextInt(allTimeZones.length);
            Clocks.addClock(getActivity(), allTimeZones[n]);
            getLoaderManager().restartLoader(0, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), Clocks.CONTENT_URI,
                    CLOCKS_PROJECTION, null, null, Clocks.TIME_DIFF  + " ASC, " + Clocks.CITY + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            mAdapter.swapCursor(null);
        }
    }
}
