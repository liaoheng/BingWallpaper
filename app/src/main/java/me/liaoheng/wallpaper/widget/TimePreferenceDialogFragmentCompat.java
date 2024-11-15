package me.liaoheng.wallpaper.widget;

import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;

import org.joda.time.LocalTime;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import me.liaoheng.wallpaper.databinding.ViewPreferenceTimeBinding;

/**
 * The Dialog for the {@link TimePreference}.
 *
 * @author Jakob Ulbrich
 */
public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    /**
     * Creates a new Instance of the TimePreferenceDialogFragment and stores the key of the related Preference
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

    private ViewPreferenceTimeBinding mViewBinding;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mViewBinding = ViewPreferenceTimeBinding.bind(view);

        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            TimePreference timePreference = ((TimePreference) preference);
            mViewBinding.time.setIs24HourView(DateFormat.is24HourFormat(getContext()));
            mViewBinding.time.setCurrentHour(timePreference.getLocalTime().getHourOfDay());
            mViewBinding.time.setCurrentMinute(timePreference.getLocalTime().getMinuteOfHour());
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
                hours = mViewBinding.time.getHour();
                minutes = mViewBinding.time.getMinute();
            } else {
                hours = mViewBinding.time.getCurrentHour();
                minutes = mViewBinding.time.getCurrentMinute();
            }

            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = ((TimePreference) preference);
                LocalTime localTime = new LocalTime(hours, minutes);
                if (timePreference.callChangeListener(localTime)) {
                    timePreference.setTime(localTime);
                }
            }
        }
    }
}