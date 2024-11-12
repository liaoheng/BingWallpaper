package me.liaoheng.wallpaper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.DialogPreference;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.Settings;

/**
 * @author liaoheng
 * @version 2019-08-01 15:22
 */
public class SeekBarDialogPreference extends DialogPreference {

    private int mProgress;
    private String mSummary;
    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs,
                androidx.preference.R.styleable.Preference, defStyleAttr, defStyleRes);

        mSummary = TypedArrayUtils.getString(a, androidx.preference.R.styleable.Preference_summary,
                androidx.preference.R.styleable.Preference_android_summary);

        a.recycle();
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.dialogPreferenceStyle,
                android.R.attr.dialogPreferenceStyle));
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
    public void setSummary(@Nullable CharSequence summary) {
        super.setSummary(summary);
        if (summary == null) {
            mSummary = null;
        } else {
            mSummary = summary.toString();
        }
    }

    @Nullable
    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if (mSummary == null) {
            return summary;
        }
        String formattedString = String.format(mSummary, mProgress);
        if (TextUtils.equals(formattedString, summary)) {
            return summary;
        }
        return formattedString;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue != null) {
            mProgress = (int) defaultValue;
        }
    }
}
