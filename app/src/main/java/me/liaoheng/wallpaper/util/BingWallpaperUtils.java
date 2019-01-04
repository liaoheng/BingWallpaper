package me.liaoheng.wallpaper.util;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.github.liaoheng.common.util.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.ui.SettingsActivity;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class BingWallpaperUtils {

    public static boolean isCrashReport(Context context) {
        return SettingTrayPreferences.get(context).getBoolean(SettingsActivity.PREF_CRASH_REPORT, true);
    }

    public static String getResolutionImageUrl(Context context, BingWallpaperImage image) {
        return getImageUrl(context, getResolution(context), image);
    }

    public static String getImageUrl(Context context, String resolution, BingWallpaperImage image) {
        String baseUrl = image.getUrlbase();
        return getCountryBaseUrl(context) + baseUrl + "_" + resolution + ".jpg";
    }

    public static boolean getOnlyWifi(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI, true);
    }

    public static String getResolution(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);

        String resolution = SettingTrayPreferences.get(context)
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
        String[] names = context.getResources()
                .getStringArray(R.array.pref_country_names);

        String country = SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_COUNTRY, "0");

        return names[Integer.parseInt(country)];
    }

    public static int getCountryValue(Context context) {
        String country = SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_COUNTRY, "0");

        return Integer.parseInt(country);
    }

    public static String getUrl(Context context) {
        return getUrl(context, 0, 1);
    }

    public static String getUrl(Context context, int index, int count) {
        String url;
        if (isChinaLocale(context)) {
            url = Constants.CHINA_URL;
        } else {
            url = Constants.GLOBAL_URL;
        }
        return String.format(url, index, count);
    }

    public static String getCountryBaseUrl(Context context) {
        if (isChinaLocale(context)) {
            return Constants.CHINA_BASE_URL;
        } else {
            return Constants.GLOBAL_BASE_URL;
        }
    }

    public static Locale getLocale(Context context) {
        int auto = getCountryValue(context);
        Locale country = Locale.getDefault();
        if (auto == 1) {
            country = Locale.CHINA;
        } else if (auto == 2) {
            country = Locale.US;
        }
        return country;
    }

    public static String getAutoLocale(Context context) {
        Locale locale = getLocale(context);
        String country = locale.getCountry();
        String language = locale.getLanguage();
        if (isChinaLocale(locale)) {
            return "zh-cn";
        } else {
            return language + "-" + country;
        }
    }

    public static boolean isChinaLocale(Context context) {
        Locale locale = getLocale(context);
        return isChinaLocale(locale);
    }

    public static boolean isChinaLocale(Locale locale) {
        return Locale.CHINA.getCountry().equalsIgnoreCase(locale.getCountry());
    }

    public static boolean isAlarm(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE, false);
    }

    public static String getAlarmTime(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getString(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME, "");
    }

    /**
     * 定时更新时间
     *
     * @return UTC
     */
    @Nullable
    public static LocalTime getDayUpdateTime(Context context) {
        String time = getAlarmTime(context);
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
        return DateTimeUtils.checkTimeToNextDay(time);
    }

    public static boolean isEnableLog(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_LOG, false);
    }

    public static boolean isEnableLogProvider(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_LOG, false);
    }

    @IntDef(value = {
            AUTOMATIC_UPDATE_TYPE_AUTO,
            AUTOMATIC_UPDATE_TYPE_SYSTEM,
            AUTOMATIC_UPDATE_TYPE_SERVICE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AutomaticUpdateTypeResult {}

    public final static int AUTOMATIC_UPDATE_TYPE_AUTO = 0;
    public final static int AUTOMATIC_UPDATE_TYPE_SYSTEM = 1;
    public final static int AUTOMATIC_UPDATE_TYPE_SERVICE = 2;

    @AutomaticUpdateTypeResult
    public static int getAutomaticUpdateType(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String type = sharedPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_TYPE, "0");
        return Integer.parseInt(type);
    }

    public static String getAutomaticUpdateTypeName(Context context) {
        int type = getAutomaticUpdateType(context);
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_day_fully_automatic_update_type_names);
        return names[type];
    }

    public static boolean isAutomaticUpdateNotification(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_NOTIFICATION, false);
    }

    public static int getAutomaticUpdateInterval(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return Integer.parseInt(sharedPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_INTERVAL, "3"));
    }

    public static boolean isMiuiLockScreenSupport(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, false);
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
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void setWallpaper(final Context context, final @Nullable BingWallpaperImage image,
            @Constants.setWallpaperMode final int mode,
            @Nullable final Callback4<Boolean> callback) {
        if (!NetworkUtils.isConnectedOrConnecting(context)) {
            UIUtils.showToast(context, R.string.network_unavailable);
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
                            BingWallpaperIntentService.start(context, image, mode, false);
                        }
                    });
        } else {
            if (callback != null) {
                callback.onYes(true);
            }
            BingWallpaperIntentService.start(context, image, mode, false);
        }
    }

    public static int getNavigationBarHeight(Context context) {
        if (DisplayUtils.hasNavigationBar(context)) {
            return DisplayUtils.getNavigationBarHeight(context);
        }
        return 0;
    }

    /**
     * Check if vivo is enabled for gestures
     *
     * @return true gestures
     */
    public static boolean vivoNavigationGestureEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "navigation_gesture_on", 0) != 0;
    }

    /**
     * Check if MIUI is enabled for gestures
     *
     * @return true gestures
     */
    public static boolean miuiNavigationGestureEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "force_fsg_nav_bar", 0) != 0;
    }

    /**
     * Check if MIUI is enabled for navigation
     *
     * @return true navigation
     */
    public static boolean emuiNavigationEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "navigationbar_is_min", 0) != 1;
    }

    /**
     * 打开忽略电池优化系统设置界面
     *
     * @see <a href="https://developer.android.com/reference/android/provider/Settings#ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS">android
     * doc</a>
     */
    public static void showIgnoreBatteryOptimizationSetting(Context context) {
        if (!AppUtils.showIgnoreBatteryOptimizationSetting(context)) {
            UIUtils.showToast(context, "No support !");
        }
    }

    public static void setPhoneScreen(Activity context) {
        if (context == null) {
            return;
        }
        if (Constants.Config.isPhone) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void openBrowser(Context context, BingWallpaperImage image) {
        if (image == null) {
            return;
        }
        String url = image.getCopyrightlink();
        if (TextUtils.isEmpty(url) || "javascript:void(0)".equals(url)) {
            url = Constants.BASE_URL;
        } else {
            if (!ValidateUtils.isWebUrl(url)) {
                url = Constants.BASE_URL + url;
            }
            if (!ValidateUtils.isWebUrl(url)) {
                url = Constants.BASE_URL;
            }
        }
        String locale = BingWallpaperUtils.getAutoLocale(context);
        url = Utils.appendUrlParameter(url, "mkt", locale);
        try {
            CustomTabsIntent build = new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary)).build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                build.intent.putExtra(Intent.EXTRA_REFERRER,
                        Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + context.getPackageName()));
            }
            Bundle headers = new Bundle();
            headers.putString("Cookie", String.format(Constants.MKT_HEADER, locale));
            build.intent.putExtra(Browser.EXTRA_HEADERS, headers);
            build.launchUrl(context, Uri.parse(url));
        } catch (Exception ignore) {
            startBrowser(context, url);
        }
    }

    public static void openBrowser(Context context, String url) {
        try {
            new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .build()
                    .launchUrl(context, Uri.parse(url));
        } catch (Exception ignore) {
            startBrowser(context, url);
        }
    }

    public static void startBrowser(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(context.getPackageManager()) == null) {
                throw new IllegalArgumentException("Unknown component");
            }
            context.startActivity(intent);
        } catch (Exception ignore) {
            UIUtils.showToast(context, R.string.unable_open_url);
        }
    }

    public static String getSystemInfo(Context context) {
        int sdk = Build.VERSION.SDK_INT;
        String device = Build.DEVICE;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String romName = ROM.getROM().getName();
        String romVersion = ROM.getROM().getVersion();
        Locale locale = Locale.getDefault();
        Locale autoLocale = getLocale(context);
        String job = BingWallpaperJobManager.check(context);
        boolean alarm = isAlarm(context);
        String alarmTime = getAlarmTime(context);
        String autoSetMode = getAutoMode(context);
        int interval = getAutomaticUpdateInterval(context);

        return "feedback info ------------------------- \n"
                + "sdk: "
                + sdk
                + " device: "
                + device
                + " model: "
                + model
                + " product: "
                + product
                + " rom_name: "
                + romName
                + " rom_version: "
                + romVersion
                + " locale: "
                + locale
                + " auto_locale: "
                + autoLocale
                + " version: "
                + BuildConfig.VERSION_NAME
                + " job: "
                + job
                + " alarm: "
                + alarm
                + " alarm_time: "
                + alarmTime
                + " autoSetMode: "
                + autoSetMode
                + " interval: "
                + interval
                + "\n"
                + "feedback info ------------------------- \n";
    }

    public static void sendFeedback(Context context) {
        String to[] = { "liaohengcn@gmail.com" };
        Intent emailIntent = AppUtils.sendEmail(to,
                context.getString(R.string.app_name) + " : " + context.getString(R.string.menu_main_feedback),
                BingWallpaperUtils.getSystemInfo(context));

        if (BingWallpaperUtils.isEnableLog(context)) {
            File logFile = LogDebugFileUtils.get().getLogFile();
            if (logFile != null && logFile.exists()) {
                Uri path;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    path = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".fileProvider", logFile);
                } else {
                    path = Uri.fromFile(logFile);
                }
                emailIntent.putExtra(Intent.EXTRA_STREAM, path);
            }
        }

        if (emailIntent.resolveActivity(context.getPackageManager()) == null) {
            UIUtils.showToast(context, "No support !");
            return;
        }

        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_email)));
    }

    public static int checkRunningService(Context context) {
        if (NetworkUtils.isConnected(context)) {
            if (BingWallpaperUtils.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiConnected(context)) {
                    return 2;
                }
            }
            //Run only once a day
            if (TasksUtils.isToDaysDoProvider(context, 1,
                    BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)) {
                BingWallpaperIntentService.start(context,
                        BingWallpaperUtils.getAutoModeValue(context));
                return 0;
            } else {
                return 3;
            }
        } else {
            return 1;
        }
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        return getGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public static int getGooglePlayServicesAvailable(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    public static String getGooglePlayServicesAvailableErrorString(int resultCode) {
        return GoogleApiAvailability.getInstance().getErrorString(resultCode);
    }

    public static String getGooglePlayServicesAvailableErrorString(Context context) {
        return getGooglePlayServicesAvailableErrorString(getGooglePlayServicesAvailable(context));
    }

    public static void setBothWallpaper(Context context, File bitmap) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            WallpaperManager.getInstance(context)
                    .setStream(FileUtils.openInputStream(bitmap), null, true,
                            WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
        } else {
            setWallpaper(context, bitmap);
        }
    }

    public static void setWallpaper(Context context, File bitmap) throws IOException {
        WallpaperManager.getInstance(context)
                .setStream(FileUtils.openInputStream(bitmap));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setLockScreenWallpaper(Context context, File bitmap) throws IOException {
        WallpaperManager.getInstance(context)
                .setStream(FileUtils.openInputStream(bitmap), null, true, WallpaperManager.FLAG_LOCK);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setHomeScreenWallpaper(Context context, File bitmap) throws IOException {
        WallpaperManager.getInstance(context)
                .setStream(FileUtils.openInputStream(bitmap), null, true, WallpaperManager.FLAG_SYSTEM);
    }

    public static Observable<Object> clearCache(Context context) {
        return Observable.just(context)
                .subscribeOn(Schedulers.io())
                .map(c -> {
                    GlideApp.get(c).clearDiskCache();
                    NetUtils.get().clearCache();
                    return c;
                }).observeOn(AndroidSchedulers.mainThread()).flatMap(c -> {
                    GlideApp.get(c).clearMemory();
                    return Observable.empty();
                });
    }

    public static String getTranslator(Context context) {
        Locale locale = getLocale(context);
        if (locale.getLanguage().equals(new Locale("pl").getLanguage())) {
            return "Translator : @dekar16";
        }
        return "";
    }
}
