package me.liaoheng.wallpaper.adapter;

import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;

import androidx.annotation.Nullable;
import me.liaoheng.wallpaper.databinding.ViewLicenseListItemBinding;
import me.liaoheng.wallpaper.model.License;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * @author liaoheng
 * @date 2021-07-22 13:39
 */
public class LicenseViewHolder extends BaseRecyclerViewHolder<License> {

    private final ViewLicenseListItemBinding mViewBinding;

    public LicenseViewHolder(ViewLicenseListItemBinding binding) {
        super(binding.getRoot());
        mViewBinding = binding;
    }

    @Override
    public void onHandle(@Nullable License item, int position) {
        if (item == null) {
            return;
        }
        itemView.setOnClickListener(v -> BingWallpaperUtils.openBrowser(getContext(), item.getUrl()));
        mViewBinding.viewLicenseListItemName.setText(item.getName());
        mViewBinding.viewLicenseListItemAuthor.setText(item.getAuthor());
        mViewBinding.viewLicenseListItemText.setText(item.getText());
    }
}