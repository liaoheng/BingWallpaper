package me.liaoheng.bingwallpaper.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.UIUtils;
import java.net.SocketTimeoutException;
import jonathanfinerty.once.Once;
import me.liaoheng.bingwallpaper.R;
import me.liaoheng.bingwallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.model.BingWallpaperState;
import me.liaoheng.bingwallpaper.service.BingWallpaperIntentService;
import me.liaoheng.bingwallpaper.service.ConnectionChangeReceiver;
import me.liaoheng.bingwallpaper.util.BingWallpaperAlarmManager;
import me.liaoheng.bingwallpaper.util.Utils;
import me.zhanghai.android.systemuihelper.SystemUiHelper;
import org.joda.time.LocalTime;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.bing_wallpaper_view) ImageView wallpaperView;

    @BindView(R.id.bing_wallpaper_progress) ProgressBar progressBar;

    @BindView(R.id.drawer_layout)     DrawerLayout   mDrawerLayout;
    @BindView(R.id.navigation_drawer) NavigationView mNavigationView;

    WallpaperBroadcastReceiver mWallpaperBroadcastReceiver;

    SystemUiHelper mSystemUiHelper;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = UIUtils.findViewById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer_home);

        mSystemUiHelper = new SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE,
                SystemUiHelper.FLAG_IMMERSIVE_STICKY);
        mSystemUiHelper.hide();
        mNavigationView.setNavigationItemSelectedListener(this);

        mWallpaperBroadcastReceiver = new WallpaperBroadcastReceiver();

        //第一次安装打开app，设置自动更新闹钟，默认使用00:00
        if (!Once.beenDone(Once.THIS_APP_INSTALL, "zzz")) {
            LocalTime localTime = Utils.getDayUpdateTime(this);
            BingWallpaperAlarmManager.clear(this);
            BingWallpaperAlarmManager.add(this, localTime);

            //默认不开启
            Utils.disabledReceiver(this, ConnectionChangeReceiver.class.getName());

            Once.markDone("zzz");
        }
        getBingWallpaper();
    }

    BingWallpaperImage mCurBingWallpaperImage;

    private void getBingWallpaper() {
        UIUtils.viewVisible(progressBar);
        isRun = true;
        BingWallpaperNetworkClient.getBingWallpaper(this)
                .compose(this.<BingWallpaperImage>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BingWallpaperImage>() {
                    @Override public void call(BingWallpaperImage bingWallpaperImage) {
                        isRun = false;
                        mCurBingWallpaperImage = bingWallpaperImage;
                        setImage(bingWallpaperImage);
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        isRun = false;
                        UIUtils.viewGone(progressBar);
                        if (throwable instanceof SocketTimeoutException) {
                            L.getSnack().e(TAG, MainActivity.this, "time out");
                            return;
                        }
                        L.getSnack().e(TAG, MainActivity.this, throwable);
                    }
                });
    }

    @BindView(R.id.bing_wallpaper_set) FloatingActionButton setWallpaperBtn;

    @OnClick(R.id.bing_wallpaper_set) void setWallpaper() {
        if (isRun) {
            UIUtils.showSnack(this, "正在设置壁纸，请稍候！");
            return;
        }
        startService(new Intent(this, BingWallpaperIntentService.class));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemId_ = item.getItemId();
        if (itemId_ == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_drawer_settings) {
            UIUtils.startActivity(MainActivity.this, SettingsActivity.class);
        } else if (item.getItemId() == R.id.menu_main_drawer_wallpaper_info) {
            if (mCurBingWallpaperImage == null) {
                return false;
            }
            new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary)).build()
                    .launchUrl(this, Uri.parse(mCurBingWallpaperImage.getCopyrightlink()));
        } else if (item.getItemId() == R.id.menu_main_drawer_wallpaper_refresh) {
            if (isRun) {
                UIUtils.showSnack(this, "正在设置壁纸，请稍候！");
            } else {
                getBingWallpaper();
            }
        } else if (item.getItemId() == R.id.menu_main_drawer_about) {
            AboutActivity.start(this);
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override protected void onResume() {
        mSystemUiHelper.hide();
        super.onResume();
    }

    private boolean isRun;

    class WallpaperBroadcastReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE)) {
                BingWallpaperState state = (BingWallpaperState) intent
                        .getSerializableExtra(BingWallpaperIntentService.EXTRA_GET_WALLPAPER_STATE);

                if (BingWallpaperState.BEGIN.equals(state)) {
                    UIUtils.viewVisible(progressBar);
                    isRun = true;
                } else if (BingWallpaperState.SUCCESS.equals(state)) {
                    isRun = false;
                    UIUtils.viewGone(progressBar);
                    UIUtils.showSnack(MainActivity.this, "壁纸设置成功！");
                } else if (BingWallpaperState.FAIL.equals(state)) {
                    isRun = false;
                    UIUtils.viewGone(progressBar);
                    UIUtils.showSnack(MainActivity.this, "bing 壁纸无法设置！");
                }
            }
        }
    }

    private void setImage(BingWallpaperImage bingWallpaperImage) {
        L.i(TAG, "加载壁纸: %s", bingWallpaperImage);
        setTitle(bingWallpaperImage.getCopyright());

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }

        String url = Utils.getUrl(getApplicationContext(), bingWallpaperImage);

        Glide.with(MainActivity.this).load(url).override(dm.widthPixels, dm.heightPixels)
                .centerCrop().crossFade().into(new ImageViewTarget<GlideDrawable>(wallpaperView) {
            @Override protected void setResource(GlideDrawable resource) {
                wallpaperView.setImageDrawable(resource);
                UIUtils.viewGone(progressBar);

                Palette.from(drawableToBitmap(resource))
                        .generate(new Palette.PaletteAsyncListener() {
                            @Override public void onGenerated(Palette palette) {

                                int lightMutedSwatch = palette.getMutedColor(ContextCompat
                                        .getColor(MainActivity.this, R.color.colorPrimaryDark));

                                int lightVibrantSwatch = palette.getVibrantColor(ContextCompat
                                        .getColor(MainActivity.this, R.color.colorAccent));

                                setWallpaperBtn.setBackgroundTintList(
                                        ColorStateList.valueOf(lightMutedSwatch));
                                setWallpaperBtn.setRippleColor(lightVibrantSwatch);
                            }
                        });
            }
        });

    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap
                .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mWallpaperBroadcastReceiver,
                new IntentFilter(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE));
    }

    @Override protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mWallpaperBroadcastReceiver);
    }

    @Override protected void onDestroy() {
        UIUtils.cancelToast();
        super.onDestroy();
    }
}
