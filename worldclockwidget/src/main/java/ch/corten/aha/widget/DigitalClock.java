/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 Armin Häberling (support for time zones)
 * Copyright (C) 2013 Armin Häberling (pause and resume)
 * Copyright (C) 2014 Armin Häberling (port to joda-time)
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

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    private static final String M12 = "h:mm:ss aa";
    private static final String M24 = "H:mm:ss";
    private FormatChangeObserver mFormatChangeObserver;

    private Runnable mTicker;
    private Handler mHandler;

    /**
     * Internal state: the clock is running if it is {@link #STATE_ATTACHED_ACTIVE}.
     */
    private int mState = STATE_DETACHED;
    private PauseSource mPauseSource = null;

    private DateTimeFormatter mDateFormat;
    private DateTimeZone mTimeZone;

    public DigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    public DateTimeZone getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        mTimeZone = timeZone;
        setFormat();
        updateClock();
    }

    private void initClock(Context context) {
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
         * requests a tick on the next hard-second boundary.
         */
        mTicker = new Runnable() {
            @Override
            public void run() {
                if (mState != STATE_ATTACHED_ACTIVE) {
                    return;
                }
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
     * Pulls 12/24 mode from system settings.
     */
    private boolean is24HourMode() {
        return android.text.format.DateFormat.is24HourFormat(getContext());
    }

    private void setFormat() {
        mDateFormat = DateTimeFormat.forPattern(is24HourMode() ? M24 : M12).withZone(mTimeZone);
    }

    private void updateClock() {
        setText(mDateFormat.print(DateTimeUtils.currentTimeMillis()));
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
