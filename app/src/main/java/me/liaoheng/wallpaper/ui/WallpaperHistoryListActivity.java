package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import android.view.View;

import com.github.liaoheng.common.adapter.core.HandleView;
import com.github.liaoheng.common.adapter.core.RecyclerViewHelper;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Callback5;
import com.github.liaoheng.common.util.Utils;
import com.github.liaoheng.common.util.ValidateUtils;

import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import io.reactivex.Observable;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.adapter.WallpaperAdapter;
import me.liaoheng.wallpaper.data.BingWallpaperNetworkClient;
import me.liaoheng.wallpaper.databinding.ActivityWallpaperHistoryListBinding;
import me.liaoheng.wallpaper.model.Wallpaper;
import me.liaoheng.wallpaper.util.CrashReportHandle;

/**
 * 壁纸历史列表
 *
 * @author liaoheng
 * @version 2018-01-31 14:14
 */
public class WallpaperHistoryListActivity extends BaseActivity {

    private RecyclerViewHelper mRecyclerViewHelper;
    private WallpaperAdapter mWallpaperAdapter;
    private ActivityWallpaperHistoryListBinding mViewBinding;
    private int index;
    @SuppressWarnings("FieldCanBeLocal")
    private final int count = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityWallpaperHistoryListBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());
        setTitle(R.string.menu_main_wallpaper_history_list);

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
        getBingWallpaperList(new Callback5.EmptyCallback() {
            @Override
            public void onAllow() {
                getBingWallpaperList(new Callback5.EmptyCallback() {
                    @Override
                    public void onAllow() {
                        mWallpaperAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void getBingWallpaperList(final Callback5 callback) {
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
                    callback.onAllow();
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
        mViewBinding.bingWallpaperListError.setText(error);
    }
}
