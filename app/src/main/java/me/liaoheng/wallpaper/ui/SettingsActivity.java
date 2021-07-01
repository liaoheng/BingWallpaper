package me.liaoheng.wallpaper.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.LanguageContextWrapper;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.ShellUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;

import org.joda.time.LocalTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.ISettingTrayPreferences;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.util.Settings;
import me.liaoheng.wallpaper.util.WallpaperUtils;
import me.liaoheng.wallpaper.widget.SeekBarDialogPreference;
import me.liaoheng.wallpaper.widget.SeekBarPreferenceDialogFragmentCompat;
import me.liaoheng.wallpaper.widget.TimePreference;
import me.liaoheng.wallpaper.widget.TimePreferenceDialogFragmentCompat;

/**
 * @author liaoheng
 * @version 2016-09-20 13:59
 */
public class SettingsActivity extends BaseActivity {
    private static boolean isChangeLanguage;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LanguageContextWrapper.wrap(context, BingWallpaperUtils.getLanguage(context)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_main_setting);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            isChangeLanguage = savedInstanceState.getBoolean("isChangeLanguage");
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, new MyPreferenceFragment(), "SettingsFragment")
                .commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isChangeLanguage", isChangeLanguage);
    }

    public static final String CLOSE_FULLY_AUTOMATIC_UPDATE = "close_fully_automatic_update";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BingWallpaperJobManager.onActivityResult(this, requestCode, resultCode, new Callback5() {
            @Override
            public void onAllow() {
            }

            @Override
            public void onDeny() {
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(CLOSE_FULLY_AUTOMATIC_UPDATE));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isChangeLanguage) {
            setResult(RESULT_OK);
        }
        isChangeLanguage = false;
        super.onBackPressed();
    }

    public static final String PREF_SET_WALLPAPER_DAILY_UPDATE = "pref_set_wallpaper_day_fully_automatic_update";
    public static final String PREF_SET_WALLPAPER_DAILY_UPDATE_MODE = "pref_set_wallpaper_day_fully_automatic_update_type";
    public static final String PREF_SET_WALLPAPER_DAILY_UPDATE_INTERVAL = "pref_set_wallpaper_day_fully_automatic_update_interval";
    public static final String PREF_SET_WALLPAPER_DAILY_UPDATE_TIME = "pref_set_wallpaper_day_auto_update_time";
    public static final String PREF_SET_WALLPAPER_DAILY_UPDATE_SUCCESS_NOTIFICATION = "pref_set_wallpaper_day_fully_automatic_update_notification";
    public static final String PREF_COUNTRY = "pref_country";
    public static final String PREF_LANGUAGE = "pref_language";
    public static final String PREF_SET_WALLPAPER_RESOLUTION = "pref_set_wallpaper_resolution";
    public static final String PREF_SAVE_WALLPAPER_RESOLUTION = "pref_save_wallpaper_resolution";
    public static final String PREF_SET_WALLPAPER_AUTO_MODE = "pref_set_wallpaper_auto_mode";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI = "pref_set_wallpaper_day_auto_update_only_wifi";
    public static final String PREF_SET_WALLPAPER_LOG = "pref_set_wallpaper_debug_log";
    public static final String PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER = "pref_set_miui_lock_screen_wallpaper";
    public static final String PREF_CRASH_REPORT = "pref_crash_report";
    public static final String PREF_STACK_BLUR = "pref_stack_blur";
    public static final String PREF_STACK_BLUR_MODE = "pref_stack_blur_mode";
    public static final String PREF_AUTO_SAVE_WALLPAPER_FILE = "pref_auto_save_wallpaper_file";

    public static class MyPreferenceFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ISettingTrayPreferences mPreferences;

        private SwitchPreference mDailyUpdatePreference;
        private ListPreference mDailyUpdateModeListPreference;
        private ListPreference mDailyUpdateIntervalPreference;
        private TimePreference mDailyUpdateTimePreference;
        private SwitchPreference mDailyUpdateSuccessNotificationPreference;
        private SwitchPreference mOnlyWifiPreference;
        private ListPreference mCountryListPreference;
        private ListPreference mResolutionListPreference;
        private ListPreference mSaveResolutionListPreference;
        private ListPreference mModeTypeListPreference;
        private SwitchPreference mLogPreference;
        private SwitchPreference mCrashPreference;
        private SwitchPreference mMIuiLockScreenPreference;
        private SeekBarDialogPreference mStackBlurPreference;
        private ListPreference mStackBlurModePreference;
        private SwitchPreference mAutoSaveWallpaperPreference;

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

        class SettingBroadcastReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (CLOSE_FULLY_AUTOMATIC_UPDATE.equals(intent.getAction())) {
                    if (mDailyUpdatePreference != null) {
                        mDailyUpdatePreference.setChecked(false);
                    }
                }
            }
        }

        private SettingBroadcastReceiver mReceiver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mReceiver = new SettingBroadcastReceiver();
            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(mReceiver, new IntentFilter(CLOSE_FULLY_AUTOMATIC_UPDATE));
            mPreferences = SettingTrayPreferences.get(getActivity());
            Preference version = findPreference("pref_version");
            version.setSummary(AppUtils.getVersionInfo(getActivity()).versionName);

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
                                new Callback5() {
                                    @Override
                                    public void onAllow() {
                                        Utils.addSubscribe(BingWallpaperUtils.clearCache(getActivity()),
                                                new Callback.EmptyCallback<Object>() {
                                                    @Override
                                                    public void onSuccess(Object o) {
                                                        UIUtils.showToast(getActivity(),
                                                                R.string.pref_clear_cache_success);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onDeny() {

                                    }
                                });
                        return true;
                    });

            findPreference("pref_translation").setOnPreferenceClickListener(preference -> {
                UIUtils.startActivity(getActivity(), TranslatorActivity.class);
                return true;
            });

            mDailyUpdatePreference = findPreference(
                    PREF_SET_WALLPAPER_DAILY_UPDATE);
            mDailyUpdateModeListPreference = findPreference(PREF_SET_WALLPAPER_DAILY_UPDATE_MODE);
            mDailyUpdateIntervalPreference = findPreference(
                    PREF_SET_WALLPAPER_DAILY_UPDATE_INTERVAL);
            mDailyUpdateTimePreference = findPreference(PREF_SET_WALLPAPER_DAILY_UPDATE_TIME);
            mDailyUpdateSuccessNotificationPreference = findPreference(
                    PREF_SET_WALLPAPER_DAILY_UPDATE_SUCCESS_NOTIFICATION);
            mCountryListPreference = findPreference(PREF_COUNTRY);
            ListPreference mLanguageListPreference = findPreference(PREF_LANGUAGE);
            mResolutionListPreference = findPreference(PREF_SET_WALLPAPER_RESOLUTION);
            mSaveResolutionListPreference = findPreference(PREF_SAVE_WALLPAPER_RESOLUTION);
            mModeTypeListPreference = findPreference(PREF_SET_WALLPAPER_AUTO_MODE);
            mOnlyWifiPreference = findPreference(PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI);
            mMIuiLockScreenPreference = findPreference(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER);
            mLogPreference = findPreference(PREF_SET_WALLPAPER_LOG);
            mCrashPreference = findPreference(PREF_CRASH_REPORT);
            mStackBlurPreference = findPreference(PREF_STACK_BLUR);
            int stackBlur = Settings.getSettingStackBlur(getActivity());
            mStackBlurPreference.setProgress(stackBlur);
            mStackBlurPreference.setSummary(String.valueOf(stackBlur));
            mStackBlurModePreference = findPreference(PREF_STACK_BLUR_MODE);
            mAutoSaveWallpaperPreference = findPreference(PREF_AUTO_SAVE_WALLPAPER_FILE);

            if (!ROM.getROM().isMiui()) {
                ((PreferenceCategory) findPreference("pref_wallpaper_group")).removePreference(
                        mMIuiLockScreenPreference);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (ROM.getROM().isMiui()) {
                    mModeTypeListPreference.setSummary(Settings.getAutoMode(getActivity()));
                    mStackBlurModePreference.setSummary(Settings.getSettingStackBlurModeName(getActivity()));
                } else {
                    ((PreferenceCategory) findPreference("pref_update_group")).removePreference(
                            mModeTypeListPreference);
                    ((PreferenceCategory) findPreference("pref_wallpaper_group")).removePreference(
                            mStackBlurModePreference);
                }
            } else {
                mModeTypeListPreference.setSummary(Settings.getAutoMode(getActivity()));
                mStackBlurModePreference.setSummary(Settings.getSettingStackBlurModeName(getActivity()));
            }

            mResolutionListPreference.setSummary(Settings.getResolution(getActivity()));
            mSaveResolutionListPreference.setSummary(Settings.getSaveResolution(getActivity()));
            mCountryListPreference.setSummary(Settings.getCountryName(getActivity()));
            mLanguageListPreference.setSummary(Settings.getLanguageName(getActivity()));

            mDailyUpdateModeListPreference.setSummary(Settings.getAutomaticUpdateTypeName(getContext()));
            mDailyUpdateIntervalPreference.setSummary(getString(R.string.pref_auto_update_check_time,
                    Settings.getAutomaticUpdateInterval(getContext())));
            LocalTime time = LocalTime.parse(Constants.DEF_TIMER_PERIODIC);
            mDailyUpdateTimePreference.setSummary(time.toString("HH:mm"));
            mDailyUpdateTimePreference.setLocalTime(time);

            mDailyUpdatePreference.setSummary(Settings.getJobTypeString(getContext()));

            switch (Settings.getAutomaticUpdateType(getContext())) {
                case Settings.AUTOMATIC_UPDATE_TYPE_AUTO:
                    int jobType = Settings.getJobType(getContext());
                    if (jobType == Settings.WORKER) {
                        initWorkerView();
                    } else if (jobType == Settings.LIVE_WALLPAPER) {
                        initLiveView();
                    } else if (jobType == Settings.TIMER) {
                        initTimerView();
                    }
                    break;
                case Settings.AUTOMATIC_UPDATE_TYPE_SYSTEM:
                    initWorkerView();
                    break;
                case Settings.AUTOMATIC_UPDATE_TYPE_SERVICE:
                    initLiveView();
                    break;
                case Settings.AUTOMATIC_UPDATE_TYPE_TIMER:
                    initTimerView();
                    break;
            }
            if (WallpaperUtils.isNotSupportedWallpaper(getContext())) {
                mDailyUpdatePreference.setEnabled(false);
            }
        }

        private void initWorkerView() {
            mDailyUpdateIntervalPreference.setEnabled(true);
            mDailyUpdateIntervalPreference.setSummary(getString(R.string.pref_auto_update_check_time,
                    Settings.getAutomaticUpdateInterval(getContext())));
            mDailyUpdateTimePreference.setEnabled(false);
        }

        private void initLiveView() {
            mDailyUpdateIntervalPreference.setEnabled(false);
            mDailyUpdateTimePreference.setEnabled(false);
        }

        private void initTimerView() {
            LocalTime localTime = BingWallpaperUtils.getDayUpdateTime(getContext());
            mDailyUpdateTimePreference.setEnabled(true);
            mDailyUpdateTimePreference.setSummary(localTime.toString("HH:mm"));
            mDailyUpdateTimePreference.setLocalTime(localTime);
            mDailyUpdateIntervalPreference.setEnabled(false);
        }

        @SuppressLint("StringFormatMatches")
        private void updateCheckTime() {
            mDailyUpdateIntervalPreference.setSummary(getString(R.string.pref_auto_update_check_time,
                    Settings.getAutomaticUpdateInterval(getActivity())));
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
                    mPreferences.put(PREF_SAVE_WALLPAPER_RESOLUTION, mSaveResolutionListPreference.getValue());
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
                case PREF_SET_WALLPAPER_AUTO_MODE:
                    if (Settings.getJobType(getContext()) == Settings.LIVE_WALLPAPER) {
                        if (Integer.parseInt(mModeTypeListPreference.getValue())
                                == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                            UIUtils.showToast(getContext(), "single choose lock wallpaper not support!");
                        }
                    }
                    mModeTypeListPreference.setSummary(mModeTypeListPreference.getEntry());
                    mPreferences.put(PREF_SET_WALLPAPER_AUTO_MODE, mModeTypeListPreference.getValue());
                    break;
                case PREF_SET_WALLPAPER_DAILY_UPDATE:
                    if (mDailyUpdatePreference.isChecked()) {
                        if (BingWallpaperJobManager.enabled(getContext()) == Settings.NONE) {
                            mDailyUpdatePreference.setChecked(false);
                            return;
                        }
                    } else {
                        BingWallpaperJobManager.disabled(getContext());
                    }
                    break;
                case PREF_SET_WALLPAPER_DAILY_UPDATE_MODE:
                    int type = Integer.parseInt(mDailyUpdateModeListPreference.getValue());
                    switch (type) {
                        case Settings.AUTOMATIC_UPDATE_TYPE_AUTO:
                            mDailyUpdateTimePreference.setEnabled(true);
                            mDailyUpdateIntervalPreference.setEnabled(true);
                            break;
                        case Settings.AUTOMATIC_UPDATE_TYPE_SYSTEM:
                            initWorkerView();
                            break;
                        case Settings.AUTOMATIC_UPDATE_TYPE_SERVICE:
                            initLiveView();
                            break;
                        case Settings.AUTOMATIC_UPDATE_TYPE_TIMER:
                            initTimerView();
                            break;
                    }
                    mDailyUpdateModeListPreference.setSummary(mDailyUpdateModeListPreference.getEntry());
                    break;
                case PREF_SET_WALLPAPER_DAILY_UPDATE_INTERVAL:
                    mPreferences.put(PREF_SET_WALLPAPER_DAILY_UPDATE_INTERVAL,
                            mDailyUpdateIntervalPreference.getValue());
                    updateCheckTime();
                    break;
                case PREF_SET_WALLPAPER_DAILY_UPDATE_TIME:
                    mPreferences.put(PREF_SET_WALLPAPER_DAILY_UPDATE_TIME,
                            mDailyUpdateTimePreference.getLocalTime().toString());
                    break;
                case PREF_SET_WALLPAPER_DAILY_UPDATE_SUCCESS_NOTIFICATION:
                    mPreferences.put(PREF_SET_WALLPAPER_DAILY_UPDATE_SUCCESS_NOTIFICATION,
                            mDailyUpdateSuccessNotificationPreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_LOG:
                    mPreferences.put(PREF_SET_WALLPAPER_LOG, mLogPreference.isChecked());
                    if (mLogPreference.isChecked()) {
                        LogDebugFileUtils.create(getContext());
                    } else {
                        LogDebugFileUtils.destroy();
                    }
                    getContext().sendBroadcast(new Intent(Constants.ACTION_DEBUG_LOG));
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
                case PREF_STACK_BLUR_MODE:
                    mPreferences.put(PREF_STACK_BLUR_MODE, mStackBlurModePreference.getValue());
                    mStackBlurModePreference.setSummary(mStackBlurModePreference.getEntry());
                    break;
                case PREF_AUTO_SAVE_WALLPAPER_FILE:
                    if (mAutoSaveWallpaperPreference.isChecked()) {
                        requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE }, 111);
                    } else {
                        mPreferences.put(PREF_AUTO_SAVE_WALLPAPER_FILE, false);
                    }
                    break;
                case PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER:
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

        @Override
        public void onDestroy() {
            if (mReceiver != null) {
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
            }
            super.onDestroy();
        }
    }
}
