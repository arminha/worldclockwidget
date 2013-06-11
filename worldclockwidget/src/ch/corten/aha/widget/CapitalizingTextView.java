/*
 * Copyright (C) 2013  Armin Häberling
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

package ch.corten.aha.widget;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class CapitalizingTextView extends TextView {
    private static final boolean SANS_ICE_CREAM = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    private static final boolean IS_GINGERBREAD = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;

    private static final int[] R_styleable_TextView = new int[] {
        android.R.attr.textAllCaps
    };
    private static final int R_styleable_TextView_textAllCaps = 0;

    private boolean mAllCaps;

    public CapitalizingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CapitalizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R_styleable_TextView, defStyle, 0);
        mAllCaps = a.getBoolean(R_styleable_TextView_textAllCaps, true);
        a.recycle();

        if (SANS_ICE_CREAM && mAllCaps && this.getText() != null) {
            this.setText(this.getText());
        }
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void setText(CharSequence text, BufferType type) {
        if (SANS_ICE_CREAM && mAllCaps && text != null) {
            if (IS_GINGERBREAD) {
                try {
                    super.setText(text.toString().toUpperCase(Locale.ROOT), type);
                } catch (NoSuchFieldError e) {
                    //Some manufacturer broke Locale.ROOT. See #572.
                    super.setText(text.toString().toUpperCase(), type);
                }
            } else {
                super.setText(text.toString().toUpperCase(), type);
            }
        } else {
            super.setText(text, type);
        }
    }
}
