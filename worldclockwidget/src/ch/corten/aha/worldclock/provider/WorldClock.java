package ch.corten.aha.worldclock.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class WorldClock {
    public static final String AUTHORITY = "ch.corten.aha.worldclock.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    
    public static class Clocks implements BaseColumns {
        static final String TABLE_NAME = "clocks";
        
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;
        
        public static final String _ID = BaseColumns._ID;
        public static final String TIMEZONE_ID = "timezone_id";
        public static final String CITY = "city";
        public static final String AREA = "area";
        public static final String TIME_DIFF = "time_diff";
    }
}
