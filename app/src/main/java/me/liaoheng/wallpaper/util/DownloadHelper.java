package me.liaoheng.wallpaper.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;

import io.reactivex.rxjava3.disposables.Disposable;
import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2019-11-06 10:49
 */
public class DownloadHelper {
    private Context mContext;
    private String TAG;
    private ProgressDialog mDownLoadProgressDialog;
    private Disposable mDownLoadSubscription;

    public DownloadHelper(Context context, String tag) {
        mContext = context;
        TAG = tag;
        mDownLoadProgressDialog = UIUtils.createProgressDialog(context, context.getString(R.string.download));
        mDownLoadProgressDialog.setOnDismissListener(dialog -> Utils.dispose(mDownLoadSubscription));
    }

    public void saveWallpaper(Activity activity, String url) {
        if (!BingWallpaperUtils.requestStoragePermissions(activity)) {
            return;
        }
        saveWallpaper(url);
    }

    private void saveWallpaper(String url) {
        if (NetworkUtils.isMobileConnected(mContext)) {
            UIUtils.showYNAlertDialog(mContext, mContext.getString(R.string.alert_mobile_data),
                    new Callback5() {
                        @Override
                        public void onAllow() {
                            downloadSaveWallpaper(url);
                        }

                        @Override
                        public void onDeny() {

                        }
                    });
        } else {
            downloadSaveWallpaper(url);
        }
    }

    private void downloadSaveWallpaper(String url) {
        mDownLoadSubscription = NetUtils.get().downloadImageToFile(mContext, url, new Callback.EmptyCallback<Uri>() {
            @Override
            public void onPreExecute() {
                UIUtils.showDialog(mDownLoadProgressDialog);
            }

            @Override
            public void onPostExecute() {
                UIUtils.dismissDialog(mDownLoadProgressDialog);
            }

            @Override
            public void onSuccess(Uri file) {
                UIUtils.showToast(mContext, R.string.alert_save_wallpaper_success);
            }

            @Override
            public void onError(Throwable e) {
                L.alog().e(TAG, e, "save wallpaper error");
                if (Settings.isEnableLogProvider(mContext)) {
                    LogDebugFileUtils.get().e(TAG, "Save wallpaper error: %s", e);
                }
                UIUtils.showToast(mContext, R.string.alert_save_wallpaper_failure);
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode,
            @NonNull int[] grantResults, String url) {
        if (requestCode == 111) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveWallpaper(url);
            } else {
                UIUtils.showToast(mContext, "no permission");
            }
        }
    }

    public void destroy() {
        Utils.dispose(mDownLoadSubscription);
    }
}
