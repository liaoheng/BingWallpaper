package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.appintro.AppIntro;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.UIUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.liaoheng.wallpaper.databinding.FragmentIntroHintBinding;
import me.liaoheng.wallpaper.databinding.FragmentIntroUpdateBinding;
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

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            FragmentIntroHintBinding binding = FragmentIntroHintBinding.inflate(inflater, container, false);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                UIUtils.viewVisible(binding.introHintIgnoreBatteryOptimization);
            }
            binding.introHintIgnoreBatteryOptimization.setOnClickListener(
                    v -> BingWallpaperUtils.showIgnoreBatteryOptimizationSetting(requireContext()));
            return binding.getRoot();
        }
    }

    public static class IntroUpdateFragment extends Fragment {

        void enable() {
            UIUtils.startActivity(requireContext(), SettingsActivity.class);
        }

        void explain() {
            BingWallpaperUtils.openBrowser(requireContext(), "https://github.com/liaoheng/BingWallpaper/wiki");
        }

        void miuiTips() {
            BingWallpaperUtils.showMiuiDialog(requireContext(),
                    !Settings.isMiuiLockScreenSupport(requireContext()) && BingWallpaperUtils.isRooted(
                            requireContext()));
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            FragmentIntroUpdateBinding binding = FragmentIntroUpdateBinding.inflate(inflater, container, false);
            if (ROM.getROM().isMiui()) {
                UIUtils.viewVisible(binding.introMiuiTips);
            }
            binding.introMiuiTips.setOnClickListener(v -> miuiTips());
            binding.introAppExplain.setOnClickListener(v -> explain());
            binding.introEnableUpdate.setOnClickListener(v -> enable());
            return binding.getRoot();
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        onSkipPressed(null);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        finishAndRemoveTask();
        if (TasksUtils.isOne()) {
            BingWallpaperUtils.initResolution(this);
            UIUtils.startActivity(this, MainActivity.class);
        }
        TasksUtils.markOne();
    }
}
