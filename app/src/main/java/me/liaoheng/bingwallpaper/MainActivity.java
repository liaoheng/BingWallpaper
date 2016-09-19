package me.liaoheng.bingwallpaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.UIUtils;
import java.util.Calendar;
import jonathanfinerty.once.Once;
import me.liaoheng.bingwallpaper.model.BingWallpaperImage;
import me.liaoheng.bingwallpaper.model.BingWallpaperState;
import me.zhanghai.android.systemuihelper.SystemUiHelper;
import org.joda.time.DateTime;

public class MainActivity extends AppCompatActivity {

    private  final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.bing_wallpaper_view) ImageView wallpaperView;

    @BindView(R.id.bing_wallpaper_copyright) Button copyright;

    @BindView(R.id.bing_wallpaper_progress)
    ProgressBar progressBar;

    WallpaperBroadcastReceiver mWallpaperBroadcastReceiver;

    SystemUiHelper mSystemUiHelper;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSystemUiHelper = new SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE,
                SystemUiHelper.FLAG_IMMERSIVE_STICKY);
        mSystemUiHelper.hide();

        mWallpaperBroadcastReceiver = new WallpaperBroadcastReceiver();

        if (!Once.beenDone(Once.THIS_APP_INSTALL, "zzz")) {
            L.i(TAG,"one");

            Intent intent = new Intent(this, AutoUpdateBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            // 取消以前同类型的提醒
            alarmManager.cancel(pendingIntent);

            DateTime now = DateTime.now();
            DateTime dateTime = new DateTime(now.getYear(), now.getMonthOfYear(),
                    now.getDayOfMonth(), 17, 5);
            // 设定每天在指定的时间运行alert
            alarmManager.setRepeating(AlarmManager.RTC,
                    dateTime.getMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);

            Once.markDone("zzz");
        }

        Intent intent = new Intent(this, BingWallpaperIntentService.class);
        intent.putExtra(BingWallpaperIntentService.AUTO, false);
        startService(intent);
    }

    @OnClick(R.id.bing_wallpaper_view) void show() {
        mSystemUiHelper.toggle();
    }

    @Override protected void onResume() {
        mSystemUiHelper.hide();
        super.onResume();
    }

    class WallpaperBroadcastReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BingWallpaperIntentService.ACTION_GET_WALLPAPER_STATE)) {
                BingWallpaperState state = (BingWallpaperState) intent.getSerializableExtra(BingWallpaperIntentService.EXTRA_WALLPAPER_STATE);

                if (BingWallpaperState.BEGIN.equals(state)) {
                    UIUtils.viewVisible(progressBar);
                }else if (BingWallpaperState.SUCCESS.equals(state)){
                    setImage(context, intent);
                }else if (BingWallpaperState.FAIL.equals(state)){
                    UIUtils.viewGone(progressBar);
                    UIUtils.showToast(getApplicationContext(),"bing 壁纸无法加载！");
                }
            }
        }
    }

    private void setImage(Context context, Intent intent) {
        final BingWallpaperImage bingWallpaperImage = (BingWallpaperImage) intent
                .getSerializableExtra(BingWallpaperIntentService.EXTRA_WALLPAPER_IMAGE);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        Glide.with(MainActivity.this).load(bingWallpaperImage.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(dm.widthPixels, dm.heightPixels).centerCrop().into(wallpaperView);

        UIUtils.viewGone(progressBar);

        copyright.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(bingWallpaperImage.getCopyrightlink()));
                startActivity(intent1);
            }
        });
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
}
