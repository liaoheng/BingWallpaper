package me.liaoheng.wallpaper.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.systembar.SystemBarHelper;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.UIUtils;

import org.joda.time.LocalTime;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.service.AutoSetWallpaperBroadcastReceiver;
import me.liaoheng.wallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NetUtils;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.widget.TimePreference;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author liaoheng
 * @version 2016-09-20 13:59
 */
public class SettingsActivity extends com.fnp.materialpreferences.PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BingWallpaperUtils.setPhoneScreen(this);
        super.onCreate(savedInstanceState);
        SystemBarHelper
                .tintStatusBar(this, ContextCompat.getColor(this, R.color.colorPrimaryDark), 0);
        setPreferenceFragment(new MyPreferenceFragment());
    }

    public static final String PREF_COUNTRY = "pref_country";
    public static final String PREF_SET_WALLPAPER_RESOLUTION = "pref_set_wallpaper_resolution";
    public static final String PREF_SAVE_WALLPAPER_RESOLUTION = "pref_save_wallpaper_resolution";
    public static final String PREF_SET_WALLPAPER_AUTO_MODE = "pref_set_wallpaper_auto_mode";
    public static final String PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE = "pref_set_wallpaper_day_fully_automatic_update";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE = "pref_set_wallpaper_day_auto_update";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME = "pref_set_wallpaper_day_auto_update_time";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI = "pref_set_wallpaper_day_auto_update_only_wifi";
    public static final String PREF_SET_WALLPAPER_LOG = "pref_set_wallpaper_debug_log";

    public static class MyPreferenceFragment extends com.fnp.materialpreferences.PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public int addPreferencesFromResource() {
            return R.xml.preferences;
        }

        private CheckBoxPreference mOnlyWifiPreference;
        private ListPreference mCountryListPreference;
        private ListPreference mResolutionListPreference;
        private ListPreference mSaveResolutionListPreference;
        private ListPreference mModeTypeListPreference;
        private TimePreference mTimePreference;
        private CheckBoxPreference mDayUpdatePreference;
        private CheckBoxPreference mAutoUpdatePreference;
        private SettingTrayPreferences mPreferences;
        private Dialog mFeedbackDialog;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPreferences = new SettingTrayPreferences(getActivity());
            Preference version = findPreference("pref_version");
            try {
                String versionName = AppUtils.getVersionInfo(getActivity()).versionName;
                version.setSummary(versionName);
            } catch (SystemException e) {
                L.Log.w(TAG, e);
            }
            mOnlyWifiPreference = (CheckBoxPreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI);

            mFeedbackDialog = UIUtils.createAlertDialog(getActivity(), getString(R.string.pref_feedback), "E-Mail",
                    "Github",
                    new Callback4.EmptyCallback<DialogInterface>() {
                        @Override
                        public void onYes(DialogInterface dialogInterface) {
                            BingWallpaperUtils.sendFeedback(getActivity());
                        }

                        @Override
                        public void onNo(DialogInterface dialogInterface) {
                            BingWallpaperUtils.openBrowser(getActivity(),
                                    "https://github.com/liaoheng/BingWallpaper/issues");
                        }
                    });

            findPreference("pref_issues").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UIUtils.showDialog(mFeedbackDialog);
                    return true;
                }
            });

            findPreference("pref_license").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UIUtils.startActivity(getActivity(), LicenseActivity.class);
                    return true;
                }
            });

            findPreference("pref_clear_cache").setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            UIUtils.showYNAlertDialog(getActivity(), getString(R.string.pref_clear_cache) + "?",
                                    new Callback4.EmptyCallback<DialogInterface>() {
                                        @Override
                                        public void onYes(DialogInterface dialogInterface) {
                                            GlideApp.get(getActivity()).clearMemory();
                                            Observable.just("")
                                                    .subscribeOn(Schedulers.io())
                                                    .map(new Func1<String, Object>() {
                                                        @Override
                                                        public Object call(String s) {
                                                            GlideApp.get(getActivity()).clearDiskCache();
                                                            NetUtils.get().clearCache();
                                                            return null;
                                                        }
                                                    })
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Action1<Object>() {
                                                        @Override
                                                        public void call(Object o) {
                                                            UIUtils.showToast(getActivity(),
                                                                    getString(R.string.pref_clear_cache_success));
                                                        }
                                                    });
                                        }
                                    });
                            return true;
                        }
                    });

            mCountryListPreference = (ListPreference) findPreference(
                    PREF_COUNTRY);
            mResolutionListPreference = (ListPreference) findPreference(
                    PREF_SET_WALLPAPER_RESOLUTION);
            mSaveResolutionListPreference = (ListPreference) findPreference(
                    PREF_SAVE_WALLPAPER_RESOLUTION);
            mModeTypeListPreference = (ListPreference) findPreference(
                    PREF_SET_WALLPAPER_AUTO_MODE);
            mDayUpdatePreference = (CheckBoxPreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_AUTO_UPDATE);
            mTimePreference = (TimePreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME);
            mAutoUpdatePreference = (CheckBoxPreference) findPreference(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE);

            String[] nameStrings = getResources().getStringArray(R.array.pref_set_wallpaper_auto_mode_name);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                nameStrings = new String[] { getString(R.string.pref_set_wallpaper_auto_mode_both) };
            }
            mModeTypeListPreference.setEntries(nameStrings);

            mResolutionListPreference.setSummary(BingWallpaperUtils.getResolution(getActivity()));
            mModeTypeListPreference.setSummary(BingWallpaperUtils.getAutoMode(getActivity()));
            mSaveResolutionListPreference.setSummary(BingWallpaperUtils.getSaveResolution(getActivity()));
            mCountryListPreference.setSummary(BingWallpaperUtils.getCountryName(getActivity()));

            LocalTime localTime = BingWallpaperUtils.getDayUpdateTime(getActivity());

            if (localTime != null) {
                mTimePreference.setSummary(localTime.toString("HH:mm"));
            } else {
                mTimePreference.setSummary(R.string.pref_not_set_time);
            }
            if (mAutoUpdatePreference.isChecked()) {
                mDayUpdatePreference.setChecked(false);
            }
            mTimePreference.setEnabled(mDayUpdatePreference.isChecked());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                String key) {
            switch (key) {
                case PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI:
                    mPreferences.put(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI, mOnlyWifiPreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_RESOLUTION:
                    mResolutionListPreference.setSummary(mResolutionListPreference.getEntry());
                    mPreferences.put(PREF_SET_WALLPAPER_RESOLUTION, mResolutionListPreference.getValue());
                    break;
                case PREF_SAVE_WALLPAPER_RESOLUTION:
                    mSaveResolutionListPreference.setSummary(mSaveResolutionListPreference.getEntry());
                    break;
                case PREF_COUNTRY:
                    mCountryListPreference.setSummary(mCountryListPreference.getEntry());
                    mPreferences.put(PREF_COUNTRY, mCountryListPreference.getValue());
                    break;
                case PREF_SET_WALLPAPER_AUTO_MODE:
                    mModeTypeListPreference.setSummary(mModeTypeListPreference.getEntry());
                    mPreferences.put(PREF_SET_WALLPAPER_AUTO_MODE, mModeTypeListPreference.getValue());
                    break;
                case PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE:
                    if (mAutoUpdatePreference.isChecked()) {
                        mTimePreference.setSummary(R.string.pref_not_set_time);
                        mDayUpdatePreference.setChecked(false);
                        BingWallpaperUtils.clearDayUpdateTime(getActivity());
                        BingWallpaperAlarmManager.disabled(getActivity());
                        BingWallpaperJobManager.enabled(getActivity());
                    } else {
                        BingWallpaperJobManager.disabled(getActivity());
                    }
                    mPreferences.put(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE, mAutoUpdatePreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_DAY_AUTO_UPDATE:
                    if (mDayUpdatePreference.isChecked()) {
                        mAutoUpdatePreference.setChecked(false);
                        BingWallpaperUtils.enabledReceiver(getActivity(),
                                AutoSetWallpaperBroadcastReceiver.class.getName());
                    } else {
                        BingWallpaperUtils.disabledReceiver(getActivity(),
                                AutoSetWallpaperBroadcastReceiver.class.getName());
                    }
                    mTimePreference.setEnabled(mDayUpdatePreference.isChecked());
                    mPreferences.put(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE, mDayUpdatePreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME:
                    if (mTimePreference.isEnabled()) {
                        BingWallpaperAlarmManager
                                .enabled(getActivity(), mTimePreference.getLocalTime());
                        mPreferences.put(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME,
                                mTimePreference.getLocalTime().toString());
                    }
                    break;
                case PREF_SET_WALLPAPER_LOG:
                    CheckBoxPreference logPreference = (CheckBoxPreference) findPreference(
                            PREF_SET_WALLPAPER_LOG);

                    mPreferences.put(PREF_SET_WALLPAPER_LOG, logPreference.isChecked());

                    if (logPreference.isChecked()) {
                        LogDebugFileUtils.get().init();
                        LogDebugFileUtils.get().open();
                    } else {
                        LogDebugFileUtils.get().clearFile();
                    }
                    break;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
