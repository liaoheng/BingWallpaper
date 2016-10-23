package me.liaoheng.bingwallpaper.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.support.v4.content.ContextCompat;
import com.flyco.systembar.SystemBarHelper;
import me.liaoheng.bingwallpaper.R;
import me.liaoheng.bingwallpaper.service.AutoSetWallpaperBroadcastReceiver;
import me.liaoheng.bingwallpaper.service.ConnectionChangeReceiver;
import me.liaoheng.bingwallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.bingwallpaper.util.LogDebugFileUtils;
import me.liaoheng.bingwallpaper.util.Utils;
import me.liaoheng.bingwallpaper.view.TimePreference;
import org.joda.time.LocalTime;

/**
 * @author liaoheng
 * @version 2016-09-20 13:59
 */
public class SettingsActivity extends com.fnp.materialpreferences.PreferenceActivity {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarHelper
                .tintStatusBar(this, ContextCompat.getColor(this, R.color.colorPrimaryDark), 0);
        setPreferenceFragment(new MyPreferenceFragment());
    }

    public static final String PREF_SET_WALLPAPER_RESOLUTION                = "pref_set_wallpaper_resolution";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE           = "pref_set_wallpaper_day_auto_update";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME      = "pref_set_wallpaper_day_auto_update_time";
    public static final String PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_ONLY_WIFI = "pref_set_wallpaper_day_auto_update_only_wifi";
    public static final String PREF_SET_WALLPAPER_URL                       = "pref_set_wallpaper_url";
    public static final String PREF_SET_WALLPAPER_LOG                       = "pref_set_wallpaper_log";
    public static final String PREF_SET_WALLPAPER_CHECK_NET                 = "pref_set_wallpaper_check_net";

    public static class MyPreferenceFragment extends com.fnp.materialpreferences.PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override public int addPreferencesFromResource() {
            return R.xml.preferences;
        }

        ListPreference     resolutionListPreference;
        TimePreference     timePreference;
        CheckBoxPreference dayUpdatePreference;
        ListPreference     urlListPreference;

        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            resolutionListPreference = (ListPreference) findPreference(
                    PREF_SET_WALLPAPER_RESOLUTION);
            resolutionListPreference.setSummary(Utils.getResolution(getActivity()));

            urlListPreference = (ListPreference) findPreference(PREF_SET_WALLPAPER_URL);
            urlListPreference.setSummary(Utils.getUrlValue(getActivity()));

            LocalTime localTime = Utils.getDayUpdateTime(getActivity());

            timePreference = (TimePreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME);
            timePreference.setSummary(localTime.toString("HH:mm"));

            dayUpdatePreference = (CheckBoxPreference) findPreference(
                    PREF_SET_WALLPAPER_DAY_AUTO_UPDATE);
            timePreference.setEnabled(dayUpdatePreference.isChecked());
        }

        @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                        String key) {
            switch (key) {
                case PREF_SET_WALLPAPER_RESOLUTION:
                    resolutionListPreference.setSummary(resolutionListPreference.getEntry());
                    break;
                case PREF_SET_WALLPAPER_DAY_AUTO_UPDATE:
                    if (dayUpdatePreference.isChecked()) {
                        Utils.enabledReceiver(getActivity(),
                                AutoSetWallpaperBroadcastReceiver.class.getName());
                    } else {
                        Utils.disabledReceiver(getActivity(),
                                AutoSetWallpaperBroadcastReceiver.class.getName());
                    }
                    timePreference.setEnabled(dayUpdatePreference.isChecked());
                    break;
                case PREF_SET_WALLPAPER_CHECK_NET:
                    CheckBoxPreference netPreference = (CheckBoxPreference) findPreference(
                            PREF_SET_WALLPAPER_CHECK_NET);
                    if (netPreference.isChecked()) {
                        Utils.enabledReceiver(getActivity(),
                                ConnectionChangeReceiver.class.getName());
                    } else {
                        Utils.disabledReceiver(getActivity(),
                                ConnectionChangeReceiver.class.getName());
                    }
                    break;
                case PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME:
                    BingWallpaperAlarmManager.clear(getActivity());
                    BingWallpaperAlarmManager
                            .add(getActivity(), timePreference.getLocalTime().getHourOfDay(),
                                    timePreference.getLocalTime().getMinuteOfHour());
                    break;
                case PREF_SET_WALLPAPER_URL:
                    urlListPreference.setSummary(urlListPreference.getEntry());
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

        @Override public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
