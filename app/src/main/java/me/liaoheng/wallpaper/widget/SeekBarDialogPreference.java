package me.liaoheng.wallpaper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;
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
        if (mProgress == progress) {
            return;
        }
        mProgress = progress;
        persistInt(progress);
        notifyChanged();
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
        mProgress = getPersistedInt((Integer) defaultValue);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SeekBarDialogPreference.SavedState myState = new SeekBarDialogPreference.SavedState(superState);
        myState.mProgress = mProgress;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SeekBarDialogPreference.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SeekBarDialogPreference.SavedState myState = (SeekBarDialogPreference.SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mProgress = myState.mProgress;
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SeekBarDialogPreference.SavedState> CREATOR =
                new Parcelable.Creator<SeekBarDialogPreference.SavedState>() {
                    @Override
                    public SeekBarDialogPreference.SavedState createFromParcel(Parcel in) {
                        return new SeekBarDialogPreference.SavedState(in);
                    }

                    @Override
                    public SeekBarDialogPreference.SavedState[] newArray(int size) {
                        return new SeekBarDialogPreference.SavedState[size];
                    }
                };

        int mProgress;

        SavedState(Parcel source) {
            super(source);
            mProgress = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mProgress);
        }
    }
}
