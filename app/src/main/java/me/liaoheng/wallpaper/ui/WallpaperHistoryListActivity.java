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
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;
import com.github.liaoheng.common.adapter.core.HandleView;
import com.github.liaoheng.common.adapter.core.RecyclerViewHelper;
import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;
import io.reactivex.Observable;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.CrashReportHandle;
import me.liaoheng.wallpaper.util.GlideApp;

import java.util.List;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mWallpaperAdapter = new WallpaperAdapter(this);
        RecyclerViewHelper.Builder builder = new RecyclerViewHelper.Builder(this,
                new GridLayoutManager(this, 3))
                .setAdapter(mWallpaperAdapter);
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            builder.setLoadMoreListener(this::getPixabayList).addLoadMoreFooterView();
        } else {
            builder.addLoadMoreFooterView(R.layout.view_wallpaper_list_footer, new HandleView.EmptyHandleView() {
                @Override
                public void handle(View view) {
                }
            });
        }
        mRecyclerViewHelper = builder.setMergedIntoLineSpanSizeLookup().build();

        mRecyclerViewHelper.changeToLoadMoreLoading();
        if (BingWallpaperUtils.isPixabaySupport(this)) {
            index++;
            getPixabayList();
            return;
        }

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

    private void getPixabayList() {
        Observable<List<BingWallpaperImage>> listObservable = BingWallpaperNetworkClient.getPixabays(index)
                .compose(this.bindToLifecycle());
        Utils.addSubscribe(listObservable, new Callback.EmptyCallback<List<BingWallpaperImage>>() {
            @Override
            public void onPreExecute() {
                mRecyclerViewHelper.setLoadMoreLoading(true);
            }

            @Override
            public void onPostExecute() {
                mRecyclerViewHelper.setLoadMoreLoading(false);
            }

            @Override
            public void onSuccess(List<BingWallpaperImage> images) {
                mRecyclerViewHelper.setLoadMoreHasLoadedAllItems(images.size() == 0);
                index++;
                mWallpaperAdapter.addAll(images);
                mWallpaperAdapter.notifyItemRangeInserted(mWallpaperAdapter.getItemCount(), images.size());
            }

            @Override
            public void onError(Throwable e) {
                setBingWallpaperError(e);
            }
        });
    }

    private void getBingWallpaperList(final Callback callback) {
        Observable<List<BingWallpaperImage>> listObservable = BingWallpaperNetworkClient.getBingWallpaper(this, index,
                count).compose(this.bindToLifecycle());
        Utils.addSubscribe(listObservable, new Callback.EmptyCallback<List<BingWallpaperImage>>() {
            @Override
            public void onPreExecute() {
                mRecyclerViewHelper.setLoadMoreLoading(true);
            }

            @Override
            public void onPostExecute() {
                mRecyclerViewHelper.setLoadMoreLoading(false);
            }

            @Override
            public void onSuccess(List<BingWallpaperImage> bingWallpaperImages) {
                if (ValidateUtils.isItemEmpty(bingWallpaperImages)) {
                    return;
                }
                if (mWallpaperAdapter.isEmpty()) {
                    mRecyclerViewHelper.setLoadMoreHasLoadedAllItems(false);
                } else {
                    bingWallpaperImages.remove(0);
                    mRecyclerViewHelper.setLoadMoreHasLoadedAllItems(true);
                }
                mWallpaperAdapter.addAll(bingWallpaperImages);
                index += bingWallpaperImages.size();
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

    public class WallpaperViewHolder extends BaseRecyclerViewHolder<BingWallpaperImage> {

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
        public void onHandle(final BingWallpaperImage item, int position) {
            if (TextUtils.isEmpty(item.getEnddate())) {
                UIUtils.viewGone(mDate);
            } else {
                UIUtils.viewVisible(mDate);
                String endDate = item.getEnddate();// YYYYMMDD
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
            String imageUrl;
            int width = Constants.WallpaperConfig.WALLPAPER_RESOLUTION_WIDTH;
            int height = Constants.WallpaperConfig.WALLPAPER_RESOLUTION_HEIGHT;
            if (BingWallpaperUtils.isPixabaySupport(getContext())) {
                imageUrl = item.getUrlbase();
                width = Target.SIZE_ORIGINAL;
                height = Target.SIZE_ORIGINAL;
            } else {
                imageUrl = BingWallpaperUtils.getImageUrl(getContext(),
                        Constants.WallpaperConfig.WALLPAPER_RESOLUTION,
                        item);
            }
            GlideApp.with(getContext())
                    .asDrawable()
                    .thumbnail(0.3f)
                    .override(width, height)
                    .error(R.drawable.lcn_empty_photo)
                    .load(imageUrl)
                    .into(new ProgressImageViewTarget(mImageView, mProgressBar));
        }
    }

    public class ProgressImageViewTarget extends ImageViewTarget<Drawable> {
        private ProgressBar mProgressBar;
        private ImageView mImageView;

        public ProgressImageViewTarget(ImageView view, ProgressBar mProgressBar) {
            super(view);
            this.mProgressBar = mProgressBar;
            this.mImageView = view;
        }

        @Override
        public void onLoadStarted(@Nullable Drawable placeholder) {
            super.onLoadStarted(placeholder);
            UIUtils.viewVisible(mProgressBar);
        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);
            UIUtils.viewGone(mProgressBar);
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            super.onLoadCleared(placeholder);
            UIUtils.viewGone(mProgressBar);
        }

        @Override
        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
            super.onResourceReady(resource, transition);
            UIUtils.viewGone(mProgressBar);
            mImageView.setImageDrawable(resource);
        }

        @Override
        protected void setResource(@Nullable Drawable resource) {
        }

        @Override
        public void onDestroy() {
            UIUtils.viewGone(mProgressBar);
            super.onDestroy();
        }
    }

    public class WallpaperAdapter extends BaseRecyclerAdapter<BingWallpaperImage, WallpaperViewHolder> {

        public WallpaperAdapter(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = inflate(R.layout.view_wallpaper_list_item, parent);
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
            lp.height = (int) (parent.getMeasuredHeight() / 2.5);
            itemView.setLayoutParams(lp);
            return new WallpaperViewHolder(itemView);
        }

        @Override
        public void onBindViewHolderItem(@NonNull WallpaperViewHolder holder, BingWallpaperImage item,
                int position) {
            holder.onHandle(item, position);
        }
    }
}
