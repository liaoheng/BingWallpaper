package me.liaoheng.wallpaper.widget;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2021-01-27 16:30
 */
public class ResolutionDialog {

    private ResolutionDialog() {
    }

    public ResolutionDialog(AlertDialog mResolutionDialog) {
        this.mResolutionDialog = mResolutionDialog;
    }

    private AlertDialog mResolutionDialog;

    public static ResolutionDialog with(Context context, Consumer<String> callback) {
        String[] mResolutions = context.getResources().getStringArray(R.array.pref_set_wallpaper_resolution_name);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(mResolutions);

        return new ResolutionDialog(
                new AlertDialog.Builder(context).setTitle(R.string.detail_wallpaper_resolution_influences)
                        .setSingleChoiceItems(arrayAdapter, 2, (dialog, which) -> {
                            dialog.dismiss();
                            callback.accept(mResolutions[which]);
                        })
                        .create());
    }

    public void show() {
        if (mResolutionDialog != null) {
            mResolutionDialog.show();
        }
    }

}
