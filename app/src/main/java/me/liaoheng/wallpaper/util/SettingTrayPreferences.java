package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.support.annotation.NonNull;

import net.grandcentrix.tray.TrayPreferences;

/**
 * @author liaoheng
 * @version 2018-01-24 12:05
 */
public class SettingTrayPreferences extends TrayPreferences {

    private static SettingTrayPreferences mPreferences;

    public static synchronized SettingTrayPreferences get(Context context) {
        if (mPreferences == null) {
            mPreferences = new SettingTrayPreferences(context);
        }
        return mPreferences;
    }

    private SettingTrayPreferences(@NonNull Context context) {
        super(context, context.getPackageName(), 1);
    }
}
