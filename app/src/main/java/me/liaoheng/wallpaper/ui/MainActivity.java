package me.liaoheng.wallpaper.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.palette.graphics.Palette;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.request.target.Target;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.ROM;
import com.github.liaoheng.common.util.ShellUtils;
import com.github.liaoheng.common.util.SystemDataException;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.liaoheng.wallpaper.BuildConfig;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.BottomViewListener;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.DownloadHelper;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.LanguageContextWrapper;
import me.liaoheng.wallpaper.util.SetWallpaperStateBroadcastReceiverHelper;
import me.liaoheng.wallpaper.util.TasksUtils;
import me.liaoheng.wallpaper.util.UIHelper;
import me.liaoheng.wallpaper.widget.FeedbackDialog;
import me.liaoheng.wallpaper.widget.ToggleImageButton;

/**
 * 壁纸主界面
 *
 * @author liaoheng
 * @version 2017-2-15
 */
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomViewListener {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LanguageContextWrapper.wrap(context, BingWallpaperUtils.getLanguage(context)));
    }

    @BindView(R.id.bing_wallpaper_view)
    ImageView mWallpaperView;
    @BindView(R.id.bing_wallpaper_error)
    TextView mErrorTextView;
    @BindView(R.id.bing_wallpaper_cover_story_content)
    View mCoverStoryContent;
    @BindView(R.id.bing_wallpaper_cover_story_text)
    TextView mCoverStoryTextView;
    @BindView(R.id.bing_wallpaper_cover_story_title)
    TextView mCoverStoryTitleView;
    TextView mHeaderCoverStoryTitleView;
    @BindView(R.id.bing_wallpaper_cover_story_toggle)
    ToggleImageButton mCoverStoryToggle;
    @BindView(R.id.bing_wallpaper_cover_story)
    View mCoverStoryView;
    @BindView(R.id.bing_wallpaper_bottom)
    View mBottomView;

    @BindView(R.id.bing_wallpaper_swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_drawer)
    NavigationView mNavigationView;
    @BindView(R.id.bing_wallpaper_set_menu)
    FloatingActionMenu mSetWallpaperActionMenu;

    private Dialog mFeedbackDialog;

    private ImageView mNavigationHeaderImage;

    private SetWallpaperStateBroadcastReceiverHelper mSetWallpaperStateBroadcastReceiverHelper;
    @Nullable
    private BingWallpaperImage mCurBingWallpaperImage;
    private boolean isRun;

    private BingWallpaperCoverStory mCoverStory;

    private int mActionMenuBottomMargin;
    private UIHelper mUiHelper = new UIHelper();
    private DownloadHelper mDownloadHelper;

    @OnClick(R.id.bing_wallpaper_cover_story_text)
    void openMap() {
        if (mCoverStory == null) {
            BingWallpaperUtils.openBrowser(this, mCurBingWallpaperImage);
            return;
        }
        String longitude = mCoverStory.getLongitude();//经度
        String latitude = mCoverStory.getLatitude();//纬度
        AppUtils.openMap(this, longitude, latitude);
    }

    @Override
    public void showBottomView() {
        int navigationBarHeight = BingWallpaperUtils.getNavigationBarHeight(this);
        if (navigationBarHeight > 0) {
            showBottomView(navigationBarHeight);
        }
    }

    public void showBottomView(int navigationBarHeight) {
        UIUtils.viewVisible(mBottomView);
        ViewGroup.LayoutParams layoutParams = mBottomView.getLayoutParams();
        layoutParams.height = navigationBarHeight;
        mBottomView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams menuLayoutParams = (ViewGroup.MarginLayoutParams) mSetWallpaperActionMenu.getLayoutParams();
        menuLayoutParams.bottomMargin = mActionMenuBottomMargin + navigationBarHeight;
        mSetWallpaperActionMenu.setLayoutParams(menuLayoutParams);
    }

    @Override
    public void hideBottomView() {
        UIUtils.viewGone(mBottomView);

        ViewGroup.MarginLayoutParams menuLayoutParams = (ViewGroup.MarginLayoutParams) mSetWallpaperActionMenu.getLayoutParams();
        menuLayoutParams.bottomMargin = mActionMenuBottomMargin;
        mSetWallpaperActionMenu.setLayoutParams(menuLayoutParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TasksUtils.isOne()) {
            UIUtils.startActivity(this, IntroActivity.class);
            if (!Constants.Config.isPhone) {
                BingWallpaperUtils.putResolution(this, "1");
                BingWallpaperUtils.putSaveResolution(this, "1");
            }
            finish();
            return;
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initStatusBarAddToolbar();
        BingWallpaperJobManager.restore(this);

        mActionMenuBottomMargin = DisplayUtils.dp2px(this, 10);
        mUiHelper.register(this, this);

        mFeedbackDialog = FeedbackDialog.create(this);

        mNavigationView.setNavigationItemSelectedListener(this);

        ((View) mCoverStoryToggle.getParent()).setOnClickListener(v -> mCoverStoryToggle.toggle());
        mCoverStoryToggle.setOnCheckedChangeListener((view, isChecked) -> {
            if (mCurBingWallpaperImage != null) {
                if (mCoverStoryContent.getVisibility() == View.VISIBLE) {
                    setTitle(mCurBingWallpaperImage.getCopyright());
                } else {
                    setTitle("");
                }
            }
            UIUtils.toggleVisibility(mCoverStoryContent);
        });

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

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (isRun) {
                UIUtils.showToast(getApplicationContext(), R.string.set_wallpaper_running);
            } else {
                if (BingWallpaperUtils.isPixabaySupport(this)) {
                    getPixabay();
                } else {
                    getBingWallpaper();
                }
            }
        });

        mNavigationHeaderImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigation_header_image);
        mHeaderCoverStoryTitleView = mNavigationView.getHeaderView(0)
                .findViewById(R.id.navigation_header_cover_story_title);
        mDownloadHelper = new DownloadHelper(this, TAG);

        if (BingWallpaperUtils.isPixabaySupport(this)) {
            getPixabay();
        } else {
            getBingWallpaper();
        }

        if (ROM.getROM().isMiui()) {
            if (BingWallpaperUtils.isMiuiLockScreenSupport(this)) {
                return;
            }
            String root = PreferenceManager.getDefaultSharedPreferences(this).getString("MIUI_root", "");
            if (!TextUtils.isEmpty(root)) {
                return;
            }
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString("MIUI_root", BuildConfig.VERSION_NAME)
                    .apply();
            if (BingWallpaperUtils.isRooted(this)) {
                showMiuiDialog(true);
                return;
            }
            showMiuiDialog(false);
        }
    }

    private void showMiuiDialog(boolean turn) {
        View view = UIUtils.inflate(this, R.layout.dialog_miui);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view)
                .setPositiveButton(android.R.string.no,
                        (dialog, which) -> {
                        }).create();
        SwitchCompat screen = view.findViewById(R.id.dialog_miui_lock_screen);
        if (turn) {
            screen.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (ShellUtils.hasRootPermission()) {
                            BingWallpaperUtils.setMiuiLockScreenSupport(getApplicationContext(),
                                    true);
                        } else {
                            BingWallpaperUtils.setMiuiLockScreenSupport(getApplicationContext(),
                                    false);
                            screen.setChecked(false);
                            UIUtils.showToast(getActivity(), R.string.unable_root_permission);
                        }
                    });
        } else {
            UIUtils.viewGone(screen);
        }
        alertDialog.show();
    }

    private void getPixabay() {
        if (!BingWallpaperUtils.isConnected(getApplicationContext())) {
            mErrorTextView.setText(getString(R.string.network_unavailable));
            return;
        }
        showSwipeRefreshLayout();

        Utils.addSubscribe(BingWallpaperNetworkClient.randomPixabayImage()
                .compose(bindToLifecycle()), new Callback.EmptyCallback<BingWallpaperImage>() {
            @Override
            public void onSuccess(BingWallpaperImage bingWallpaper) {
                mCurBingWallpaperImage = bingWallpaper;
                UIUtils.viewGone(mCoverStoryView);
                setImage(bingWallpaper);
            }

            @Override
            public void onError(Throwable e) {
                setBingWallpaperError(e);
            }
        });
    }

    @SuppressLint({ "SetTextI18n", "CheckResult" })
    private void getBingWallpaper() {
        if (!BingWallpaperUtils.isConnected(getApplicationContext())) {
            mErrorTextView.setText(getString(R.string.network_unavailable));
            return;
        }
        showSwipeRefreshLayout();

        BingWallpaperNetworkClient.getBingWallpaper(this)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bingWallpaperImage -> {
                    if (bingWallpaperImage == null) {
                        setBingWallpaperError(new SystemDataException("Bing not data"));
                        return;
                    }
                    mCurBingWallpaperImage = bingWallpaperImage;
                    if (!TextUtils.isEmpty(bingWallpaperImage.getCaption())) {
                        UIUtils.viewVisible(mCoverStoryView);
                        mCoverStoryTitleView.setText(
                                bingWallpaperImage.getCaption() + bingWallpaperImage.getCopyrightonly());
                        mCoverStoryTextView.setText(bingWallpaperImage.getDesc());
                    } else {
                        //if (!BingWallpaperUtils.isChinaLocale(getApplicationContext())) {
                        //    UIUtils.viewGone(mCoverStoryView);
                        //}
                        UIUtils.viewGone(mCoverStoryView);
                    }

                    setImage(bingWallpaperImage);
                }, this::setBingWallpaperError);

        //if (BingWallpaperUtils.isChinaLocale(this)) {
        //    BingWallpaperNetworkClient.getCoverStory()
        //            .compose(bindToLifecycle())
        //            .observeOn(AndroidSchedulers.mainThread())
        //            .subscribe(
        //                    bingWallpaperCoverStory -> {
        //                        UIUtils.viewVisible(mCoverStoryView);
        //                        mCoverStory = bingWallpaperCoverStory;
        //                        mCoverStoryTitleView.setText(bingWallpaperCoverStory.getTitle());
        //                        mCoverStoryTextView.setText(
        //                                bingWallpaperCoverStory.getPara1() + bingWallpaperCoverStory.getPara2());
        //                    }, throwable -> {
        //                        UIUtils.viewGone(mCoverStoryView);
        //                        L.alog().e(TAG, throwable);
        //                        if (throwable instanceof EOFException) {
        //                            return;
        //                        }
        //                        CrashReportHandle.collectException(getApplicationContext(), TAG, "getCoverStory",
        //                                throwable);
        //                    });
        //}
    }

    @SuppressLint("SetTextI18n")
    private void setBingWallpaperError(Throwable throwable) {
        dismissProgressDialog();
        String error = CrashReportHandle.loadFailed(this, TAG, throwable);
        mErrorTextView.setText(getString(R.string.pull_refresh) + error);
    }

    /**
     * @param type 0. both , 1. home , 2. lock
     */
    private void setWallpaper(int type) {
        if (isRun) {
            UIUtils.showToast(getApplicationContext(), R.string.set_wallpaper_running);
            return;
        }
        if (mCurBingWallpaperImage == null) {
            return;
        }
        String url = getUrl();

        BingWallpaperUtils.setWallpaper(this, mCurBingWallpaperImage.copy(url), type,
                new Callback4.EmptyCallback<Boolean>() {
                    @Override
                    public void onYes(Boolean aBoolean) {
                        isRun = true;
                        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
                    }
                });
    }

    private String getUrl() {
        return getUrl(BingWallpaperUtils.getResolution(this));
    }

    private String getSaveUrl() {
        return getUrl(BingWallpaperUtils.getSaveResolution(this));
    }

    private String getUrl(String resolution) {
        if (mCurBingWallpaperImage == null) {
            throw new IllegalArgumentException("image is null");
        }
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            return mCurBingWallpaperImage.getUrl();
        } else {
            return BingWallpaperUtils.getImageUrl(this, resolution,
                    mCurBingWallpaperImage);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId_ = item.getItemId();
        if (itemId_ == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dismissSwipeRefreshLayout() {
        mSetWallpaperActionMenu.post(() -> mSetWallpaperActionMenu.showMenu(true));
        dismissProgressDialog();
    }

    private void dismissProgressDialog() {
        isRun = false;
        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));
    }

    private void showSwipeRefreshLayout() {
        mErrorTextView.setText("");
        showProgressDialog();
    }

    private void showProgressDialog() {
        isRun = true;
        mSetWallpaperActionMenu.hideMenu(true);
        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 123) {
                recreate();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_main_drawer_settings) {
            UIUtils.startActivityForResult(this, SettingsActivity.class, 123);
        } else if (item.getItemId() == R.id.menu_main_drawer_wallpaper_history_list) {
            UIUtils.startActivity(this, WallpaperHistoryListActivity.class);
        } else if (item.getItemId() == R.id.menu_main_drawer_wallpaper_info) {
            if (mCurBingWallpaperImage != null) {
                if (BingWallpaperUtils.isPixabaySupport(this)) {
                    BingWallpaperUtils.openBrowser(this, mCurBingWallpaperImage.getCopyrightlink());
                } else {
                    BingWallpaperUtils.openBrowser(this, mCurBingWallpaperImage);
                }
            }
        } else if (item.getItemId() == R.id.menu_main_drawer_help) {
            BingWallpaperUtils.openBrowser(this, "https://github.com/liaoheng/BingWallpaper/blob/image/HELP.md");
        } else if (item.getItemId() == R.id.menu_main_drawer_feedback) {
            UIUtils.showDialog(mFeedbackDialog);
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void setImage(BingWallpaperImage image) {
        setTitle(image.getCopyright());
        mHeaderCoverStoryTitleView.setText(image.getCopyright());
        String url;
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            url = image.getUrl().replace("_1280", "_960");
        } else {
            url = BingWallpaperUtils.getImageUrl(getApplicationContext(),
                    Constants.WallpaperConfig.MAIN_WALLPAPER_RESOLUTION, image);
        }
        if (isDestroyed()) {
            return;
        }
        BingWallpaperUtils.loadImage(GlideApp.with(this).asBitmap()
                        .load(url)
                        .dontAnimate()
                        .thumbnail(0.5f)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL), mWallpaperView,
                new Callback.EmptyCallback<Bitmap>() {
                    @Override
                    public void onPreExecute() {
                        showSwipeRefreshLayout();
                    }

                    @Override
                    public void onPostExecute() {
                        dismissSwipeRefreshLayout();
                    }

                    @Override
                    public void onSuccess(@NonNull Bitmap bitmap) {
                        try {
                            mWallpaperView.setImageBitmap(bitmap);
                            mNavigationHeaderImage.setImageBitmap(bitmap);
                            Palette.from(bitmap)
                                    .generate(palette -> {
                                        int defMuted = ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark);
                                        int defVibrant = ContextCompat.getColor(getActivity(), R.color.colorAccent);
                                        int lightMutedSwatch = defMuted;
                                        int lightVibrantSwatch = defVibrant;

                                        if (palette != null) {
                                            lightMutedSwatch = palette.getMutedColor(defMuted);
                                            lightVibrantSwatch = palette.getVibrantColor(defVibrant);
                                            if (lightMutedSwatch == defMuted) {
                                                if (lightVibrantSwatch != defVibrant) {
                                                    lightMutedSwatch = lightVibrantSwatch;
                                                }
                                            }
                                        }

                                        mSetWallpaperActionMenu.removeAllMenuButtons();
                                        mSetWallpaperActionMenu.setMenuButtonColorNormal(lightMutedSwatch);
                                        mSetWallpaperActionMenu.setMenuButtonColorPressed(lightMutedSwatch);
                                        mSetWallpaperActionMenu.setMenuButtonColorRipple(lightVibrantSwatch);

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            AddBothActionButton(image, lightMutedSwatch, lightVibrantSwatch, false);
                                        } else {
                                            if (ROM.getROM().isMiui()) {
                                                AddBothActionButton(image, lightMutedSwatch, lightVibrantSwatch, false);
                                            } else {
                                                AddBothActionButton(image, lightMutedSwatch, lightVibrantSwatch, true);
                                            }
                                        }

                                        mSetWallpaperActionMenu.showMenu(true);
                                    });
                        } catch (RuntimeException e) {
                            setBingWallpaperError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        setBingWallpaperError(e);
                    }
                });
    }

    private void AddBothActionButton(BingWallpaperImage image, @ColorInt int lightMutedSwatch,
            @ColorInt int lightVibrantSwatch, boolean mini) {
        addActionButton(lightMutedSwatch, lightVibrantSwatch,
                getString(R.string.share),
                R.drawable.ic_share_24dp, v -> {
                    BingWallpaperUtils.shareImage(this, new Config(this),
                            getUrl(), image.getCopyright());
                    mSetWallpaperActionMenu.close(true);
                });

        addActionButton(lightMutedSwatch, lightVibrantSwatch,
                getString(R.string.save),
                R.drawable.ic_save_white_24dp, v -> {
                    if (mCurBingWallpaperImage == null) {
                        return;
                    }
                    BingWallpaperUtils.showSaveWallpaperDialog(this, new Callback5() {
                        @Override
                        public void onAllow() {
                            mSetWallpaperActionMenu.close(true);
                            mDownloadHelper.saveWallpaper(getActivity(), getSaveUrl());
                        }

                        @Override
                        public void onDeny() {

                        }
                    });
                });

        if (!mini) {
            addActionButton(lightMutedSwatch, lightVibrantSwatch,
                    getString(R.string.pref_set_wallpaper_auto_mode_home),
                    R.drawable.ic_home_white_24dp, v -> {
                        setWallpaper(1);
                        mSetWallpaperActionMenu.close(true);
                    });

            addActionButton(lightMutedSwatch, lightVibrantSwatch,
                    getString(R.string.pref_set_wallpaper_auto_mode_lock),
                    R.drawable.ic_lock_white_24dp, v -> {
                        setWallpaper(2);
                        mSetWallpaperActionMenu.close(true);
                    });
        }

        addActionButton(lightMutedSwatch, lightVibrantSwatch,
                mini ? getString(R.string.set_wallpaper) : getString(R.string.pref_set_wallpaper_auto_mode_both),
                R.drawable.ic_smartphone_white_24dp, v -> {
                    setWallpaper(0);
                    mSetWallpaperActionMenu.close(true);
                });
    }

    private void addActionButton(@ColorInt int lightMutedSwatch, @ColorInt int lightVibrantSwatch, String text,
            @DrawableRes int resId, View.OnClickListener listener) {
        FloatingActionButton actionButton = new FloatingActionButton(getActivity());
        actionButton.setLabelText(text);
        actionButton.setColorNormal(lightMutedSwatch);
        actionButton.setColorPressed(lightMutedSwatch);
        actionButton.setColorRipple(lightVibrantSwatch);
        actionButton.setImageResource(resId);
        actionButton.setButtonSize(FloatingActionButton.SIZE_MINI);
        mSetWallpaperActionMenu.addMenuButton(actionButton);
        actionButton.setLabelColors(lightMutedSwatch, lightMutedSwatch, lightVibrantSwatch);
        actionButton.setOnClickListener(listener);
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
        mUiHelper.unregister(this);
        if (mDownloadHelper != null) {
            mDownloadHelper.destroy();
        }
        UIUtils.cancelToast();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (mDownloadHelper != null) {
            mDownloadHelper.onRequestPermissionsResult(requestCode, grantResults, getSaveUrl());
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
