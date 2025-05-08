package me.liaoheng.wallpaper.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Browser;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.DateTimeUtils;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.LanguageContextWrapper;
import com.github.liaoheng.common.util.MD5Utils;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.ShellUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;
import com.github.liaoheng.common.util.YNCallback;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.scottyab.rootbeer.RootBeer;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.service.LiveWallpaperService;

/**
 * @author liaoheng
 * @version 2016-09-20 17:17
 */
public class BingWallpaperUtils {

    public static String getResolutionImageUrl(Context context, String baseUrl) {
        return getImageUrl(context, Settings.getResolution(context), baseUrl);
    }

    public static String getImageUrl(Context context, String resolution, String baseUrl) {
        return getBaseUrl(context) + baseUrl + "_" + resolution + ".jpg";
    }

    public static String getResolution(Context context) {
        return getResolution(context, false);
    }

    public static String getResolution(Context context, boolean excludeUHD) {
        int resolutionValue = Settings.getResolutionValue(context);
        if (excludeUHD) {
            if (resolutionValue == 11) {
                resolutionValue = 10;
            }
        }
        if (resolutionValue < 10) {
            boolean portrait = false;
            if (resolutionValue % 2 == 0) {
                portrait = true;
            }
            if (isPortrait(context)) {
                if (!portrait) {
                    resolutionValue--;
                }
            } else {
                if (portrait) {
                    resolutionValue++;
                }
            }
        }
        return Settings.getResolution(context, resolutionValue);
    }

    public static void initResolution(Context context) {
        String resolution = Constants.Config.isPhone ? "0" : "1";
        DisplayMetrics screenInfo = DisplayUtils.getScreenInfo(context, true);
        if (isPortrait(context)) {
            if (screenInfo.widthPixels <= 720 && screenInfo.widthPixels > 480) {//720p
                if (Constants.Config.isPhone) {
                    resolution = "4";
                } else {
                    resolution = "5";
                }
            } else if (screenInfo.widthPixels <= 480) {//480p
                if (Constants.Config.isPhone) {
                    resolution = "2";
                } else {
                    resolution = "3";
                }
            }
        } else {
            if (screenInfo.heightPixels <= 720 && screenInfo.heightPixels > 480) {//720p
                if (Constants.Config.isPhone) {
                    resolution = "4";
                } else {
                    resolution = "5";
                }
            } else if (screenInfo.heightPixels <= 480) {//480p
                if (Constants.Config.isPhone) {
                    resolution = "2";
                } else {
                    resolution = "3";
                }
            }
        }
        Settings.putResolution(context, resolution);
        Settings.putSaveResolution(context, resolution);
    }

    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    //https://stackoverflow.com/questions/2795833/check-orientation-on-android-phone
    public static boolean isPortrait2(Context context) {
        int rotation;
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        //    rotation = context.getDisplay().getRotation();
        //}else {
        rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        //}
        if (rotation == Surface.ROTATION_0) {//"portrait"
            return true;
        } else if (rotation == Surface.ROTATION_90) {//"landscape"
            return false;
        } else if (rotation == Surface.ROTATION_180) {
            return true;//"reverse portrait"
        }
        return false;//"reverse landscape"
    }

    public static Wallpaper generateUrl(Context context, Wallpaper wallpaper) {
        wallpaper.setImageUrl(getImageUrl(context, getResolution(context), wallpaper.getBaseUrl()));
        return wallpaper;
    }

