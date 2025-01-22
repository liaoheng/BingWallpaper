package me.liaoheng.wallpaper.widget;

import android.os.Build;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * @author liaoheng
 * @date 2025-01-21 16:09
 */
public class SeekBarDialogHelper {

    public void create(SeekBar seekBar, TextView seekBarValue,int value,int max,int min){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMax(max);
            seekBar.setMin(min);
            seekBar.setProgress(value);
        }else {
            seekBar.setMax(max+Math.abs(min));
            seekBar.setProgress(value+Math.abs(min));
        }
        seekBarValue.setText(String.valueOf(getProgress(value, min)));
    }

    public int getProgress(int value, int min) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return value;
        } else {
            return value - Math.abs(min);
        }
    }
}
