package me.liaoheng.wallpaper.util;


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
 * @version 2018-01-21 16:58
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = SdkConfig.MAX_SDK_VERSION)
public class TasksUtilsTest extends BaseTest {
    @Test
    public void isToDaysDoTest() {
        DateTime now = DateTime.now();
        //log("now : %s", now.toString());
        DateTime next = DateTime.now().minusHours(now.getHourOfDay() + 1);
        //log("next : %s", next.toString());
        boolean toDaysDo = TasksUtils.isToDaysDo(next, now, 1);
        //log("toDaysDo : %s", toDaysDo);
        assertTrue(toDaysDo);
    }
}
