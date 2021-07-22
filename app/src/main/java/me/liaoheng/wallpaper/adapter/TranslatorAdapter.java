package me.liaoheng.wallpaper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.adapter.model.Group;

import java.util.List;

import me.liaoheng.wallpaper.databinding.ViewTranslatorListItemBinding;
import me.liaoheng.wallpaper.databinding.ViewTranslatorListItemHeadBinding;
import me.liaoheng.wallpaper.model.Translator;
import me.liaoheng.wallpaper.util.BaseGroupRecyclerAdapter;

/**
 * @author liaoheng
 * @date 2021-07-22 13:43
 */
public class TranslatorAdapter extends BaseGroupRecyclerAdapter<Translator> {

    public TranslatorAdapter(Context context, List<Group<Translator>> list) {
        super(context, list);
    }

    @Override
    public BaseRecyclerViewHolder<Group<Translator>> onCreateGroupHeaderViewHolder(ViewGroup parent, int viewType) {
        return new TranslatorLanguageViewHolder(
                ViewTranslatorListItemHeadBinding.inflate(LayoutInflater.from(getContext()), parent, false));
    }

    @Override
    public BaseRecyclerViewHolder<Group<Translator>> onCreateGroupFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public BaseRecyclerViewHolder<Group<Translator>> onCreateGroupContentViewHolder(ViewGroup parent,
            int viewType) {
        return new TranslatorViewHolder(
                ViewTranslatorListItemBinding.inflate(LayoutInflater.from(getContext()), parent, false));
    }
}
