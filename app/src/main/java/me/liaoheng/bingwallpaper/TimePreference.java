package me.liaoheng.bingwallpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import org.joda.time.LocalTime;

/**
 * @author liaoheng
 * @version 2016-09-20 14:15
 */
public class TimePreference extends DialogPreference {
    private TimePicker picker;
    private LocalTime  localTime;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override protected View onCreateDialogView() {
        picker = new TimePicker(getContext());

        return (picker);
    }

    @Override protected void onBindDialogView(@NonNull View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(localTime.getHourOfDay());
        picker.setCurrentMinute(localTime.getMinuteOfHour());
    }

    @Override protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            localTime = new LocalTime(picker.getCurrentHour(), picker.getCurrentMinute());

            String time = localTime.toString("HH:mm");

            if (callChangeListener(localTime.toString())) {
                persistString(localTime.toString());
            }
            setSummary(time);
        }
    }

    @Override protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            if (defaultValue == null) {
                localTime = LocalTime.parse(getPersistedString("00:00:00.000"));
            } else {
                localTime = LocalTime.parse(getPersistedString(defaultValue.toString()));
            }
        } else {
            localTime = LocalTime.parse(defaultValue.toString());
        }
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    @Override public CharSequence getSummary() {
        if (localTime==null){
            return "";
        }
        return localTime.toString("HH:mm");
    }
}
