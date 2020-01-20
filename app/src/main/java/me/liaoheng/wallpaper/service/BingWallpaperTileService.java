package me.liaoheng.wallpaper.service;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.github.liaoheng.common.util.Callback4;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;

/**
 * @author liaoheng
 * @version 2020-01-14 16:16
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class BingWallpaperTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();
        BingWallpaperUtils.setWallpaper(getApplicationContext(), null,
                new Config.Builder().setWallpaperMode(Constants.EXTRA_SET_WALLPAPER_MODE_BOTH)
                        .setBackground(false)
                        .setShowNotification(true)
                        .build(), new Callback4.EmptyCallback<Boolean>() {
                    @Override
                    public void onYes(Boolean aBoolean) {
                        Toast.makeText(getApplicationContext(), getString(R.string.set_wallpaper_running),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        getQsTile().setState(Tile.STATE_UNAVAILABLE);
    }
}
