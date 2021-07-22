package me.liaoheng.wallpaper.adapter;

import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.adapter.model.Group;

import androidx.annotation.Nullable;
import me.liaoheng.wallpaper.databinding.ViewTranslatorListItemBinding;
import me.liaoheng.wallpaper.model.Translator;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * @author liaoheng
 * @date 2021-07-22 13:43
 */
public class TranslatorViewHolder extends BaseRecyclerViewHolder<Group<Translator>> {

    private final ViewTranslatorListItemBinding mViewBinding;

    public TranslatorViewHolder(ViewTranslatorListItemBinding binding) {
        super(binding.getRoot());
        mViewBinding = binding;
    }

    @Override
    public void onHandle(@Nullable Group<Translator> item, int position) {
        if (item == null || item.getContent() == null) {
            return;
        }
        itemView.setOnClickListener(v -> BingWallpaperUtils.openBrowser(getContext(), item.getContent().getUrl()));
        mViewBinding.translatorListName.setText(item.getContent().getName());
    }
}
