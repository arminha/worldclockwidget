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
 */

package ch.corten.aha.preference;

import ch.corten.aha.worldclock.BuildConfig;
import ch.corten.aha.worldclock.R;
import android.content.Context;
import android.preference.DialogPreference;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class AboutPreference extends DialogPreference {

    public AboutPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        setVersionText(view);
        makeClickable(view, R.id.project_page_text);
        makeClickable(view, R.id.developer_text);
        makeClickable(view, R.id.acknowledgement_weather_text);
        makeClickable(view, R.id.acknowledgement_icons_text);
        makeClickable(view, R.id.acknowledgement_geonames_text);
    }

    private void makeClickable(View view, int id) {
        View v = view.findViewById(id);
        if (v != null && v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void setVersionText(View view) {
        View v = view.findViewById(R.id.version_text);
        if (v != null && v instanceof TextView) {
            TextView versionText = (TextView) v;
            String versionName = BuildConfig.VERSION_NAME;
            int versionCode = BuildConfig.VERSION_CODE;
            String buildTag = BuildConfig.BUILD_TAG;
            versionText.setText(versionName + " (" + versionCode + "-" + buildTag + ")");
        }
    }

}
