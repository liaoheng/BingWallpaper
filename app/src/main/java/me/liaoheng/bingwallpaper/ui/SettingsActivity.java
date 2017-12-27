package me.liaoheng.bingwallpaper.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v4.content.ContextCompat;

import com.flyco.systembar.SystemBarHelper;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.UIUtils;

import org.joda.time.LocalTime;

import me.liaoheng.bingwallpaper.R;
import me.liaoheng.bingwallpaper.service.AutoSetWallpaperBroadcastReceiver;
import me.liaoheng.bingwallpaper.util.BUtils;
import me.liaoheng.bingwallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.bingwallpaper.util.JobManager;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.view.TimePreference;
import me.liaoheng.bingwallpaper.view.VersionPreference;

/**
 * @author liaoheng
 * @version 2016-09-20 13:59
 */
public class SettingsActivity extends com.fnp.materialpreferences.PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarHelper
                .tintStatusBar(this, ContextCompat.getColor(this, R.color.colorPrimaryDark), 0);
        setPreferenceFragment(new MyPreferenceFragment());
    }

    public static final String PREF_SET_WALLPAPER_RESOLUTION = "pref_set_wallpaper_resolution";
    public static final String PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE = "pref_set_wallpaper_day_fully_automatic_update";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE = "pref_set_wallpaper_day_auto_update";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME = "pref_set_wallpaper_day_auto_update_time";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI = "pref_set_wallpaper_day_auto_update_only_wifi";
    public static final String PREF_SET_WALLPAPER_LOG = "pref_set_wallpaper_log";

    public static class MyPreferenceFragment extends com.fnp.materialpreferences.PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {


        @Override
        public int addPreferencesFromResource() {
            return R.xml.preferences;
        }

        ListPreference resolutionListPreference;
        TimePreference timePreference;
        CheckBoxPreference dayUpdatePreference;
        CheckBoxPreference autoUpdatePreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            VersionPreference version = (VersionPreference) findPreference("pref_version");
            try {
                String versionName = AppUtils.getVersionInfo(getActivity()).versionName;
                version.setVersion(versionName);
            } catch (SystemException e) {
                L.Log.w(TAG, e);
            }
            Preference licenses = findPreference("pref_license");
            licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    UIUtils.startActivity(getActivity(), LicenseActivity.class);
                    return false;
                }
            });
            ((PreferenceCategory) findPreference("pref_other_group")).removePreference(findPreference(PREF_SET_WALLPAPER_LOG));

            resolutionListPreference = (ListPreference) findPreference(
                    PREF_SET_WALLPAPER_RESOLUTION);
            dayUpdatePreference = (CheckBoxPreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_AUTO_UPDATE);
            timePreference = (TimePreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME);
            autoUpdatePreference = (CheckBoxPreference) findPreference(PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE);

            resolutionListPreference.setSummary(BUtils.getResolution(getActivity()));

            LocalTime localTime = BUtils.getDayUpdateTime(getActivity());

            if (localTime != null) {
                timePreference.setSummary(localTime.toString("HH:mm"));
            } else {
                timePreference.setSummary(R.string.pref_not_set_time);
            }
            if (autoUpdatePreference.isChecked()) {
                dayUpdatePreference.setChecked(false);
            }
            timePreference.setEnabled(dayUpdatePreference.isChecked());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            switch (key) {
                case PREF_SET_WALLPAPER_RESOLUTION:
                    resolutionListPreference.setSummary(resolutionListPreference.getEntry());
                    break;
                case PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE:
                    if (autoUpdatePreference.isChecked()) {
                        timePreference.setSummary(R.string.pref_not_set_time);
                        dayUpdatePreference.setChecked(false);
                        BUtils.clearDayUpdateTime(getActivity());
                        BingWallpaperAlarmManager.clear(getActivity());
                        JobManager.enabled(getActivity());
                    } else {
                        JobManager.disabled(getActivity());
                    }
                    break;
                case PREF_SET_WALLPAPER_DAY_AUTO_UPDATE:
                    if (dayUpdatePreference.isChecked()) {
                        autoUpdatePreference.setChecked(false);
                        BUtils.enabledReceiver(getActivity(),
                                AutoSetWallpaperBroadcastReceiver.class.getName());
                    } else {
                        BUtils.disabledReceiver(getActivity(),
                                AutoSetWallpaperBroadcastReceiver.class.getName());
                    }
                    timePreference.setEnabled(dayUpdatePreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME:
                    if (timePreference.isEnabled()) {
                        BingWallpaperAlarmManager
                                .add(getActivity(), timePreference.getLocalTime().getHourOfDay(),
                                        timePreference.getLocalTime().getMinuteOfHour());
                    }
                    break;
                case PREF_SET_WALLPAPER_LOG:
                    CheckBoxPreference logPreference = (CheckBoxPreference) findPreference(
                            PREF_SET_WALLPAPER_LOG);
                    if (logPreference.isChecked()) {
                        LogDebugFileUtils.get().init("log.txt");
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
