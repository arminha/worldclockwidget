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

import java.text.DateFormat;
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

    private static final int STATE_DETACHED = 0;
    private static final int STATE_ATTACHED_ACTIVE = 1;
    private static final int STATE_ATTACHED_PAUSED = 2;

    Calendar mCalendar;
    private final static String m12 = "h:mm:ss aa";
    private final static String m24 = "H:mm:ss";
    private FormatChangeObserver mFormatChangeObserver;

    private Runnable mTicker;
    private Handler mHandler;

    /**
     * Internal state: the clock is running if it is {@link #STATE_ATTACHED_ACTIVE}
     */
    private int mState = STATE_DETACHED;
    private PauseSource mPauseSource = null;

    private DateFormat mDateFormat;
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
        registerObserver();
        setFormat();
    }

    private void registerObserver() {
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);
    }

    private void unregisterObserver() {
        getContext().getContentResolver().unregisterContentObserver(mFormatChangeObserver);
    }

    @Override
    protected void onAttachedToWindow() {
        mState = STATE_ATTACHED_ACTIVE;
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
                if (mState != STATE_ATTACHED_ACTIVE) return;
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
        mState = STATE_DETACHED;
        if (mPauseSource != null) {
            mPauseSource.removePauseListener(this);
        }
    }

    @Override
    public void onPause() {
        if (mState == STATE_ATTACHED_ACTIVE) {
            mState = STATE_ATTACHED_PAUSED;
            unregisterObserver();
        }
    }

    @Override
    public void onResume() {
        if (mState == STATE_ATTACHED_PAUSED) {
            mState = STATE_ATTACHED_ACTIVE;
            mTicker.run();
            registerObserver();
            setFormat();
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
        if (mState != STATE_DETACHED) {
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
            mDateFormat = new SimpleDateFormat(m24);
        } else {
            mDateFormat = new SimpleDateFormat(m12);
        }
    }

    private void updateClock() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        if (mTimeZone != null) {
            mDateFormat.setTimeZone(mTimeZone);
        }
        setText(mDateFormat.format(mCalendar.getTime()));
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
