package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.support.annotation.NonNull;

import net.grandcentrix.tray.TrayPreferences;

import me.liaoheng.wallpaper.BuildConfig;

/**
 * @author liaoheng
 * @version 2018-01-24 12:05
 */
public class SettingTrayPreferences extends TrayPreferences {

    public SettingTrayPreferences(@NonNull Context context) {
        super(context, context.getPackageName(), BuildConfig.VERSION_CODE);
    }
}
