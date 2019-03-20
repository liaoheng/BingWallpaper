package me.liaoheng.wallpaper.util;

import androidx.annotation.NonNull;
import net.grandcentrix.tray.core.PreferenceAccessor;
import net.grandcentrix.tray.core.TrayItem;

/**
 * @author liaoheng
 * @version 2018-09-30 04:54
 */
public interface ISettingTrayPreferences extends PreferenceAccessor<TrayItem> {

    @NonNull
    String getString(@NonNull final String key, @NonNull final String defaultValue);

}
