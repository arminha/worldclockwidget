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
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import org.joda.time.DateTimeZone;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

public class EditClockActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        LayoutInflater inflator = (LayoutInflater) actionBar
                .getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflator.inflate(R.layout.actionbar_custom_view_done_discard, null);
        customActionBarView.findViewById(R.id.actionbar_discard)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_done)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFragment().done();
                    }
                });

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
                        | ActionBar.DISPLAY_SHOW_HOME);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        FragmentManager fm = getSupportFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            EditClockFragment fragment = new EditClockFragment();
            fm.beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }

    private EditClockFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (EditClockFragment) fm.findFragmentById(android.R.id.content);
    }

    public static class EditClockFragment extends SherlockFragment {
        private static final boolean SANS_ICE_CREAM = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;

        private static final String[] PROJECTION = {
            Clocks.CITY,
            Clocks.AREA,
            Clocks.LATITUDE,
            Clocks.LONGITUDE,
            Clocks.USE_IN_WIDGET,
            Clocks.TIMEZONE_ID
        };
        private long mId;
        private EditText mCityText;
        private EditText mDescText;
        private EditText mLatitudeText;
        private EditText mLongitudeText;
        private CheckBox mUseInWidgetCheckBox;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (savedInstanceState != null) {
                mId = savedInstanceState.getLong(Clocks._ID);
            } else {
                mId = getActivity().getIntent().getExtras().getLong(Clocks._ID);
            }
            Uri uri = ContentUris.withAppendedId(Clocks.CONTENT_URI, mId);
            Cursor c = getActivity().getContentResolver().query(uri, PROJECTION, null, null, null);
            try {
                c.moveToFirst();

                View view = getView();
                mCityText = (EditText) view.findViewById(R.id.city_edittext);
                mCityText.setText(c.getString(c.getColumnIndex(Clocks.CITY)));
                mDescText = (EditText) view.findViewById(R.id.description_edittext);
                mDescText.setText(c.getString(c.getColumnIndex(Clocks.AREA)));
                mLatitudeText = (EditText) view.findViewById(R.id.latitude_edittext);
                mLatitudeText.setText(printNumber(c.getDouble(c.getColumnIndex(Clocks.LATITUDE))));
                mLongitudeText = (EditText) view.findViewById(R.id.longitude_edittext);
                mLongitudeText.setText(printNumber(c.getDouble(c.getColumnIndex(Clocks.LONGITUDE))));
                mUseInWidgetCheckBox = (CheckBox) view.findViewById(R.id.use_in_widget_checkbox);
                mUseInWidgetCheckBox.setChecked(c.getInt(c.getColumnIndex(Clocks.USE_IN_WIDGET)) != 0);
                String id = c.getString(c.getColumnIndex(Clocks.TIMEZONE_ID));
                DateTimeZone tz = DateTimeZone.forID(id);
                ((TextView) view.findViewById(R.id.time_zone_name)).setText(TimeZoneInfo.getDescription(tz));
                ((TextView) view.findViewById(R.id.time_zone_details)).setText(TimeZoneInfo.getTimeDifferenceString(tz));

                if (SANS_ICE_CREAM) {
                    // capitalize text of the checkbox - pre ics does not support textAllCaps.
                    mUseInWidgetCheckBox.setText(mUseInWidgetCheckBox.getText().toString().toUpperCase());
                }
            } catch (CursorIndexOutOfBoundsException e) {
                // item is not in the database any more
                this.getActivity().finish();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putLong(Clocks._ID, mId);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.edit_clock, null);
        }

        private static double parseNumber(Editable editable) {
            try {
                return Double.parseDouble(editable.toString().trim());
            } catch (NumberFormatException e) {
                Log.e("EditClockActivity", "Failure to parse number!", e);
                return 0.0;
            }
        }

        private static String printNumber(double d) {
            return Double.toString(d);
        }

        public void done() {
            saveChanges();
            getActivity().finish();
        }

        private void saveChanges() {
            Uri uri = ContentUris.withAppendedId(Clocks.CONTENT_URI, mId);
            ContentValues values = new ContentValues();
            values.put(Clocks.CITY, mCityText.getText().toString().trim());
            values.put(Clocks.AREA, mDescText.getText().toString().trim());
            double latitude = parseNumber(mLatitudeText.getText());
            values.put(Clocks.LATITUDE, latitude);
            double longitude = parseNumber(mLongitudeText.getText());
            values.put(Clocks.LONGITUDE, longitude);
            values.put(Clocks.USE_IN_WIDGET, mUseInWidgetCheckBox.isChecked());
            int changed = getActivity().getContentResolver().update(uri, values, null, null);
            getActivity().setResult(changed);
        }
    }
}
