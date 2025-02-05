package me.liaoheng.wallpaper.util;

import android.content.Context;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.ui.SettingsActivity;

/**
 * @author liaoheng
 * @version 2020-07-03 16:35
 */
public class Settings {

    public static boolean isCrashReport(Context context) {
        return SettingTrayPreferences.get(context).getBoolean(SettingsActivity.PREF_CRASH_REPORT, true);
    }

    public static boolean getOnlyWifi(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI, true);
    }

    public static String getResolution(Context context) {
        return getResolution(context, getResolutionValue(context));
    }

    public static String getResolution(Context context, int resolution) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);
        return names[resolution];
    }

    public static int getResolutionValue(Context context) {
        return Integer.parseInt(SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, "0"));
    }

    public static void putResolution(Context context, String resolution) {
        SettingTrayPreferences.get(context)
                .put(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, resolution);
    }

    public static void putSaveResolution(Context context, String resolution) {
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

    public static int getCountryValue(Context context) {
        String country = SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_COUNTRY, "0");
        return Integer.parseInt(country);
    }

    public static int getLanguageValue(Context context) {
        return Integer.parseInt(SettingTrayPreferences.get(context).getString(SettingsActivity.PREF_LANGUAGE, "0"));
    }

    public static String getAlarmTime(Context context) {
        return SettingTrayPreferences.get(context).getString(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_TIME, "");
    }

    public static boolean isAutoSave(Context context) {
        return SettingTrayPreferences.get(context).getBoolean(SettingsActivity.PREF_AUTO_SAVE_WALLPAPER_FILE, false);
    }

    public static boolean isEnableLog(Context context) {
        return isEnableLogProvider(context);
    }

    public static boolean isEnableLogProvider(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_LOG, false);
    }

    @IntDef(value = {
            AUTOMATIC_UPDATE_TYPE_AUTO,
            AUTOMATIC_UPDATE_TYPE_SYSTEM,
            AUTOMATIC_UPDATE_TYPE_SERVICE,
            AUTOMATIC_UPDATE_TYPE_TIMER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AutomaticUpdateTypeResult {}

    public final static int AUTOMATIC_UPDATE_TYPE_AUTO = 0;
    public final static int AUTOMATIC_UPDATE_TYPE_SYSTEM = 1;
    public final static int AUTOMATIC_UPDATE_TYPE_SERVICE = 2;
    public final static int AUTOMATIC_UPDATE_TYPE_TIMER = 3;

    @AutomaticUpdateTypeResult
    public static int getAutomaticUpdateType(Context context) {
        String type = SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_MODE, "0");
        return Integer.parseInt(Objects.requireNonNull(type));
    }

    public static boolean isAutomaticUpdateNotification(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_SUCCESS_NOTIFICATION, true);
    }

    // hour
    public static int getAutomaticUpdateInterval(Context context) {
        return Integer.parseInt(Objects.requireNonNull(SettingTrayPreferences.get(context)
                .getString(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_INTERVAL,
                        String.valueOf(Constants.DEF_SCHEDULER_PERIODIC))));
    }

    public static boolean isMiuiLockScreenSupport(Context context) {
        return SettingTrayPreferences.get(context)
                .getBoolean(SettingsActivity.PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, false);
    }

    public static boolean setMiuiLockScreenSupport(Context context, boolean support) {
        return SettingTrayPreferences.get(context)
                .put(SettingsActivity.PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, support);
    }

    public static int getSettingStackBlur() {
        return SettingTrayPreferences.get().getInt(SettingsActivity.PREF_STACK_BLUR, 0);
    }

    public static int getSettingStackBlurMode() {
        return Integer.parseInt(SettingTrayPreferences.get().getString(SettingsActivity.PREF_STACK_BLUR_MODE, "0"));
    }

    public static int getSettingBrightness() {
        return SettingTrayPreferences.get().getInt(SettingsActivity.PREF_BRIGHTNESS, 0);
    }

    public static int getSettingBrightnessMode() {
        return Integer.parseInt(SettingTrayPreferences.get().getString(SettingsActivity.PREF_BRIGHTNESS_MODE, "0"));
    }

    public static String getSettingStackBlurModeName(Context context) {
        String[] names = context.getResources()
                .getStringArray(R.array.pref_set_wallpaper_auto_mode_name);
        return names[getSettingStackBlurMode()];
    }

    public static void setLastWallpaperImageUrl(Context context, String url) {
        SettingTrayPreferences.get(context).put(Constants.PREF_LAST_WALLPAPER_IMAGE_URL, url);
    }

    public static String getLastWallpaperImageUrl(Context context) {
        return SettingTrayPreferences.get(context).getString(Constants.PREF_LAST_WALLPAPER_IMAGE_URL, "");
    }

    @IntDef(value = {
            NONE,
            GOOGLE_SERVICE,
            SYSTEM,
            DAEMON_SERVICE,
            WORKER,
            LIVE_WALLPAPER,
            TIMER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface JobType {}

    public final static int NONE = -1;
    @Deprecated
    public final static int GOOGLE_SERVICE = 0;
    @Deprecated
    public final static int SYSTEM = 1;
    @Deprecated
    public final static int DAEMON_SERVICE = 2;
    public final static int WORKER = 3;
    public final static int LIVE_WALLPAPER = 4;
    public final static int TIMER = 5;

    public static final String BING_WALLPAPER_JOB_TYPE = "bing_wallpaper_job_type";
    public static final String LIVE_WALLPAPER_HEART_BEAT = "live_wallpaper_heart_beat";

    public static void setJobType(Context context, @JobType int type) {
        SettingTrayPreferences.get(context).put(BING_WALLPAPER_JOB_TYPE, type);
    }

    @JobType
    public static int getJobType(Context context) {
        return SettingTrayPreferences.get(context).getInt(BING_WALLPAPER_JOB_TYPE, -1);
    }

    public static String getJobTypeString(Context context) {
        String[] jts = context.getResources().getStringArray(R.array.job_type);
        return jts[getJobType(context) + 1];
    }

    public static void updateLiveWallpaperHeartbeat(long time) {
        SettingTrayPreferences.get().put(LIVE_WALLPAPER_HEART_BEAT, String.valueOf(time));
    }

    public static long getLiveWallpaperHeartbeat() {
        return Long.parseLong(SettingTrayPreferences.get().getString(LIVE_WALLPAPER_HEART_BEAT, "0"));
    }

}
