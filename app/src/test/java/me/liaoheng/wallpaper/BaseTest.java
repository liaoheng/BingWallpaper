package me.liaoheng.wallpaper;

import net.danlew.android.joda.JodaTimeAndroid;

import org.junit.Before;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;

/**
 * @author liaoheng
 * @version 2016-07-25 15:31
 */
public class BaseTest {

    public final String TAG = this.getClass().getSimpleName();

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        JodaTimeAndroid.init(RuntimeEnvironment.application);
    }

    public void log(String msg, Object... o) {
        ShadowLog.d(TAG, String.format(msg, o));
    }
}
