package me.liaoheng.wallpaper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import me.liaoheng.wallpaper.databinding.ViewWallpaperListItemBinding;
import me.liaoheng.wallpaper.model.Wallpaper;

/**
 * @author liaoheng
 * @date 2021-07-22 13:53
 */
public class WallpaperAdapter extends BaseRecyclerAdapter<Wallpaper, WallpaperViewHolder> {

    public WallpaperAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewWallpaperListItemBinding binding = ViewWallpaperListItemBinding.inflate(
                LayoutInflater.from(getContext()), parent, false);
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) binding.getRoot().getLayoutParams();
        lp.height = parent.getMeasuredHeight() / 3;
        binding.getRoot().setLayoutParams(lp);
        return new WallpaperViewHolder(binding);
    }

    @Override
    public void onBindViewHolderItem(@NonNull WallpaperViewHolder holder, Wallpaper item,
            int position) {
        holder.onHandle(item, position);
    }
}
