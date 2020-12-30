package me.liaoheng.wallpaper.data.db;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.preference.PreferenceManager;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import java.util.Objects;

import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.data.provider.TasksContract;
import me.liaoheng.wallpaper.ui.SettingsActivity;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.Settings;

/**
 * @author liaoheng
 * @version 2018-01-16 15:44
 */
public class DBHelper extends SQLiteOpenHelper {
    private final Context context;
    private static final int JOB_ID = 0x483;
    private static final String JOB_TAG = "bing_wallpaper_job_" + JOB_ID;

    public DBHelper(Context context) {
        super(context, Constants.PROJECT_NAME + ".db", null, BuildConfig.VERSION_CODE);
        this.context = context;
    }

    private static final String DB_CREATE = "create table " + TasksContract.TaskEntry.TABLE_NAME +
            " (" + TasksContract.TaskEntry._ID + " integer primary key autoincrement, " +
            TasksContract.TaskEntry.COLUMN_TAG + " text not null, " +
            TasksContract.TaskEntry.COLUMN_DATE + " integer not null);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion > 193) {
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        int type = sharedPreferences.getInt(Settings.BING_WALLPAPER_JOB_TYPE, -1);
        if (type != -1) {
            Settings.setJobType(context, type);
        }
        sharedPreferences.edit().remove(Settings.BING_WALLPAPER_JOB_TYPE).apply();
        boolean alarm = Settings.isAlarm(context);
        if (alarm) {
            sharedPreferences.edit()
                    .putString(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE_MODE,
                            String.valueOf(Settings.AUTOMATIC_UPDATE_TYPE_TIMER))
                    .apply();
            sharedPreferences.edit().remove("pref_set_wallpaper_day_auto_update").apply();
            PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAILY_UPDATE, true)
                    .apply();
        }

        int jobType = Settings.getJobType(context);
        if (jobType == Settings.SYSTEM || jobType == Settings.GOOGLE_SERVICE) {
            try {
                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
                dispatcher.cancel(JOB_TAG);
                JobScheduler jobScheduler = (JobScheduler)
                        context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                Objects.requireNonNull(jobScheduler).cancel(JOB_ID);
            } catch (Throwable ignored) {
            } finally {
                BingWallpaperJobManager.enableSystem(context);
            }
        }
    }
}
