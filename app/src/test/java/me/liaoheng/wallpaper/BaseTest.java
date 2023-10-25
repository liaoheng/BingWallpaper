package me.liaoheng.wallpaper;

import android.util.Log;

import org.junit.Before;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.shadows.ShadowLog;

/**
 * @author liaoheng
 * @version 2016-07-25 15:31
 */
@ConscryptMode(ConscryptMode.Mode.OFF)
public class BaseTest {

    public final String TAG = this.getClass().getSimpleName();

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    public void log(String msg, Object... o) {
        Log.d(TAG, String.format(msg, o));
    }
}
