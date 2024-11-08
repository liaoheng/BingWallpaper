package me.liaoheng.wallpaper.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Wallpaper;

/**
 * @author liaoheng
 * @version 2018-06-11 09:38
 */
public class AppWidget_5x1 extends BaseAppWidget {

    public static void start(Context context, Wallpaper wallpaper) {
        start(context, AppWidget_5x1.class, wallpaper);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        setText(context, mCurWallpaper);
    }

    @Override
    protected void setText(Context context, Wallpaper image) {
        RemoteViews remoteViews = getRemoteViews(context, R.layout.view_appwidget_5x1);

        if (image == null) {
            remoteViews.setTextViewText(R.id.app_widget_title, context.getString(R.string.request_failed_click_retry));
            updateRetry(context, AppWidget_5x1.class, remoteViews);
            return;
        }
        remoteViews.setTextViewText(R.id.app_widget_title, image.getTitle());
        update(context, AppWidget_5x1.class, remoteViews);
    }

    @Override
    protected void loadStart(Context context) {
        RemoteViews remoteViews = getRemoteViews(context, R.layout.view_appwidget_5x1);
        remoteViews.setTextViewText(R.id.app_widget_title, context.getString(R.string.loading));
        update(context, AppWidget_5x1.class, remoteViews);
    }
}
