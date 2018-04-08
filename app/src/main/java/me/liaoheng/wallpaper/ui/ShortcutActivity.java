package me.liaoheng.wallpaper.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.liaoheng.common.util.UIUtils;

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
        if (!BingWallpaperUtils.isConnectedOrConnecting(this)) {
            UIUtils.showToast(getApplicationContext(), getString(R.string.network_unavailable));
            return;
        }
        BingWallpaperIntentService.start(this, mode, false);
        finish();
    }
}
