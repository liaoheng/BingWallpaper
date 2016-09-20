package me.liaoheng.bingwallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

}
