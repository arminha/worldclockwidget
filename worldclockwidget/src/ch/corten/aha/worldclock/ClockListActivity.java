package ch.corten.aha.worldclock;

import java.util.Date;
import java.util.TimeZone;

import ch.corten.aha.widget.DigitalClock;
import ch.corten.aha.worldclock.provider.WorldClock.Clocks;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
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

        private CursorAdapter mAdapter;

        private static final String[] CLOCKS_PROJECTION = { Clocks._ID,
                Clocks.TIMEZONE_ID, Clocks.CITY, Clocks.AREA, Clocks.TIME_DIFF };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            return super.onCreateView(inflater, container, savedInstanceState);
        }

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
            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), Clocks.CONTENT_URI,
                    CLOCKS_PROJECTION, null, null, Clocks.TIME_DIFF);
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
