package ch.corten.aha.worldclock;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import ch.corten.aha.worldclock.provider.WorldClock.Clocks;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class EditClockActivity extends SherlockFragmentActivity {
    
    private EditClockFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.edit_clock);
        
        FragmentManager fm = getSupportFragmentManager();
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            fragment = new EditClockFragment();
            fm.beginTransaction().add(android.R.id.content, fragment).commit();
        }
    }
    
    public void ok(View view) {
        fragment.ok(view);
    }
    
    public void cancel(View view) {
        fragment.cancel(view);
    }
    
    public static class EditClockFragment extends SherlockFragment {
        private static final String[] PROJECTION = {
            Clocks.CITY,
            Clocks.AREA,
            Clocks.LATITUDE,
            Clocks.LONGITUDE,
            Clocks.USE_IN_WIDGET
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
            c.moveToFirst();
            
            mCityText = (EditText) getView().findViewById(R.id.city_edittext);
            mCityText.setText(c.getString(c.getColumnIndex(Clocks.CITY)));
            mDescText = (EditText) getView().findViewById(R.id.description_edittext);
            mDescText.setText(c.getString(c.getColumnIndex(Clocks.AREA)));
            mLatitudeText = (EditText) getView().findViewById(R.id.latitude_edittext);
            mLatitudeText.setText(printFloat(c.getFloat(c.getColumnIndex(Clocks.LATITUDE))));
            mLongitudeText = (EditText) getView().findViewById(R.id.longitude_edittext);
            mLongitudeText.setText(printFloat(c.getFloat(c.getColumnIndex(Clocks.LONGITUDE))));
            mUseInWidgetCheckBox = (CheckBox) getView().findViewById(R.id.use_in_widget_checkbox);
            mUseInWidgetCheckBox.setChecked(c.getInt(c.getColumnIndex(Clocks.USE_IN_WIDGET)) != 0);
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
        
        private static float parseFloat(Editable editable) {
            try {
                return Float.parseFloat(editable.toString().trim());
            } catch (NumberFormatException e) {
                Log.e("EditClockActivity", "Failure to parse number!", e);
                return 0f;
            }
        }
        
        private static String printFloat(float f) {
            return Float.toString(f);
        }
        
        public void cancel(View view) {
            getActivity().finish();
        }
        
        public void ok(View view) {
            saveChanges();
            getActivity().finish();
        }
        
        private void saveChanges() {
            Uri uri = ContentUris.withAppendedId(Clocks.CONTENT_URI, mId);
            ContentValues values = new ContentValues();
            values.put(Clocks.CITY, mCityText.getText().toString().trim());
            values.put(Clocks.AREA, mDescText.getText().toString().trim());
            float latitude = parseFloat(mLatitudeText.getText());
            values.put(Clocks.LATITUDE, latitude);
            float longitude = parseFloat(mLongitudeText.getText());
            values.put(Clocks.LONGITUDE, longitude);
            values.put(Clocks.USE_IN_WIDGET, mUseInWidgetCheckBox.isChecked());
            int changed = getActivity().getContentResolver().update(uri, values, null, null);
            getActivity().setResult(changed);
        }
    }
}
