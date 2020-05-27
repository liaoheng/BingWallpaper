package me.liaoheng.wallpaper.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.appintro.AppIntro;
import com.github.liaoheng.common.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.ISettingTrayPreferences;
import me.liaoheng.wallpaper.util.SettingTrayPreferences;
import me.liaoheng.wallpaper.util.TasksUtils;

/**
 * @author liaoheng
 * @version 2018-03-05 17:29
 */
public class IntroActivity extends AppIntro {
    private ISettingTrayPreferences mPreferences;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TasksUtils.markOne();
        super.onCreate(savedInstanceState);
        mPreferences = SettingTrayPreferences.get(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        addSlide(new IntroHintFragment());
        addSlide(new IntroUpdateFragment());
    }

    public static class IntroHintFragment extends Fragment {
        @BindView(R.id.intro_hint_ignore_battery_optimization)
        View ignore;

        @OnClick(R.id.intro_hint_ignore_battery_optimization)
        void ignoreBatteryOptimization() {
            BingWallpaperUtils.showIgnoreBatteryOptimizationSetting(getActivity());
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
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

        int updateFlag;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            View contentView = inflater.inflate(R.layout.fragment_intro_update, container, false);
            ButterKnife.bind(this, contentView);
            mSelectGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.intro_update_select_group_item_auto:
                        updateFlag = 1;
                        break;
                    case R.id.intro_update_select_group_item_skip:
                        updateFlag = 0;
                        break;
                }
            });
            return contentView;
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        if (currentFragment instanceof IntroUpdateFragment) {
            IntroUpdateFragment fragment = (IntroUpdateFragment) currentFragment;
            switch (fragment.updateFlag) {
                case 1:
                    BingWallpaperJobManager.disabled(this);
                    if (!BingWallpaperJobManager.enabled(this)) {
                        return;
                    }
                    mSharedPreferences.edit()
                            .putBoolean(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE, true)
                            .apply();
                    mPreferences.put(SettingsActivity.PREF_SET_WALLPAPER_DAY_FULLY_AUTOMATIC_UPDATE, true);
                    break;
            }
        } else {
            Toast.makeText(getApplicationContext(),"Setting invalid",Toast.LENGTH_LONG).show();
        }
        onSkipPressed(null);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        UIUtils.startActivity(this, MainActivity.class);
        finish();
    }
}
