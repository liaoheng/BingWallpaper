package me.liaoheng.wallpaper.model;

/**
 * @author liaoheng
 * @version 2016-9-19 17:10
 */
public enum BingWallpaperState {
    BEGIN(0), SUCCESS(1), FAIL(2);

    int state;

    BingWallpaperState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public static BingWallpaperState find(int state) {
        if (BEGIN.getState() == state) {
            return BEGIN;
        } else if (SUCCESS.getState() == state) {
            return SUCCESS;
        } else if (FAIL.getState() == state) {
            return FAIL;
        } else {
            return null;
        }
    }
}
