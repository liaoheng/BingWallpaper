package me.liaoheng.wallpaper.ui;

import android.app.Activity;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.github.liaoheng.common.util.UIUtils;

import androidx.annotation.Nullable;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * @author liaoheng
 * @version 2018-04-08 14:51
 */
public class ShortcutActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int mode = getIntent().getIntExtra(BingWallpaperIntentService.EXTRA_SET_WALLPAPER_MODE, -1);
        if (mode < 0) {
            return;
        }
        if (BingWallpaperUtils.isConnectedOrConnecting(this)) {
            BingWallpaperIntentService.start(this, mode, false);
            Toast.makeText(getApplicationContext(), getString(R.string.set_wallpaper_running), Toast.LENGTH_SHORT)
                    .show();
        } else {
            UIUtils.showToast(getApplicationContext(), R.string.network_unavailable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            getSystemService(ShortcutManager.class).reportShortcutUsed("toggle");
        }
        finish();
    }
}
