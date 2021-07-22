package me.liaoheng.wallpaper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.liaoheng.wallpaper.databinding.ViewLicenseListItemBinding;
import me.liaoheng.wallpaper.model.License;

/**
 * @author liaoheng
 * @date 2021-07-22 13:38
 */
public class LicenseAdapter extends BaseRecyclerAdapter<License, LicenseViewHolder> {

    public LicenseAdapter(Context context, List<License> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public LicenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LicenseViewHolder(ViewLicenseListItemBinding.inflate(LayoutInflater.from(getContext()),
                parent, false));
    }

    @Override
    public void onBindViewHolderItem(@NonNull LicenseViewHolder holder, @Nullable
            License item, int position) {
        holder.onHandle(item, position);
    }
}
