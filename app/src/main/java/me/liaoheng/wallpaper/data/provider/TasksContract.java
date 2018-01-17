package me.liaoheng.wallpaper.data.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author liaoheng
 * @version 2018-01-16 15:43
 */
public class TasksContract {
    public static final String CONTENT_AUTHORITY = "me.liaoheng.wallpaper";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH = "tasks";

    public static final class TaskEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "tasks";

        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_DATE = "date";
    }
}
