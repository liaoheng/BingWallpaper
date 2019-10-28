package me.liaoheng.wallpaper.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.LogDebugFileUtils;
import me.liaoheng.wallpaper.util.NetUtils;
import me.liaoheng.wallpaper.util.SetWallpaperStateBroadcastReceiverHelper;
import me.liaoheng.wallpaper.widget.SeekBarDialogFragment;
import me.liaoheng.wallpaper.widget.ToggleImageButton;

/**
 * 壁纸详情
 *
 * @author liaoheng
 * @version 2018-01-31 14:14
 */
public class WallpaperDetailActivity extends BaseActivity implements
        SeekBarDialogFragment.SeekBarDialogFragmentCallback {

    @BindView(R.id.bing_wallpaper_detail_image)
    ImageView mImageView;
    @BindView(R.id.bing_wallpaper_detail_bottom)
    View mBottomView;
    @BindView(R.id.bing_wallpaper_detail_bottom_text)
    TextView mBottomTextView;
    @BindView(R.id.bing_wallpaper_detail_loading)
    ProgressBar mProgressBar;
    @BindView(R.id.bing_wallpaper_detail_error)
    TextView mErrorTextView;

    @BindView(R.id.bing_wallpaper_detail_cover_story_content)
    View mCoverStoryContent;
    @BindView(R.id.bing_wallpaper_detail_cover_story_text)
    TextView mCoverStoryTextView;
    @BindView(R.id.bing_wallpaper_detail_cover_story_title)
    TextView mCoverStoryTitleView;
    @BindView(R.id.bing_wallpaper_detail_cover_story_toggle)
    ToggleImageButton mCoverStoryToggle;

    @BindArray(R.array.pref_set_wallpaper_resolution_name)
    String[] mResolutions;

    @BindArray(R.array.pref_set_wallpaper_resolution_value)
    String[] mResolutionValue;

    private String mSelectedResolution;

    private AlertDialog mResolutionDialog;

    private BingWallpaperImage mWallpaperImage;
    private ProgressDialog mDownLoadProgressDialog;
    private ProgressDialog mSetWallpaperProgressDialog;
    private Disposable mDownLoadSubscription;
    private SetWallpaperStateBroadcastReceiverHelper mSetWallpaperStateBroadcastReceiverHelper;
    private Config config = new Config();

    public static void start(Context context, BingWallpaperImage item, Bundle bundle) {
        Intent intent = new Intent(context, WallpaperDetailActivity.class);
        intent.putExtra("image", item);
        ActivityCompat.startActivity(context, intent, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        normalScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_detail);
        ButterKnife.bind(this);
        initStatusBarAddToolbar();

        mWallpaperImage = getIntent().getParcelableExtra("image");
        if (mWallpaperImage == null) {
            UIUtils.showToast(getApplicationContext(), "unknown error");
            finish();
            return;
        }

        mSetWallpaperStateBroadcastReceiverHelper = new SetWallpaperStateBroadcastReceiverHelper(
                new Callback4.EmptyCallback<BingWallpaperState>() {
                    @Override
                    public void onYes(BingWallpaperState bingWallpaperState) {
                        UIUtils.showToast(getApplicationContext(), R.string.set_wallpaper_success);
                    }

                    @Override
                    public void onNo(BingWallpaperState bingWallpaperState) {
                        UIUtils.showToast(getApplicationContext(), R.string.set_wallpaper_failure);
                    }

                    @Override
                    public void onFinish(BingWallpaperState bingWallpaperState) {
                        dismissProgressDialog();
                    }
                });

        ((View) mCoverStoryToggle.getParent()).setOnClickListener(v -> mCoverStoryToggle.toggle());
        mCoverStoryToggle.setOnCheckedChangeListener((view, isChecked) -> {
            if (mCoverStoryContent.getVisibility() == View.VISIBLE) {
                UIUtils.viewVisible(mBottomTextView);
            } else {
                UIUtils.viewGone(mBottomTextView);
            }
            UIUtils.toggleVisibility(mCoverStoryContent);
        });

        mBottomTextView.setText(mWallpaperImage.getCopyright());

        if (TextUtils.isEmpty(mWallpaperImage.getCaption())) {
            UIUtils.viewParentGone(mCoverStoryToggle.getParent());
        } else {
            UIUtils.viewParentVisible(mCoverStoryToggle.getParent());
            mCoverStoryTitleView.setText(mWallpaperImage.getCaption());
            mCoverStoryTextView.setText(mWallpaperImage.getDesc());
        }

        mBottomView.setPadding(mBottomView.getPaddingLeft(), mBottomView.getPaddingTop(),
                mBottomView.getPaddingRight(), BingWallpaperUtils.getNavigationBarPadding(this));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(mResolutions);

        mResolutionDialog = new AlertDialog.Builder(this).setTitle(R.string.detail_wallpaper_resolution_influences)
                .setSingleChoiceItems(arrayAdapter, 2, (dialog, which) -> {
                    mSelectedResolution = mResolutions[which];
                    mResolutionDialog.dismiss();
                    loadImage(getUrl(Constants.WallpaperConfig.WALLPAPER_RESOLUTION));
                })
                .create();

        mSetWallpaperProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.set_wallpaper_running));
        mSetWallpaperProgressDialog.setCancelable(false);
        mDownLoadProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.download));
        mDownLoadProgressDialog.setOnDismissListener(dialog -> Utils.dispose(mDownLoadSubscription));
        mImageView.setOnClickListener(v -> toggleToolbar());
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            loadImage(mWallpaperImage.getUrl());
        } else {
            loadImage(
                    BingWallpaperUtils.getImageUrl(getApplicationContext(),
                            Constants.WallpaperConfig.WALLPAPER_RESOLUTION,
                            mWallpaperImage));
        }
    }

    private void toggleToolbar() {
        if (getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
            fullScreen();
            UIUtils.viewGone(mBottomView);
        } else {
            getSupportActionBar().show();
            normalScreen();
            UIUtils.viewVisible(mBottomView);
        }
    }

    private void fullScreen() {
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void normalScreen() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadImage();
    }

    private void reloadImage() {
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            loadImage(mWallpaperImage.getUrl());
        } else {
            loadImage(getUrl(Constants.WallpaperConfig.WALLPAPER_RESOLUTION));
        }
    }

    private String getUrl(String def) {
        if (TextUtils.isEmpty(mSelectedResolution)) {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), def, mWallpaperImage);
        } else {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), mSelectedResolution, mWallpaperImage);
        }
    }

    private void loadImage(String url) {
        BingWallpaperUtils.loadImage(GlideApp.with(this).asBitmap()
                        .load(url)
                        .dontAnimate()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL), mImageView,
                new Callback.EmptyCallback<Bitmap>() {
                    @Override
                    public void onPreExecute() {
                        UIUtils.viewVisible(mProgressBar);
                        UIUtils.viewGone(mErrorTextView);
                    }

                    @Override
                    public void onPostExecute() {
                        UIUtils.viewGone(mProgressBar);
                        UIUtils.viewVisible(mErrorTextView);
                    }

                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        if (config.getStackBlur() > 0) {
                            bitmap = BingWallpaperUtils.toStackBlur(bitmap, config.getStackBlur());
                        }
                        mImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(Throwable e) {
                        String error = CrashReportHandle.loadFailed(getApplicationContext(), TAG, e);
                        mErrorTextView.setText(error);
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
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            menu.removeItem(R.id.menu_wallpaper_resolution);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_wallpaper_home:
                setWallpaper(1);
                break;
            case R.id.menu_wallpaper_lock:
                setWallpaper(2);
                break;
            case R.id.menu_wallpaper_both:
                setWallpaper(0);
                break;
            case R.id.menu_wallpaper_save:
                UIUtils.showYNAlertDialog(this, getString(R.string.menu_save_wallpaper) + "?",
                        new Callback4.EmptyCallback<DialogInterface>() {
                            @Override
                            public void onYes(DialogInterface dialogInterface) {
                                if (ActivityCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                            111);
                                } else {
                                    saveWallpaper();
                                }
                            }
                        });
                break;
            case R.id.menu_wallpaper_resolution:
                mResolutionDialog.show();
                break;
            case R.id.menu_wallpaper_info:
                if (BingWallpaperUtils.isPixabaySupport(this)) {
                    BingWallpaperUtils.openBrowser(this, mWallpaperImage.getCopyrightlink());
                } else {
                    BingWallpaperUtils.openBrowser(this, mWallpaperImage);
                }
                break;
            case R.id.menu_wallpaper_share:
                BingWallpaperUtils.shareImage(getApplicationContext(), config,
                        getUrl(BingWallpaperUtils.getResolution(this)),
                        mWallpaperImage.getCopyright());
                break;
            case R.id.menu_wallpaper_stack_blur:
                SeekBarDialogFragment.newInstance(getString(R.string.pref_stack_blur), config.getStackBlur(), this)
                        .show(getSupportFragmentManager(), "SeekBarDialogFragment");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveWallpaper() {
        if (NetworkUtils.isMobileConnected(this)) {
            UIUtils.showYNAlertDialog(this, getString(R.string.alert_mobile_data),
                    new Callback4.EmptyCallback<DialogInterface>() {
                        @Override
                        public void onYes(DialogInterface dialogInterface) {
                            downloadSaveWallpaper();
                        }
                    });
        } else {
            downloadSaveWallpaper();
        }
    }

    private void downloadSaveWallpaper() {
        String url;
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            url = mWallpaperImage.getUrl();
        } else {
            url = getUrl(BingWallpaperUtils.getSaveResolution(this));
        }
        mDownLoadSubscription = NetUtils.get().downloadImageToFile(this, url, new Callback.EmptyCallback<Uri>() {
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
                UIUtils.showToast(getApplicationContext(), R.string.alert_save_wallpaper_success);
            }

            @Override
            public void onError(Throwable e) {
                CrashReportHandle.collectException(getApplicationContext(), TAG, e);
                L.alog().e(TAG, e, "save wallpaper error");
                if (BingWallpaperUtils.isEnableLogProvider(getApplicationContext())) {
                    LogDebugFileUtils.get().e(TAG, "save wallpaper error: %s", e);
                }
                UIUtils.showToast(getApplicationContext(), R.string.alert_save_wallpaper_failure);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == 111) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveWallpaper();
            } else {
                UIUtils.showToast(getApplicationContext(), "no permission");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * @param type 0. both , 1. home , 2. lock
     */
    private void setWallpaper(final int type) {
        if (mWallpaperImage == null) {
            return;
        }

        String message = getString(R.string.menu_set_wallpaper_mode_both);
        if (type == 1) {
            message = getString(R.string.menu_set_wallpaper_mode_home);
        } else if (type == 2) {
            message = getString(R.string.menu_set_wallpaper_mode_lock);
        }
        String url;
        if (BingWallpaperUtils.isPixabaySupport(getApplicationContext())) {
            url = mWallpaperImage.getUrl();
        } else {
            url = getUrl(BingWallpaperUtils.getResolution(this));
        }
        UIUtils.showYNAlertDialog(this, message + "?",
                new Callback4.EmptyCallback<DialogInterface>() {
                    @Override
                    public void onYes(DialogInterface dialogInterface) {
                        BingWallpaperUtils.setWallpaper(getActivity(), mWallpaperImage.copy(url), type, config,
                                new EmptyCallback<Boolean>() {
                                    @Override
                                    public void onYes(Boolean aBoolean) {
                                        showProgressDialog();
                                    }
                                });
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
    public void onSeekBarValue(int value) {
        config.setStackBlur(value);
        reloadImage();
    }
}
