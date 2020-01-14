package me.liaoheng.wallpaper.service;

import android.os.Build;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.github.liaoheng.common.util.Callback4;

import me.liaoheng.wallpaper.R;
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
        Toast.makeText(this, R.string.set_wallpaper_running, Toast.LENGTH_LONG).show();
        BingWallpaperUtils.setWallpaper(this, null, Constants.EXTRA_SET_WALLPAPER_MODE_BOTH,
                new Callback4.EmptyCallback<>());
    }
}
