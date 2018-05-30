package me.liaoheng.wallpaper.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.liaoheng.common.util.UIUtils;
import com.github.paolorotolo.appintro.AppIntro;

import org.joda.time.LocalTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.util.TasksUtils;

/**
 * @author liaoheng
 * @version 2018-03-05 17:29
 */
public class IntroActivity extends AppIntro {
    private SettingTrayPreferences mPreferences;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = new SettingTrayPreferences(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        addSlide(new IntroHintFragment());
        addSlide(new IntroUpdateFragment());
    }

    public static class IntroHintFragment extends Fragment {
        @BindView(R.id.intro_hint_ignore_battery_optimization)
        View ignore;

        @OnClick(R.id.intro_hint_ignore_battery_optimization)
        void ignoreBatteryOptimization() {
            BingWallpaperUtils.showIgnoreBatteryOptimizationDialog(getActivity());
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_intro_hint, container, false);
            ButterKnife.bind(this, view);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                UIUtils.viewVisible(ignore);
            }
            return view;
        }
    }

    public static class IntroUpdateFragment extends Fragment {

        @BindView(R.id.intro_update_select_group)
        RadioGroup mSelectGroup;
        @BindView(R.id.intro_update_select_group_item_timing_time)
        TextView mTimingTime;

        @OnClick(R.id.intro_update_select_group_item_timing)
        void onTiming() {
            mAlertDialog.show();
        }

        public int updateFlag;
        public LocalTime localTime;
        AlertDialog mAlertDialog;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final TimePicker picker = new TimePicker(getContext());
            picker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
            mAlertDialog = new AlertDialog.Builder(getContext())
                    .setCustomTitle(picker)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            localTime = new LocalTime(picker.getCurrentHour(), picker.getCurrentMinute());
                            UIUtils.viewVisible(mTimingTime);
                            mTimingTime.setText(
                                    getString(R.string.intro_update_set_timing_time) + localTime.toString("HH:mm"));
                        }
                    })
                    .create();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            View contentView = inflater.inflate(R.layout.fragment_intro_update, container, false);
            ButterKnife.bind(this, contentView);
            mSelectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.intro_update_select_group_item_auto:
                            updateFlag = 1;
                            localTime = null;
                            UIUtils.viewGone(mTimingTime);
                            break;
                        case R.id.intro_update_select_group_item_timing:
                            updateFlag = 2;
                            break;
                        case R.id.intro_update_select_group_item_skip:
                            updateFlag = 0;
                            localTime = null;
                            UIUtils.viewGone(mTimingTime);
                            break;
                    }
                }
            });
            return contentView;
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        IntroUpdateFragment fragment = (IntroUpdateFragment) currentFragment;
        switch (fragment.updateFlag) {
            case 1:
                BingWallpaperJobManager.enabled(this);
                mSharedPreferences.edit()
                        .putBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE, true)
                        .apply();
                mPreferences.put(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE, true);
                break;
            case 2:
                if (fragment.localTime != null) {
                    BingWallpaperAlarmManager
                            .add(this, fragment.localTime);
                    mSharedPreferences.edit()
                            .putBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE, true)
                            .apply();
                    mSharedPreferences.edit()
                            .putString(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME,
                                    fragment.localTime.toString())
                            .apply();
                    mPreferences.put(SettingsActivity.PREF_SET_WALLPAPER_DAY_AUTO_UPDATE_TIME,
                            fragment.localTime.toString());
                }
                break;
        }
        onSkipPressed(null);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        TasksUtils.markOne();
        UIUtils.startActivity(this, MainActivity.class);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if (oldFragment instanceof IntroHintFragment) {
            if (!BingWallpaperUtils.isIgnoreBatteryOptimization(this)) {
                BingWallpaperUtils.showIgnoreBatteryOptimizationDialog(this);
            }
        }
    }
}
