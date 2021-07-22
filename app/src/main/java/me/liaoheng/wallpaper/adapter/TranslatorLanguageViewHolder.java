package me.liaoheng.wallpaper.adapter;

import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.adapter.model.Group;

import androidx.annotation.Nullable;
import me.liaoheng.wallpaper.databinding.ViewTranslatorListItemHeadBinding;
import me.liaoheng.wallpaper.model.Translator;

/**
 * @author liaoheng
 * @date 2021-07-22 13:42
 */
public class TranslatorLanguageViewHolder extends BaseRecyclerViewHolder<Group<Translator>> {
    private final ViewTranslatorListItemHeadBinding mViewBinding;

    public TranslatorLanguageViewHolder(ViewTranslatorListItemHeadBinding binding) {
        super(binding.getRoot());
        mViewBinding = binding;
    }

    @Override
    public void onHandle(@Nullable Group<Translator> item, int position) {
        if (item == null) {
            return;
        }
        mViewBinding.translatorListTitle.setText(item.getText());
    }
}
