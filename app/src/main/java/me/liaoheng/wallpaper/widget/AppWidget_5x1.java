package me.liaoheng.wallpaper.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.github.liaoheng.common.util.L;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.Constants;

/**
 * @author liaoheng
 * @version 2018-06-11 09:38
 */
public class AppWidget_5x1 extends BaseAppWidget {

    public static void start(Context context, Wallpaper wallpaper) {
        if (getWidgetActive(context, Constants.PREF_APPWIDGET_5X1_ENABLE)) {
            return;
        }
        start(context, AppWidget_5x1.class, wallpaper);
    }

    protected void setWidgetActive(Context context, boolean active) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(Constants.PREF_APPWIDGET_5X1_ENABLE, active).apply();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        L.alog().d(TAG, "onReceive action: %s", intent.getAction());
        super.onReceive(context, intent);
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
