package me.liaoheng.wallpaper.util;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import me.liaoheng.wallpaper.BaseTest;
import me.liaoheng.wallpaper.TestApplication;

import static org.junit.Assert.assertEquals;

/**
 * @author liaoheng
 * @version 2018-01-21 17:29
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = TestApplication.class)
public class BingWallpaperUtilsTest extends BaseTest {

    @Test
    public void checkTimeTest() {
        DateTime dateTime = DateTime.now().minusHours(1);
        DateTime dateTime1 = BingWallpaperUtils.checkTime(dateTime.toLocalTime());
        assertEquals(dateTime1.getDayOfMonth(), dateTime.plusDays(1).getDayOfMonth());
    }
}
