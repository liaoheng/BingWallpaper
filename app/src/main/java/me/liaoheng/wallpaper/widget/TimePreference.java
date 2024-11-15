package me.liaoheng.wallpaper.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import org.joda.time.LocalTime;

import me.liaoheng.wallpaper.R;

/**
 * A Preference to select a specific Time with a {@link android.widget.TimePicker}.
 *
 * @author Jakob Ulbrich
 */
public class TimePreference extends DialogPreference {

    private LocalTime localTime;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LocalTime getLocalTime() {
        if (localTime == null) {
            localTime = LocalTime.parse("00:00:00.000");
        }
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public void setTime(LocalTime time) {
        if (time == null || getLocalTime().equals(time)) {
            return;
        }
        localTime = time;
        persistString(time.toString());
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.view_preference_time;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        localTime = LocalTime.parse(getPersistedString(String.valueOf(defaultValue)));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final TimePreference.SavedState myState = new TimePreference.SavedState(superState);
        myState.time = localTime;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(TimePreference.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        TimePreference.SavedState myState = (TimePreference.SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        localTime = myState.time;
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<TimePreference.SavedState> CREATOR =
                new Parcelable.Creator<TimePreference.SavedState>() {
                    @Override
                    public TimePreference.SavedState createFromParcel(Parcel in) {
                        return new TimePreference.SavedState(in);
                    }

                    @Override
                    public TimePreference.SavedState[] newArray(int size) {
                        return new TimePreference.SavedState[size];
                    }
                };

        LocalTime time;

        SavedState(Parcel source) {
            super(source);
            time = LocalTime.parse(source.readString());
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(time.toString());
        }
    }

}
