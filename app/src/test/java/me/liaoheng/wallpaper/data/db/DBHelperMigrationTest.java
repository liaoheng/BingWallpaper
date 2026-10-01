package me.liaoheng.wallpaper.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.function.BooleanSupplier;

import me.liaoheng.wallpaper.BaseTest;
import me.liaoheng.wallpaper.TestApplication;
import me.liaoheng.wallpaper.ui.SettingsActivity;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.util.Settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

/**
 * The tray.db to DataStore migration rewrites user settings, so verify the keys it moves and that
 * the old database is removed.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class DBHelperMigrationTest extends BaseTest {

    private Context context;

    @Before
    public void setUp() {
        super.setUp();
        context = RuntimeEnvironment.getApplication();
        SettingTrayPreferences.init(context);
        assumeFalse(SettingTrayPreferences.get() instanceof SettingTrayPreferences.TestPreferenceAccessor);
    }

    private void await(BooleanSupplier condition, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("interrupted");
            }
        }
        fail("condition not met within " + timeoutMs + "ms");
    }

    private void insert(SQLiteDatabase db, String key, String value) {
        db.execSQL("insert or replace into " + DBHelper.TrayDBHelper.TABLE_NAME + " ("
                + DBHelper.TrayDBHelper.KEY + ", " + DBHelper.TrayDBHelper.VALUE + ") values (?, ?)",
                new Object[] { key, value });
    }

    @Test
    public void migratesTrayPreferencesToDataStore() {
        try (DBHelper.TrayDBHelper helper = new DBHelper.TrayDBHelper(context)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("create table if not exists " + DBHelper.TrayDBHelper.TABLE_NAME + " ("
                    + DBHelper.TrayDBHelper.KEY + " text primary key, " + DBHelper.TrayDBHelper.VALUE
                    + " text)");
            insert(db, SettingsActivity.PREF_STACK_BLUR, "42");
            insert(db, Settings.BING_WALLPAPER_JOB_TYPE, String.valueOf(Settings.WORKER));
            insert(db, SettingsActivity.PREF_SET_WALLPAPER_LOG, "true");
            insert(db, SettingsActivity.PREF_COUNTRY, "2");
            insert(db, Constants.PREF_LAST_WALLPAPER_IMAGE_URL, "https://example.com/wallpaper.jpg");
        }

        assertTrue(context.getDatabasePath(DBHelper.TrayDBHelper.DATABASE_NAME).exists());

        DBHelper.toChangeDataStore(context);

        await(() -> Settings.getJobType(context) == Settings.WORKER, 10000);
        assertEquals(42, Settings.getSettingStackBlur());
        assertTrue(Settings.isEnableLog(context));
        assertEquals(2, Settings.getCountryValue());
        assertEquals("https://example.com/wallpaper.jpg", Settings.getLastWallpaperImageUrl(context));

        await(() -> !context.getDatabasePath(DBHelper.TrayDBHelper.DATABASE_NAME).exists(), 10000);
        assertFalse(context.getDatabasePath(DBHelper.TrayDBHelper.DATABASE_NAME).exists());
    }
}
