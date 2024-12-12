package me.liaoheng.wallpaper.data.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.preference.PreferenceManager;

import com.github.liaoheng.common.util.L;

import java.io.File;

import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.data.provider.TasksContract;
import me.liaoheng.wallpaper.ui.SettingsActivity;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.util.Settings;

/**
 * @author liaoheng
 * @version 2018-01-16 15:44
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, Constants.PROJECT_NAME + "_" + BingWallpaperUtils.getUserId(context) + "_" + ".db", null,
                BuildConfig.VERSION_CODE);
    }

    private static final String DB_CREATE =
            "create table " + TasksContract.TaskEntry.TABLE_NAME + " (" + TasksContract.TaskEntry._ID
                    + " integer primary key autoincrement, " + TasksContract.TaskEntry.COLUMN_TAG + " text not null, "
                    + TasksContract.TaskEntry.COLUMN_DATE + " integer not null);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Migrate config to DataStore
     */
    public static void toChangeDataStore(Context context) {
        File tray = context.getDatabasePath(DBHelper.TrayDBHelper.DATABASE_NAME);
        if (tray != null && tray.exists()) {
            new Thread(() -> {
                SettingTrayPreferences trayPreferences = SettingTrayPreferences.get();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                trayPreferences.putBoolean(SettingsActivity.PREF_DOH,
                        preferences.getBoolean(SettingsActivity.PREF_DOH, false));
                trayPreferences.putBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE,
                        preferences.getBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE, false));
                trayPreferences.putString(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_MODE,
                        preferences.getString(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_MODE, "0"));
                trayPreferences.putString(SettingsActivity.PREF_LANGUAGE,
                        preferences.getString(SettingsActivity.PREF_LANGUAGE, "0"));

                try (TrayDBHelper dbHelper = new TrayDBHelper(context)) {
                    try (Cursor query = dbHelper.getReadableDatabase()
                            .query(TrayDBHelper.TABLE_NAME, null, null, null, null, null, null)) {
                        while (query.moveToNext()) {
                            String key = query.getString(query.getColumnIndexOrThrow(TrayDBHelper.KEY));
                            String value = query.getString(query.getColumnIndexOrThrow(TrayDBHelper.VALUE));
                            L.alog().w("toChangeDataStore", "key: " + key + "  value: " + value);
                            try {
                                switch (key) {
                                    case SettingsActivity.PREF_STACK_BLUR:
                                    case Settings.BING_WALLPAPER_JOB_TYPE:
                                        trayPreferences.putInt(key, Integer.parseInt(value));
                                        break;
                                    case SettingsActivity.PREF_SET_WALLPAPER_LOG:
                                    case SettingsActivity.PREF_CRASH_REPORT:
                                    case SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_SUCCESS_NOTIFICATION:
                                    case SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI:
                                    case SettingsActivity.PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER:
                                    case SettingsActivity.PREF_AUTO_SAVE_WALLPAPER_FILE:
                                        trayPreferences.putBoolean(key, Boolean.parseBoolean(value));
                                        break;
                                    case SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_INTERVAL:
                                    case SettingsActivity.PREF_STACK_BLUR_MODE:
                                    case SettingsActivity.PREF_COUNTRY:
                                    case SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION:
                                    case SettingsActivity.PREF_SAVE_WALLPAPER_RESOLUTION:
                                    case SettingsActivity.PREF_SET_WALLPAPER_AUTO_MODE:
                                    case SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_TIME:
                                    case Constants.PREF_LAST_WALLPAPER_IMAGE_URL:
                                        trayPreferences.putString(key, value);
                                        break;
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
                context.deleteDatabase(TrayDBHelper.DATABASE_NAME);
            }).start();
        }
    }

    public static class TrayDBHelper extends SQLiteOpenHelper {
        public static final String TABLE_NAME = "TrayPreferences";

        public static final String DATABASE_NAME = "tray.db";

        public static final String KEY = "KEY";

        public static final String VALUE = "VALUE";

        static final int DATABASE_VERSION = 2;

        public TrayDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
