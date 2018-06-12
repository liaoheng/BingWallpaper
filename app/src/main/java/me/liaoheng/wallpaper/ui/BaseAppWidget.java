package me.liaoheng.wallpaper.ui;

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

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.util.Constants;

/**
 * @author liaoheng
 * @version 2018-06-12 14:03
 */
public abstract class BaseAppWidget extends AppWidgetProvider {
    protected final String TITLE_CLICK = "TITLE_CLICK";
    protected final String CONTENT_CLICK = "CONTENT_CLICK";
    protected final String TAG = this.getClass().getSimpleName();

    protected static void start(Context context,Class<?> cls,BingWallpaperImage bingWallpaperImage){
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

    protected void update(Context context, Class<?> cls,RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        ComponentName componentName = new ComponentName(context, cls);

        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }
}
