package me.liaoheng.bingwallpaper.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import me.liaoheng.bingwallpaper.R;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.ui.SettingsActivity;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class Utils {

    public static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm;
    }

    public static String getUrl(Context context, BingWallpaperImage image) {
        String baseUrl = image.getUrlbase();
        String resolution = Utils.getResolution(context);
        return Constants.BASE_URL + baseUrl + "_" + resolution + ".jpg";
    }

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

    public static boolean isEnableLog(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_LOG, false);
    }

    public static void disabledReceiver(Context context, String receiver) {
        settingReceiver(context, receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    public static void enabledReceiver(Context context, String receiver) {
        settingReceiver(context, receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    public static void settingReceiver(Context context, String receiver, int newState) {
        ComponentName componentName = new ComponentName(context, receiver);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
    }

}
