package me.liaoheng.bingwallpaper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import me.liaoheng.bingwallpaper.R;
import me.liaoheng.bingwallpaper.ui.SettingsActivity;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class SettingsUtils {

    public static boolean getOnlyWifi(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI, true);
    }

    public static boolean isEnableDayUpdate(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE, true);
    }

    public static String getResolution(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);

        String resolution = sharedPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, "0");

        return names[Integer.parseInt(resolution)];
    }

    public static String getUrlValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        String value = sharedPreferences.getString(SettingsActivity.PREF_SET_WALLPAPER_URL, "0");

        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_url_name);

        return names[Integer.parseInt(value)];
    }

    public static String getUrl(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        String value = sharedPreferences.getString(SettingsActivity.PREF_SET_WALLPAPER_URL, "0");

        if (value.equals("0")) {
            return Constants.CHINA_URL;
        } else {
            return Constants.GLOBAL_URL;
        }
    }

    public static LocalTime getDayUpdateTime(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String time = sharedPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME,
                        "00:00:00.000");
        return LocalTime.parse(time);
    }

    public static DateTime checkTime(LocalTime time) {
        DateTime now = DateTime.now();
        if (time.isBefore(now.toLocalTime())) {
            now = now.plusDays(1);
        }
        return new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                time.getHourOfDay(), time.getMinuteOfHour());
    }

}
