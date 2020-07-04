package me.liaoheng.wallpaper.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Browser;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.DateTimeUtils;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;
import com.google.common.io.Files;
import com.scottyab.rootbeer.RootBeer;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.service.LiveWallpaperService;
import me.liaoheng.wallpaper.ui.SettingsActivity;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class BingWallpaperUtils {

    public static boolean isEnableLog(Context context) {
        return SettingUtils.isEnableLog(context);
    }

    public static boolean isEnableLogProvider(Context context) {
        return SettingUtils.isEnableLogProvider(context);
    }

    public static String getResolutionImageUrl(Context context, Wallpaper image) {
        return getImageUrl(context, SettingUtils.getResolution(context), image.getBaseUrl());
    }

    public static String getImageUrl(Context context, String resolution, String baseUrl) {
        return getBaseUrl(context) + baseUrl + "_" + resolution + ".jpg";
    }

    public static boolean isAutoCountry(Context context) {
        return SettingUtils.getCountryValue(context) == 0;
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

    @NonNull
    public static Locale getLocale(Context context) {
        int auto = SettingUtils.getCountryValue(context);
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
            case 7:
                return Locale.ITALY;
            case 8:
                return new Locale("fa", "IR");
            case 9:
                return new Locale("bg", "BG");
            default:
                Locale originalLocale = LanguageContextWrapper.getOriginalLocale();
                return originalLocale == null ? LanguageContextWrapper.getCurrentLocale(context) : originalLocale;
        }
    }

    public static String getAutoLocale(Context context) {
        Locale locale = getLocale(context);
        String country = locale.getCountry();
        String language = locale.getLanguage();
        return language + "-" + country;
    }

    public static Locale getLanguage(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        int language = Integer.parseInt(sharedPreferences.getString(SettingsActivity.PREF_LANGUAGE, "0"));
        switch (language) {
            case 1:
                return Locale.US;
            case 2:
                return Locale.SIMPLIFIED_CHINESE;
            case 3:
                return Locale.TRADITIONAL_CHINESE;
            case 4:
                return LocaleList.ruLocale();
            case 5:
                return Locale.GERMANY;
            case 6:
                return LocaleList.plLocale();
            case 7:
                return LocaleList.csLocale();
            case 8:
                return LocaleList.nlLocale();
            case 9:
                return Locale.FRANCE;
            default:
                Locale originalLocale = LanguageContextWrapper.getOriginalLocale();
                return originalLocale == null ? LanguageContextWrapper.getCurrentLocale(context) : originalLocale;
        }
    }

    //https://juejin.im/post/5d0b1739e51d4510a73280cc
    public static Uri saveFileToPictureCompat(Context context, String url, File from) throws Exception {
        String name = FileUtils.getName(url);
        String[] split = name.split("=");
        if (split.length > 1) {
            name = split[1];
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return saveFileToPicture(context, name, from);
        } else {
            boolean isExternalStorageLegacy = true;
            try {
                isExternalStorageLegacy = Environment.isExternalStorageLegacy();
            } catch (NoSuchMethodError ignored) {
            }
            if (isExternalStorageLegacy) {
                return saveFileToPicture(context, name, from);
            }
            return saveFileToPictureQ(context, name, from);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Uri saveFileToPicture(Context context, String name, File from) throws Exception {
        File p = new File(Environment.DIRECTORY_PICTURES, Common.getProjectName());
        File file = new File(FileUtils.getExternalStoragePath(), p.getAbsolutePath());
        File outFile = FileUtils.createFile(file, name);
        Files.copy(from, outFile);
        Uri uri = Uri.fromFile(outFile);
        context.sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        return uri;
    }

    //https://developer.android.com/training/data-storage/files/external-scoped
    @SuppressWarnings("UnstableApiUsage")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri saveFileToPictureQ(Context context, String name, File from) throws Exception {
        Uri uri;
        ContentValues contentValues = null;
        try (Cursor query = context.getContentResolver().query(MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY), null,
                MediaStore.Images.Media.DISPLAY_NAME + "='" + name + "' AND "
                        + MediaStore.Images.Media.OWNER_PACKAGE_NAME + "='"
                        + context.getPackageName() + "'",
                null, null)) {
            if (query != null && query.moveToFirst()) {
                long id = query.getLong(query.getColumnIndex(MediaStore.Images.Media._ID));
                uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), id);
            } else {
                contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + Common.getProjectName());
                uri = context.getContentResolver().insert(MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);
            }
        }

        if (uri == null) {
            throw new IOException("getContentResolver uri is null");
        }
        ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "w");
        if (fd == null) {
            throw new IOException("openFileDescriptor is null");
        }
        Files.copy(from, new FileOutputStream(fd.getFileDescriptor()));
        if (contentValues != null) {
            contentValues.clear();
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
            context.getContentResolver().update(uri, contentValues, null, null);
        }
        return uri;
    }

    /**
     * 定时更新时间
     *
     * @return UTC
     */
    @Nullable
    public static LocalTime getDayUpdateTime(Context context) {
        String time = SettingUtils.getAlarmTime(context);
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

    public static void showSaveWallpaperDialog(Context context, Callback5 callback) {
        UIUtils.showYNAlertDialog(context, context.getString(R.string.menu_save_wallpaper) + "?",
                callback);
    }

    public static void showWallpaperDialog(Context context, @Nullable Wallpaper image, @NonNull Config config,
            @Nullable Callback4<Boolean> callback) {
        String message = context.getString(R.string.menu_set_wallpaper_mode_both);
        if (config.getWallpaperMode() == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            message = context.getString(R.string.menu_set_wallpaper_mode_home);
        } else if (config.getWallpaperMode() == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            message = context.getString(R.string.menu_set_wallpaper_mode_lock);
        }

        UIUtils.showYNAlertDialog(context, message + "?", new Callback5() {
            @Override
            public void onAllow() {
                setWallpaperDialog(context, image, config,
                        callback);
            }

            @Override
            public void onDeny() {

            }
        });
    }

    public static void setWallpaperDialog(final Context context, final @Nullable Wallpaper image,
            @NonNull Config config,
            @Nullable final Callback4<Boolean> callback) {
        if (!BingWallpaperUtils.isConnected(context)) {
            UIUtils.showToast(context, R.string.network_unavailable);
            return;
        }
        // use mobile network show alert
        if (NetworkUtils.isMobileConnected(context)) {
            UIUtils.showYNAlertDialog(context, context.getString(R.string.alert_mobile_data),
                    new Callback5() {
                        @Override
                        public void onAllow() {
                            setWallpaperAction(context, image, config, callback);
                        }

                        @Override
                        public void onDeny() {

                        }
                    });
        } else {
            setWallpaperAction(context, image, config, callback);
        }
    }

    private static void setWallpaperAction(Context context, @Nullable Wallpaper image, @NonNull Config config,
            @Nullable Callback4<Boolean> callback) {
        if (callback != null) {
            callback.onYes(true);
        }
        startWallpaper(context, image, config);
        //BingWallpaperIntentService.start(context, image, config);
    }

    public static void startWallpaper(Context context, Wallpaper image, Config config) {
        if (BingWallpaperJobManager.LIVE_WALLPAPER == BingWallpaperJobManager.getJobType(context)) {
            Intent intent = new Intent(LiveWallpaperService.UPDATE_LIVE_WALLPAPER);
            intent.putExtra(Config.EXTRA_SET_WALLPAPER_IMAGE, image);
            intent.putExtra(Config.EXTRA_SET_WALLPAPER_CONFIG, config);
            context.sendBroadcast(intent);
        } else {
            BingWallpaperIntentService.start(context, image, config);
        }
    }

    public static void setWallpaper(Context context, @Nullable Wallpaper image, @NonNull Config config,
            @Nullable Callback4<Boolean> callback) {
        if (!BingWallpaperUtils.isConnected(context)) {
            Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (callback != null) {
            callback.onYes(true);
        }
        startWallpaper(context, image, config);
        //BingWallpaperIntentService.start(context, image, config);
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
        return DisplayUtils.vivoNavigationGestureEnabled(context);
    }

    /**
     * Check if MIUI is enabled for gestures
     *
     * @return true gestures
     */
    public static boolean miuiNavigationGestureEnabled(Context context) {
        return DisplayUtils.miuiNavigationGestureEnabled(context);
    }

    /**
     * Check if EMUI is enabled for navigation
     *
     * @return true navigation
     */
    public static boolean emuiNavigationEnabled(Context context) {
        return DisplayUtils.emuiNavigationEnabled(context);
    }

    /**
     * 打开忽略电池优化系统设置界面
     *
     * @see <a href="https://developer.android.com/reference/android/provider/Settings#ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS">android
     * doc</a>
     */
    public static void showIgnoreBatteryOptimizationSetting(Context context) {
        if (!AppUtils.showIgnoreBatteryOptimizationSetting(context)) {
            Toast.makeText(context, "No support !", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void setPhoneScreen(Activity context) {
        if (context == null) {
            return;
        }
        if (Constants.Config.isPhone) {
            try {
                context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } catch (Throwable ignored) {
            }
        }
    }

    public static void openBrowser(Context context, Wallpaper image) {
        if (image == null) {
            return;
        }
        String url = image.getWebUrl();
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
        } catch (Throwable ignore) {
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
        } catch (Throwable ignore) {
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
        } catch (Throwable ignore) {
            UIUtils.showToast(context, R.string.unable_open_url);
        }
    }

    public static String getSystemInfo(Context context) {
        String device = Build.DEVICE;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String romName = ROM.getROM().getName();
        int romType = ROM.getROM().getRom();
        String romVersion = ROM.getROM().getVersion();
        Locale locale = Locale.getDefault();
        Locale autoLocale = getLocale(context);
        String job = BingWallpaperJobManager.check(context);
        boolean alarm = SettingUtils.isAlarm(context);
        String alarmTime = SettingUtils.getAlarmTime(context);
        String autoSetMode = SettingUtils.getAutoMode(context);
        int interval = SettingUtils.getAutomaticUpdateInterval(context);
        String resolution = SettingUtils.getResolution(context);
        DisplayMetrics r = BingWallpaperUtils.getSysResolution(context);
        String SysResolution = r.widthPixels + "x" + r.heightPixels;

        return "feedback info ------------------------- \n"
                + "sdk: "
                + Build.VERSION.SDK_INT
                + " device: "
                + device
                + " model: "
                + model
                + " product: "
                + product
                + " rom_name: "
                + romName
                + " rom_type: "
                + romType
                + " rom_version: "
                + romVersion
                + " locale: "
                + locale
                + " auto_locale: "
                + autoLocale
                + " version: "
                + BuildConfig.VERSION_NAME
                + " resolution: "
                + resolution
                + " sys_resolution: "
                + SysResolution
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

        if (SettingUtils.isEnableLog(context)) {
            File logFile = LogDebugFileUtils.get().getLogFile();
            if (logFile != null && logFile.exists()) {
                emailIntent.putExtra(Intent.EXTRA_STREAM, getUriForFile(context, logFile));
            }
        }

        if (emailIntent.resolveActivity(context.getPackageManager()) == null) {
            UIUtils.showToast(context, "No support !");
            return;
        }

        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_email)));
    }

    public static Uri getUriForFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static void checkRunningService(Context context, String TAG) {
        Intent intent = checkRunningServiceIntent(context, TAG);
        if (intent != null) {
            BingWallpaperIntentService.start(context, intent);
        }
    }

    public static Intent checkRunningServiceIntent(Context context, String TAG) {
        boolean enableLog = SettingUtils.isEnableLogProvider(context);
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        if (isConnected(context)) {
            if (SettingUtils.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiConnected(context)) {
                    L.alog().d(TAG, "isWifiConnected :false");
                    if (enableLog) {
                        LogDebugFileUtils.get()
                                .i(TAG, "Network not wifi");
                    }
                    return null;
                }
            }
            if (!isTaskUndone(context)) {
                L.alog().d(TAG, "isToDaysDo :false");
                if (enableLog) {
                    LogDebugFileUtils.get()
                            .i(TAG, "Already executed");
                }
                return null;
            }
            Config config = new Config.Builder().loadConfig(context)
                    .setWallpaperMode(SettingUtils.getAutoModeValue(context))
                    .setBackground(true)
                    .build();
            intent.putExtra(Config.EXTRA_SET_WALLPAPER_CONFIG, config);
            return intent;
        } else {
            L.alog().d(TAG, "isConnectedOrConnecting :false");
            if (enableLog) {
                LogDebugFileUtils.get()
                        .i(TAG, "Network unavailable");
            }
            return null;
        }
    }

    public static void runningService(Context context, String TAG) {
        checkRunningService(context, TAG);
    }

    public static boolean isConnected(Context context) {
        NetworkInfo[] nets = NetworkUtils.getConnManager(context).getAllNetworkInfo();
        if (nets.length > 0) {
            for (NetworkInfo net : nets) {
                if (net.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void setWallpaper(Context context, File file, int which) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try (InputStream fileInputStream = new FileInputStream(file)) {
                int s = WallpaperManager.getInstance(context)
                        .setStream(fileInputStream, null, true, which);
                //if (s == 0) {
                //    throw new IOException("WallpaperManager error");
                //}
            }
        } else {
            setWallpaper(context, file);
        }
    }

    public static void setWallpaper(Context context, File file) throws IOException {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            WallpaperManager.getInstance(context).setStream(fileInputStream);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setLockScreenWallpaper(Context context, File file) throws IOException {
        setWallpaper(context, file, WallpaperManager.FLAG_LOCK);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setHomeScreenWallpaper(Context context, File file) throws IOException {
        setWallpaper(context, file, WallpaperManager.FLAG_SYSTEM);
    }

    public static Observable<Object> clearCache(Context context) {
        return Observable.just(context)
                .subscribeOn(Schedulers.io())
                .map(c -> {
                    GlideApp.get(c).clearDiskCache();
                    NetUtils.get().clearCache();
                    CacheUtils.get().clear();
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
        Locale locale = getLanguage(context);
        if (locale.getLanguage().equals(LocaleList.plLocale().getLanguage())) {
            return "Translator : @dekar16";
        } else if (locale.getLanguage().equals(LocaleList.ruLocale().getLanguage())) {
            return "Translator : @tullev(Lev Tulubjev), @FanHamMer(Oleg Popenkov)";
        } else if (locale.getLanguage().equals(LocaleList.csLocale().getLanguage())) {
            return "Translator : @foreteller";
        } else if (locale.getLanguage().equals(Locale.GERMANY.getLanguage())) {
            return "Translator : @Bergradler";
        } else if (locale.getLanguage().equals(LocaleList.nlLocale().getLanguage())) {
            return "Translator : @5qx9Pe7Lvj8Fn7zg(Jasper)";
        } else if (locale.getLanguage().equals(Locale.FRANCE.getLanguage())) {
            return "Translator : @Faux-ami(Nicolas)";
        }
        return "";
    }

    public static void taskComplete(Context context, String TAG) {
        if (isTaskUndone(context)) {
            L.alog().i(TAG, "today complete");
            if (SettingUtils.isEnableLogProvider(context)) {
                LogDebugFileUtils.get().i(TAG, "Today complete");
            }
            TasksUtils.markDoneProvider(context, Constants.TASK_FLAG_SET_WALLPAPER_STATE);
        }
    }

    public static boolean isTaskUndone(Context context) {
        return TasksUtils.isToDaysDoProvider(context, 1, Constants.TASK_FLAG_SET_WALLPAPER_STATE);
    }

    public static void clearTaskComplete(Context context) {
        TasksUtils.deleteDoneProvider(context, Constants.TASK_FLAG_SET_WALLPAPER_STATE);
    }

    public static boolean checkStoragePermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestStoragePermissions(Activity activity) {
        if (checkStoragePermissions(activity)) {
            return true;
        }
        ActivityCompat.requestPermissions(activity,
                new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE },
                111);
        return false;
    }

    public static boolean isRooted(Context context) {
        return new RootBeer(context).isRooted();
    }

    public static DisplayMetrics getSysResolution(Context context) {
        return DisplayUtils.getScreenInfo(context, true);
    }
}
