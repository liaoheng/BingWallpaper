package me.liaoheng.wallpaper.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.commit451.nativestackblur.NativeStackBlur;
import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;

/**
 * @author liaoheng
 * @version 2020-07-01 13:40
 */
public class WallpaperUtils {

    public static void autoSaveWallpaper(Context context, String TAG, Wallpaper image, File wallpaper) {
        if (!Settings.isAutoSave(context)) {
            return;
        }
        File saveFile = new File(wallpaper.toURI());
        try {
            if (!BingWallpaperUtils.checkStoragePermissions(context)) {
                throw new IOException("Permission denied");
            }
            String saveResolution = Settings.getSaveResolution(context);
            String resolution = Settings.getResolution(context);
            String saveImageUrl = image.getImageUrl();
            if (!saveResolution.equals(resolution)) {
                saveImageUrl = BingWallpaperUtils.getImageUrl(context, saveResolution,
                        image.getBaseUrl());
                saveFile = getImageFile(context, saveImageUrl);
            }
            saveToFile(context, saveImageUrl, saveFile);
            L.alog().i(TAG, "wallpaper save url: %s", saveImageUrl);
        } catch (Throwable e) {
            if (BingWallpaperUtils.isEnableLogProvider(context)) {
                LogDebugFileUtils.get().e(TAG, e, "Auto download wallpaper failure");
            }
        }
    }

    public static Uri saveToFile(Context context, String url, File from) throws IOException {
        String name = BingWallpaperUtils.getName(url);
        String[] split = name.split("=");
        if (split.length > 1) {
            name = split[1];
        }
        return FileUtils.saveFileToPictureCompat(context, name, from);
    }

    public static File getImageFile(Context context, String url) throws Exception {
        return GlideApp.with(context).downloadOnly().load(url).submit().get();
    }

    public static File getImageFile(Context context, @NonNull Config config, @NonNull String url) throws Exception {
        return getImageStackBlurFile(config.getStackBlur(), getImageFile(context, url));
    }

    public static File getImageStackBlurFile(int stackBlur, File wallpaper) {
        if (stackBlur > 0) {
            String key = BingWallpaperUtils.createKey(wallpaper.getAbsolutePath() + "_blur_" + stackBlur);
            File stackBlurFile = CacheUtils.get().get(key);
            if (stackBlurFile == null) {
                Bitmap bitmap = toStackBlur2(BitmapFactory.decodeFile(wallpaper.getAbsolutePath()), stackBlur);
                return CacheUtils.get().put(key, BitmapUtils.bitmapToStream(bitmap,
                        Bitmap.CompressFormat.JPEG));
            } else {
                return stackBlurFile;
            }
        } else {
            return wallpaper;
        }
    }

    public static File getImageWaterMarkFile(@NonNull Context context, File wallpaper, String str) {
        String key = BingWallpaperUtils.createKey(wallpaper.getAbsolutePath() + "_mark_" + str);
        File mark = CacheUtils.get().get(key);
        if (mark == null) {
            Bitmap bitmap = waterMark(context, BitmapFactory.decodeFile(wallpaper.getAbsolutePath()), str);
            return CacheUtils.get().put(key, BitmapUtils.bitmapToStream(bitmap,
                    Bitmap.CompressFormat.JPEG));
        } else {
            return mark;
        }
    }

    public static Bitmap transformStackBlur(Bitmap bitmap, int stackBlur) {
        if (stackBlur <= 0) {
            return bitmap;
        }
        return toStackBlur2(bitmap, stackBlur);
    }

    public static void shareImage(@NonNull Context context, @NonNull Config config, @NonNull String url,
            @NonNull final String str) {
        ProgressDialog dialog = UIUtils.showProgressDialog(context, context.getString(R.string.share) + "...");
        Observable<File> fileObservable = Observable.just(str).subscribeOn(Schedulers.io()).map(
                s -> getImageFile(context, config, url)).flatMap(
                (Function<File, ObservableSource<File>>) file -> Observable.just(
                        getImageWaterMarkFile(context, file, str)));
        Utils.addSubscribe(fileObservable, new Callback.EmptyCallback<File>() {
            @Override
            public void onSuccess(File file) {
                UIUtils.dismissDialog(dialog);
                Intent share = ShareCompat.IntentBuilder.from((Activity) context)
                        .setType("image/jpeg")
                        .setStream(BingWallpaperUtils.getUriForFile(context, file))
                        .getIntent();
                context.startActivity(share);
            }

            @Override
            public void onError(Throwable e) {
                UIUtils.dismissDialog(dialog);
                L.alog().e("Share", e);
                UIUtils.showToast(context, "Share error");
            }
        });
    }

    public static void loadImage(GlideRequest<Bitmap> request, ImageView imageView,
            Callback<Bitmap> callback) {
        request.listener(new RequestListener<Bitmap>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target,
                    boolean isFirstResource) {
                callback.onPostExecute();
                callback.onError(e);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                    DataSource dataSource,
                    boolean isFirstResource) {
                return false;
            }
        }).into(new BitmapImageViewTarget(imageView) {

            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                callback.onPreExecute();
            }

            @Override
            public void onResourceReady(@NonNull Bitmap resource,
                    @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                callback.onPostExecute();
                callback.onSuccess(resource);
            }
        });
    }

    //https://github.com/halibobo/WaterMark
    public static Bitmap waterMark(Context context, Bitmap bitmap, String str) {
        int destWidth = bitmap.getWidth();
        int destHeight = bitmap.getHeight();
        Bitmap icon = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);

        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);

        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect dst = new Rect(0, 0, destWidth, destHeight);
        canvas.drawBitmap(bitmap, src, dst, photoPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize(DisplayUtils.dp2px(context, 9));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(1);
        textPaint.setAlpha(120);
        textPaint.setColor(Color.WHITE);

        Rect bounds = new Rect();
        textPaint.getTextBounds(str, 0, str.length(), bounds);

        canvas.drawText(str, destWidth - bounds.width() - 20, destHeight - bounds.height() / 2F - 5, textPaint);
        canvas.save();
        canvas.restore();
        return icon;
    }

    @NonNull
    public static Bitmap toStackBlur2(Bitmap original, int radius) {
        return NativeStackBlur.process(original, radius);
    }
}
