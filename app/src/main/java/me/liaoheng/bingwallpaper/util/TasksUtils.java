package me.liaoheng.bingwallpaper.util;

import android.content.Context;
import com.github.liaoheng.common.util.PreferencesUtils;
import com.github.liaoheng.common.util.SystemRuntimeException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

/**
 * @author liaoheng
 * @version 2016-10-29 13:01
 */
public class TasksUtils {
    private static PreferencesUtils mTaskPreferencesUtils;

    public static void init(Context context) {
        PreferencesUtils.init(context);
        mTaskPreferencesUtils = PreferencesUtils.from(TASK_FILE_NAME);
    }

    private final static String TASK_FILE_NAME = "COM_GITHUB_LIAOHENG_COMMON_TASKS";

    public static int taskCount(int count, String tag) {
        if (count < 1) {
            throw new SystemRuntimeException("count < 1 ");
        }
        int c = mTaskPreferencesUtils.getInt(tag, -1);
        if (c != -1) {
            if (c == 0) {
                markTaskDone(tag);
                return c;
            }
            c--;
            mTaskPreferencesUtils.put(tag, c).apply();
            return c;
        }
        mTaskPreferencesUtils.put(tag, count).apply();
        return -1;
    }

    public static void markTaskDone(String tag) {
        mTaskPreferencesUtils.remove(tag);
    }

    public static boolean isToDaysDo(int day, String tag) {
        long data = mTaskPreferencesUtils.getLong(tag, -1);
        if (data == -1){
            return true;
        }

        DateTime dateTime = new DateTime(data, DateTimeZone.UTC);
        int days = Days.daysBetween(dateTime.toLocalDate(), DateTime.now().toLocalDate()).getDays();
        return days >= day;
    }

    public static void markDone(String tag) {
        mTaskPreferencesUtils.put(tag, DateTime.now(DateTimeZone.UTC).getMillis()).apply();
    }
}
