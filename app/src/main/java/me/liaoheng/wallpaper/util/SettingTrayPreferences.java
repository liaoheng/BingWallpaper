package me.liaoheng.wallpaper.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.preference.PreferenceDataStore;

import java.util.Set;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Use <a href="https://developer.android.com/topic/libraries/architecture/datastore">Preferences DataStore</a>
 *
 * @author liaoheng
 * @data 2024-11-12 10:32
 */
public class SettingTrayPreferences extends PreferenceDataStore {

    private static SettingTrayPreferences mPreferences;

    public static void init(Context context) {
        try {
            mPreferences = new SettingTrayPreferences(
                    new RxPreferenceDataStoreBuilder(context, "setting_preferences").setIoScheduler(
                            Schedulers.io()).build());
        } catch (Throwable e) {
            mPreferences = new TestPreferenceAccessor(null);
        }
    }

    @Deprecated
    public static SettingTrayPreferences get(Context context) {
        return mPreferences;
    }

    public static SettingTrayPreferences get() {
        return mPreferences;
    }

    final RxDataStore<Preferences> mAccessor;

    private SettingTrayPreferences(RxDataStore<Preferences> accessor) {
        mAccessor = accessor;
    }

    public boolean clear() {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.clear();
            return Single.just(preferences);
        }).subscribe();
        return true;
    }

    public boolean contains(String key) {
        return mAccessor.data().map(p -> p.asMap().containsKey(new Preferences.Key<>(key))).blockingFirst(false);
    }

    public boolean getBoolean(@NonNull String key) {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return mAccessor.data()
                .flatMap(p -> getValue(p, PreferencesKeys.booleanKey(key), defaultValue))
                .blockingFirst();
    }

    <T> Flowable<T> getValue(Preferences preferences, Preferences.Key<T> mkey, T def) {
        T o = preferences.get(mkey);
        if (null == o) {
            return Flowable.just(def);
        }
        return Flowable.just(o);
    }

    public float getFloat(@NonNull String key) {
        return getFloat(key, 0);
    }

    @Override
    public float getFloat(@NonNull String key, float defaultValue) {
        return mAccessor.data().flatMap(p -> getValue(p, PreferencesKeys.floatKey(key), defaultValue)).blockingFirst();
    }

    public int getInt(@NonNull String key) {
        return getInt(key, 0);
    }

    @Override
    public int getInt(@NonNull String key, int defaultValue) {
        return mAccessor.data().flatMap(p -> getValue(p, PreferencesKeys.intKey(key), defaultValue)).blockingFirst();
    }

    public long getLong(@NonNull String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(@NonNull String key, long defaultValue) {
        return mAccessor.data().flatMap(p -> getValue(p, PreferencesKeys.longKey(key), defaultValue)).blockingFirst();
    }

    @NonNull
    public String getString(@NonNull String key) {
        return getString(key, "");
    }

    @NonNull
    @Override
    public String getString(@NonNull String key, @Nullable String defaultValue) {
        return mAccessor.data()
                .flatMap(p -> getValue(p, PreferencesKeys.stringKey(key), defaultValue == null ? "" : defaultValue))
                .blockingFirst();
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return mAccessor.data().flatMap(p -> getValue(p, PreferencesKeys.stringSetKey(key), defValues)).blockingFirst();
    }

    public boolean put(@NonNull String key, @Nullable String value) {
        putString(key, value);
        return true;
    }

    public boolean put(@NonNull String key, int value) {
        putInt(key, value);
        return true;
    }

    public boolean put(@NonNull String key, boolean value) {
        putBoolean(key, value);
        return true;
    }

    @Override
    public void putString(String key, @Nullable String value) {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.set(PreferencesKeys.stringKey(key), value);
            return Single.just(preferences);
        }).subscribe();
    }

    @Override
    public void putStringSet(String key, @Nullable Set<String> values) {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.set(new Preferences.Key<>(key), values);
            return Single.just(preferences);
        }).subscribe();
    }

    @Override
    public void putInt(String key, int value) {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.set(PreferencesKeys.intKey(key), value);
            return Single.just(preferences);
        }).subscribe();
    }

    @Override
    public void putLong(String key, long value) {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.set(PreferencesKeys.longKey(key), value);
            return Single.just(preferences);
        }).subscribe();
    }

    @Override
    public void putFloat(String key, float value) {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.set(PreferencesKeys.floatKey(key), value);
            return Single.just(preferences);
        }).subscribe();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.set(PreferencesKeys.booleanKey(key), value);
            return Single.just(preferences);
        }).subscribe();
    }

    public boolean remove(@NonNull String key) {
        mAccessor.updateDataAsync(p -> {
            MutablePreferences preferences = p.toMutablePreferences();
            preferences.remove(new Preferences.Key<>(key));
            return Single.just(preferences);
        }).subscribe();
        return true;
    }

    public static class TestPreferenceAccessor extends SettingTrayPreferences {

        private TestPreferenceAccessor(RxDataStore<Preferences> accessor) {
            super(accessor);
        }

        @Override
        public boolean clear() {
            return false;
        }

        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public boolean getBoolean(@NonNull String key) {
            return false;
        }

        @Override
        public boolean getBoolean(@NonNull String key, boolean defaultValue) {
            return false;
        }

        @Override
        public float getFloat(@NonNull String key) {
            return 0;
        }

        @Override
        public float getFloat(@NonNull String key, float defaultValue) {
            return 0;
        }

        @Override
        public int getInt(@NonNull String key) {
            return 0;
        }

        @Override
        public int getInt(@NonNull String key, int defaultValue) {
            return 0;
        }

        @Override
        public long getLong(@NonNull String key) {
            return 0;
        }

        @Override
        public long getLong(@NonNull String key, long defaultValue) {
            return 0;
        }

        @NonNull
        @Override
        public String getString(@NonNull String key) {
            return "";
        }

        @NonNull
        @Override
        public String getString(@NonNull String key, @Nullable String defaultValue) {
            return "";
        }

        @Override
        public boolean put(@NonNull String key, @Nullable String value) {
            return false;
        }

        @Override
        public boolean put(@NonNull String key, int value) {
            return false;
        }

        @Override
        public boolean put(@NonNull String key, boolean value) {
            return false;
        }

        @Override
        public boolean remove(@NonNull String key) {
            return false;
        }
    }
}
