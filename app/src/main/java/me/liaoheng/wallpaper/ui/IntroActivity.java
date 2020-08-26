package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Settings;
import me.liaoheng.wallpaper.util.TasksUtils;

/**
 * @author liaoheng
 * @version 2018-03-05 17:29
 */
public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        @OnClick(R.id.intro_enable_update)
        void enable() {
            UIUtils.startActivity(getContext(), SettingsActivity.class);
        }

        @OnClick(R.id.intro_app_explain)
        void explain() {
            BingWallpaperUtils.openBrowser(getContext(), "https://github.com/liaoheng/BingWallpaper/wiki");
        }

        @OnClick(R.id.intro_miui_tips)
        void miuiTips() {
            BingWallpaperUtils.showMiuiDialog(getContext(),
                    !Settings.isMiuiLockScreenSupport(getContext()) && BingWallpaperUtils.isRooted(getContext()));
        }

        @BindView(R.id.intro_miui_tips)
        Button miuiTips;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            View contentView = inflater.inflate(R.layout.fragment_intro_update, container, false);
            ButterKnife.bind(this, contentView);
            if (ROM.getROM().isMiui()) {
                UIUtils.viewVisible(miuiTips);
            }
            return contentView;
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        onSkipPressed(null);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        if (TasksUtils.isOne()) {
            UIUtils.startActivity(this, MainActivity.class);
        }
        TasksUtils.markOne();
        finish();
    }
}
