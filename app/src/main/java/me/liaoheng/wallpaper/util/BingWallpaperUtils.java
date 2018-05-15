package me.liaoheng.wallpaper.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.UIUtils;

import net.grandcentrix.tray.AppPreferences;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Locale;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.ui.SettingsActivity;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class BingWallpaperUtils {

    public static String getResolutionImageUrl(Context context, BingWallpaperImage image) {
        return getImageUrl(getResolution(context), image);
    }

    public static String getImageUrl(Context context, BingWallpaperImage image) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);
        return getImageUrl(names[4], image);
    }

    public static String getImageUrl(String resolution, BingWallpaperImage image) {
        String baseUrl = image.getUrlbase();
        return Constants.BASE_URL + baseUrl + "_" + resolution + ".jpg";
    }

    public static boolean getOnlyWifi(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI, true);
    }

    public static String getResolution(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);

        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);

        String resolution = appPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, "0");

        return names[Integer.parseInt(resolution)];
    }

    public static String getSaveResolution(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);

        String resolution = sharedPreferences
                .getString(SettingsActivity.PREF_SAVE_WALLPAPER_RESOLUTION, "0");

        return names[Integer.parseInt(resolution)];
    }

    public static int getAutoModeValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        return Integer.parseInt(sharedPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_AUTO_MODE, "0"));
    }

    public static String getAutoMode(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_auto_mode_name);

        int value = getAutoModeValue(context);

        return names[value];
    }

    public static String getCountryName(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);

        String[] names = context.getResources()
                .getStringArray(R.array.pref_country_names);

        String country = appPreferences
                .getString(SettingsActivity.PREF_COUNTRY, "0");

        return names[Integer.parseInt(country)];
    }

    public static int getCountryValue(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);

        String country = appPreferences
                .getString(SettingsActivity.PREF_COUNTRY, "0");

        return Integer.parseInt(country);
    }

    public static String getUrl(Context context) {
        return getUrl(context, 0, 1);
    }

    public static String getUrl(Context context, int index, int count) {
        int auto = getCountryValue(context);
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if (auto == 1) {
            language = Locale.CHINA.getLanguage();
            country = Locale.CHINA.getCountry();
        }
        String url;
        if (country.equalsIgnoreCase("cn")) {
            url = Constants.CHINA_URL;
        } else {
            url = Constants.GLOBAL_URL + "&setmkt=" + language + "-" + country;
        }
        return String.format(url, index, count);
    }

    /**
     * 定时更新时间
     *
     * @return UTC
     */
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

    /**
     * 传入时间的在当前时间后，则改变转入时间到下一天。
     *
     * @param time Local
     * @return Local
     */
    public static DateTime checkTime(LocalTime time) {
        DateTime now = DateTime.now();
        DateTime set = DateTime.now().withTime(time);
        if (set.toLocalTime().isBefore(now.toLocalTime())) {
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

    public static boolean isEnableLogProvider(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        return appPreferences
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

    /**
     * 判断有无网络正在连接中（查找网络、校验、获取IP等）。
     *
     * @return boolean 不管wifi，还是mobile net，只有当前在连接状态（可有效传输数据）才返回true,反之false。
     */
    public static boolean isConnectedOrConnecting(Context context) {
        return NetworkUtils.isConnectedOrConnecting(context);
    }

    /**
     * 获得导航栏的高度
     *
     * @see <a href="https://stackoverflow.com/questions/20264268/how-do-i-get-the-height-and-width-of-the-android-navigation-bar-programmatically">stackoverflow</a>
     */
    public static int getNavigationBarHeight(Context context) {
        return DisplayUtils.getNavigationBarHeight(context);
    }

    /**
     * 判断是否存在导航栏
     *
     * @return false if physical, true if virtual
     * @see <a href="https://stackoverflow.com/questions/16092431/check-for-navigation-bar">stackoverflow</a>
     */
    public static boolean isNavigationBar(Context context) {
        return DisplayUtils.isNavigationBar(context);
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void setWallpaper(final Context context, @Constants.setWallpaperMode final int mode,
            @Nullable final Callback4<Boolean> callback) {
        setWallpaper(context, "", mode, callback);
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void setWallpaper(final Context context, final String url, @Constants.setWallpaperMode final int mode,
            @Nullable final Callback4<Boolean> callback) {
        if (!BingWallpaperUtils.isConnectedOrConnecting(context)) {
            UIUtils.showToast(context, context.getString(R.string.network_unavailable));
            return;
        }
        // use mobile network show alert
        if (NetworkUtils.isMobileConnected(context)) {
            UIUtils.showYNAlertDialog(context, context.getString(R.string.alert_mobile_data),
                    new Callback4.EmptyCallback<DialogInterface>() {
                        @Override
                        public void onYes(DialogInterface dialogInterface) {
                            if (callback != null) {
                                callback.onYes(true);
                            }
                            BingWallpaperIntentService.start(context, url, mode, false);
                        }
                    });
        } else {
            if (callback != null) {
                callback.onYes(true);
            }
            BingWallpaperIntentService.start(context, url, mode, false);
        }
    }
}
