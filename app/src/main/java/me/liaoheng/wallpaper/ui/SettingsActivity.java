package me.liaoheng.wallpaper.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.systembar.SystemBarHelper;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.ShellUtils;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.UIUtils;

import org.joda.time.LocalTime;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.reactivex.functions.Consumer;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.service.AutoSetWallpaperBroadcastReceiver;
import me.liaoheng.wallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.ISettingTrayPreferences;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.ROM;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.widget.TimePreference;

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
    public static final String PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_TYPE = "pref_set_wallpaper_day_fully_automatic_update_type";
    public static final String PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_NOTIFICATION = "pref_set_wallpaper_day_fully_automatic_update_notification";
    public static final String PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_INTERVAL = "pref_set_wallpaper_day_fully_automatic_update_interval";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE = "pref_set_wallpaper_day_auto_update";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME = "pref_set_wallpaper_day_auto_update_time";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI = "pref_set_wallpaper_day_auto_update_only_wifi";
    public static final String PREF_SET_WALLPAPER_LOG = "pref_set_wallpaper_debug_log";
    public static final String PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER = "pref_set_miui_lock_screen_wallpaper";
    public static final String PREF_CRASH_REPORT = "pref_crash_report";

    public static class MyPreferenceFragment extends com.fnp.materialpreferences.PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ISettingTrayPreferences mPreferences;

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
        private CheckBoxPreference mAutoUpdateNotificationPreference;
        private ListPreference mAutoUpdateIntervalPreference;
        private CheckBoxPreference mLogPreference;
        private CheckBoxPreference mCrashPreference;
        private ListPreference mAutoUpdateTypeListPreference;
        private CheckBoxPreference mMIuiLockScreenPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPreferences = SettingTrayPreferences.get(getActivity());
            Preference version = findPreference("pref_version");
            try {
                String versionName = AppUtils.getVersionInfo(getActivity()).versionName;
                version.setSummary(versionName);
            } catch (SystemException e) {
                L.alog().w(TAG, e);
            }

            findPreference("pref_intro").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UIUtils.startActivity(getActivity(), IntroActivity.class);
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
                                        @SuppressLint("CheckResult")
                                        @Override
                                        public void onYes(DialogInterface dialogInterface) {
                                            BingWallpaperUtils.clearCache(getActivity())
                                                    .subscribe(new Consumer<Object>() {
                                                        @Override
                                                        public void accept(Object o) throws Exception {
                                                            BingWallpaperUtils.showToast(getActivity(),
                                                                    getString(R.string.pref_clear_cache_success));
                                                        }
                                                    });
                                        }
                                    });
                            return true;
                        }
                    });

            Preference translation = findPreference("pref_translation");
            String translator = BingWallpaperUtils.getTranslator(getActivity());
            if (!TextUtils.isEmpty(translator)) {
                translation.setSummary(translator);
            }
            translation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    BingWallpaperUtils.openBrowser(getActivity(), "https://crowdin.com/project/starth-bing-wallpaper");
                    return false;
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
            mAutoUpdateTypeListPreference = (ListPreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_TYPE);

            mOnlyWifiPreference = (CheckBoxPreference) findPreference(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI);
            mAutoUpdatePreference = (CheckBoxPreference) findPreference(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE);
            mAutoUpdateIntervalPreference = (ListPreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_INTERVAL);
            mAutoUpdateNotificationPreference = (CheckBoxPreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_NOTIFICATION);
            mMIuiLockScreenPreference = (CheckBoxPreference) findPreference(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER);
            mLogPreference = (CheckBoxPreference) findPreference(PREF_SET_WALLPAPER_LOG);
            mCrashPreference = (CheckBoxPreference) findPreference(PREF_CRASH_REPORT);

            if (!ROM.getROM().isMiui()) {
                ((PreferenceCategory) findPreference("pref_other_group")).removePreference(mMIuiLockScreenPreference);
            } else {
                mMIuiLockScreenPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (mMIuiLockScreenPreference.isChecked()) {
                            if (ShellUtils.hasRootPermission()) {
                                mPreferences.put(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, true);
                            } else {
                                mMIuiLockScreenPreference.setChecked(false);
                                BingWallpaperUtils.showToast(getActivity(), R.string.unable_root_permission);
                            }
                        } else {
                            mPreferences.put(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, false);
                        }
                        return false;
                    }
                });
                mMIuiLockScreenPreference.setChecked(BingWallpaperUtils.isMiuiLockScreenSupport(getActivity()));
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (ROM.getROM().isMiui()) {
                    mModeTypeListPreference.setSummary(BingWallpaperUtils.getAutoMode(getActivity()));
                } else {
                    ((PreferenceCategory) findPreference("pref_wallpaper_group")).removePreference(
                            mModeTypeListPreference);
                }
            } else {
                mModeTypeListPreference.setSummary(BingWallpaperUtils.getAutoMode(getActivity()));
            }

            mResolutionListPreference.setSummary(BingWallpaperUtils.getResolution(getActivity()));
            mSaveResolutionListPreference.setSummary(BingWallpaperUtils.getSaveResolution(getActivity()));
            mCountryListPreference.setSummary(BingWallpaperUtils.getCountryName(getActivity()));

            mAutoUpdateTypeListPreference.setSummary(BingWallpaperUtils.getAutomaticUpdateTypeName(getActivity()));

            updateCheckTime();

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

        private void updateCheckTime() {
            mAutoUpdatePreference.setSummary(getString(R.string.pref_auto_update_check_time,
                    BingWallpaperUtils.getAutomaticUpdateInterval(getActivity())));
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
                    BingWallpaperUtils.clearCache(getActivity()).subscribe();
                    break;
                case PREF_SET_WALLPAPER_AUTO_MODE:
                    mModeTypeListPreference.setSummary(mModeTypeListPreference.getEntry());
                    mPreferences.put(PREF_SET_WALLPAPER_AUTO_MODE, mModeTypeListPreference.getValue());
                    break;
                case PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE:
                    if (mAutoUpdatePreference.isChecked()) {
                        if (!BingWallpaperJobManager.enabled(getActivity())) {
                            mAutoUpdatePreference.setChecked(false);
                            return;
                        }
                        BingWallpaperUtils.clearDayUpdateTime(getActivity());
                        BingWallpaperAlarmManager.disabled(getActivity());
                        mTimePreference.setSummary(R.string.pref_not_set_time);
                        mDayUpdatePreference.setChecked(false);
                    } else {
                        BingWallpaperJobManager.disabled(getActivity());
                    }
                    mPreferences.put(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE, mAutoUpdatePreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_TYPE:
                    mAutoUpdateTypeListPreference.setSummary(mAutoUpdateTypeListPreference.getEntry());
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
                case PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_NOTIFICATION:
                    mPreferences.put(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_NOTIFICATION,
                            mAutoUpdateNotificationPreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_INTERVAL:
                    mPreferences.put(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_INTERVAL,
                            mAutoUpdateIntervalPreference.getValue());
                    updateCheckTime();
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
                    mPreferences.put(PREF_SET_WALLPAPER_LOG, mLogPreference.isChecked());
                    if (mLogPreference.isChecked()) {
                        LogDebugFileUtils.init(getActivity());
                        LogDebugFileUtils.get().open();
                    } else {
                        LogDebugFileUtils.get().clearFile();
                    }
                    break;
                case PREF_CRASH_REPORT:
                    mPreferences.put(PREF_CRASH_REPORT, mCrashPreference.isChecked());
                    if (mCrashPreference.isChecked()) {
                        CrashReportHandle.enable(getActivity());
                    } else {
                        CrashReportHandle.disable(getActivity());
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
