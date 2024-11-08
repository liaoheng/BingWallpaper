package me.liaoheng.wallpaper.widget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.text.TextUtils;
import android.widget.RemoteViews;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Wallpaper;

/**
 * @author liaoheng
 * @version 2018-06-11 09:38
 */
public class AppWidget_5x2 extends BaseAppWidget {

    public static void start(Context context, Wallpaper wallpaper) {
        start(context, AppWidget_5x2.class, wallpaper);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        setText(context, mCurWallpaper);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void setText(final Context context, final Wallpaper image) {
        final RemoteViews remoteViews = getRemoteViews(context, R.layout.view_appwidget_5x2);

        if (image == null) {
            remoteViews.setTextViewText(R.id.app_widget_title, context.getString(R.string.request_failed_click_retry));
            updateRetry(context, AppWidget_5x2.class, remoteViews);
            return;
        }

        remoteViews.setTextViewText(R.id.app_widget_title, image.getTitle());
        remoteViews.setTextViewText(R.id.app_widget_content, TextUtils.isEmpty(image.getDesc()) ? "" : image.getDesc());
        update(context, AppWidget_5x2.class, remoteViews);
    }

    @Override
    protected void loadStart(Context context) {
        RemoteViews remoteViews = getRemoteViews(context, R.layout.view_appwidget_5x2);
        remoteViews.setTextViewText(R.id.app_widget_title, context.getString(R.string.loading));
        update(context, AppWidget_5x2.class, remoteViews);
    }
}
