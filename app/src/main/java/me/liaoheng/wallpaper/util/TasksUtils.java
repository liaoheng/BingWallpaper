package me.liaoheng.wallpaper.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.github.liaoheng.common.util.PreferencesUtils;
import com.github.liaoheng.common.util.SystemDataException;
import com.github.liaoheng.common.util.SystemRuntimeException;

import org.joda.time.DateTime;
import org.joda.time.Days;

import me.liaoheng.wallpaper.data.provider.TasksContract;

/**
 * @author liaoheng
 * @version 2016-10-29 13:01
 */
public class TasksUtils {
    private static PreferencesUtils mTaskPreferencesUtils;

    public static void init(Context context) {
        mTaskPreferencesUtils = PreferencesUtils.from(context, TASK_FILE_NAME);
    }

    private final static String TASK_FILE_NAME = "com.github.liaoheng.common_tasks";
    private final static String TASK_ONE = "task_one";

    public static boolean isOne() {
        return mTaskPreferencesUtils.getBoolean(TASK_ONE, true);
    }

    public static void markOne() {
        mTaskPreferencesUtils.putBoolean(TASK_ONE, false).apply();
    }

    public static int taskCount(int count, String tag) {
        if (count < 1) {
            throw new SystemRuntimeException(new SystemDataException("count < 1 "));
        }
        int c = mTaskPreferencesUtils.getInt(tag, -1);
        if (c != -1) {
            if (c == 0) {
                markTaskDone(tag);
                return c;
            }
            c--;
            mTaskPreferencesUtils.putInt(tag, c).apply();
            return c;
        }
        mTaskPreferencesUtils.putInt(tag, count).apply();
        return -1;
    }

    public static void markTaskDone(String tag) {
        mTaskPreferencesUtils.remove(tag);
    }

    public static boolean isToDaysDo(int day, String tag) {
        long date = mTaskPreferencesUtils.getLong(tag, -1);
        return isToDaysDo(date, day);
    }

    public static boolean isToDaysDoProvider(Context context, int day, String tag) {
        long date;
        try (Cursor cursor = context.getContentResolver()
                .query(TasksContract.TaskEntry.CONTENT_URI, null, TasksContract.TaskEntry.COLUMN_TAG + "=?",
                        new String[] { tag }, null)) {
            if (cursor != null && cursor.moveToNext()) {
                date = cursor.getLong(2);
            } else {
               return true;
            }
        }
        return isToDaysDo(date, day);
    }

    public static boolean isToDaysDo(long date, int day) {
        if (date == -1) {
            return true;
        }

        DateTime next = new DateTime(date);
        DateTime now = DateTime.now();
        return isToDaysDo(next, now, day);
    }

    /**
     * 上一次操作与现在操作的距离，单位天
     *
     * @param next local date
     * @param now  local date
     * @param day  距离，天
     * @return true，超过或等于@param day
     */
    public static boolean isToDaysDo(DateTime next, DateTime now, int day) {
        int days = Days.daysBetween(next.toLocalDate(), now.toLocalDate()).getDays();
        return days >= day;
    }

    public static void markDone(String tag) {
        mTaskPreferencesUtils.putLong(tag, DateTime.now().getMillis()).apply();
    }

    public static void markDoneProvider(Context context, String tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TasksContract.TaskEntry.COLUMN_TAG, tag);
        contentValues.put(TasksContract.TaskEntry.COLUMN_DATE, DateTime.now().getMillis());
        context.getContentResolver()
                .update(TasksContract.TaskEntry.CONTENT_URI, contentValues, TasksContract.TaskEntry.COLUMN_TAG + "=?",
                        new String[] { tag });
    }

    public static void deleteDoneProvider(Context context,String tag){
        context.getContentResolver().delete(TasksContract.TaskEntry.CONTENT_URI,TasksContract.TaskEntry.COLUMN_TAG + "=?",
                new String[] { tag });
    }
}
