/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 Armin Häberling (support for time zones)
 * Copyright (C) 2013 Armin Häberling (pause and resume)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.corten.aha.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Like AnalogClock, but digital.  Shows seconds.
 *
 * FIXME: implement separate views for hours/minutes/seconds, so
 * proportional fonts don't shake rendering
 */
public class DigitalClock extends TextView implements PauseListener {

    Calendar mCalendar;
    private final static String m12 = "h:mm:ss aa";
    private final static String m24 = "H:mm:ss";
    private FormatChangeObserver mFormatChangeObserver;

    private Runnable mTicker;
    private Handler mHandler;

    private boolean mTickerStopped = false;
    private boolean mAttached = false;
    private PauseSource mPauseSource = null;

    private String mFormat;
    private TimeZone mTimeZone;

    public DigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    public TimeZone getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        mTimeZone = timeZone;
        updateClock();
    }

    private void initClock(Context context) {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        setFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        mAttached = true;
        mTickerStopped = false;
        if (mPauseSource != null) {
            mPauseSource.addPauseListener(this);
        }
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            @Override
            public void run() {
                if (mTickerStopped) return;
                updateClock();
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
        mAttached = false;
        if (mPauseSource != null) {
            mPauseSource.removePauseListener(this);
        }
    }

    @Override
    public void onPause() {
        mTickerStopped = true;
    }

    @Override
    public void onResume() {
        if (mAttached) {
            // restart ticker
            if (mTickerStopped) {
                mTickerStopped = false;
                mTicker.run();
            }
        }
    }

    public void setPauseSource(PauseSource pauseSource) {
        if (pauseSource == null) {
            throw new IllegalArgumentException("pauseSource must not be null.");
        }
        if (mPauseSource != null) {
            return;
        }
        mPauseSource = pauseSource;
        if (mAttached) {
            pauseSource.addPauseListener(this);
        }
    }

    /**
     * Pulls 12/24 mode from system settings
     */
    private boolean get24HourMode() {
        return android.text.format.DateFormat.is24HourFormat(getContext());
    }

    private void setFormat() {
        if (get24HourMode()) {
            mFormat = m24;
        } else {
            mFormat = m12;
        }
    }

    private void updateClock() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        java.text.DateFormat df = new SimpleDateFormat(mFormat);
        if (mTimeZone != null) {
            df.setTimeZone(mTimeZone);
        }
        setText(df.format(mCalendar.getTime()));
        invalidate();
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            setFormat();
        }
    }
}
