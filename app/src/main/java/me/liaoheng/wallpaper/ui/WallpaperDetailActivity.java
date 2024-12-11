package me.liaoheng.wallpaper.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.YNCallback;

import java.io.File;
import java.io.IOException;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.databinding.ActivityWallpaperDetailBinding;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Callback4;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.DownloadHelper;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.SetWallpaperStateBroadcastReceiverHelper;
import me.liaoheng.wallpaper.util.Settings;
import me.liaoheng.wallpaper.util.WallpaperUtils;
import me.liaoheng.wallpaper.widget.ResolutionDialog;
import me.liaoheng.wallpaper.widget.SeekBarDialogFragment;

/**
 * 壁纸详情
 *
 * @author liaoheng
 * @version 2018-01-31 14:14
 */
public class WallpaperDetailActivity extends BaseActivity implements
        SeekBarDialogFragment.SeekBarDialogFragmentCallback {

    private ActivityWallpaperDetailBinding mViewBinding;

    private String mSelectedResolution;

    private ResolutionDialog mResolutionDialog;

    private Wallpaper mWallpaper;
    private ProgressDialog mSetWallpaperProgressDialog;
    private SetWallpaperStateBroadcastReceiverHelper mSetWallpaperStateBroadcastReceiverHelper;
    private Config mConfig;
    private DownloadHelper mDownloadHelper;

    public static void start(Context context, Wallpaper item, Bundle bundle) {
        Intent intent = new Intent(context, WallpaperDetailActivity.class);
        intent.putExtra("image", item);
        ActivityCompat.startActivity(context, intent, bundle);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("SelectedResolution", mSelectedResolution);
        outState.putParcelable("Wallpaper", mWallpaper);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        normalScreen();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager.LayoutParams lp = this.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            getWindow().setAttributes(lp);
        }
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityWallpaperDetailBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());
        initStatusBarAddToolbar();
        mConfig = new Config.Builder().build();
        if (savedInstanceState != null) {
            mSelectedResolution = savedInstanceState.getString("SelectedResolution");
            mWallpaper = savedInstanceState.getParcelable("Wallpaper");
        } else {
            mWallpaper = getIntent().getParcelableExtra("image");
        }
        if (mWallpaper == null) {
            UIUtils.showToast(getApplicationContext(), "unknown error");
            finish();
            return;
        }
        mViewBinding.bingWallpaperDetailSubscaleView.setMinimumScaleType(
                SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);

        mSetWallpaperStateBroadcastReceiverHelper = new SetWallpaperStateBroadcastReceiverHelper(
                new Callback4.EmptyCallback<BingWallpaperState>() {
                    @Override
                    public void onYes(BingWallpaperState bingWallpaperState) {
                        Toast.makeText(getApplicationContext(), R.string.set_wallpaper_success, Toast.LENGTH_LONG)
                                .show();
                    }

                    @Override
                    public void onNo(BingWallpaperState bingWallpaperState) {
                        Toast.makeText(getApplicationContext(), R.string.set_wallpaper_failure, Toast.LENGTH_LONG)
                                .show();
                    }

                    @Override
                    public void onFinish(BingWallpaperState bingWallpaperState) {
                        dismissProgressDialog();
                    }
                });

        ((View) mViewBinding.bingWallpaperDetailCoverStoryToggle.getParent()).setOnClickListener(
                v -> mViewBinding.bingWallpaperDetailCoverStoryToggle.toggle());
        mViewBinding.bingWallpaperDetailCoverStoryToggle.setOnCheckedChangeListener((view, isChecked) -> {
            if (mViewBinding.bingWallpaperDetailCoverStoryText.getVisibility() == View.VISIBLE) {
                UIUtils.viewVisible(mViewBinding.bingWallpaperDetailBottomText);
            } else {
                UIUtils.viewGone(mViewBinding.bingWallpaperDetailBottomText);
            }
            UIUtils.toggleVisibility(mViewBinding.bingWallpaperDetailCoverStoryText);
        });

        mViewBinding.bingWallpaperDetailBottomText.setText(mWallpaper.getTitle());

        if (TextUtils.isEmpty(mWallpaper.getDesc())) {
            UIUtils.viewParentGone(mViewBinding.bingWallpaperDetailCoverStoryToggle.getParent());
        } else {
            UIUtils.viewParentVisible(mViewBinding.bingWallpaperDetailCoverStoryToggle.getParent());
            mViewBinding.bingWallpaperDetailCoverStoryText.setText(mWallpaper.getDesc());
        }

        mViewBinding.bingWallpaperDetailBottom.setPadding(mViewBinding.bingWallpaperDetailBottom.getPaddingLeft(),
                mViewBinding.bingWallpaperDetailBottom.getPaddingTop(),
                mViewBinding.bingWallpaperDetailBottom.getPaddingRight(),
                BingWallpaperUtils.getNavigationBarPadding(this));

        mResolutionDialog = ResolutionDialog.with(this, resolution -> {
            mSelectedResolution = resolution;
            loadImage();
        });

        mSetWallpaperProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.set_wallpaper_running));
        mSetWallpaperProgressDialog.setCancelable(false);
        mDownloadHelper = new DownloadHelper(this, TAG);
        mViewBinding.bingWallpaperDetailImage.setOnClickListener(v -> toggleToolbar());
        loadImage();
    }

    private void toggleToolbar() {
        if (getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
            fullScreen();
            UIUtils.viewGone(mViewBinding.bingWallpaperDetailBottom);
        } else {
            getSupportActionBar().show();
            normalScreen();
            UIUtils.viewVisible(mViewBinding.bingWallpaperDetailBottom);
        }
    }

    private void fullScreen() {
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void normalScreen() {
        initTranslucent();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadImage();
    }

    private String getUrl(String defResolution) {
        if (TextUtils.isEmpty(mSelectedResolution)) {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), defResolution, mWallpaper.getBaseUrl());
        } else {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), mSelectedResolution,
                    mWallpaper.getBaseUrl());
        }
    }

    private void loadImage() {
        View image = mViewBinding.bingWallpaperDetailImage;
        if (!TextUtils.isEmpty(mSelectedResolution) && mSelectedResolution.equals("UHD")) {
            image = mViewBinding.bingWallpaperDetailSubscaleView;
        }
        WallpaperUtils.loadImage(GlideApp.with(this).asFile()
                        .load(getUrl(Constants.WallpaperConfig.WALLPAPER_RESOLUTION))
                        .dontAnimate()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL), image,
                new Callback.EmptyCallback<File>() {
                    @Override
                    public void onPreExecute() {
                        mViewBinding.bingWallpaperDetailLoading.post(
                                () -> mViewBinding.bingWallpaperDetailLoading.setVisibility(View.VISIBLE));
                        UIUtils.viewGone(mViewBinding.bingWallpaperDetailError);
                    }

                    @Override
                    public void onPostExecute() {
                        mViewBinding.bingWallpaperDetailLoading.post(
                                () -> mViewBinding.bingWallpaperDetailLoading.setVisibility(View.GONE));
                    }

                    @Override
                    public void onSuccess(File file) {
                        try {
                            if (!TextUtils.isEmpty(mSelectedResolution) && mSelectedResolution.equals("UHD")) {
                                mViewBinding.bingWallpaperDetailSubscaleView.setVisibility(View.VISIBLE);
                                mViewBinding.bingWallpaperDetailImage.setVisibility(View.GONE);
                                mViewBinding.bingWallpaperDetailSubscaleView.setImage(
                                        ImageSource.uri(Uri.fromFile(file)));
                            } else {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                if (bitmap == null) {
                                    onError(new IOException("bitmap is null"));
                                    return;
                                }
                                bitmap = WallpaperUtils.transformStackBlur(bitmap, mConfig.getStackBlur());
                                mViewBinding.bingWallpaperDetailSubscaleView.setVisibility(View.GONE);
                                mViewBinding.bingWallpaperDetailSubscaleView.recycle();
                                mViewBinding.bingWallpaperDetailImage.setVisibility(View.VISIBLE);
                                mViewBinding.bingWallpaperDetailImage.setImageBitmap(bitmap);
                            }
                        } catch (OutOfMemoryError e) {
                            onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        String error = CrashReportHandle.loadFailed(getApplicationContext(), TAG, e);
                        mViewBinding.bingWallpaperDetailError.setText(error);
                        UIUtils.viewVisible(mViewBinding.bingWallpaperDetailError);
                    }
                });
    }

    private void dismissProgressDialog() {
        UIUtils.dismissDialog(mSetWallpaperProgressDialog);
    }

    private void showProgressDialog() {
        UIUtils.showDialog(mSetWallpaperProgressDialog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wallpaper_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            menu.removeItem(R.id.menu_wallpaper_lock);
            menu.removeItem(R.id.menu_wallpaper_home);
            menu.findItem(R.id.menu_wallpaper_both).setTitle(R.string.set_wallpaper);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_wallpaper_home) {
            setWallpaper(1);
        } else if (item.getItemId() == R.id.menu_wallpaper_lock) {
            setWallpaper(2);
        } else if (item.getItemId() == R.id.menu_wallpaper_both) {
            setWallpaper(0);
        } else if (item.getItemId() == R.id.menu_wallpaper_save) {
            BingWallpaperUtils.showSaveWallpaperDialog(this, new YNCallback.EmptyCallback() {
                @Override
                public void onAllow() {
                    mDownloadHelper.saveWallpaper(getActivity(), getSaveUrl());
                }
            });
        } else if (item.getItemId() == R.id.menu_wallpaper_resolution) {
            mResolutionDialog.show();
        } else if (item.getItemId() == R.id.menu_wallpaper_info) {
            BingWallpaperUtils.openBrowser(this, mWallpaper);
        } else if (item.getItemId() == R.id.menu_wallpaper_share) {
            WallpaperUtils.shareImage(this, mConfig,
                    getUrl(Settings.getResolution(this)),
                    mWallpaper.getTitle());
        } else if (item.getItemId() == R.id.menu_wallpaper_stack_blur) {
            SeekBarDialogFragment.newInstance(getString(R.string.pref_stack_blur), mConfig.getStackBlur(), this)
                    .show(getSupportFragmentManager(), "SeekBarDialogFragment");
        } else if (item.getItemId() == R.id.menu_wallpaper_copyright) {
            UIUtils.showInfoAlertDialog(this, mWallpaper.getCopyrightInfo(), new YNCallback.EmptyCallback());
        }
        return super.onOptionsItemSelected(item);
    }

    private String getSaveUrl() {
        return getUrl(Settings.getSaveResolution(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (mDownloadHelper != null) {
            mDownloadHelper.onRequestPermissionsResult(requestCode, grantResults, getSaveUrl());
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * @param type 0. both , 1. home , 2. lock
     */
    private void setWallpaper(final int type) {
        String url = getUrl(Settings.getResolution(this));
        mConfig.setWallpaperMode(type);
        BingWallpaperUtils.showWallpaperDialog(this, mWallpaper.copy(url), mConfig,
                new YNCallback.EmptyCallback() {
                    @Override
                    public void onAllow() {
                        showProgressDialog();
                    }
                });
    }

    @Override
    protected void onStart() {
        if (mSetWallpaperStateBroadcastReceiverHelper != null) {
            mSetWallpaperStateBroadcastReceiverHelper.register(this);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mSetWallpaperStateBroadcastReceiverHelper != null) {
            mSetWallpaperStateBroadcastReceiverHelper.unregister(this);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mDownloadHelper != null) {
            mDownloadHelper.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSeekBarValue(int value) {
        mConfig.setStackBlur(value);
        loadImage();
    }
}
