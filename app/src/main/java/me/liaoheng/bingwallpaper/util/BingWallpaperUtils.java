package me.liaoheng.bingwallpaper.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.github.liaoheng.common.util.Utils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Locale;

import me.liaoheng.bingwallpaper.BuildConfig;
import me.liaoheng.bingwallpaper.R;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.ui.SettingsActivity;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class BingWallpaperUtils {

    public static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        return dm;
    }

    public static String getUrl(Context context, BingWallpaperImage image) {
        String baseUrl = image.getUrlbase();
        String resolution = BingWallpaperUtils.getResolution(context);
        return Constants.BASE_URL + baseUrl + "_" + resolution + ".jpg";
    }

    public static boolean getOnlyWifi(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI, true);
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

    public static String getUrl() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if (country.equalsIgnoreCase("cn")) {
            return Constants.CHINA_URL;
        } else {
            return Utils.appendUrlParameter(Constants.GLOBAL_URL, "setmkt", language + "-" + country);
        }
    }

    @Nullable
    public static LocalTime getDayUpdateTime(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String time = sharedPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME, null);
        if (TextUtils.isEmpty(time)) {
            return null;
        }
        return LocalTime.parse(time);
    }

    public static void clearDayUpdateTime(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME,
                null).apply();
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
        if (!BuildConfig.DEBUG) {
            return false;
        }
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
