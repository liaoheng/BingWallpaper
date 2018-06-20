package me.liaoheng.wallpaper.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.UIUtils;

import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperCoverStory;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.ui.MainActivity;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * @author liaoheng
 * @version 2018-06-11 09:38
 */
public class AppWidget_5x2 extends BaseAppWidget {

    public static void start(Context context, BingWallpaperImage bingWallpaperImage) {
        start(context, AppWidget_5x2.class, bingWallpaperImage);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        L.alog().d(TAG, "onReceive action: %s", intent.getAction());

        String action = intent.getAction();

        if (Constants.ACTION_UPDATE_WALLPAPER_COVER_STORY.equals(action)) {
            final BingWallpaperImage image = intent.getParcelableExtra(
                    Constants.EXTRA_UPDATE_WALLPAPER_COVER_STORY);

            setText(context, image);
        } else if (TITLE_CLICK.equals(action)) {
            UIUtils.startActivity(context, MainActivity.class);
        } else if (CONTENT_CLICK.equals(action)) {
            UIUtils.startActivity(context, MainActivity.class);
        }
    }

    @Override
    protected void setText(final Context context, final BingWallpaperImage image) {
        final RemoteViews remoteViews = getRemoteViews(context, R.layout.view_appwidget_5x2);

        if (image == null) {
            remoteViews.setTextViewText(R.id.app_widget_title, "failure !");
            return;
        }

        if (BingWallpaperUtils.isChinaLocale(context)) {
            BingWallpaperNetworkClient.getCoverStory()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<BingWallpaperCoverStory>() {

                                @Override
                                public void call(BingWallpaperCoverStory bingWallpaperCoverStory) {
                                    remoteViews.setTextViewText(R.id.app_widget_title, image.getCopyright());
                                    remoteViews.setTextViewText(R.id.app_widget_content,
                                            bingWallpaperCoverStory.getPara1()
                                                    + bingWallpaperCoverStory.getPara2());

                                    update(context, AppWidget_5x2.class, remoteViews);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {

                                }
                            });
        } else {
            remoteViews.setTextViewText(R.id.app_widget_title, image.getCopyright());
            remoteViews.setTextViewText(R.id.app_widget_content, "");
            update(context, AppWidget_5x2.class, remoteViews);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = getRemoteViews(context, R.layout.view_appwidget_5x2);

        addTitleClick(context, AppWidget_5x2.class, remoteViews);
        addContentClick(context, AppWidget_5x2.class, remoteViews);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
