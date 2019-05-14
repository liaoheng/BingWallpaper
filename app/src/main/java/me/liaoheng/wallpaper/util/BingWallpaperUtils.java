package me.liaoheng.wallpaper.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
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
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.ui.SettingsActivity;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Objects;

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
        return getBaseUrl(context) + baseUrl + "_" + resolution + ".jpg";
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
        return names[Integer.parseInt(Objects.requireNonNull(resolution))];
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
        return Integer.parseInt(SettingTrayPreferences.get(context)
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

    public static boolean isAutoCountry(Context context) {
        return getCountryValue(context) == 0;
    }

    public static String getUrl(Context context) {
        return getUrl(context, 0, 1, getAutoLocale(context));
    }

    public static String getUrl(Context context, int index, int count, String mtk) {
        String url;
        if (isAutoCountry(context)) {
            url = Constants.LOCAL_API_URL;
        } else {
            url = Constants.GLOBAL_API_URL;
        }
        return String.format(url, index, count, mtk);
    }

    public static String getBaseUrl(Context context) {
        if (isAutoCountry(context)) {
            return Constants.LOCAL_BASE_URL;
        } else {
            return Constants.GLOBAL_BASE_URL;
        }
    }

    public static Locale getLocale(Context context) {
        int auto = getCountryValue(context);
        Locale locale = getCurrentLocale(context);
        switch (auto) {
            case 1:
                return Locale.CHINA;
            case 2:
                return Locale.US;
            case 3:
                return Locale.UK;
            case 4:
                return Locale.FRANCE;
            case 5:
                return Locale.GERMANY;
            case 6:
                return Locale.JAPAN;
            default:
                return locale;
        }
    }

    public static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    public static String getAutoLocale(Context context) {
        Locale locale = getLocale(context);
        String country = locale.getCountry();
        String language = locale.getLanguage();
        return language + "-" + country;
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
        return Integer.parseInt(Objects.requireNonNull(type));
    }

    public static String getAutomaticUpdateTypeName(Context context) {
        int type = getAutomaticUpdateType(context);
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_day_fully_automatic_update_type_names);
        return names[type];
    }

    public static boolean isAutomaticUpdateNotification(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_NOTIFICATION, true);
    }

    // hour
    public static int getAutomaticUpdateInterval(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return Integer.parseInt(Objects.requireNonNull(sharedPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_INTERVAL,
                        String.valueOf(Constants.JOB_SCHEDULER_PERIODIC))));
    }

    public static boolean isMiuiLockScreenSupport(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, false);
    }

    public static boolean isPixabaySupport(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_PREF_PIXABAY_SUPPORT, false);
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
        if (!BingWallpaperUtils.isConnected(context)) {
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

    public static int getNavigationBarPadding(Context context) {
        int bottomPadding = BingWallpaperUtils.getNavigationBarHeight(context);
        if (ROM.getROM().isEmui()) {
            if (!BingWallpaperUtils.emuiNavigationEnabled(context)) {
                bottomPadding = 0;
            }
        } else if (ROM.getROM().isVivo()) {
            if (BingWallpaperUtils.vivoNavigationGestureEnabled(context)) {
                bottomPadding = 0;
            }
        } else if (ROM.getROM().isMiui()) {
            if (BingWallpaperUtils.miuiNavigationGestureEnabled(context)) {
                bottomPadding = 0;
            }
        }
        return bottomPadding;
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
     * Check if EMUI is enabled for navigation
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
        String baseUrl = getBaseUrl(context);
        if (TextUtils.isEmpty(url) || "javascript:void(0)".equals(url)) {
            url = baseUrl;
        } else {
            if (!ValidateUtils.isWebUrl(url)) {
                url = baseUrl + url;
            }
            if (!ValidateUtils.isWebUrl(url)) {
                url = baseUrl;
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
            CustomTabsIntent build = new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                build.intent.putExtra(Intent.EXTRA_REFERRER,
                        Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + context.getPackageName()));
            }
            build.launchUrl(context, Uri.parse(url));
        } catch (Exception ignore) {
            startBrowser(context, url);
        }
    }

    public static void startBrowser(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(context.getPackageManager()) == null) {
                UIUtils.showToast(context, R.string.unable_open_url);
                return;
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

    public static int checkRunningService(Context context, String TAG) {
        if (isConnected(context)) {
            if (getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiConnected(context)) {
                    return 2;
                }
            }
            if (BingWallpaperUtils.isPixabaySupport(context)) {
                if (TasksUtils.isToDaysDoProvider(context, 1,
                        BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE)) {
                    BingWallpaperIntentService.start(context,
                            BingWallpaperUtils.getAutoModeValue(context));
                    return 0;
                } else {
                    return 3;
                }
            } else {
                Utils.addSubscribe(BingWallpaperNetworkClient.getBingWallpaper(context),
                        new Callback.EmptyCallback<BingWallpaperImage>() {
                            @Override
                            public void onSuccess(BingWallpaperImage image) {
                                if (getLastWallpaperImageUrl(context).equals(image.getUrlbase())) {
                                    if (isEnableLog(context)) {
                                        LogDebugFileUtils.get().i(TAG, "Equals last skip");
                                    }
                                    return;
                                }
                                BingWallpaperIntentService.start(context,
                                        BingWallpaperUtils.getAutoModeValue(context));
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (isEnableLog(context)) {
                                    LogDebugFileUtils.get().e(TAG, "Check error", e);
                                }
                            }
                        });
                return 0;
            }
        } else {
            return 1;
        }
    }

    public static void runningService(Context context, String TAG) {
        boolean enableLog = isEnableLog(context);
        int state = checkRunningService(context, TAG);
        if (state == 1) {
            L.alog().d(TAG, "isConnectedOrConnecting :false");
            if (enableLog) {
                LogDebugFileUtils.get()
                        .i(TAG, "Network unavailable");
            }
        } else if (state == 2) {
            L.alog().d(TAG, "isWifiConnected :false");
            if (enableLog) {
                LogDebugFileUtils.get()
                        .i(TAG, "Network not wifi");
            }
        } else if (state == 3) {
            L.alog().d(TAG, "isToDaysDo :false");
            if (enableLog) {
                LogDebugFileUtils.get()
                        .i(TAG, "Already executed");
            }
        }
    }

    public static boolean isConnected(Context context) {
        NetworkInfo[] nets = NetworkUtils.getConnManager(context).getAllNetworkInfo();
        if (nets != null && nets.length > 0) {
            for (NetworkInfo net : nets) {
                if (net.isConnected()) {
                    return true;
                }
            }
        }
        return false;
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

    @SuppressLint("InlinedApi")
    public static boolean setBothWallpaper(Context context, File file) throws IOException {
        return setWallpaper(context, file, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
    }

    public static boolean setWallpaper(Context context, File file, int which) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try (InputStream fileInputStream = FileUtils.openInputStream(file)) {
                return WallpaperManager.getInstance(context)
                        .setStream(fileInputStream, null, true, which) != 0;
            }
        } else {
            return setWallpaper(context, file);
        }
    }

    public static boolean setWallpaper(Context context, File file) throws IOException {
        try (InputStream fileInputStream = FileUtils.openInputStream(file)) {
            WallpaperManager.getInstance(context).setStream(fileInputStream);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean setLockScreenWallpaper(Context context, File file) throws IOException {
        return setWallpaper(context, file, WallpaperManager.FLAG_LOCK);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean setHomeScreenWallpaper(Context context, File file) throws IOException {
        return setWallpaper(context, file, WallpaperManager.FLAG_SYSTEM);
    }

    public static Observable<Object> clearCache(Context context) {
        return Observable.just(context)
                .subscribeOn(Schedulers.io())
                .map(c -> {
                    GlideApp.get(c).clearDiskCache();
                    NetUtils.get().clearCache();
                    return c;
                }).observeOn(AndroidSchedulers.mainThread()).map(c -> {
                    GlideApp.get(c).clearMemory();
                    return "ok";
                });
    }

    public static Observable<String> clearNetCache() {
        return Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(c -> {
                    NetUtils.get().clearCache();
                    return c;
                }).observeOn(AndroidSchedulers.mainThread());
    }

    public static String getTranslator(Context context) {
        Locale locale = getLocale(context);
        if (locale.getLanguage().equals(new Locale("pl").getLanguage())) {
            return "Translator : @dekar16";
        } else if (locale.getLanguage().equals(new Locale("ru").getLanguage())) {
            return "Translator : @tullev";
        }
        return "";
    }

    public static void setLastWallpaperImageUrl(Context context, String url) {
        SettingTrayPreferences.get(context).put(Constants.PREF_LAST_WALLPAPER_IMAGE_URL, url);
    }

    public static String getLastWallpaperImageUrl(Context context) {
        return SettingTrayPreferences.get(context).getString(Constants.PREF_LAST_WALLPAPER_IMAGE_URL, "");
    }
}
