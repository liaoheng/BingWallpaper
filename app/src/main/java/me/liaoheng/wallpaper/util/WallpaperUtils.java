package me.liaoheng.wallpaper.util;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
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
import android.os.Build;
import android.view.SurfaceHolder;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.commit451.nativestackblur.NativeStackBlur;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.liaoheng.common.util.BitmapUtils;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.FileUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.model.Config;
import me.liaoheng.wallpaper.model.Wallpaper;
import org.jetbrains.annotations.NotNull;

/**
 * @author liaoheng
 * @version 2020-07-01 13:40
 */
public class WallpaperUtils {

    public static boolean isNotSupportedWallpaper(Context context) {
        try {
            WallpaperManager manager = WallpaperManager.getInstance(context);
            if (manager == null) {
                Toast.makeText(context, "This device not support wallpaper", Toast.LENGTH_LONG).show();
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!manager.isWallpaperSupported()) {
                    Toast.makeText(context, "This device not support wallpaper", Toast.LENGTH_LONG).show();
                    return true;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!manager.isSetWallpaperAllowed()) {
                    Toast.makeText(context, "This device not support set wallpaper", Toast.LENGTH_LONG).show();
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

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
                stackBlurFile = CacheUtils.get().put(key, BitmapUtils.bitmapToStream(bitmap,
                        Bitmap.CompressFormat.JPEG));
                bitmap.recycle();
            }
            return stackBlurFile;
        } else {
            return wallpaper;
        }
    }

    public static File getImageWaterMarkFile(@NonNull Context context, File wallpaper, String str) {
        String key = BingWallpaperUtils.createKey(wallpaper.getAbsolutePath() + "_mark_" + str);
        File mark = CacheUtils.get().get(key);
        if (mark == null) {
            Bitmap bitmap = waterMark(context, BitmapFactory.decodeFile(wallpaper.getAbsolutePath()), str);
            mark = CacheUtils.get().put(key, BitmapUtils.bitmapToStream(bitmap,
                    Bitmap.CompressFormat.JPEG));
            bitmap.recycle();
        }
        return mark;
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
                Intent share = new ShareCompat.IntentBuilder(context)
                        .setType("image/jpeg")
                        .setText(BingWallpaperUtils.getName(url))
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

    public static void loadImage(GlideRequest<File> request, SubsamplingScaleImageView imageView,
            Callback<File> callback) {
        request.addListener(new RequestListener<File>() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target,
                    boolean isFirstResource) {
                if (callback != null) {
                    callback.onPostExecute();
                    callback.onError(e);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(File resource, Object model, Target<File> target,
                    DataSource dataSource,
                    boolean isFirstResource) {
                return false;
            }
        }).into(new CustomViewTarget<SubsamplingScaleImageView, File>(imageView) {
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
            }

            @Override
            public void onResourceReady(@NonNull @NotNull File resource,
                    @Nullable Transition<? super File> transition) {
                if (callback != null) {
                    callback.onPostExecute();
                    callback.onSuccess(resource);
                } else {
                    view.setImage(ImageSource.uri(Uri.fromFile(resource)));
                }
            }

            @Override
            protected void onResourceCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onResourceLoading(Drawable placeholder) {
                super.onResourceLoading(placeholder);
                if (callback != null) {
                    callback.onPreExecute();
                }
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

    public static void drawSurfaceHolder(SurfaceHolder holder, Consumer<Canvas> callback) {
        if (!holder.getSurface().isValid()) {
            return;
        }
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                callback.accept(canvas);
            }
        } catch (Throwable ignored) {
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public static void drawText(Canvas canvas, String text, int textSize, int width, int height) {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(1);
        textPaint.setAlpha(120);
        textPaint.setColor(Color.WHITE);

        canvas.drawText(text, width / 2F - textPaint.measureText(text) / 2, height / 2F, textPaint);
    }

}
