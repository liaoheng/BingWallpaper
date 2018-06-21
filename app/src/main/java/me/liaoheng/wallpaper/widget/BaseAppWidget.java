package me.liaoheng.wallpaper.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.github.liaoheng.common.util.NetworkUtils;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.util.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * @author liaoheng
 * @version 2018-06-12 14:03
 */
public abstract class BaseAppWidget extends AppWidgetProvider {
    protected final String TITLE_CLICK = "TITLE_CLICK";
    protected final String CONTENT_CLICK = "CONTENT_CLICK";
    protected final String TAG = this.getClass().getSimpleName();

    protected static void start(Context context, Class<?> cls, BingWallpaperImage bingWallpaperImage) {
        Intent intent = new Intent(context, cls);
        intent.setAction(Constants.ACTION_UPDATE_WALLPAPER_COVER_STORY);
        intent.putExtra(Constants.EXTRA_UPDATE_WALLPAPER_COVER_STORY, bingWallpaperImage);
        context.sendBroadcast(intent);
    }

    @NonNull
    protected RemoteViews getRemoteViews(Context context, @LayoutRes int layout) {
        return new RemoteViews(context.getPackageName(), layout);
    }

    protected void addTitleClick(Context context, Class<?> cls, RemoteViews remoteViews) {
        add(context, cls, remoteViews, TITLE_CLICK, R.id.app_widget_title);
    }

    protected void addContentClick(Context context, Class<?> cls, RemoteViews remoteViews) {
        add(context, cls, remoteViews, CONTENT_CLICK, R.id.app_widget_content);
    }

    private void add(Context context, Class<?> cls, RemoteViews remoteViews, String action, @IdRes int id) {
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        remoteViews.setOnClickPendingIntent(id, pendingIntent);
    }

    protected void update(Context context, Class<?> cls, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        ComponentName componentName = new ComponentName(context, cls);

        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        getBingWallpaper(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if ("android.appwidget.action.APPWIDGET_UPDATE".equals(intent.getAction())) {
            getBingWallpaper(context);
        }
    }

    protected void getBingWallpaper(final Context context) {
        if (!NetworkUtils.isConnectedOrConnecting(context)) {
            return;
        }
        BingWallpaperNetworkClient.getBingWallpaperSingle(context)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BingWallpaperImage>() {
                    @Override
                    public void call(BingWallpaperImage bingWallpaperImage) {
                        setText(context, bingWallpaperImage);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
    }

    protected void setText(Context context, BingWallpaperImage image) {}
}
