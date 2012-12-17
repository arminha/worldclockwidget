package ch.corten.aha.widget;

import android.widget.RemoteViews;

public class RemoteViewUtil {
    public static void setBackgroundColor(RemoteViews rv, int viewId, int color) {
        rv.setInt(viewId, "setBackgroundColor", color);
    }

    public static void setBackground(RemoteViews rv, int viewId, int resource) {
        rv.setInt(viewId, "setBackgroundResource", resource);
    }

    public static void setTextClockTimeZone(RemoteViews rv, int viewId, String timeZone) {
        rv.setString(viewId, "setTimeZone", timeZone);
    }
}
