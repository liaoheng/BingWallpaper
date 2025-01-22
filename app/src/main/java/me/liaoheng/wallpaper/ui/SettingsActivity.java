package me.liaoheng.wallpaper.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.LanguageContextWrapper;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.ShellUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.YNCallback;

import java.util.Locale;
import java.util.Objects;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
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

    private Fragment mSettingPreferenceFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_main_setting);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState != null) {
            isChangeLanguage = savedInstanceState.getBoolean("isChangeLanguage");
            mSettingPreferenceFragment = getSupportFragmentManager().getFragment(savedInstanceState, "Settings");
        } else {
            mSettingPreferenceFragment = new SettingsPreferenceFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, mSettingPreferenceFragment, "SettingsFragment")
                .commitAllowingStateLoss();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isChangeLanguage", isChangeLanguage);
        getSupportFragmentManager().putFragment(outState, "Settings", mSettingPreferenceFragment);
    }

    public static final String CLOSE_FULLY_AUTOMATIC_UPDATE = "close_fully_automatic_update";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BingWallpaperJobManager.onActivityResult(this, requestCode, resultCode, new YNCallback() {
            final Intent intent = new Intent(CLOSE_FULLY_AUTOMATIC_UPDATE);

            @Override
            public void onAllow() {
                intent.putExtra("enable", true);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onDeny() {
                intent.putExtra("enable", false);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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
    public static final String PREF_DOH = "pref_doh";
    public static final String PREF_STACK_BLUR = "pref_stack_blur";
    public static final String PREF_STACK_BLUR_MODE = "pref_stack_blur_mode";
    public static final String PREF_BRIGHTNESS = "pref_brightness";
    public static final String PREF_BRIGHTNESS_MODE = "pref_brightness_mode";
    public static final String PREF_AUTO_SAVE_WALLPAPER_FILE = "pref_auto_save_wallpaper_file";

    public final static class SettingsPreferenceFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        private SwitchPreferenceCompat mDailyUpdatePreference;
        private ListPreference mDailyUpdateIntervalPreference;
        private TimePreference mDailyUpdateTimePreference;
        private SwitchPreferenceCompat mAutoSaveWallpaperPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setPreferenceDataStore(SettingTrayPreferences.get());
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @Override
        public void onDisplayPreferenceDialog(@NonNull Preference preference) {
            if (preference instanceof TimePreference) {
                FragmentManager fragmentManager = getParentFragmentManager();
                DialogFragment dialogFragment = TimePreferenceDialogFragmentCompat.newInstance(preference.getKey());
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, "TimePreference");
            } else if (preference instanceof SeekBarDialogPreference) {
                FragmentManager fragmentManager = getParentFragmentManager();
                int max = 100;
                int min = 0;
                if (Objects.equals(preference.getKey(), PREF_BRIGHTNESS)) {
                    min = -100;
                }
                DialogFragment dialogFragment = SeekBarPreferenceDialogFragmentCompat.newInstance(preference.getKey(),
                        max, min);
                dialogFragment.setTargetFragment(this, 1);
                dialogFragment.show(fragmentManager, "SeekBarDialogPreference");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        final class SettingBroadcastReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (CLOSE_FULLY_AUTOMATIC_UPDATE.equals(intent.getAction())) {
                    if (mDailyUpdatePreference == null) {
                        return;
                    }
                    boolean enable = intent.getBooleanExtra("enable", false);
                    mDailyUpdatePreference.setChecked(enable);
                }
            }
        }

        private SettingBroadcastReceiver mReceiver;
        private ActivityResultLauncher<String[]> mAutoSavePermissions;

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mReceiver = new SettingBroadcastReceiver();
            LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(mReceiver, new IntentFilter(CLOSE_FULLY_AUTOMATIC_UPDATE));
            mAutoSavePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    map -> onAutoSaveWallpaperRequestPermissionsResult(
                            BingWallpaperUtils.checkStoragePermissions(map)));
            Preference version = findPreference("pref_version");
            version.setSummary(AppUtils.getVersionInfo(requireContext()).versionName);

            findPreference("pref_github").setOnPreferenceClickListener(preference -> {
                BingWallpaperUtils.openBrowser(requireContext(), "https://github.com/liaoheng/BingWallpaper");
                return true;
            });

            findPreference("pref_intro").setOnPreferenceClickListener(preference -> {
                UIUtils.startActivity(requireContext(), IntroActivity.class);
                return true;
            });

            findPreference("pref_license").setOnPreferenceClickListener(preference -> {
                UIUtils.startActivity(requireContext(), LicenseActivity.class);
                return true;
            });

            findPreference("pref_clear_cache").setOnPreferenceClickListener(preference -> {
                UIUtils.showYNAlertDialog(requireContext(), getString(R.string.pref_clear_cache) + "?",
                        new YNCallback() {
                            @Override
                            public void onAllow() {
                                Utils.addSubscribe(BingWallpaperUtils.clearCache(getActivity()),
                                        new Callback.EmptyCallback<Object>() {
                                            @Override
                                            public void onSuccess(Object o) {
                                                UIUtils.showToast(requireContext(), R.string.pref_clear_cache_success);
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
                UIUtils.startActivity(requireContext(), TranslatorActivity.class);
                return true;
            });

            mDailyUpdatePreference = findPreference(PREF_SET_WALLPAPER_DAILY_UPDATE);
            mDailyUpdatePreference.setOnPreferenceChangeListener(this);
            Preference mDailyUpdateModeListPreference = findPreference(PREF_SET_WALLPAPER_DAILY_UPDATE_MODE);
            mDailyUpdateModeListPreference.setOnPreferenceChangeListener(this);
            mDailyUpdateIntervalPreference = findPreference(PREF_SET_WALLPAPER_DAILY_UPDATE_INTERVAL);
            mDailyUpdateIntervalPreference.setSummaryProvider(new Preference.SummaryProvider<ListPreference>() {
                @Nullable
                @Override
                public CharSequence provideSummary(@NonNull ListPreference preference) {
                    return requireContext().getString(R.string.pref_auto_update_check_time,
                            Integer.parseInt(preference.getEntry().toString()));
                }
            });
            mDailyUpdateTimePreference = findPreference(PREF_SET_WALLPAPER_DAILY_UPDATE_TIME);
            Preference mCountryListPreference = findPreference(PREF_COUNTRY);
            mCountryListPreference.setOnPreferenceChangeListener(this);
            Preference mLanguageListPreference = findPreference(PREF_LANGUAGE);
            mLanguageListPreference.setOnPreferenceChangeListener(this);
            Preference mModeTypeListPreference = findPreference(PREF_SET_WALLPAPER_AUTO_MODE);
            mModeTypeListPreference.setOnPreferenceChangeListener(this);
            Preference mMIuiLockScreenPreference = findPreference(PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER);
            mMIuiLockScreenPreference.setOnPreferenceChangeListener(this);
            Preference mLogPreference = findPreference(PREF_SET_WALLPAPER_LOG);
            mLogPreference.setOnPreferenceChangeListener(this);
            Preference mCrashPreference = findPreference(PREF_CRASH_REPORT);
            mCrashPreference.setOnPreferenceChangeListener(this);
            mAutoSaveWallpaperPreference = findPreference(PREF_AUTO_SAVE_WALLPAPER_FILE);
            mAutoSaveWallpaperPreference.setOnPreferenceChangeListener(this);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R || !ROM.getROM().isMiui()) {
                ((PreferenceCategory) findPreference("pref_wallpaper_group")).removePreference(
                        mMIuiLockScreenPreference);
            }
            mDailyUpdateTimePreference.setDefaultValue(Constants.DEF_TIMER_PERIODIC);
            mDailyUpdateTimePreference.setSummaryProvider(new Preference.SummaryProvider<TimePreference>() {
                @Nullable
                @Override
                public CharSequence provideSummary(@NonNull TimePreference preference) {
                    return preference.getLocalTime().toString("HH:mm");
                }
            });
            findPreference(PREF_STACK_BLUR).setSummaryProvider(new Preference.SummaryProvider<SeekBarDialogPreference>() {
                @Nullable
                @Override
                public CharSequence provideSummary(@NonNull SeekBarDialogPreference preference) {
                    return String.valueOf(preference.getProgress());
                }
            });
            findPreference(PREF_BRIGHTNESS).setSummaryProvider(new Preference.SummaryProvider<SeekBarDialogPreference>() {
                @Nullable
                @Override
                public CharSequence provideSummary(@NonNull SeekBarDialogPreference preference) {
                    return String.valueOf(preference.getProgress());
                }
            });

            mDailyUpdatePreference.setSummary(Settings.getJobTypeString(requireContext()));

            switch (Settings.getAutomaticUpdateType(requireContext())) {
                case Settings.AUTOMATIC_UPDATE_TYPE_AUTO:
                    int jobType = Settings.getJobType(requireContext());
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
            if (WallpaperUtils.isNotSupportedWallpaper(requireContext())) {
                mDailyUpdatePreference.setEnabled(false);
            }
        }

        private void initWorkerView() {
            mDailyUpdateIntervalPreference.setEnabled(true);
            mDailyUpdateTimePreference.setEnabled(false);
        }

        private void initLiveView() {
            mDailyUpdateIntervalPreference.setEnabled(false);
            mDailyUpdateTimePreference.setEnabled(false);
        }

        private void initTimerView() {
            mDailyUpdateTimePreference.setEnabled(true);
            mDailyUpdateIntervalPreference.setEnabled(false);
        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            switch (Objects.requireNonNull(preference.getKey())) {
                case PREF_COUNTRY:
                    BingWallpaperUtils.clearNetCache().subscribe();
                    break;
                case PREF_LANGUAGE:
                    Locale currentLocale = LanguageContextWrapper.getCurrentLocale(requireContext());
                    Locale newLocale = BingWallpaperUtils.getLanguage(Integer.parseInt(String.valueOf(newValue)),
                            LanguageContextWrapper.getOriginalLocale());
                    if (currentLocale.equals(newLocale)) {
                        break;
                    }
                    LanguageContextWrapper.wrap(requireContext(), newLocale);
                    isChangeLanguage = true;
                    requireActivity().recreate();
                    break;
                case PREF_SET_WALLPAPER_AUTO_MODE:
                    if (Integer.parseInt(String.valueOf(newValue))
                            == Constants.EXTRA_SET_WALLPAPER_MODE_LOCK) {
                        if (Settings.getJobType(requireContext()) == Settings.LIVE_WALLPAPER) {
                            return false;
                        }
                    }
                    break;
                case PREF_SET_WALLPAPER_DAILY_UPDATE:
                    if (Boolean.parseBoolean(String.valueOf(newValue))) {
                        int type = BingWallpaperJobManager.enabled(requireContext());
                        if (type == Settings.NONE) {
                            return false;
                        } else {
                            if (type == Settings.LIVE_WALLPAPER) {
                                return false;
                            }
                        }
                    } else {
                        BingWallpaperJobManager.disabled(requireContext());
                    }
                    break;
                case PREF_SET_WALLPAPER_DAILY_UPDATE_MODE:
                    int type = Integer.parseInt(String.valueOf(newValue));
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
                    break;
                case PREF_SET_WALLPAPER_LOG:
                    if (Boolean.parseBoolean(String.valueOf(newValue))) {
                        LogDebugFileUtils.create(requireContext());
                        requireContext().sendBroadcast(new Intent(Constants.ACTION_DEBUG_LOG));
                    } else {
                        LogDebugFileUtils.destroy();
                    }
                    break;
                case PREF_CRASH_REPORT:
                    if (Boolean.parseBoolean(String.valueOf(newValue))) {
                        CrashReportHandle.enable(getActivity());
                    } else {
                        CrashReportHandle.disable(getActivity());
                    }
                    break;
                case PREF_AUTO_SAVE_WALLPAPER_FILE:
                    if (Boolean.parseBoolean(String.valueOf(newValue))) {
                        return BingWallpaperUtils.requestStoragePermissions(requireActivity(), mAutoSavePermissions);
                    }
                    break;
                case PREF_SET_MIUI_LOCK_SCREEN_WALLPAPER:
                    if (Boolean.parseBoolean(String.valueOf(newValue))) {
                        if (!ShellUtils.hasRootPermission()) {
                            UIUtils.showToast(requireContext(), R.string.unable_root_permission);
                            return false;
                        }
                    }
                    break;
            }
            return true;
        }

        public void onAutoSaveWallpaperRequestPermissionsResult(boolean granted) {
            mAutoSaveWallpaperPreference.setChecked(granted);
        }

        @Override
        public void onDestroy() {
            if (mReceiver != null) {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mReceiver);
            }
            super.onDestroy();
        }
    }
}
