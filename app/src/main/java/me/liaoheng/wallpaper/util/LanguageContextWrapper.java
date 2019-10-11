package me.liaoheng.wallpaper.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

/**
 * @author liaoheng
 * @version 2019-10-08 13:10
 * @see <a href="https://gist.github.com/sud007/1dfec39432a04d83c8945f2b448359c7">gist</a>
 */
public class LanguageContextWrapper {
    private static Locale originalLocale;

    public static Locale getOriginalLocale() {
        return originalLocale;
    }

    public static void init(Context context) {
        originalLocale = BingWallpaperUtils.getCurrentLocale(context);
    }

    /**
     * @param context use activity
     */
    public static Context wrap(Context context, Locale locale) {
        Configuration config = context.getResources().getConfiguration();
        Locale sysLocale = BingWallpaperUtils.getCurrentLocale(context);
        if (!sysLocale.equals(locale)) {
            Locale.setDefault(locale);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setSystemLocale(config, locale);
            } else {
                setSystemLocaleLegacy(config, locale);
            }

            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
        return context.createConfigurationContext(config);
    }

    public static void setSystemLocaleLegacy(Configuration config, Locale locale) {
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static void setSystemLocale(Configuration config, Locale locale) {
        config.setLocale(locale);
    }
}
