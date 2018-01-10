package me.liaoheng.bingwallpaper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.github.liaoheng.common.util.ValidateUtils;

import org.joda.time.LocalTime;

/**
 * 设置时间选择框
 *
 * @author liaoheng
 * @version 2016-09-20 14:15
 */
public class TimePreference extends DialogPreference {
    private TimePicker picker;
    private LocalTime localTime;

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        return (picker);
    }

    @Override
    protected void onBindDialogView(@NonNull View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(getLocalTime().getHourOfDay());
        picker.setCurrentMinute(getLocalTime().getMinuteOfHour());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
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

    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (defaultValue != null) {
            localTime = LocalTime.parse(getPersistedString(String.valueOf(defaultValue)));
        }
    }

    public LocalTime getLocalTime() {
        if (localTime == null) {
            localTime = LocalTime.parse("00:00:00.000");
        }
        return localTime;
    }

//    @Override
//    public CharSequence getSummary() {
//        if (localTime == null) {
//            return "";
//        }
//        return localTime.toString("HH:mm");
//    }
}
