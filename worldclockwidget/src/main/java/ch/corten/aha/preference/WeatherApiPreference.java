/*
 * Copyright (C) 2014  Armin Häberling
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
 *
 *
 *
 * Description: this is a new addition to
 *
 */

package ch.corten.aha.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ch.corten.aha.worldclock.BuildConfig;
import ch.corten.aha.worldclock.R;

public class WeatherApiPreference extends DialogPreference {

    public WeatherApiPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WeatherApiPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        //Set the api key from Api key
        setApikey(view);
        makeClickable(view, R.id.openweather_signup);
    }

    private void makeClickable(View view, int id) {
        View v = view.findViewById(id);
        if (v != null && v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
    private void setApikey(View view) {
        View v = view.findViewById(R.id.Api_key_value);
        if (v != null && v instanceof TextView) {
            TextView Owm_api_key_text = (TextView) v;

            //Owm_api_key_text.setText("--set your new apikey here--");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
            String OWM_API_KEY =prefs.getString(this.getContext().getString(R.string.new_api_key),null);
            Owm_api_key_text.setText(OWM_API_KEY);
            //#TODO: // FIXME: 2/25/2017
        }
    }
}
