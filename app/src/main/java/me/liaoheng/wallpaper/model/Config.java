package me.liaoheng.wallpaper.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntRange;
import androidx.annotation.Size;

import java.util.HashMap;
import java.util.Map;

import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.Settings;

/**
 * @author liaoheng
 * @version 2019-07-31 17:00
 */
public class Config implements Parcelable {
    /**
     * <p>0. both</p>
     * <p>1. home</p>
     * <p>2. lock</p>
     */
    public final static String EXTRA_SET_WALLPAPER_MODE = "set_wallpaper_mode";
    public final static String EXTRA_SET_WALLPAPER_IMAGE = "set_wallpaper_image";
    public final static String EXTRA_SET_WALLPAPER_CONFIG = "set_wallpaper_config";
    private int stackBlur;
    @Constants.setWallpaperMode
    private int stackBlurMode;
    private int brightness;
    @Constants.setWallpaperMode
    private int brightnessMode;
    private boolean showNotification;
    private boolean background;
    /**
     * 0. both , 1. home , 2. lock
     */
    @Constants.setWallpaperMode
    private int wallpaperMode;

    public static class Builder {
        private int stackBlur;
        @Constants.setWallpaperMode
        private int stackBlurMode = Constants.EXTRA_SET_WALLPAPER_MODE_BOTH;
        private int brightness;
        @Constants.setWallpaperMode
        private int brightnessMode = Constants.EXTRA_SET_WALLPAPER_MODE_BOTH;
        private boolean showNotification;
        private boolean background;
        @Constants.setWallpaperMode
        private int wallpaperMode = Constants.EXTRA_SET_WALLPAPER_MODE_BOTH;

        public Builder loadConfig(Context context) {
            stackBlur = Settings.getSettingStackBlur();
            stackBlurMode = Settings.getSettingStackBlurMode();
            brightness = Settings.getSettingBrightness();
            brightnessMode = Settings.getSettingBrightnessMode();
            return this;
        }

        public Builder setShowNotification(boolean showNotification) {
            this.showNotification = showNotification;
            return this;
        }

        public Builder setBackground(boolean background) {
            this.background = background;
            return this;
        }

        public Builder setWallpaperMode(int wallpaperMode) {
            this.wallpaperMode = wallpaperMode;
            return this;
        }

        public void setBrightness(int brightness) {
            this.brightness = brightness;
        }

        public void setBrightnessMode(int brightnessMode) {
            this.brightnessMode = brightnessMode;
        }

        public Config build() {
            return new Config(stackBlur, stackBlurMode, brightness, brightnessMode, showNotification, background,
                    wallpaperMode);
        }
    }

    private Config() {
    }

    private Config(int stackBlur, int stackBlurMode, int brightness, int brightnessMode, boolean showNotification,
            boolean background, int wallpaperMode) {
        this.stackBlur = stackBlur;
        this.stackBlurMode = stackBlurMode;
        this.brightness = brightness;
        this.brightnessMode = brightnessMode;
        this.showNotification = showNotification;
        this.background = background;
        this.wallpaperMode = wallpaperMode;
    }

    public int getStackBlur() {
        return stackBlur;
    }

    @Constants.setWallpaperMode
    public int getStackBlurMode() {
        return stackBlurMode;
    }

    public void setStackBlur(@Size(min = 0, max = 100) int stackBlur) {
        this.stackBlur = stackBlur;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(@IntRange(from = -100, to = 100) int brightness) {
        this.brightness = brightness;
    }

    @Constants.setWallpaperMode
    public int getBrightnessMode() {
        return brightnessMode;
    }

    public boolean isShowNotification() {
        return showNotification;
    }

    public boolean isBackground() {
        return background;
    }

    @Constants.setWallpaperMode
    public int getWallpaperMode() {
        return wallpaperMode;
    }

    public void setWallpaperMode(@Constants.setWallpaperMode int wallpaperMode) {
        this.wallpaperMode = wallpaperMode;
    }

    @Override
    public String toString() {
        return "Config{" +
                "stackBlur=" + stackBlur +
                ", stackBlurMode=" + stackBlurMode +
                ", brightness=" + brightness +
                ", brightnessMode=" + brightnessMode +
                ", showNotification=" + showNotification +
                ", background=" + background +
                ", wallpaperMode=" + wallpaperMode +
                '}';
    }

    protected Config(Parcel in) {
        stackBlur = in.readInt();
        stackBlurMode = in.readInt();
        brightness = in.readInt();
        brightnessMode = in.readInt();
        showNotification = in.readInt() == 1;
        background = in.readInt() == 1;
        wallpaperMode = in.readInt();
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(stackBlur);
        dest.writeInt(stackBlurMode);
        dest.writeInt(brightness);
        dest.writeInt(brightnessMode);
        dest.writeInt(showNotification ? 1 : 0);
        dest.writeInt(background ? 1 : 0);
        dest.writeInt(wallpaperMode);
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("Config", true);
        map.put("Config_stackBlur", stackBlur);
        map.put("Config_stackBlurMode", stackBlurMode);
        map.put("Config_brightness", brightness);
        map.put("Config_brightnessMode", brightnessMode);
        map.put("Config_showNotification", showNotification);
        map.put("Config_background", background);
        map.put("Config_wallpaperMode", wallpaperMode);
        return map;
    }

    public static Config to(Map<String, Object> map) {
        boolean isConfig = BingWallpaperUtils.getOrDefault(map, "Config", false);
        if (!isConfig) {
            return null;
        }
        return new Config(map);
    }

    public Config(Map<String, Object> map) {
        stackBlur = BingWallpaperUtils.getOrDefault(map, "Config_stackBlur", 0);
        stackBlurMode = BingWallpaperUtils.getOrDefault(map, "Config_stackBlurMode", 0);
        brightness = BingWallpaperUtils.getOrDefault(map, "Config_brightness", 0);
        brightnessMode = BingWallpaperUtils.getOrDefault(map, "Config_brightnessMode", 0);
        showNotification = BingWallpaperUtils.getOrDefault(map, "Config_showNotification", false);
        background = BingWallpaperUtils.getOrDefault(map, "Config_background", false);
        wallpaperMode = BingWallpaperUtils.getOrDefault(map, "Config_wallpaperMode", 0);
    }
}
