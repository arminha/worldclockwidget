package ch.corten.aha.worldclock;

import java.util.TimeZone;

import ch.corten.aha.worldclock.provider.WorldClock;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    
    public static class TimeZoneListFragment extends ListFragment {
        private ArrayAdapter<TimeZoneInfo> mAdapter;
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mAdapter = new TimeZoneArrayAdapter(getActivity(),
                    TimeZoneInfo.getAllTimeZones());
            setListAdapter(mAdapter);
        }
        
        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            TimeZoneInfo timeZone = mAdapter.getItem(position);
            WorldClock.Clocks.addClock(getActivity(), timeZone);
            getActivity().setResult(1);
            getActivity().finish();
        }
    }
    
    private static class TimeZoneArrayAdapter extends ArrayAdapter<TimeZoneInfo> {
        
        public TimeZoneArrayAdapter(Context context, TimeZoneInfo[] objects) {
            super(context, R.layout.time_zone_item, R.id.city_text, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.time_zone_item, null);
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
    }
}
