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
import java.util.concurrent.TimeUnit;

import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.data.provider.TasksContract;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.SettingUtils;

/**
 * @author liaoheng
 * @version 2018-01-16 15:44
 */
public class DBHelper extends SQLiteOpenHelper {
    private Context context;
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
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        BingWallpaperJobManager.setJobType(context, sharedPreferences.getInt("bing_wallpaper_job_type", -1));

        int jobType = BingWallpaperJobManager.getJobType(context);
        if (jobType == BingWallpaperJobManager.SYSTEM || jobType == BingWallpaperJobManager.GOOGLE_SERVICE) {
            try {
                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
                dispatcher.cancel(JOB_TAG);
                JobScheduler jobScheduler = (JobScheduler)
                        context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                Objects.requireNonNull(jobScheduler).cancel(JOB_ID);
            } catch (Throwable ignored) {
            } finally {
                BingWallpaperJobManager.enableSystem(context,
                        TimeUnit.HOURS.toSeconds(SettingUtils.getAutomaticUpdateInterval(context)));
            }
        }
    }
}