    public static boolean isAutoCountry(Context context) {
        return Settings.getCountryValue(context) == 0;
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
        switch (Settings.getCountryValue(context)) {
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
                return LocaleList.bgLocale();
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

    @NonNull
    public static Locale getLanguage(Context context) {
        Locale originalLocale = LanguageContextWrapper.getOriginalLocale();
        return getLanguage(Settings.getLanguageValue(context),
                originalLocale == null ? LanguageContextWrapper.getCurrentLocale(context) : originalLocale);
    }

    @NonNull
    public static Locale getLanguage(int languageValue, Locale def) {
        switch (languageValue) {
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
            case 10:
                return LocaleList.bgLocale();
            case 11:
                return LocaleList.skLocale();
            case 12:
                return Locale.JAPAN;
            case 13:
                return Locale.ITALY;
            case 14:
                return LocaleList.esLocale();
            case 15:
                return LocaleList.ptBRLocale();
            case 16:
                return LocaleList.elLocale();
            default:
                return def;
        }
    }

    /**
     * 定时更新时间
     *
     * @return UTC
     */
    @NonNull
    public static LocalTime getDayUpdateTime(Context context) {
        String time = Settings.getAlarmTime(context);
        if (TextUtils.isEmpty(time)) {
            return LocalTime.parse(Constants.DEF_TIMER_PERIODIC);
        }
        return LocalTime.parse(time);
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

    public static void showSaveWallpaperDialog(Context context, YNCallback callback) {
        UIUtils.showYNAlertDialog(context, context.getString(R.string.menu_save_wallpaper) + "?",
                callback);
    }

    public static void showWallpaperDialog(Context context, @Nullable Wallpaper image, @NonNull Config config,
            @Nullable YNCallback callback) {
        String message = context.getString(R.string.menu_set_wallpaper_mode_both);
        if (config.getWallpaperMode() == Constants.EXTRA_SET_WALLPAPER_MODE_HOME) {
            message = context.getString(R.string.menu_set_wallpaper_mode_home);
        } else if (config.getWallpaperMode() == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
            message = context.getString(R.string.menu_set_wallpaper_mode_lock);
        }

        UIUtils.showYNAlertDialog(context, message + "?", new YNCallback() {
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
            @Nullable final YNCallback callback) {
        if (!BingWallpaperUtils.isConnected(context)) {
            UIUtils.showToast(context, R.string.network_unavailable);
            return;
        }
        // use mobile network show alert
        if (Settings.getOnlyWifi(context) && NetworkUtils.isMobileConnected(context)) {
            UIUtils.showYNAlertDialog(context, context.getString(R.string.alert_mobile_data),
                    new YNCallback() {
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
            @Nullable YNCallback callback) {
        if (callback != null) {
            callback.onAllow();
        }
        startSetWallpaper(context, image, config);
    }

    public static void startSetWallpaper(Context context, Wallpaper image, Config config) {
        if (Settings.LIVE_WALLPAPER == Settings.getJobType(context)) {
            Intent intent = new Intent(LiveWallpaperService.UPDATE_LIVE_WALLPAPER);
            intent.putExtra(Config.EXTRA_SET_WALLPAPER_IMAGE, image);
            intent.putExtra(Config.EXTRA_SET_WALLPAPER_CONFIG, config);
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent, LiveWallpaperService.PERMISSION_UPDATE_LIVE_WALLPAPER);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WorkerManager.start(context, image, config);
            } else {
                BingWallpaperIntentService.start(context, image, config);
            }
        }
    }

    public static void setWallpaper(Context context, @Nullable Wallpaper image, @NonNull Config config,
            @Nullable YNCallback callback) {
        if (!BingWallpaperUtils.isConnected(context)) {
            Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (callback != null) {
            callback.onAllow();
        }
        startSetWallpaper(context, image, config);
    }

    public static int getNavigationBarHeight(Context context) {
        return DisplayUtils.getNavigationBarHeight(context);
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

    public static boolean isROMSystem() {
        if (UIHelper.isSamsungFoldableDevice()) {
            return false;
        }
        return ROM.getROM().isMiui() || ROM.getROM().isEmui() || ROM.getROM().isOneUi() || ROM.getROM().isOppo()
                || ROM.getROM().isVivo() || ROM.getROM().isColorOS() || ROM.getROM().isFuntouchOS() || ROM.getROM()
                .isFlyme();
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
        int autoSetMode = Settings.getAutoModeValue(context);
        int interval = Settings.getAutomaticUpdateInterval(context);
        String resolution = Settings.getResolution(context);
        DisplayMetrics r = BingWallpaperUtils.getSysResolution(context);
        String SysResolution = r.widthPixels + "x" + r.heightPixels;

        return "------------feedback info------------- \n"
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
                + " autoSetMode: "
                + autoSetMode
                + " interval: "
                + interval
                + "\n"
                + "------------feedback info------------- \n";
    }

    public static void sendFeedback(Context context) {
        String to[] = { "liaohengcn@gmail.com" };
        Intent emailIntent = AppUtils.sendEmail(to,
                context.getString(R.string.app_name) + " : " + context.getString(R.string.menu_main_feedback),
                BingWallpaperUtils.getSystemInfo(context));

        if (Settings.isEnableLog(context)) {
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

    public static void checkStartSetWallpaper(Context context, String TAG) {
        Config config = checkRunningToConfig(context, TAG);
        if (config == null) {
            return;
        }
        startSetWallpaper(context, null, config);
    }

    public static boolean checkRunning(Context context, String TAG) {
        boolean enableLog = Settings.isEnableLogProvider(context);
        if (isConnected(context)) {
            if (Settings.getOnlyWifi(context)) {
                if (!NetworkUtils.isWifiConnected(context)) {
                    L.alog().d(TAG, "isWifiConnected :false");
                    if (enableLog) {
                        LogDebugFileUtils.get()
                                .i(TAG, "Network not wifi");
                    }
                    return false;
                }
            }
            if (!isTaskUndone(context)) {
                L.alog().d(TAG, "isToDaysDo :false");
                if (enableLog) {
                    LogDebugFileUtils.get()
                            .i(TAG, "Already executed");
                }
                return false;
            }
            return true;
        } else {
            L.alog().d(TAG, "isConnectedOrConnecting :false");
            if (enableLog) {
                LogDebugFileUtils.get()
                        .i(TAG, "Network unavailable");
            }
            return false;
        }
    }

    @Nullable
    public static Config checkRunningToConfig(Context context, String TAG) {
        boolean isRunning = checkRunning(context, TAG);
        if (!isRunning) {
            return null;
        }
        return new Config.Builder().loadConfig(context)
                .setWallpaperMode(Settings.getAutoModeValue(context))
                .setBackground(true)
                .setShowNotification(true)
                .build();
    }

    public static boolean isConnected(Context context) {
        return NetworkUtils.isConnected(context);
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

    public static void taskComplete(Context context, String TAG) {
        if (isTaskUndone(context)) {
            L.alog().i(TAG, "today complete");
            if (Settings.isEnableLogProvider(context)) {
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

    public static boolean checkStoragePermissions(Map<String, Boolean> map) {
        return Boolean.TRUE.equals(map.get(Manifest.permission.READ_EXTERNAL_STORAGE)) && Boolean.TRUE.equals(
                map.get(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    public static boolean requestStoragePermissions(Activity activity) {
        return requestStoragePermissions(activity, null);
    }

    public static boolean requestStoragePermissions(Activity activity,
            ActivityResultLauncher<String[]> resultLauncher) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || checkStoragePermissions(activity)) {
            return true;
        }
        if (resultLauncher == null) {
            ActivityCompat.requestPermissions(activity, getStoragePermissions(), 111);
        } else {
            resultLauncher.launch(getStoragePermissions());
        }
        return false;
    }

    public static String[] getStoragePermissions() {
        return new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };
    }

    public static boolean isRooted(Context context) {
        return new RootBeer(context).isRooted();
    }

    public static DisplayMetrics getSysResolution(Context context) {
        return DisplayUtils.getScreenInfo(context, true);
    }

    public static String createKey(String str) {
        return MD5Utils.md5Hex(str).toLowerCase();
    }

    public static void showMiuiDialog(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            return;
        }
        if (ROM.getROM().isMiui()) {
            if (Settings.isMiuiLockScreenSupport(context)) {
                return;
            }
            String root = PreferenceManager.getDefaultSharedPreferences(context).getString("MIUI_root", "");
            if (!TextUtils.isEmpty(root)) {
                return;
            }
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString("MIUI_root", BuildConfig.VERSION_NAME)
                    .apply();
            showMiuiDialog(context, BingWallpaperUtils.isRooted(context));
        }
    }

    public static void showMiuiDialog(Context context, boolean turn) {
        View view = UIUtils.inflate(context, R.layout.dialog_miui);
        AlertDialog alertDialog = new AlertDialog.Builder(context).setView(view)
                .setPositiveButton(android.R.string.no,
                        (dialog, which) -> {
                        }).create();
        SwitchCompat screen = view.findViewById(R.id.dialog_miui_lock_screen);
        if (turn) {
            screen.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (ShellUtils.hasRootPermission()) {
                            Settings.setMiuiLockScreenSupport(context, true);
                        } else {
                            Settings.setMiuiLockScreenSupport(context, false);
                            screen.setChecked(false);
                            UIUtils.showToast(context, R.string.unable_root_permission);
                        }
                    });
        } else {
            UIUtils.viewGone(screen);
        }
        alertDialog.show();
    }

    public static String getName(String fullName) {
        String extension = FileUtils.getExtension(fullName);
        return Files.getNameWithoutExtension(fullName) + (Strings.isNullOrEmpty(extension) ? "" : "." + extension);
    }

    public static String getWallpaperName(String url) {
        String name = getName(url);
        String[] split = name.split("=");
        if (split.length > 1) {
            name = split[1];
        }
        return DateTimeFormat.forPattern(DateTimeUtils.DATAFORMAT_YYYYMMDD)
                .withLocale(Locale.getDefault())
                .print(DateTime.now()) + "_"
                + name;
    }

    //https://stackoverflow.com/questions/14749504/android-usermanager-check-if-user-is-owner-admin
    @SuppressWarnings({ "ConstantConditions", "JavaReflectionMemberAccess" })
    public static int getUserId(Context context) {
        try {
            Method getUserHandle = UserManager.class.getMethod("getUserHandle");
            return (Integer) getUserHandle.invoke(context.getSystemService(Context.USER_SERVICE));
        } catch (Throwable ignored) {
        }
        return 0;
    }

    public static int getPendingIntentFlag() {
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flag;
    }

    public static <T> T getOrDefault(Map<String, Object> map, String key, T defaultValue) {
        T v;
        return (((v = (T) map.get(key)) != null) || map.containsKey(key)) ? v : defaultValue;
    }
}
