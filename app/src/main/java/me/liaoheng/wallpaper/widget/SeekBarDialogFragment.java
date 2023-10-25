package me.liaoheng.wallpaper.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.databinding.ViewPreferenceSeekbarBinding;

/**
 * @author liaoheng
 * @version 2019-08-01 16:14
 */
public class SeekBarDialogFragment extends DialogFragment {

    private SeekBarDialogFragmentCallback mCallback;

    public static SeekBarDialogFragment newInstance(String title, int value, SeekBarDialogFragmentCallback callback) {
        SeekBarDialogFragment fragment = new SeekBarDialogFragment();
        fragment.setCallback(callback);
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putInt("value", value);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ViewPreferenceSeekbarBinding binding = ViewPreferenceSeekbarBinding.inflate(
                LayoutInflater.from(requireContext()));
        int value = getArguments().getInt("value");
        String title = getArguments().getString("title");
        binding.seekbar.setProgress(value);
        binding.seekbarValue.setText(String.valueOf(binding.seekbar.getProgress()));
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.seekbarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(title);
        builder.setView(binding.getRoot()).setPositiveButton(com.github.liaoheng.common.R.string.lcm_ok, (dialog, which) -> {
            if (mCallback != null) {
                mCallback.onSeekBarValue(binding.seekbar.getProgress());
            }
        }).setNegativeButton(com.github.liaoheng.common.R.string.lcm_no, (dialog, which) -> {
        });

        return builder.create();
    }

    public void setCallback(SeekBarDialogFragmentCallback callback) {
        mCallback = callback;
    }

    public interface SeekBarDialogFragmentCallback {
        void onSeekBarValue(int value);
    }

}
