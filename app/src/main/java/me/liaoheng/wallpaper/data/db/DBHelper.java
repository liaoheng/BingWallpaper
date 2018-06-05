package me.liaoheng.wallpaper.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import net.grandcentrix.tray.AppPreferences;

import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.provider.TasksContract;
import me.liaoheng.wallpaper.ui.SettingsActivity;
import me.liaoheng.wallpaper.util.Constants;

/**
 * @author liaoheng
 * @version 2018-01-16 15:44
 */
public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    public DBHelper(Context context) {
        super(context, Constants.PROJECT_NAME + ".db", null, BuildConfig.VERSION_CODE);
        mContext = context;
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
        AppPreferences appPreferences = new AppPreferences(mContext);

        String[] names = mContext.getResources()
                .getStringArray(R.array.pref_set_wallpaper_resolution_name);

        String resolution = appPreferences
                .getString(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, "0");
        try {
            String name = names[Integer.parseInt(resolution)];
        } catch (Exception e) {
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putString(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, "0")
                    .apply();
            appPreferences.put(SettingsActivity.PREF_SET_WALLPAPER_RESOLUTION, "0");
        }
    }
}
