package me.liaoheng.bingwallpaper.util;

import android.content.Context;
import com.github.liaoheng.common.util.PreferencesUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

/**
 * @author liaoheng
 * @version 2016-10-29 13:01
 */
public class TasksUtils {
    private static PreferencesUtils mTaskPreferencesUtils;
    private static PreferencesUtils mDoPreferencesUtils;

    public static void init(Context context) {
        PreferencesUtils.init(context);
        mTaskPreferencesUtils = PreferencesUtils.from(TASK_FILE_NAME);
        mDoPreferencesUtils = PreferencesUtils.from(DO_FILE_NAME);
    }

    private final static String TASK_FILE_NAME = "COM_GITHUB_LIAOHENG_COMMON_TASKS";
    private final static String DO_FILE_NAME   = "COM_GITHUB_LIAOHENG_COMMON_TASKS_DO";

    public static void toDo(String tag) {
        mDoPreferencesUtils.put(tag, 0);
        mTaskPreferencesUtils.put(tag, DateTime.now(DateTimeZone.UTC).getMillis()).apply();
    }

    public static boolean isToDaysDo(int day, String tag) {
        int tagDo = mDoPreferencesUtils.getInt(tag, -1);
        if (tagDo == -1) {
            toDo(tag);
            return true;
        }

        if (tagDo == 0) {
            return true;
        }

        long data = mTaskPreferencesUtils.getLong(tag, -1);
        DateTime dateTime = new DateTime(data, DateTimeZone.UTC);
        int days = Days.daysBetween(dateTime.toLocalDate(), DateTime.now().toLocalDate()).getDays();
        return days >= day;
    }

    public static void markDone(String tag) {
        mDoPreferencesUtils.put(tag, 1);
        mTaskPreferencesUtils.remove(tag);
    }
}
