package me.liaoheng.wallpaper.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;
import com.github.liaoheng.common.adapter.core.HandleView;
import com.github.liaoheng.common.adapter.core.RecyclerViewHelper;
import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.GlideApp;
import me.liaoheng.wallpaper.util.WallpaperUtils;

/**
 * 壁纸历史列表
 *
 * @author liaoheng
 * @version 2018-01-31 14:14
 */
public class WallpaperHistoryListActivity extends BaseActivity {

    private RecyclerViewHelper mRecyclerViewHelper;
    private WallpaperAdapter mWallpaperAdapter;
    @BindView(R.id.bing_wallpaper_list_error)
    TextView mErrorTextView;
    private int index;
    private int count = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_history_list);
        setTitle(R.string.menu_main_wallpaper_history_list);
        ButterKnife.bind(this);

        mWallpaperAdapter = new WallpaperAdapter(this);
        RecyclerViewHelper.Builder builder = new RecyclerViewHelper.Builder(this,
                new GridLayoutManager(this, 3))
                .setAdapter(mWallpaperAdapter);
        builder.addLoadMoreFooterView(R.layout.view_wallpaper_list_footer, new HandleView.EmptyHandleView() {
            @Override
            public void handle(View view) {
            }
        });
        mRecyclerViewHelper = builder.setMergedIntoLineSpanSizeLookup().build();

        mRecyclerViewHelper.changeToLoadMoreLoading();

        getBingWallpaperList(new Callback.EmptyCallback() {
            @Override
            public void onFinish() {
                getBingWallpaperList(new EmptyCallback() {
                    @Override
                    public void onFinish() {
                        mWallpaperAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void getBingWallpaperList(final Callback callback) {
        Observable<List<Wallpaper>> listObservable = BingWallpaperNetworkClient.getBingWallpaper(this, index,
                count).compose(this.bindToLifecycle());
        Utils.addSubscribe(listObservable, new Callback.EmptyCallback<List<Wallpaper>>() {
            @Override
            public void onPreExecute() {
                mRecyclerViewHelper.setLoadMoreLoading(true);
            }

            @Override
            public void onPostExecute() {
                mRecyclerViewHelper.setLoadMoreLoading(false);
            }

            @Override
            public void onSuccess(List<Wallpaper> wallpapers) {
                if (ValidateUtils.isItemEmpty(wallpapers)) {
                    return;
                }
                if (mWallpaperAdapter.isEmpty()) {
                    mRecyclerViewHelper.setLoadMoreHasLoadedAllItems(false);
                } else {
                    wallpapers.remove(0);
                    mRecyclerViewHelper.setLoadMoreHasLoadedAllItems(true);
                }
                mWallpaperAdapter.addAll(wallpapers);
                index += wallpapers.size();
                if (callback != null) {
                    callback.onFinish();
                }
            }

            @Override
            public void onError(Throwable e) {
                mRecyclerViewHelper.changeToLoadMoreComplete();
                setBingWallpaperError(e);
            }
        });
    }

    private void setBingWallpaperError(Throwable throwable) {
        String error = CrashReportHandle.loadFailed(this, TAG, throwable);
        mErrorTextView.setText(error);
    }

    public class WallpaperViewHolder extends BaseRecyclerViewHolder<Wallpaper> {

        @BindView(R.id.bing_wallpaper_list_item_image)
        ImageView mImageView;
        @BindView(R.id.bing_wallpaper_list_item_image_date)
        TextView mDate;
        @BindView(R.id.bing_wallpaper_list_item_loading)
        ProgressBar mProgressBar;

        public WallpaperViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onHandle(final Wallpaper item, int position) {
            if (TextUtils.isEmpty(item.getDateTime())) {
                UIUtils.viewGone(mDate);
            } else {
                UIUtils.viewVisible(mDate);
                String endDate = item.getDateTime();// YYYYMMDD
                try {
                    String m = endDate.substring(4, 6);
                    String d = endDate.substring(6, 8);
                    mDate.setText(m + "/" + d);
                } catch (Exception e) {
                    mDate.setText(endDate);
                }
            }

            itemView.setOnClickListener(v -> {
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                mImageView, "bing_wallpaper_detail_image");
                WallpaperDetailActivity.start(getActivity(), item, options.toBundle());
            });
            int width = Constants.WallpaperConfig.WALLPAPER_RESOLUTION_WIDTH;
            int height = Constants.WallpaperConfig.WALLPAPER_RESOLUTION_HEIGHT;
            String imageUrl = BingWallpaperUtils.getImageUrl(getContext(),
                    Constants.WallpaperConfig.WALLPAPER_RESOLUTION,
                    item.getBaseUrl());
            WallpaperUtils.loadImage(GlideApp.with(getContext())
                    .asDrawable()
                    .thumbnail(0.3f)
                    .override(width, height)
                    .error(R.drawable.lcn_empty_photo)
                    .load(imageUrl), mImageView, new Callback.EmptyCallback<Drawable>() {
                @Override
                public void onError(Throwable e) {
                    UIUtils.viewGone(mProgressBar);
                }

                @Override
                public void onPreExecute() {
                    UIUtils.viewVisible(mProgressBar);
                }

                @Override
                public void onSuccess(Drawable resource) {
                    mImageView.setImageDrawable(resource);
                }

                @Override
                public void onFinish() {
                    UIUtils.viewGone(mProgressBar);
                }
            });
        }
    }

    public class WallpaperAdapter extends BaseRecyclerAdapter<Wallpaper, WallpaperViewHolder> {

        public WallpaperAdapter(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = inflate(R.layout.view_wallpaper_list_item, parent);
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
            lp.height = parent.getMeasuredHeight() / 3;
            itemView.setLayoutParams(lp);
            return new WallpaperViewHolder(itemView);
        }

        @Override
        public void onBindViewHolderItem(@NonNull WallpaperViewHolder holder, Wallpaper item,
                int position) {
            holder.onHandle(item, position);
        }
    }
}
