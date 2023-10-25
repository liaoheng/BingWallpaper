package me.liaoheng.wallpaper.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.CallSuper;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;

import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle4.LifecycleProvider;
import com.trello.rxlifecycle4.LifecycleTransformer;

import io.reactivex.rxjava3.core.Observable;
import me.liaoheng.wallpaper.R;

/**
 * 基础通用 RxJava Lifecycle Activity
 *
 * @author liaoheng
 */
public abstract class BaseActivity extends AppCompatActivity implements LifecycleProvider<Lifecycle.Event> {
    private final LifecycleProvider<Lifecycle.Event> mLifecycleProvider = AndroidLifecycle.createLifecycleProvider(
            this);

    protected final String TAG = this.getClass().getSimpleName();

    public BaseActivity getActivity() {
        return this;
    }

    protected Toolbar mToolbar;

    protected void initStatusBarAddToolbar() {
        mToolbar = UIUtils.findViewById(this, R.id.toolbar);
        int statusBarHeight = DisplayUtils.getStatusBarHeight(this);
        ViewGroup.LayoutParams lp = mToolbar.getLayoutParams();
        lp.height += statusBarHeight;
        mToolbar.setPadding(mToolbar.getPaddingLeft(), mToolbar.getPaddingTop() + statusBarHeight,
                mToolbar.getPaddingRight(), mToolbar.getPaddingBottom());
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
    }

    protected void initTranslucent() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    @NonNull
    @CheckResult
    public final Observable<Lifecycle.Event> lifecycle() {
        return mLifecycleProvider.lifecycle();
    }

    @NonNull
    @Override
    public <T> LifecycleTransformer<T> bindUntilEvent(@NonNull Lifecycle.Event event) {
        return mLifecycleProvider.bindUntilEvent(event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return mLifecycleProvider.bindToLifecycle();
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
