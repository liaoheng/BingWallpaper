package me.liaoheng.wallpaper.widget;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2019-08-01 15:22
 */
public class SeekBarPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private SeekBar mSeekBar;
    private TextView mSeekBarValue;

    public static SeekBarPreferenceDialogFragmentCompat newInstance(String key) {
        final SeekBarPreferenceDialogFragmentCompat
                fragment = new SeekBarPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSeekBar = view.findViewById(R.id.seekbar);
        mSeekBarValue = view.findViewById(R.id.seekbar_value);
        mSeekBar.setMax(100);
        DialogPreference preference = getPreference();
        if (preference instanceof SeekBarDialogPreference) {
            SeekBarDialogPreference seekBarDialogPreference = (SeekBarDialogPreference) preference;
            mSeekBar.setProgress(seekBarDialogPreference.getProgress());
            mSeekBarValue.setText(String.valueOf(seekBarDialogPreference.getProgress()));
        }
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSeekBarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DialogPreference preference = getPreference();
            if (preference instanceof SeekBarDialogPreference) {
                SeekBarDialogPreference seekBarDialogPreference = ((SeekBarDialogPreference) preference);
                int progress = mSeekBar.getProgress();
                seekBarDialogPreference.setProgress(progress);
                if (seekBarDialogPreference.callChangeListener(progress)) {
                    seekBarDialogPreference.save(progress);
                }
                seekBarDialogPreference.setSummary(String.valueOf(progress));
            }
        }
    }
}