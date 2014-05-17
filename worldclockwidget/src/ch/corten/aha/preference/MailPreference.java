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

import ch.corten.aha.worldclock.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class MailPreference extends Preference {

    private String mRecipient = "";
    private String mSubject = "";

    public MailPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    public MailPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MailPreference(Context context) {
        super(context);
        updateIntent();
    }

    public String getRecipient() {
        return mRecipient;
    }

    public void setRecipient(String recipient) {
        this.mRecipient = recipient;
        updateIntent();
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
        updateIntent();
    }

    private void updateIntent() {
        String recipient = mRecipient != null ? mRecipient : "";
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", recipient, null));
        if (mSubject != null && !mSubject.isEmpty()) {
            intent.putExtra(Intent.EXTRA_SUBJECT, mSubject);
        }
        setIntent(intent);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.MailPreference, 0, 0);
        try {
            mRecipient = a.getString(R.styleable.MailPreference_recipient);
            mSubject = a.getString(R.styleable.MailPreference_subject);
        } finally {
            a.recycle();
        }
        updateIntent();
    }

}
