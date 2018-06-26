package me.liaoheng.wallpaper.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.flyco.systembar.SystemBarHelper;
import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;
import com.github.liaoheng.common.adapter.core.HandleView;
import com.github.liaoheng.common.adapter.core.RecyclerViewHelper;
import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback2;
import com.github.liaoheng.common.util.UIUtils;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.Constants;
import me.liaoheng.wallpaper.util.ExceptionHandle;
import me.liaoheng.wallpaper.util.GlideApp;
import rx.Observable;

/**
 * 壁纸历史列表
 *
 * @author liaoheng
 * @version 2018-01-31 14:14
 */
public class WallpaperHistoryListActivity extends BaseActivity {

    private final String TAG = WallpaperHistoryListActivity.class.getSimpleName();
    RecyclerViewHelper mRecyclerViewHelper;
    WallpaperAdapter mWallpaperAdapter;
    @BindView(R.id.bing_wallpaper_list_error)
    TextView mErrorTextView;
    private int index;
    private int count = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_history_list);
        ButterKnife.bind(this);
        SystemBarHelper
                .tintStatusBar(this, ContextCompat.getColor(this, R.color.colorPrimaryDark), 0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mWallpaperAdapter = new WallpaperAdapter(this);

        mRecyclerViewHelper = new RecyclerViewHelper.Builder(this,
                new GridLayoutManager(this, 3))
                .addLoadMoreFooterView(R.layout.view_wallpaper_list_footer, new HandleView.EmptyHandleView() {
                    @Override
                    public void handle(View view) {
                    }
                })
                .setMergedIntoLineSpanSizeLookup()
                .setAdapter(mWallpaperAdapter).build();
        mRecyclerViewHelper.getRecyclerView().setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
                if (holder instanceof WallpaperViewHolder) {
                    WallpaperViewHolder vh = (WallpaperViewHolder) holder;
                    GlideApp.with(getActivity()).clear(vh.mImageView);
                }
            }
        });
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
        Observable<List<BingWallpaperImage>> listObservable = BingWallpaperNetworkClient.getBingWallpaper(this, index,
                count)
                .compose(this.<List<BingWallpaperImage>>bindToLifecycle());
        Utils.addSubscribe2(listObservable, new Callback2.EmptyCallback<List<BingWallpaperImage>>() {
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
                setBingWallpaperError(e);
            }
        });
    }

    private void setBingWallpaperError(Throwable throwable) {
        String error = ExceptionHandle.loadFailed(this, TAG, throwable);
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

        @Override
        public void onHandle(final BingWallpaperImage item, int position) {
            DateTime dateTime = DateTime.parse(item.getEnddate(), DateTimeFormat.forPattern("YYYYMMdd"));
            mDate.setText(dateTime.toString("MMMM dd", BingWallpaperUtils.getLocale(getContext())));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), WallpaperDetailActivity.class);
                    intent.putExtra("image", item);
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                    mImageView, "bing_wallpaper_detail_image");
                    ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
                }
            });

            String imageUrl = BingWallpaperUtils.getImageUrl(getApplicationContext(),
                    Constants.WallpaperConfig.WALLPAPER_RESOLUTION,
                    item);
            GlideApp.with(getContext())
                    .asDrawable()
                    .thumbnail(0.5f)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .error(R.drawable.lcn_empty_photo)
                    .transition(
                            new DrawableTransitionOptions()
                                    .crossFade())
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
        public WallpaperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflate(R.layout.view_wallpaper_list_item, parent);
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) itemView.getLayoutParams();
            lp.height = (int) (parent.getMeasuredHeight() / 2.5);
            itemView.setLayoutParams(lp);
            return new WallpaperViewHolder(itemView);
        }

        @Override
        public void onBindViewHolderItem(WallpaperViewHolder holder, BingWallpaperImage item,
                int position) {
            holder.onHandle(item, position);
        }
    }
}
