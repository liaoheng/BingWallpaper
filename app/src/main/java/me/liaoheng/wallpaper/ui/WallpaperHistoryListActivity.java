package me.liaoheng.wallpaper.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.flyco.systembar.SystemBarHelper;
import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;
import com.github.liaoheng.common.adapter.core.HandleView;
import com.github.liaoheng.common.adapter.core.RecyclerViewHelper;
import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback2;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;

import java.net.SocketTimeoutException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.model.BingWallpaperImage;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
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
        String error = getString(R.string.network_request_error);
        if (throwable instanceof SocketTimeoutException) {
            error = getString(R.string.connection_timed_out);
        }
        mErrorTextView.setText(error);
        if (throwable == null) {
            L.Log.e(TAG, error);
        } else {
            L.Log.e(TAG, throwable);
        }
    }

    public class WallpaperViewHolder extends BaseRecyclerViewHolder<BingWallpaperImage> {

        @BindView(R.id.bing_wallpaper_list_item_image)
        ImageView mImageView;
        @BindView(R.id.bing_wallpaper_list_item_image_date)
        TextView mDate;

        public WallpaperViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onHandle(final BingWallpaperImage item, int position) {
            mDate.setText(item.getEnddate());

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
            String[] names = getResources()
                    .getStringArray(R.array.pref_set_wallpaper_resolution_name);
            String imageUrl = BingWallpaperUtils.getImageUrl(names[3], item);
            Glide.with(getContext()).load(imageUrl).thumbnail(0.3f).centerCrop().crossFade().into(mImageView);//TODO optimization
        }
    }

    public class WallpaperAdapter extends BaseRecyclerAdapter<BingWallpaperImage, WallpaperViewHolder> {

        public WallpaperAdapter(Context context) {
            super(context);
        }

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
