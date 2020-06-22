package me.liaoheng.wallpaper.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Browser;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.liaoheng.common.Common;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.DateTimeUtils;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.MD5Utils;
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.service.BingWallpaperCheckIntentService;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.service.LiveWallpaperService;
import me.liaoheng.wallpaper.ui.SettingsActivity;

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
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI, true);
    }

    public static String getResolution(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);

        String resolution = SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, "0");
        return names[Integer.parseInt(Objects.requireNonNull(resolution))];
    }

    public static void putResolution(Context context, String resolution) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, resolution)
                .apply();
        SettingTrayPreferences.get(context)
                .put(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, resolution);
    }

    public static void putSaveResolution(Context context, String resolution) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(SettingsActivity.PREF_SAVE_WALLPAPER_RESOLUTION, resolution)
                .apply();
        SettingTrayPreferences.get(context)
                .put(SettingsActivity.PREF_SAVE_WALLPAPER_RESOLUTION, resolution);
    }

    public static String getSaveResolution(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);

        String resolution = SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_SAVE_WALLPAPER_RESOLUTION, "0");
        return names[Integer.parseInt(Objects.requireNonNull(resolution))];
    }

    public static int getAutoModeValue(Context context) {
        return Integer.parseInt(SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_SET_WALLPAPER_AUTO_MODE, "0"));
    }

    public static String getAutoMode(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_auto_mode_name);
        return names[getAutoModeValue(context)];
    }

    public static String getCountryName(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_country_names);
        return names[getCountryValue(context)];
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

    @NonNull
    public static Locale getLocale(Context context) {
        int auto = getCountryValue(context);
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
            default:
                Locale originalLocale = LanguageContextWrapper.getOriginalLocale();
                return originalLocale == null ? getCurrentLocale(context) : originalLocale;
        }
    }

    public static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getSystemLocale(context.getResources().getConfiguration());
        } else {
            return getSystemLocaleLegacy(context.getResources().getConfiguration());
        }
    }

    private static Locale getSystemLocaleLegacy(Configuration config) {
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Locale getSystemLocale(Configuration config) {
        return config.getLocales().get(0);
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
                return originalLocale == null ? getCurrentLocale(context) : originalLocale;
        }
    }

    public static int getLanguageValue(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return Integer.parseInt(sharedPreferences.getString(SettingsActivity.PREF_LANGUAGE, "0"));
    }

    public static String getLanguageName(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_language_names);
        return names[getLanguageValue(context)];
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

    public static boolean isAutoSave(Context context) {
        return SettingTrayPreferences.get(context).getBoolean(SettingsActivity.PREF_AUTO_SAVE_WALLPAPER_FILE, false);
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

    public static boolean setMiuiLockScreenSupport(Context context, boolean support) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(SettingsActivity.PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, support).apply();
        return SettingTrayPreferences.get(context)
                .put(SettingsActivity.PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, support);
    }

    public static boolean isPixabaySupport(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_PREF_PIXABAY_SUPPORT, false);
    }

    public static int getSettingStackBlur(Context context) {
        return SettingTrayPreferences.get(context).getInt(SettingsActivity.PREF_STACK_BLUR, 0);
    }

    public static int getSettingStackBlurMode(Context context) {
        return SettingTrayPreferences.get(context).getInt(SettingsActivity.PREF_STACK_BLUR_MODE, 0);
    }

    public static String getSettingStackBlurModeName(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_auto_mode_name);
        return names[getSettingStackBlurMode(context)];
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

    public static void showWallpaperDialog(Context context, @Nullable BingWallpaperImage image, @NonNull Config config,
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

    public static void setWallpaperDialog(final Context context, final @Nullable BingWallpaperImage image,
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

    private static void setWallpaperAction(Context context, @Nullable BingWallpaperImage image, @NonNull Config config,
            @Nullable Callback4<Boolean> callback) {
        if (callback != null) {
            callback.onYes(true);
        }
        startWallpaper(context, image, config);
        //BingWallpaperIntentService.start(context, image, config);
    }

    public static void startWallpaper(Context context, BingWallpaperImage image, Config config) {
        if (BingWallpaperJobManager.LIVE_WALLPAPER == BingWallpaperJobManager.getJobType(context)) {
            Intent intent = new Intent(LiveWallpaperService.UPDATE_LIVE_WALLPAPER);
            intent.putExtra("image", image);
            context.sendBroadcast(intent);
        } else {
            BingWallpaperIntentService.start(context, image, config);
        }
    }

    public static void setWallpaper(Context context, @Nullable BingWallpaperImage image, @NonNull Config config,
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

    @SuppressLint("SourceLockedOrientationActivity")
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
        int romType = ROM.getROM().getRom();
        String romVersion = ROM.getROM().getVersion();
        Locale locale = Locale.getDefault();
        Locale autoLocale = getLocale(context);
        String job = BingWallpaperJobManager.check(context);
        boolean alarm = isAlarm(context);
        String alarmTime = getAlarmTime(context);
        String autoSetMode = getAutoMode(context);
        int interval = getAutomaticUpdateInterval(context);
        String resolution = getResolution(context);
        DisplayMetrics r = BingWallpaperUtils.getSysResolution(context);
        String SysResolution = r.widthPixels + "x" + r.heightPixels;

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

        if (BingWallpaperUtils.isEnableLog(context)) {
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

    public static void checkRunningService(Context context, String TAG) {
        Intent intent = checkRunningServiceIntent(context, TAG, true);
        if (intent != null) {
            BingWallpaperIntentService.start(context, intent);
        }
    }

    public static Intent checkRunningServiceIntent(Context context, String TAG, boolean check) {
        boolean enableLog = isEnableLogProvider(context);
        Intent intent = new Intent(context, BingWallpaperIntentService.class);
        if (isConnected(context)) {
            if (getOnlyWifi(context)) {
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
                    .setWallpaperMode(getAutoModeValue(context))
                    .setBackground(true)
                    .build();
            intent.putExtra(BingWallpaperIntentService.EXTRA_SET_WALLPAPER_CONFIG, config);
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

    public static void startCheckService(Context context, String TAG) {
        BingWallpaperCheckIntentService.start(context, TAG);
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

    public static void setLastWallpaperImageUrl(Context context, String url) {
        SettingTrayPreferences.get(context).put(Constants.PREF_LAST_WALLPAPER_IMAGE_URL, url);
    }

    public static String getLastWallpaperImageUrl(Context context) {
        return SettingTrayPreferences.get(context).getString(Constants.PREF_LAST_WALLPAPER_IMAGE_URL, "");
    }

    public static void taskComplete(Context context, String TAG) {
        if (isTaskUndone(context)) {
            L.alog().i(TAG, "today complete");
            if (BingWallpaperUtils.isEnableLogProvider(context)) {
                LogDebugFileUtils.get().i(TAG, "Today complete");
            }
            TasksUtils.markDoneProvider(context, BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE);
        }
    }

    public static boolean isTaskUndone(Context context) {
        return TasksUtils.isToDaysDoProvider(context, 1, BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE);
    }

    public static void clearTaskComplete(Context context) {
        TasksUtils.deleteDoneProvider(context, BingWallpaperIntentService.FLAG_SET_WALLPAPER_STATE);
    }

    //https://github.com/halibobo/WaterMark
    public static Bitmap waterMark(Context context, Bitmap bitmap, String str) {
        int destWidth = bitmap.getWidth();
        int destHeight = bitmap.getHeight();
        Bitmap icon = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);

        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);

        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect dst = new Rect(0, 0, destWidth, destHeight);
        canvas.drawBitmap(bitmap, src, dst, photoPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize(DisplayUtils.dp2px(context, 9));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(1);
        textPaint.setAlpha(120);
        textPaint.setColor(Color.WHITE);

        Rect bounds = new Rect();
        textPaint.getTextBounds(str, 0, str.length(), bounds);

        canvas.drawText(str, destWidth - bounds.width() - 20, destHeight - bounds.height() / 2F - 5, textPaint);
        canvas.save();
        canvas.restore();
        return icon;
    }

    public static void shareImage(@NonNull Context context, @NonNull Config config, @NonNull String url,
            @NonNull final String str) {
        String key = MD5Utils.md5Hex(url + "_" + config.getStackBlur());
        Observable<File> fileObservable = Observable.just(str).subscribeOn(Schedulers.io()).map(
                s -> getImageBitmap(context, config, url)).flatMap(
                (Function<Bitmap, ObservableSource<File>>) bitmap -> {
                    File tempFile = CacheUtils.get().get(key);
                    if (tempFile == null) {
                        tempFile = CacheUtils.get()
                                .put(key, BitmapUtils.bitmapToStream(waterMark(context, bitmap, str),
                                        Bitmap.CompressFormat.JPEG));
                    }
                    return Observable.just(tempFile);
                });
        Utils.addSubscribe(fileObservable, new Callback.EmptyCallback<File>() {
            @Override
            public void onSuccess(File file) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                share.putExtra(Intent.EXTRA_STREAM, getUriForFile(context, file));
                share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(share);
            }

            @Override
            public void onError(Throwable e) {
                L.alog().e("Share", e);
                UIUtils.showToast(context, "Share error");
            }
        });
    }

    public static Uri getUriForFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static Bitmap getImageBitmap(@NonNull Context context, @NonNull Config config, @NonNull String url)
            throws ExecutionException, InterruptedException {
        Bitmap bitmap = GlideApp.with(context).asBitmap().load(url).submit().get();
        return transformStackBlur(bitmap, config.getStackBlur());
    }

    public static void loadImage(GlideRequest<Bitmap> request, ImageView imageView,
            Callback<Bitmap> callback) {
        request.listener(new RequestListener<Bitmap>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target,
                    boolean isFirstResource) {
                callback.onPostExecute();
                callback.onError(e);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                    DataSource dataSource,
                    boolean isFirstResource) {
                return false;
            }
        }).into(new BitmapImageViewTarget(imageView) {

            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                callback.onPreExecute();
            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource,
                    @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                callback.onPostExecute();
                callback.onSuccess(resource);
            }
        });
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
        RootBeer rootBeer = new RootBeer(context);
        if (rootBeer.checkForBusyBoxBinary()) {
            return rootBeer.isRooted();
        } else {
            return rootBeer.isRootedWithoutBusyBoxCheck();
        }
    }

    public static Bitmap transformStackBlur(Bitmap bitmap, int stackBlur) {
        if (stackBlur <= 0) {
            return bitmap;
        }
        return toStackBlur(bitmap, stackBlur);
    }

    public static DisplayMetrics getSysResolution(Context context) {
        WindowManager wm = ContextCompat.getSystemService(context, WindowManager.class);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (wm == null) {
            return outMetrics;
        }
        wm.getDefaultDisplay().getRealMetrics(outMetrics);
        return outMetrics;
    }

    //public static Bitmap getGlideBitmap(Context context, String url) throws Exception {
    //    return GlideApp.with(context)
    //            .asBitmap()
    //            .load(url)
    //            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    //            .get(2, TimeUnit.MINUTES);
    //}

    public static File getGlideFile(Context context, String url) throws Exception {
        return GlideApp.with(context)
                .asFile()
                .load(url)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get(2, TimeUnit.MINUTES);
    }

    public static void autoSaveWallpaper(Context context, String TAG, BingWallpaperImage image, File wallpaper) {
        if (isAutoSave(context)) {
            try {
                if (!checkStoragePermissions(context)) {
                    throw new IOException("Permission denied");
                }
                String saveResolution = getSaveResolution(context);
                String resolution = getResolution(context);
                if (!saveResolution.equals(resolution)) {
                    String saveImageUrl = getImageUrl(context, saveResolution,
                            image);
                    L.alog().i(TAG, "wallpaper save url: " + saveImageUrl);
                    saveFileToPictureCompat(context, saveImageUrl, getGlideFile(context, saveImageUrl));
                } else {
                    saveFileToPictureCompat(context, image.getImageUrl(), wallpaper);
                }
            } catch (Throwable e) {
                CrashReportHandle.saveWallpaper(context, TAG, e);
            }
        }
    }

    public static BingWallpaperImage getImage(Context context, boolean cache) throws IOException {
        BingWallpaperImage image;
        if (BingWallpaperUtils.isPixabaySupport(context)) {
            image = BingWallpaperNetworkClient.getPixabaysExecute();
            image.setImageUrl(image.getUrl());
        } else {
            image = BingWallpaperNetworkClient.getBingWallpaperSingleCall(context, cache);
            image.setResolutionImageUrl(context);
        }
        return image;
    }

    /**
     * Stack Blur v1.0 from
     * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
     * Java Author: Mario Klingemann <mario at quasimondo.com>
     * http://incubator.quasimondo.com
     * <p>
     * created Feburary 29, 2004
     * Android port : Yahel Bouaziz <yahel at kayenko.com>
     * http://www.kayenko.com
     * ported april 5th, 2012
     * <p>
     * This is a compromise between Gaussian Blur and Box blur
     * It creates much better looking blurs than Box Blur, but is
     * 7x faster than my Gaussian Blur implementation.
     * <p>
     * I called it Stack Blur because this describes best how this
     * filter works internally: it creates a kind of moving stack
     * of colors whilst scanning through the image. Thereby it
     * just has to add one new block of color to the right side
     * of the stack and remove the leftmost color. The remaining
     * colors on the topmost layer of the stack are either added on
     * or reduced by one, depending on if they are on the right or
     * on the left side of the stack.
     * <p>
     * If you are using this algorithm in your code please add
     * the following line:
     * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
     */
    @NonNull
    //TODO Need to optimize, try using ndk
    public static Bitmap toStackBlur(Bitmap sentBitmap, int radius) {
        if (radius < 1) {
            return sentBitmap;
        }
        Bitmap bitmap = null;
        try {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            if (bitmap == null) {
                return sentBitmap;
            }

            System.gc();

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            //Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }

            //Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        } catch (OutOfMemoryError ignored) {
        }
        return bitmap == null ? sentBitmap : bitmap;
    }
}
