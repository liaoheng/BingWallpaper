package me.liaoheng.wallpaper.util;

import android.content.Context;
import android.view.ViewGroup;

import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;
import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.adapter.model.Group;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author liaoheng
 * @version 2017-01-25 09:42
 */
public abstract class BaseGroupRecyclerAdapter<K>
        extends BaseRecyclerAdapter<Group<K>, BaseRecyclerViewHolder<Group<K>>> {

    public BaseGroupRecyclerAdapter(Context context) {
        super(context);
    }

    public BaseGroupRecyclerAdapter(Context context, List<Group<K>> list) {
        super(context, list);
    }

    @Override
    public int getItemViewType(int position) {
        return getList().get(position).getType().getCode();
    }

    @NonNull
    @Override
    public BaseRecyclerViewHolder<Group<K>> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Group.GroupType.HEADER.getCode()) {
            return onCreateGroupHeaderViewHolder(parent, viewType);
        } else if (viewType == Group.GroupType.FOOTER.getCode()) {
            return onCreateGroupFooterViewHolder(parent, viewType);
        } else {
            return onCreateGroupContentViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRecyclerViewHolder<Group<K>> holder, int position) {
        Group<K> item = getList().get(position);
        if (Group.GroupType.CONTENT.equals(item.getType())) {
            initOnItemClick(item, holder.itemView, position);
            initOnItemLongClick(item, holder.itemView, position);
        }
        onBindViewHolderItem(holder, item, position);
    }

    @Override
    public void onBindViewHolderItem(@NonNull BaseRecyclerViewHolder<Group<K>> holder, @Nullable Group<K> item,
            int position) {
        holder.onHandle(item, position);
    }

    public abstract BaseRecyclerViewHolder<Group<K>> onCreateGroupHeaderViewHolder(ViewGroup parent,
            int viewType);

    public abstract BaseRecyclerViewHolder<Group<K>> onCreateGroupFooterViewHolder(ViewGroup parent,
            int viewType);

    public abstract BaseRecyclerViewHolder<Group<K>> onCreateGroupContentViewHolder(ViewGroup parent,
            int viewType);
}


