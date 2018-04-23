package me.liaoheng.wallpaper.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.NetworkUtils;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.service.WallpaperBroadcastReceiver;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
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
    ContentLoadingProgressBar mProgressBar;

    private BingWallpaperImage mWallpaperImage;
    private ProgressDialog mDownLoadProgressDialog;
    private ProgressDialog mSetWallpaperProgressDialog;
    private Subscription mDownLoadSubscription;
    private WallpaperBroadcastReceiver mWallpaperBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_wallpaper_detail);
        ButterKnife.bind(this);
        Toolbar toolbar = UIUtils.findViewById(this, R.id.toolbar);
        int statusBarHeight = DisplayUtils.getStatusBarHeight(this);
        ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
        lp.height += statusBarHeight;
        toolbar.setPadding(toolbar.getPaddingLeft(), toolbar.getPaddingTop() + statusBarHeight,
                toolbar.getPaddingRight(), toolbar.getPaddingBottom());
        setSupportActionBar(toolbar);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        mWallpaperImage = (BingWallpaperImage) getIntent().getSerializableExtra("image");

        mBottomTextView.setText(mWallpaperImage.getCopyright());

        if (BingWallpaperUtils.isNavigationBar(this)) {
            int navigationBarHeight = BingWallpaperUtils.getNavigationBarHeight(this);
            if (navigationBarHeight > 0) {
                mBottomView.setPadding(mBottomView.getPaddingLeft(), mBottomView.getPaddingTop(),
                        mBottomView.getPaddingRight(), mBottomView.getPaddingBottom() + navigationBarHeight);
            }
        }

        Glide.with(this)
                .load(BingWallpaperUtils.getImageUrl(this, mWallpaperImage))
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.1f)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                            boolean isFirstResource) {
                        mProgressBar.hide();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.hide();
                        return false;
                    }
                })
                .into(mImageView);
        mSetWallpaperProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.set_wallpaper_running));
        mSetWallpaperProgressDialog.setCancelable(false);
        mDownLoadProgressDialog = UIUtils.createProgressDialog(this, getString(R.string.download));
        mDownLoadProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Utils.unsubscribe(mDownLoadSubscription);
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
        String url = BingWallpaperUtils.getImageUrl(BingWallpaperUtils.getSaveResolution(this), mWallpaperImage);
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
        String message = getString(R.string.menu_set_wallpaper_mode_both);
        if (type == 1) {
            message = getString(R.string.menu_set_wallpaper_mode_home);
        } else if (type == 2) {
            message = getString(R.string.menu_set_wallpaper_mode_lock);
        }
        final String url = BingWallpaperUtils.getResolutionImageUrl(this, mWallpaperImage);
        UIUtils.showYNAlertDialog(this, message + "?",
                new Callback4.EmptyCallback<DialogInterface>() {
                    @Override
                    public void onYes(DialogInterface dialogInterface) {
                        BingWallpaperUtils.setWallpaper(getActivity(), url, type, new EmptyCallback<Boolean>() {
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
        super.onStart();
        registerReceiver(mWallpaperBroadcastReceiver,
                new IntentFilter(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mWallpaperBroadcastReceiver);
    }
}
