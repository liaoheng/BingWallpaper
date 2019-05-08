package me.liaoheng.wallpaper.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.palette.graphics.Palette;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.liaoheng.common.util.*;
import com.google.android.material.navigation.NavigationView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.util.TasksUtils;
import me.liaoheng.wallpaper.util.*;
import me.liaoheng.wallpaper.widget.FeedbackDialog;
import me.liaoheng.wallpaper.widget.ToggleImageButton;

import java.io.EOFException;

/**
 * 壁纸主界面
 *
 * @author liaoheng
 * @version 2017-2-15
 */
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomViewListener {

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

        if (BingWallpaperUtils.isPixabaySupport(this)) {
            getPixabay();
        } else {
            getBingWallpaper();
        }
    }

    private void getPixabay() {
        if (!BingWallpaperUtils.isConnected(getApplicationContext())) {
            mErrorTextView.setText(getString(R.string.network_unavailable));
            return;
        }
        showSwipeRefreshLayout();

        Observable<BingWallpaperImage> listObservable = BingWallpaperNetworkClient.randomPixabayImage()
                .compose(this.bindToLifecycle());
        Utils.addSubscribe(listObservable, new Callback.EmptyCallback<BingWallpaperImage>() {
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
                        if (!BingWallpaperUtils.isChinaLocale(getApplicationContext())) {
                            UIUtils.viewGone(mCoverStoryView);
                        }
                    }

                    setImage(bingWallpaperImage);
                }, this::setBingWallpaperError);

        if (BingWallpaperUtils.isChinaLocale(this)) {
            BingWallpaperNetworkClient.getCoverStory()
                    .compose(bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bingWallpaperCoverStory -> {
                                UIUtils.viewVisible(mCoverStoryView);
                                mCoverStory = bingWallpaperCoverStory;
                                mCoverStoryTitleView.setText(bingWallpaperCoverStory.getTitle());
                                mCoverStoryTextView.setText(
                                        bingWallpaperCoverStory.getPara1() + bingWallpaperCoverStory.getPara2());
                            }, throwable -> {
                                UIUtils.viewGone(mCoverStoryView);
                                L.alog().e(TAG, throwable);
                                if (throwable instanceof EOFException) {
                                    return;
                                }
                                CrashReportHandle.collectException(getApplicationContext(), TAG, "getCoverStory",
                                        throwable);
                            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void setBingWallpaperError(Throwable throwable) {
        dismissSwipeRefreshLayout();
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
        String url;
        if (BingWallpaperUtils.isPixabaySupport(getApplicationContext())) {
            url = mCurBingWallpaperImage.getUrl();
        } else {
            url = BingWallpaperUtils.getResolutionImageUrl(this, mCurBingWallpaperImage);
        }

        BingWallpaperUtils.setWallpaper(this, mCurBingWallpaperImage.copy(url), type,
                new Callback4.EmptyCallback<Boolean>() {
                    @Override
                    public void onYes(Boolean aBoolean) {
                        showProgressDialog();
                    }
                });
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
        dismissProgressDialog();
    }

    private void dismissProgressDialog() {
        isRun = false;
        mSetWallpaperActionMenu.showMenu(true);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_main_drawer_settings) {
            UIUtils.startActivity(this, SettingsActivity.class);
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

    private void setImage(BingWallpaperImage bingWallpaperImage) {
        setTitle(bingWallpaperImage.getCopyright());
        mHeaderCoverStoryTitleView.setText(bingWallpaperImage.getCopyright());
        String url;
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            url = bingWallpaperImage.getUrl().replace("_1280", "_960");
        } else {
            url = BingWallpaperUtils.getImageUrl(getApplicationContext(),
                    Constants.WallpaperConfig.MAIN_WALLPAPER_RESOLUTION,
                    bingWallpaperImage);
        }

        GlideApp.with(getActivity())
                .load(url)
                .dontAnimate()
                .thumbnail(0.5f)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                            boolean isFirstResource) {
                        setBingWallpaperError(e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource) {
                        return false;
                    }
                })
                .into(new ImageViewTarget<Drawable>(mWallpaperView) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        showSwipeRefreshLayout();
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                            @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        mWallpaperView.setImageDrawable(resource);
                        mNavigationHeaderImage.setImageDrawable(resource);

                        Palette.from(BitmapUtils.drawableToBitmap(resource))
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
                                        AddBothActionButton(lightMutedSwatch, lightVibrantSwatch);
                                    } else {
                                        if (ROM.getROM().isMiui()) {
                                            AddBothActionButton(lightMutedSwatch, lightVibrantSwatch);
                                        } else {
                                            mSetWallpaperActionMenu.setOnMenuButtonClickListener(v -> setWallpaper(0));
                                        }
                                    }

                                    mSetWallpaperActionMenu.showMenu(true);
                                });

                        isRun = false;
                        dismissSwipeRefreshLayout();
                    }

                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                    }
                });

    }

    private void AddBothActionButton(@ColorInt int lightMutedSwatch, @ColorInt int lightVibrantSwatch) {
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

        addActionButton(lightMutedSwatch, lightVibrantSwatch,
                getString(R.string.pref_set_wallpaper_auto_mode_both),
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
        UIUtils.cancelToast();
        super.onDestroy();
    }
}
