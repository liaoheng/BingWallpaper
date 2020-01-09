package me.liaoheng.wallpaper.util;

import androidx.annotation.NonNull;

import net.grandcentrix.tray.core.PreferenceAccessor;
import net.grandcentrix.tray.core.TrayItem;

/**
 * @author liaoheng
 * @version 2018-09-30 04:54
 */
public interface ISettingTrayPreferences extends PreferenceAccessor<TrayItem> {

    /**
     * {@inheritDoc}
     */
    boolean getBoolean(@NonNull final String key);

    /**
     * {@inheritDoc}
     */
    boolean getBoolean(@NonNull final String key, final boolean defaultValue);

    /**
     * {@inheritDoc}
     */
    float getFloat(@NonNull final String key);

    /**
     * {@inheritDoc}
     */
    float getFloat(@NonNull final String key, final float defaultValue);

    /**
     * {@inheritDoc}
     */
    int getInt(@NonNull final String key);

    /**
     * {@inheritDoc}
     */
    int getInt(@NonNull final String key, final int defaultValue);

    /**
     * {@inheritDoc}
     */
    long getLong(@NonNull final String key);

    /**
     * {@inheritDoc}
     */
    long getLong(@NonNull final String key, final long defaultValue);

    /**
     * {@inheritDoc}
     */
    @NonNull
    String getString(@NonNull final String key);

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    String getString(@NonNull final String key, @NonNull final String defaultValue);
}
