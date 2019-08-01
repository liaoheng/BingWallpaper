package me.liaoheng.wallpaper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;
import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2019-08-01 15:22
 */
public class SeekBarDialogPreference extends DialogPreference {

    private int mProgress;

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarDialogPreference(Context context) {
        super(context);
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }
    public void save(int progress){
        persistInt(progress);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.view_preference_seekbar;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue != null) {
            mProgress = (int) defaultValue;
        }
    }
}
