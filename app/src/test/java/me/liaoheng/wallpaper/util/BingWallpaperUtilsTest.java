package me.liaoheng.wallpaper.util;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;

import me.liaoheng.wallpaper.BaseTest;
import me.liaoheng.wallpaper.BuildConfig;

import static org.junit.Assert.assertTrue;

/**
 * @author liaoheng
 * @version 2018-01-21 17:29
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = SdkConfig.MAX_SDK_VERSION)
public class BingWallpaperUtilsTest extends BaseTest {

    @Test
    public void checkTimeTest() {
        DateTime dateTime = DateTime.now().minusHours(1);
        DateTime dateTime1 = BingWallpaperUtils.checkTime(dateTime.toLocalTime());
        assertTrue(dateTime1.getDayOfMonth() == dateTime.getDayOfMonth() + 1);
    }
}
