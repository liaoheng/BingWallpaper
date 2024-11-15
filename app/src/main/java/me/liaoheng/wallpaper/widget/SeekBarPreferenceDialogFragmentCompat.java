package me.liaoheng.wallpaper.widget;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import me.liaoheng.wallpaper.databinding.ViewPreferenceSeekbarBinding;

/**
 * @author liaoheng
 * @version 2019-08-01 15:22
 */
public class SeekBarPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static SeekBarPreferenceDialogFragmentCompat newInstance(String key) {
        final SeekBarPreferenceDialogFragmentCompat
                fragment = new SeekBarPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    private ViewPreferenceSeekbarBinding mViewBinding;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mViewBinding = ViewPreferenceSeekbarBinding.bind(view);
        DialogPreference preference = getPreference();
        if (preference instanceof SeekBarDialogPreference) {
            SeekBarDialogPreference seekBarDialogPreference = (SeekBarDialogPreference) preference;
            mViewBinding.seekbar.setProgress(seekBarDialogPreference.getProgress());
            mViewBinding.seekbarValue.setText(String.valueOf(seekBarDialogPreference.getProgress()));
        }
        mViewBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mViewBinding.seekbarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Called when the Dialog is closed.
     *
     * @param positiveResult Whether the Dialog was accepted or canceled.
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            DialogPreference preference = getPreference();
            if (preference instanceof SeekBarDialogPreference) {
                SeekBarDialogPreference seekBarDialogPreference = ((SeekBarDialogPreference) preference);
                int progress = mViewBinding.seekbar.getProgress();
                if (seekBarDialogPreference.callChangeListener(progress)) {
                    seekBarDialogPreference.setProgress(progress);
                }
            }
        }
    }
}