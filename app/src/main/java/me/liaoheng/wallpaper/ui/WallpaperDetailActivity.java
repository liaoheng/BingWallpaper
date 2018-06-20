package me.liaoheng.wallpaper.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;

import java.io.File;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.service.WallpaperBroadcastReceiver;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.ExceptionHandle;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.NetUtils;
import rx.Subscription;

/**
 * 壁纸详情
 *
 * @author liaoheng
 * @version 2018-01-31 14:14
 */
public class WallpaperDetailActivity extends BaseActivity {

    private final String TAG = WallpaperDetailActivity.class.getSimpleName();

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

    @BindArray(R.array.pref_set_wallpaper_resolution_name)
    String[] mResolutions;

    @BindArray(R.array.pref_set_wallpaper_resolution_value)
    String[] mResolutionValue;

    private String mSelectedResolution;

    private AlertDialog mResolutionDialog;

    private BingWallpaperImage mWallpaperImage;
    private ProgressDialog mDownLoadProgressDialog;
    private ProgressDialog mSetWallpaperProgressDialog;
    private Subscription mDownLoadSubscription;
    private WallpaperBroadcastReceiver mWallpaperBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        normalScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_detail);
        ButterKnife.bind(this);
        initStatusBarAddToolbar();

        mWallpaperBroadcastReceiver = new WallpaperBroadcastReceiver(new Callback4.EmptyCallback<BingWallpaperState>() {
            @Override
            public void onYes(BingWallpaperState bingWallpaperState) {
                dismissProgressDialog();
                UIUtils.showToast(getApplicationContext(), getString(R.string.set_wallpaper_success));
            }

            @Override
            public void onNo(BingWallpaperState bingWallpaperState) {
                dismissProgressDialog();
                UIUtils.showToast(getApplicationContext(), getString(R.string.set_wallpaper_failure));
            }
        });
        registerReceiver(mWallpaperBroadcastReceiver,
                new IntentFilter(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE));

        mWallpaperImage = getIntent().getParcelableExtra("image");
        if (mWallpaperImage == null) {
            UIUtils.showToast(getApplicationContext(), "unknown error");
            finish();
            return;
        }

        mBottomTextView.setText(mWallpaperImage.getCopyright());

        mBottomView.setPadding(mBottomView.getPaddingLeft(), mBottomView.getPaddingTop(),
                mBottomView.getPaddingRight(), BingWallpaperUtils.getNavigationBarHeight(this));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(mResolutions);

        mResolutionDialog = new AlertDialog.Builder(this).setTitle(R.string.menu_wallpaper_resolution)
                .setSingleChoiceItems(arrayAdapter, 2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedResolution = mResolutions[which];
                        mResolutionDialog.dismiss();
                        loadImage(getUrl(Constants.WallpaperConfig.WALLPAPER_RESOLUTION));
                    }
                })
                .create();

        mSetWallpaperProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.set_wallpaper_running));
        mSetWallpaperProgressDialog.setCancelable(false);
        mDownLoadProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.download));
        mDownLoadProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Utils.unsubscribe(mDownLoadSubscription);
            }
        });
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                toggleToolbar();
                return super.onSingleTapUp(e);
            }
        });
        loadImage(
                BingWallpaperUtils.getImageUrl(getApplicationContext(), Constants.WallpaperConfig.WALLPAPER_RESOLUTION,
                        mWallpaperImage));

    }

    private GestureDetector mGestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadImage(getUrl(Constants.WallpaperConfig.WALLPAPER_RESOLUTION));
    }

    private String getUrl(String def) {
        if (TextUtils.isEmpty(mSelectedResolution)) {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), def, mWallpaperImage);
        } else {
            return BingWallpaperUtils.getImageUrl(getApplicationContext(), mSelectedResolution, mWallpaperImage);
        }
    }

    private void loadImage(String url) {
        GlideApp.with(this)
                .load(url)
                .dontAnimate()
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                            boolean isFirstResource) {
                        UIUtils.viewGone(mProgressBar);
                        UIUtils.viewVisible(mErrorTextView);
                        String error = ExceptionHandle.loadFailed(getApplicationContext(), TAG, e);
                        mErrorTextView.setText(error);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource) {
                        return false;
                    }
                }).into(new ImageViewTarget<Drawable>(mImageView) {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                UIUtils.viewVisible(mProgressBar);
                UIUtils.viewGone(mErrorTextView);
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                UIUtils.viewGone(mProgressBar);
                UIUtils.viewGone(mErrorTextView);
                mImageView.setImageDrawable(resource);
            }

            @Override
            protected void setResource(Drawable resource) {
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
        String url = getUrl(BingWallpaperUtils.getSaveResolution(this));
        mDownLoadSubscription = NetUtils.get().downloadImageToFile(this, url, new Callback.EmptyCallback<File>() {
            @Override
            public void onPreExecute() {
                UIUtils.showDialog(mDownLoadProgressDialog);
            }

            @Override
            public void onPostExecute() {
                UIUtils.dismissDialog(mDownLoadProgressDialog);
            }

            @Override
            public void onSuccess(File file) {
                UIUtils.showToast(getApplicationContext(), getString(R.string.alert_save_wallpaper_success));
            }

            @Override
            public void onError(SystemException e) {
                L.getToast().e(TAG, getApplicationContext(), getString(R.string.alert_save_wallpaper_failure), e);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == 111) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveWallpaper();
            } else {
                UIUtils.showToast(this, "no permission");
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

        final String url = getUrl(BingWallpaperUtils.getResolution(this));
        UIUtils.showYNAlertDialog(this, message + "?",
                new Callback4.EmptyCallback<DialogInterface>() {
                    @Override
                    public void onYes(DialogInterface dialogInterface) {
                        BingWallpaperUtils.setWallpaper(getActivity(), mWallpaperImage.copy(url), type,
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
    protected void onDestroy() {
        unregisterReceiver(mWallpaperBroadcastReceiver);
        super.onDestroy();
    }
}
