package me.liaoheng.wallpaper.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.bumptech.glide.request.target.DrawableThumbnailImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.util.UIUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.databinding.ViewWallpaperListItemBinding;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.ui.WallpaperDetailActivity;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.GlideApp;

/**
 * @author liaoheng
 * @date 2021-07-22 13:53
 */
public class WallpaperViewHolder extends BaseRecyclerViewHolder<Wallpaper> {

    private final ViewWallpaperListItemBinding mViewBinding;

    public WallpaperViewHolder(ViewWallpaperListItemBinding binding) {
        super(binding.getRoot());
        mViewBinding = binding;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onHandle(final Wallpaper item, int position) {
        if (item == null) {
            return;
        }
        if (TextUtils.isEmpty(item.getDateTime())) {
            UIUtils.viewGone(mViewBinding.bingWallpaperListItemImageDate);
        } else {
            UIUtils.viewVisible(mViewBinding.bingWallpaperListItemImageDate);
            String endDate = item.getDateTime();// YYYYMMDD
            try {
                String m = endDate.substring(4, 6);
                String d = endDate.substring(6, 8);
                mViewBinding.bingWallpaperListItemImageDate.setText(m + "/" + d);
            } catch (Exception e) {
                mViewBinding.bingWallpaperListItemImageDate.setText(endDate);
            }
        }

        itemView.setOnClickListener(v -> {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(),
                            mViewBinding.bingWallpaperListItemImage, "bing_wallpaper_detail_image");
            WallpaperDetailActivity.start(getContext(), item, options.toBundle());
        });
        int width = Constants.WallpaperConfig.WALLPAPER_RESOLUTION_WIDTH;
        int height = Constants.WallpaperConfig.WALLPAPER_RESOLUTION_HEIGHT;
        String imageUrl = BingWallpaperUtils.getImageUrl(getContext(),
                Constants.WallpaperConfig.WALLPAPER_RESOLUTION,
                item.getBaseUrl());

        GlideApp.with(getContext())
                .asDrawable()
                .thumbnail(0.3f)
                .override(width, height)
                .error(R.drawable.lcn_empty_photo)
                .load(imageUrl).into(new DrawableThumbnailImageViewTarget(mViewBinding.bingWallpaperListItemImage) {
            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                super.onLoadStarted(placeholder);
                UIUtils.viewVisible(mViewBinding.bingWallpaperListItemLoading);
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource,
                    @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                UIUtils.viewGone(mViewBinding.bingWallpaperListItemLoading);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                UIUtils.viewGone(mViewBinding.bingWallpaperListItemLoading);
            }
        });
    }
}
