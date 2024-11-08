package me.liaoheng.wallpaper.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.Utils;

import java.util.Arrays;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.ui.MainActivity;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;

/**
 * @author liaoheng
 * @version 2018-06-12 14:03
 */
public abstract class BaseAppWidget extends AppWidgetProvider {
    protected final String TITLE_CLICK = "TITLE_CLICK";
    protected final String CONTENT_CLICK = "CONTENT_CLICK";
    protected final String CLICK_RETRY = "CLICK_RETRY";
    protected final String TAG = this.getClass().getSimpleName();

    public static void start(Context context, Class<?> cls, Wallpaper wallpaper) {
        Intent intent = new Intent(context, cls);
        intent.setAction(Constants.ACTION_UPDATE_WALLPAPER_COVER_STORY);
        if (wallpaper != null) {
            intent.putExtra(Constants.EXTRA_UPDATE_WALLPAPER_COVER_STORY, wallpaper);
        }
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                BingWallpaperUtils.getPendingIntentFlag());

        remoteViews.setOnClickPendingIntent(id, pendingIntent);
    }

    protected void update(Context context, Class<?> cls, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        ComponentName componentName = new ComponentName(context, cls);

        addTitleClick(context, cls, remoteViews);
        addContentClick(context, cls, remoteViews);

        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }

    protected void updateRetry(Context context, Class<?> cls, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        ComponentName componentName = new ComponentName(context, cls);

        add(context, cls, remoteViews, CLICK_RETRY, R.id.app_widget_title);

        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        L.alog().d(TAG, "onDisabled");
        mCurWallpaper = null;
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        L.alog().d(TAG, "onEnabled");
        getBingWallpaper(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            super.onReceive(context, intent);
        } catch (Exception ignored) {
        }
        String action = intent.getAction();
        L.alog().d(TAG, "onReceive: " + action);
        if (Constants.ACTION_UPDATE_WALLPAPER_COVER_STORY.equals(action)) {
            Wallpaper image = intent.getParcelableExtra(Constants.EXTRA_UPDATE_WALLPAPER_COVER_STORY);
            if (image == null) {
                getBingWallpaper(context);
            } else {
                setText(context, image);
            }
            mCurWallpaper = image;
        } else if (TITLE_CLICK.equals(action) || CONTENT_CLICK.equals(action)) {
            Intent openIntent = new Intent(context, MainActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openIntent);
        } else if (CLICK_RETRY.equals(action)) {
            getBingWallpaper(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        L.alog().d(TAG, "onUpdate: " + Arrays.toString(appWidgetIds));
    }

    protected static Wallpaper mCurWallpaper;

    protected void getBingWallpaper(final Context context) {
        Utils.addSubscribe(BingWallpaperNetworkClient.getBingWallpaper(context),
                new Callback.EmptyCallback<Wallpaper>() {
                    @Override
                    public void onPreExecute() {
                        loadStart(context);
                    }

                    @Override
                    public void onSuccess(Wallpaper image) {
                        L.alog().d(TAG, "onSuccess: " + image);
                        mCurWallpaper = image;
                        setText(context, image);
                    }

                    @Override
                    public void onError(Throwable e) {
                        L.alog().d(TAG, "onError: " + e);
                        setText(context, null);
                    }
                });
    }

    protected void setText(Context context, Wallpaper image) {}

    protected void loadStart(Context context) {}
}
