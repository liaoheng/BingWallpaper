package me.liaoheng.wallpaper.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;

import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Locale;

import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.ui.SettingsActivity;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class BingWallpaperUtils {

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
        SettingTrayPreferences appPreferences = SettingTrayPreferences.get(context);

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
        SettingTrayPreferences appPreferences = SettingTrayPreferences.get(context);

        String[] names = context.getResources()
                .getStringArray(R.array.pref_country_names);

        String country = appPreferences
                .getString(SettingsActivity.PREF_COUNTRY, "0");

        return names[Integer.parseInt(country)];
    }

    public static int getCountryValue(Context context) {
        SettingTrayPreferences appPreferences = SettingTrayPreferences.get(context);

        String country = appPreferences
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
        DateTime now = DateTime.now();
        DateTime set = DateTime.now().withTime(time);
        if (set.toLocalTime().isBefore(now.toLocalTime())) {
            now = now.plusDays(1);
        }
        return new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                time.getHourOfDay(), time.getMinuteOfHour(), DateTimeZone.getDefault());
    }

    public static boolean isEnableLog(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_LOG, false);
    }

    public static boolean isEnableLogProvider(Context context) {
        SettingTrayPreferences appPreferences = SettingTrayPreferences.get(context);
        return appPreferences
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

    public static boolean isMiuiLockScreenSupport(Context context) {
        SettingTrayPreferences appPreferences = SettingTrayPreferences.get(context);
        return appPreferences
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
     * 判断有无网络正在连接中（查找网络、校验、获取IP等）。
     *
     * @return boolean 不管wifi，还是mobile net，只有当前在连接状态（可有效传输数据）才返回true,反之false。
     */
    @Deprecated
    public static boolean isConnectedOrConnecting(Context context) {
        return NetworkUtils.isConnectedOrConnecting(context);
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void setWallpaper(final Context context, @Constants.setWallpaperMode final int mode,
            @Nullable final Callback4<Boolean> callback) {
        setWallpaper(context, null, mode, callback);
    }

    /**
     * @param mode 0. both , 1. home , 2. lock
     */
    public static void setWallpaper(final Context context, final @Nullable BingWallpaperImage image,
            @Constants.setWallpaperMode final int mode,
            @Nullable final Callback4<Boolean> callback) {
        if (!NetworkUtils.isConnectedOrConnecting(context)) {
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
        if (hasNavigationBar(context)) {
            return DisplayUtils.getNavigationBarHeight(context);
        }
        return 0;
    }

    /**
     * 判断是否存在虚拟导航栏
     *
     * @return false if physical, true if virtual
     * @see <a href="https://stackoverflow.com/questions/16092431/check-for-navigation-bar">stackoverflow</a>
     * @see <a href="https://windysha.github.io/2018/02/07/Android-APP%E9%80%82%E9%85%8D%E5%85%A8%E9%9D%A2%E5%B1%8F%E6%89%8B%E6%9C%BA%E7%9A%84%E6%8A%80%E6%9C%AF%E8%A6%81%E7%82%B9/">Android-APP适配全面屏手机的技术要点</a>
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("PrivateApi")
    public static boolean isNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources resources = context.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = resources.getBoolean(id);
        } else {    // Check for keys
            try {
                //反射获取SystemProperties类，并调用它的get方法
                Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
                Method m = systemPropertiesClass.getMethod("get", String.class);
                String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
                if ("1".equals(navBarOverride)) {
                    hasNavigationBar = false;
                } else if ("0".equals(navBarOverride)) {
                    hasNavigationBar = true;
                }
            } catch (Exception ignored) {
            }
        }
        return hasNavigationBar;
    }

    /**
     * 判断设备是否存在NavigationBar
     *
     * @return true 存在, false 不存在
     * @see <a href="https://windysha.github.io/2018/02/07/Android-APP%E9%80%82%E9%85%8D%E5%85%A8%E9%9D%A2%E5%B1%8F%E6%89%8B%E6%9C%BA%E7%9A%84%E6%8A%80%E6%9C%AF%E8%A6%81%E7%82%B9/">Android-APP适配全面屏手机的技术要点</a>
     */
    @SuppressLint("PrivateApi")
    public static boolean hasNavigationBar(Context context) {
        if (DisplayUtils.isEmulator()) {
            return true;
        }
        try {
            //1.通过WindowManagerGlobal获取windowManagerService
            // 反射方法：IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
            Class<?> windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal");
            Method getWmServiceMethod = windowManagerGlobalClass.getDeclaredMethod("getWindowManagerService");
            getWmServiceMethod.setAccessible(true);
            //getWindowManagerService是静态方法，所以invoke null
            Object iWindowManager = getWmServiceMethod.invoke(null);

            //2.获取windowMangerService的hasNavigationBar方法返回值
            // 反射方法：haveNav = windowManagerService.hasNavigationBar();
            Class<?> iWindowManagerClass = iWindowManager.getClass();
            Method hasNavBarMethod = iWindowManagerClass.getDeclaredMethod("hasNavigationBar");
            hasNavBarMethod.setAccessible(true);
            return (Boolean) hasNavBarMethod.invoke(iWindowManager);
        } catch (Exception ignored) {
            return isNavigationBar(context);
        }
    }

    /**
     * 获取vivo手机设置中的"navigation_gesture_on"值，判断当前系统是使用导航键还是手势导航操作
     *
     * @return false 表示使用的是虚拟导航键(NavigationBar)， true 表示使用的是手势， 默认是false
     */
    public static boolean vivoNavigationGestureEnabled(Context context) {
        int val = Settings.Secure.getInt(context.getContentResolver(), "navigation_gesture_on", 0);
        return val != 0;
    }

    public static boolean emuiNavigationEnabled(Context context) {
        int g = Settings.Global.getInt(context.getContentResolver(), "navigationbar_is_min", 0);
        return g != 1;
    }

    /**
     * 打开忽略电池优化系统设置界面
     *
     * @see <a href="https://developer.android.com/reference/android/provider/Settings#ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS">android doc</a>
     */
    public static void showIgnoreBatteryOptimizationSetting(Context context) {
        if (context == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            UIUtils.showToast(context, "No support !");
        }
    }

    /**
     * 判断应用是否在忽略电池优化中
     *
     * @see <a href="https://developer.android.com/reference/android/provider/Settings#ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS">android doc</a>
     */
    public static boolean isIgnoreBatteryOptimization(Context context) {
        if (context == null) {
            return true;
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return true;
        }
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager == null || powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public static void setPhoneScreen(Activity context) {
        if (context == null) {
            return;
        }
        if (Constants.Config.isPhone) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * open map application
     *
     * @param longitude 经度
     * @param latitude 纬度
     */
    public static void openMap(Context context, String longitude, String latitude) {
        if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) {
            return;
        }
        Uri uri = Uri.parse("geo:" + latitude + "," + longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return;
        }
        context.startActivity(intent);
    }

    public static void openBrowser(Context context, BingWallpaperImage image) {
        if (image == null) {
            return;
        }
        try {
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
        } catch (AndroidRuntimeException e) {
            UIUtils.showToast(context, context.getString(R.string.unable_open_url));
        }
    }

    public static void openBrowser(Context context, String url) {
        try {
            new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .build()
                    .launchUrl(context, Uri.parse(url));
        } catch (AndroidRuntimeException e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
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

        return "feedback info ------------------------- \n"
                + " sdk: "
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
                + "\n"
                + "feedback info ------------------------- \n";
    }

    public static void sendFeedback(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        String to[] = { "liaohengcn@gmail.com" };
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                context.getString(R.string.app_name) + " : " + context.getString(R.string.menu_main_feedback));
        emailIntent.putExtra(Intent.EXTRA_TEXT, BingWallpaperUtils.getSystemInfo(context));

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
        if (NetworkUtils.isConnectedOrConnecting(context)) {
            if (BingWallpaperUtils.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiConnected(context)) {
                    return 2;
                }
            }
            //每天成功执行一次
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setBothWallpaper(Context context, Bitmap bitmap) throws IOException {
        WallpaperManager.getInstance(context)
                .setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setLockScreenWallpaper(Context context, Bitmap bitmap) throws IOException {
        WallpaperManager.getInstance(context)
                .setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setHomeScreenWallpaper(Context context, Bitmap bitmap) throws IOException {
        WallpaperManager.getInstance(context)
                .setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
    }

    public static Observable<Object> clearCache(Context context) {
        return Observable.just(context)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Context, Object>() {
                    @Override
                    public Object call(Context context) {
                        GlideApp.get(context).clearMemory();
                        GlideApp.get(context).clearDiskCache();
                        NetUtils.get().clearCache();
                        return null;
                    }
                });
    }
}
