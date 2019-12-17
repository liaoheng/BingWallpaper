package me.liaoheng.wallpaper.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.UIUtils;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.DownloadHelper;
import me.liaoheng.wallpaper.util.GlideApp;
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
    private ProgressDialog mSetWallpaperProgressDialog;
    private SetWallpaperStateBroadcastReceiverHelper mSetWallpaperStateBroadcastReceiverHelper;
    private Config config = new Config();
    private DownloadHelper mDownloadHelper;

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
                    loadImage();
                })
                .create();

        mSetWallpaperProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.set_wallpaper_running));
        mSetWallpaperProgressDialog.setCancelable(false);
        mDownloadHelper = new DownloadHelper(this, TAG);
        mImageView.setOnClickListener(v -> toggleToolbar());
        loadImage();
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadImage();
    }

    private String getUrl(String defResolution) {
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            return mWallpaperImage.getUrl();
        }
        if (TextUtils.isEmpty(mSelectedResolution)) {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), defResolution, mWallpaperImage);
        } else {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), mSelectedResolution, mWallpaperImage);
        }
    }

    private void loadImage() {
        BingWallpaperUtils.loadImage(GlideApp.with(this).asBitmap()
                        .load(getUrl(Constants.WallpaperConfig.WALLPAPER_RESOLUTION))
                        .dontAnimate()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL), mImageView,
                new Callback.EmptyCallback<Bitmap>() {
                    @Override
                    public void onPreExecute() {
                        mProgressBar.post(() -> mProgressBar.setVisibility(View.VISIBLE));
                        UIUtils.viewGone(mErrorTextView);
                    }

                    @Override
                    public void onPostExecute() {
                        mProgressBar.post(() -> mProgressBar.setVisibility(View.GONE));
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
                        UIUtils.viewVisible(mErrorTextView);
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
                        new Callback5() {
                            @Override
                            public void onAllow() {
                                mDownloadHelper.saveWallpaper(getActivity(), getSaveUrl());
                            }

                            @Override
                            public void onDeny() {

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

    private String getSaveUrl() {
        return getUrl(BingWallpaperUtils.getSaveResolution(this));
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
                new Callback5() {
                    @Override
                    public void onAllow() {
                        BingWallpaperUtils.setWallpaper(getActivity(), mWallpaperImage.copy(url), type, config,
                                new Callback4.EmptyCallback<Boolean>() {
                                    @Override
                                    public void onYes(Boolean aBoolean) {
                                        showProgressDialog();
                                    }
                                });
                    }

                    @Override
                    public void onDeny() {

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
        config.setStackBlur(value);
        loadImage();
    }
}
