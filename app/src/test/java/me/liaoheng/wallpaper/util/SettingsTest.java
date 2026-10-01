package me.liaoheng.wallpaper.util;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.function.BooleanSupplier;

import me.liaoheng.wallpaper.BaseTest;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.TestApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class SettingsTest extends BaseTest {

    private Context context;

    @Before
    public void setUp() {
        super.setUp();
        context = RuntimeEnvironment.getApplication();
        SettingTrayPreferences.init(context);
        // the DataStore backed preferences need coroutines-rx3 on the test runtime; if the builder
        // fell back to the no-op accessor these tests would assert against frozen defaults
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

    @Test
    public void jobTypeDefaultsToNone() {
        assertEquals(Settings.NONE, Settings.getJobType(context));
        assertEquals("None", Settings.getJobTypeString(context));
    }

    @Test
    public void jobTypeRoundTripAndStringMapping() {
        Settings.setJobType(context, Settings.WORKER);
        await(() -> Settings.getJobType(context) == Settings.WORKER, 5000);
        assertEquals(context.getString(R.string.pref_set_wallpaper_day_fully_automatic_update_type_system),
                Settings.getJobTypeString(context));

        Settings.setJobType(context, Settings.LIVE_WALLPAPER);
        await(() -> Settings.getJobType(context) == Settings.LIVE_WALLPAPER, 5000);
        assertEquals(context.getString(R.string.pref_set_wallpaper_day_fully_automatic_update_type_service),
                Settings.getJobTypeString(context));

        Settings.setJobType(context, Settings.TIMER);
        await(() -> Settings.getJobType(context) == Settings.TIMER, 5000);
        assertEquals(context.getString(R.string.pref_set_wallpaper_day_fully_automatic_update_type_timer),
                Settings.getJobTypeString(context));

        assertFalse(Settings.getJobType(context) == Settings.NONE);
    }

    @Test
    public void automaticUpdateTypeDefaultsToAuto() {
        assertEquals(Settings.AUTOMATIC_UPDATE_TYPE_AUTO, Settings.getAutomaticUpdateType(context));
    }
}
