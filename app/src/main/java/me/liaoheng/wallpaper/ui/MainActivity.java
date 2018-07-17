package me.liaoheng.wallpaper.ui;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.liaoheng.common.core.OnCheckedChangeListener;
import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.Callback4;
import com.github.liaoheng.common.util.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.model.BingWallpaperState;
import me.liaoheng.wallpaper.service.BingWallpaperIntentService;
import me.liaoheng.wallpaper.service.SetWallpaperStateBroadcastReceiver;
import me.liaoheng.wallpaper.util.BingWallpaperJobManager;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.ExceptionHandle;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.TasksUtils;
import me.liaoheng.wallpaper.widget.ToggleImageButton;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 壁纸主界面
 *
 * @author liaoheng
 * @version 2017-2-15
 */
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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

    private ImageView mNavigationHeaderImage;

    private SetWallpaperStateBroadcastReceiver mSetWallpaperStateBroadcastReceiver;
    private BingWallpaperImage mCurBingWallpaperImage;
    private boolean isRun;

    private BingWallpaperCoverStory mCoverStory;

    @OnClick(R.id.bing_wallpaper_cover_story_text)
    void openMap() {
        if (mCoverStory == null) {
            BingWallpaperUtils.openBrowser(this, mCurBingWallpaperImage);
            return;
        }
        String longitude = mCoverStory.getLongitude();//经度
        String latitude = mCoverStory.getLatitude();//纬度
        BingWallpaperUtils.openMap(this, longitude, latitude);
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer_home);

        if (Constants.DAEMON_SERVICE_FLAG
                && BingWallpaperJobManager.getJobType(this) == BingWallpaperJobManager.DAEMON_SERVICE) {
            BingWallpaperJobManager.startDaemonService(this);
        }

        int navigationBarHeight = BingWallpaperUtils.getNavigationBarHeight(this);
        if (navigationBarHeight > 0) {
            UIUtils.viewVisible(mBottomView);
            ViewGroup.LayoutParams layoutParams = mBottomView.getLayoutParams();
            layoutParams.height = navigationBarHeight;
            mBottomView.setLayoutParams(layoutParams);

            ViewGroup.MarginLayoutParams menuLayoutParams = (ViewGroup.MarginLayoutParams) mSetWallpaperActionMenu.getLayoutParams();
            menuLayoutParams.bottomMargin = menuLayoutParams.bottomMargin + navigationBarHeight;
            mSetWallpaperActionMenu.setLayoutParams(menuLayoutParams);
        }

        mNavigationView.setNavigationItemSelectedListener(this);

        ((View) mCoverStoryToggle.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCoverStoryToggle.toggle();
            }
        });
        mCoverStoryToggle.setOnCheckedChangeListener(new OnCheckedChangeListener<ToggleImageButton>() {
            @Override
            public void onCheckedChanged(ToggleImageButton view, boolean isChecked) {
                if (mCurBingWallpaperImage != null) {
                    if (mCoverStoryContent.getVisibility() == View.VISIBLE) {
                        setTitle(mCurBingWallpaperImage.getCopyright());
                    } else {
                        setTitle("");
                    }
                }
                UIUtils.toggleVisibility(mCoverStoryContent);
            }
        });

        mSetWallpaperStateBroadcastReceiver = new SetWallpaperStateBroadcastReceiver(
                new Callback4.EmptyCallback<BingWallpaperState>() {
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

        registerReceiver(mSetWallpaperStateBroadcastReceiver,
                new IntentFilter(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isRun) {
                    UIUtils.showToast(getActivity(), getString(R.string.set_wallpaper_running));
                } else {
                    getBingWallpaper();
                }
            }
        });

        mNavigationHeaderImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigation_header_image);
        mHeaderCoverStoryTitleView = mNavigationView.getHeaderView(0)
                .findViewById(R.id.navigation_header_cover_story_title);

        getBingWallpaper();
    }

    private void getBingWallpaper() {
        if (!BingWallpaperUtils.isConnectedOrConnecting(getApplicationContext())) {
            mErrorTextView.setText(getString(R.string.network_unavailable));
            return;
        }
        showSwipeRefreshLayout();

        BingWallpaperNetworkClient.getBingWallpaper(this)
                .compose(this.<BingWallpaperImage>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BingWallpaperImage>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void call(BingWallpaperImage bingWallpaperImage) {
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
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        setBingWallpaperError(throwable);
                    }
                });

        if (BingWallpaperUtils.isChinaLocale(this)) {
            BingWallpaperNetworkClient.getCoverStory()
                    .compose(this.<BingWallpaperCoverStory>bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<BingWallpaperCoverStory>() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void call(BingWallpaperCoverStory bingWallpaperCoverStory) {
                                    UIUtils.viewVisible(mCoverStoryView);
                                    mCoverStory = bingWallpaperCoverStory;
                                    mCoverStoryTitleView.setText(bingWallpaperCoverStory.getTitle());
                                    mCoverStoryTextView.setText(
                                            bingWallpaperCoverStory.getPara1() + bingWallpaperCoverStory.getPara2());
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    UIUtils.viewGone(mCoverStoryView);
                                }
                            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void setBingWallpaperError(Throwable throwable) {
        dismissSwipeRefreshLayout();
        String error = ExceptionHandle.loadFailed(this, TAG, throwable);
        mErrorTextView.setText(getString(R.string.pull_refresh) + error);
    }

    /**
     * @param type 0. both , 1. home , 2. lock
     */
    private void setWallpaper(int type) {
        if (isRun) {
            UIUtils.showToast(this, getString(R.string.set_wallpaper_running));
            return;
        }
        if (mCurBingWallpaperImage == null) {
            return;
        }
        String url = BingWallpaperUtils.getResolutionImageUrl(this, mCurBingWallpaperImage);
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
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void dismissProgressDialog() {
        isRun = false;
        mSetWallpaperActionMenu.showMenu(true);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showSwipeRefreshLayout() {
        mErrorTextView.setText("");
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void showProgressDialog() {
        isRun = true;
        mSetWallpaperActionMenu.hideMenu(true);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_main_drawer_settings) {
            UIUtils.startActivity(this, SettingsActivity.class);
        } else if (item.getItemId() == R.id.menu_main_drawer_wallpaper_history_list) {
            UIUtils.startActivity(this, WallpaperHistoryListActivity.class);
        } else if (item.getItemId() == R.id.menu_main_drawer_wallpaper_info) {
            BingWallpaperUtils.openBrowser(this, mCurBingWallpaperImage);
        } else if (item.getItemId() == R.id.menu_main_drawer_help) {
            BingWallpaperUtils.openBrowser(this, "https://github.com/liaoheng/BingWallpaper/wiki");
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void setImage(BingWallpaperImage bingWallpaperImage) {
        setTitle(bingWallpaperImage.getCopyright());
        mHeaderCoverStoryTitleView.setText(bingWallpaperImage.getCopyright());

        String url = BingWallpaperUtils.getImageUrl(getApplicationContext(),
                Constants.WallpaperConfig.MAIN_WALLPAPER_RESOLUTION,
                bingWallpaperImage);

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
                                .generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette palette) {

                                        int lightMutedSwatch = palette.getMutedColor(ContextCompat
                                                .getColor(getActivity(), R.color.colorPrimaryDark));

                                        int lightVibrantSwatch = palette.getVibrantColor(ContextCompat
                                                .getColor(getActivity(), R.color.colorAccent));

                                        mSetWallpaperActionMenu.removeAllMenuButtons();

                                        mSetWallpaperActionMenu.setMenuButtonColorNormal(lightMutedSwatch);
                                        mSetWallpaperActionMenu.setMenuButtonColorPressed(lightMutedSwatch);
                                        mSetWallpaperActionMenu.setMenuButtonColorRipple(lightVibrantSwatch);

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            addActionButton(lightMutedSwatch, lightVibrantSwatch,
                                                    getString(R.string.pref_set_wallpaper_auto_mode_home),
                                                    R.drawable.ic_home_white_24dp, new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            setWallpaper(1);
                                                            mSetWallpaperActionMenu.close(true);
                                                        }
                                                    });

                                            addActionButton(lightMutedSwatch, lightVibrantSwatch,
                                                    getString(R.string.pref_set_wallpaper_auto_mode_lock),
                                                    R.drawable.ic_lock_white_24dp, new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            setWallpaper(2);
                                                            mSetWallpaperActionMenu.close(true);
                                                        }
                                                    });

                                            addActionButton(lightMutedSwatch, lightVibrantSwatch,
                                                    getString(R.string.pref_set_wallpaper_auto_mode_both),
                                                    R.drawable.ic_smartphone_white_24dp, new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            setWallpaper(0);
                                                            mSetWallpaperActionMenu.close(true);
                                                        }
                                                    });
                                        } else {
                                            mSetWallpaperActionMenu.setOnMenuButtonClickListener(
                                                    new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            setWallpaper(0);
                                                        }
                                                    });
                                        }

                                        mSetWallpaperActionMenu.showMenu(true);
                                    }
                                });

                        isRun = false;
                        dismissSwipeRefreshLayout();
                    }

                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                    }
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
        actionButton.setButtonSize(com.github.clans.fab.FloatingActionButton.SIZE_MINI);
        mSetWallpaperActionMenu.addMenuButton(actionButton);
        actionButton.setLabelColors(lightMutedSwatch, lightMutedSwatch, lightVibrantSwatch);
        actionButton.setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        if (mSetWallpaperStateBroadcastReceiver != null) {
            unregisterReceiver(mSetWallpaperStateBroadcastReceiver);
        }
        UIUtils.cancelToast();
        super.onDestroy();
    }
}
