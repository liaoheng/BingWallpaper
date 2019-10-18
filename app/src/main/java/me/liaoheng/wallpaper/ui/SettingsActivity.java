package me.liaoheng.wallpaper.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.ShellUtils;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;

import org.joda.time.LocalTime;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.service.AutoSetWallpaperBroadcastReceiver;
import me.liaoheng.wallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.ISettingTrayPreferences;
import me.liaoheng.wallpaper.util.LanguageContextWrapper;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.widget.SeekBarDialogPreference;
import me.liaoheng.wallpaper.widget.SeekBarPreferenceDialogFragmentCompat;
import me.liaoheng.wallpaper.widget.TimePreference;
import me.liaoheng.wallpaper.widget.TimePreferenceDialogFragmentCompat;

/**
 * @author liaoheng
 * @version 2016-09-20 13:59
 */
public class SettingsActivity extends BaseActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static boolean isChangeLanguage;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LanguageContextWrapper.wrap(context, BingWallpaperUtils.getLanguage(context)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BingWallpaperUtils.setPhoneScreen(this);
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_main_setting);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, new MyPreferenceFragment(), "SettingsFragment")
                .commit();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isChangeLanguage = savedInstanceState.getBoolean("isChangeLanguage");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isChangeLanguage", isChangeLanguage);
    }

    @Override
    public void onBackPressed() {
        if (isChangeLanguage) {
            setResult(RESULT_OK);
        }
        isChangeLanguage = false;
        super.onBackPressed();
    }

    public static final String PREF_COUNTRY = "pref_country";
    public static final String PREF_LANGUAGE = "pref_language";
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
    public static final String PREF_PREF_PIXABAY_SUPPORT = "pref_pixabay_support";
    public static final String PREF_CRASH_REPORT = "pref_crash_report";
    public static final String PREF_STACK_BLUR = "pref_stack_blur";
    public static final String PREF_AUTO_SAVE_WALLPAPER_FILE = "pref_auto_save_wallpaper_file";

    public static class MyPreferenceFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ISettingTrayPreferences mPreferences;

        private SwitchPreferenceCompat mOnlyWifiPreference;
        private ListPreference mCountryListPreference;
        private ListPreference mLanguageListPreference;
        private ListPreference mResolutionListPreference;
        private ListPreference mSaveResolutionListPreference;
        private ListPreference mModeTypeListPreference;
        private TimePreference mTimePreference;
        private SwitchPreferenceCompat mDayUpdatePreference;
        private SwitchPreferenceCompat mAutoUpdatePreference;
        private SwitchPreferenceCompat mAutoUpdateNotificationPreference;
        private ListPreference mAutoUpdateIntervalPreference;
        private SwitchPreferenceCompat mLogPreference;
        private SwitchPreferenceCompat mCrashPreference;
        private ListPreference mAutoUpdateTypeListPreference;
        private SwitchPreferenceCompat mMIuiLockScreenPreference;
        private SwitchPreferenceCompat mPixabaySupportPreference;
        private SeekBarDialogPreference mStackBlurPreference;
        private SwitchPreferenceCompat mAutoSaveWallpaperPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof TimePreference) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    DialogFragment dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
                    dialogFragment.setTargetFragment(this, 0);
                    dialogFragment.show(fragmentManager, "TimePreference");
                }
            } else if (preference instanceof SeekBarDialogPreference) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    DialogFragment dialogFragment = SeekBarPreferenceDialogFragmentCompat.newInstance(
                            preference.getKey());
                    dialogFragment.setTargetFragment(this, 1);
                    dialogFragment.show(fragmentManager, "SeekBarDialogPreference");
                }
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

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

            findPreference("pref_github").setOnPreferenceClickListener(preference -> {
                BingWallpaperUtils.openBrowser(getActivity(), "https://github.com/liaoheng/BingWallpaper");
                return true;
            });

            findPreference("pref_intro").setOnPreferenceClickListener(preference -> {
                UIUtils.startActivity(getActivity(), IntroActivity.class);
                return true;
            });

            findPreference("pref_license").setOnPreferenceClickListener(preference -> {
                UIUtils.startActivity(getActivity(), LicenseActivity.class);
                return true;
            });

            findPreference("pref_clear_cache").setOnPreferenceClickListener(
                    preference -> {
                        UIUtils.showYNAlertDialog(getActivity(), getString(R.string.pref_clear_cache) + "?",
                                new Callback4.EmptyCallback<DialogInterface>() {
                                    @Override
                                    public void onYes(DialogInterface dialogInterface) {
                                        Utils.addSubscribe(BingWallpaperUtils.clearCache(getActivity()),
                                                new Callback.EmptyCallback<Object>() {
                                                    @Override
                                                    public void onSuccess(Object o) {
                                                        UIUtils.showToast(getActivity(),
                                                                getString(R.string.pref_clear_cache_success));
                                                    }
                                                });
                                    }
                                });
                        return true;
                    });

            Preference translation = findPreference("pref_translation");
            String translator = BingWallpaperUtils.getTranslator(getActivity());
            if (!TextUtils.isEmpty(translator)) {
                translation.setSummary(translator);
            }
            translation.setOnPreferenceClickListener(preference -> {
                BingWallpaperUtils.openBrowser(getActivity(), "https://crowdin.com/project/starth-bing-wallpaper");
                return true;
            });

            mCountryListPreference = findPreference(PREF_COUNTRY);
            mLanguageListPreference = findPreference(PREF_LANGUAGE);
            mResolutionListPreference = findPreference(PREF_SET_WALLPAPER_RESOLUTION);
            mSaveResolutionListPreference = findPreference(PREF_SAVE_WALLPAPER_RESOLUTION);
            mModeTypeListPreference = findPreference(PREF_SET_WALLPAPER_AUTO_MODE);
            mDayUpdatePreference = findPreference(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE);
            mTimePreference = findPreference(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME);
            mAutoUpdateTypeListPreference = findPreference(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_TYPE);

            mOnlyWifiPreference = findPreference(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI);
            mAutoUpdatePreference = findPreference(
                    PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE);
            mAutoUpdateIntervalPreference = findPreference(
                    PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_INTERVAL);
            mAutoUpdateNotificationPreference = findPreference(
                    PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE_NOTIFICATION);
            mMIuiLockScreenPreference = findPreference(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER);
            mPixabaySupportPreference = findPreference(PREF_PREF_PIXABAY_SUPPORT);
            mLogPreference = findPreference(PREF_SET_WALLPAPER_LOG);
            mCrashPreference = findPreference(PREF_CRASH_REPORT);
            mStackBlurPreference = findPreference(PREF_STACK_BLUR);
            mStackBlurPreference.setSummary(String.valueOf(BingWallpaperUtils.getSettingStackBlur(getActivity())));
            mAutoSaveWallpaperPreference = findPreference(PREF_AUTO_SAVE_WALLPAPER_FILE);

            if (!ROM.getROM().isMiui()) {
                ((PreferenceCategory) findPreference("pref_wallpaper_group")).removePreference(
                        mMIuiLockScreenPreference);
            } else {
                mMIuiLockScreenPreference.setOnPreferenceClickListener(preference -> {
                    if (mMIuiLockScreenPreference.isChecked()) {
                        if (ShellUtils.hasRootPermission()) {
                            mPreferences.put(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, true);
                        } else {
                            mMIuiLockScreenPreference.setChecked(false);
                            UIUtils.showToast(getActivity(), R.string.unable_root_permission);
                        }
                    } else {
                        mPreferences.put(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER, false);
                    }
                    return false;
                });
                mMIuiLockScreenPreference.setChecked(BingWallpaperUtils.isMiuiLockScreenSupport(getActivity()));
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (ROM.getROM().isMiui()) {
                    mModeTypeListPreference.setSummary(BingWallpaperUtils.getAutoMode(getActivity()));
                } else {
                    ((PreferenceCategory) findPreference("pref_update_group")).removePreference(
                            mModeTypeListPreference);
                }
            } else {
                mModeTypeListPreference.setSummary(BingWallpaperUtils.getAutoMode(getActivity()));
            }

            mResolutionListPreference.setSummary(BingWallpaperUtils.getResolution(getActivity()));
            mSaveResolutionListPreference.setSummary(BingWallpaperUtils.getSaveResolution(getActivity()));
            mCountryListPreference.setSummary(BingWallpaperUtils.getCountryName(getActivity()));
            mLanguageListPreference.setSummary(BingWallpaperUtils.getLanguageName(getActivity()));

            mAutoUpdateTypeListPreference.setSummary(BingWallpaperUtils.getAutomaticUpdateTypeName(getActivity()));

            updateCheckTime();

            LocalTime localTime = BingWallpaperUtils.getDayUpdateTime(getActivity());

            if (localTime != null) {
                mTimePreference.setSummary(localTime.toString("HH:mm"));
                mTimePreference.setLocalTime(localTime);
            } else {
                mTimePreference.setSummary(R.string.pref_not_set_time);
                mTimePreference.setLocalTime(null);
            }
            if (mAutoUpdatePreference.isChecked()) {
                mDayUpdatePreference.setChecked(false);
            }
            mTimePreference.setEnabled(mDayUpdatePreference.isChecked());
        }

        @SuppressLint("StringFormatMatches")
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
                    BingWallpaperUtils.clearNetCache().subscribe();
                    break;
                case PREF_LANGUAGE:
                    LanguageContextWrapper.wrap(getActivity(), BingWallpaperUtils.getLanguage(getContext()));
                    isChangeLanguage = true;
                    getActivity().recreate();
                    break;
                case PREF_PREF_PIXABAY_SUPPORT:
                    mPreferences.put(PREF_PREF_PIXABAY_SUPPORT, mPixabaySupportPreference.isChecked());
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
                        LogDebugFileUtils.get().init(getContext());
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
                case PREF_STACK_BLUR:
                    mPreferences.put(PREF_STACK_BLUR, mStackBlurPreference.getProgress());
                    break;
                case PREF_AUTO_SAVE_WALLPAPER_FILE:
                    if (mAutoSaveWallpaperPreference.isChecked()) {
                        requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE }, 111);
                    } else {
                        mPreferences.put(PREF_AUTO_SAVE_WALLPAPER_FILE, false);
                    }
                    break;
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                @NonNull int[] grantResults) {
            if (requestCode == 111) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPreferences.put(PREF_AUTO_SAVE_WALLPAPER_FILE, true);
                } else {
                    mAutoSaveWallpaperPreference.setChecked(false);
                }
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
