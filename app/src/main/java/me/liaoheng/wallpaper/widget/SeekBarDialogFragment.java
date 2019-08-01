package me.liaoheng.wallpaper.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.github.liaoheng.common.util.UIUtils;
import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2019-08-01 16:14
 */
public class SeekBarDialogFragment extends DialogFragment {

    private SeekBar mSeekBar;
    private TextView mSeekBarValue;
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
        setStyle(STYLE_NORMAL, R.style.Theme_AppCompat_Dialog_Alert);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = UIUtils.inflate(getActivity(), R.layout.view_preference_seekbar, null);
        mSeekBar = view.findViewById(R.id.seekbar);
        mSeekBarValue = view.findViewById(R.id.seekbar_value);
        int value = getArguments().getInt("value");
        String title = getArguments().getString("title");
        mSeekBar.setProgress(value);
        mSeekBarValue.setText(String.valueOf(mSeekBar.getProgress()));
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(title);
        builder.setView(view).setPositiveButton(R.string.lcm_ok, (dialog, which) -> {
            if (mCallback != null) {
                mCallback.onSeekBarValue(mSeekBar.getProgress());
            }
        }).setNegativeButton(R.string.lcm_no, (dialog, which) -> {
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
