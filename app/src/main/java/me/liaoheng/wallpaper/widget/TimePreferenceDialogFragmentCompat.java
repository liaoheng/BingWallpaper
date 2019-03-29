package me.liaoheng.wallpaper.widget;

import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import me.liaoheng.wallpaper.R;
import org.joda.time.LocalTime;

/**
 * The Dialog for the {@link TimePreference}.
 *
 * @author Jakob Ulbrich
 */
public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    /**
     * The TimePicker widget
     */
    private TimePicker mTimePicker;

    /**
     * Creates a new Instance of the TimePreferenceDialogFragment and stores the key of the
     * related Preference
     *
     * @param key The key of the related Preference
     * @return A new Instance of the TimePreferenceDialogFragment
     */
    public static TimePreferenceDialogFragmentCompat newInstance(String key) {
        final TimePreferenceDialogFragmentCompat
                fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mTimePicker = view.findViewById(R.id.time);

        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            TimePreference timePreference = ((TimePreference) preference);
            mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
            mTimePicker.setCurrentHour(timePreference.getLocalTime().getHourOfDay());
            mTimePicker.setCurrentMinute(timePreference.getLocalTime().getMinuteOfHour());
        }
    }

    /**
     * Called when the Dialog is closed.
     *
     * @param positiveResult Whether the Dialog was accepted or canceled.
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hours;
            int minutes;
            if (Build.VERSION.SDK_INT >= 23) {
                hours = mTimePicker.getHour();
                minutes = mTimePicker.getMinute();
            } else {
                hours = mTimePicker.getCurrentHour();
                minutes = mTimePicker.getCurrentMinute();
            }

            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = ((TimePreference) preference);
                LocalTime localTime = new LocalTime(hours, minutes);
                if (timePreference.callChangeListener(localTime.toString())) {
                    timePreference.setTime(localTime.toString());
                }
                timePreference.setSummary(localTime.toString("HH:mm"));
            }
        }
    }
}