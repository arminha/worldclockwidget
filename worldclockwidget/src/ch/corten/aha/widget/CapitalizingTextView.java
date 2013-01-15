package ch.corten.aha.widget;

import java.util.Locale;

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
