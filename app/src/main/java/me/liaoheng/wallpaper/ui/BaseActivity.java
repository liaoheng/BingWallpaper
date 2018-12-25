package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.UIUtils;
import com.trello.rxlifecycle3.LifecycleProvider;
import com.trello.rxlifecycle3.LifecycleTransformer;
import com.trello.rxlifecycle3.RxLifecycle;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.android.RxLifecycleAndroid;

import androidx.annotation.CallSuper;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * 基础通用 RxJava Lifecycle Activity
 *
 * @author liaoheng
 */
public abstract class BaseActivity extends AppCompatActivity implements LifecycleProvider<ActivityEvent> {
    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

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
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    @NonNull
    @CheckResult
    public final Observable<ActivityEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindActivity(lifecycleSubject);
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        BingWallpaperUtils.setPhoneScreen(this);
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(ActivityEvent.CREATE);
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        lifecycleSubject.onNext(ActivityEvent.START);
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        lifecycleSubject.onNext(ActivityEvent.RESUME);
    }

    @Override
    @CallSuper
    protected void onPause() {
        lifecycleSubject.onNext(ActivityEvent.PAUSE);
        super.onPause();
    }

    @Override
    @CallSuper
    protected void onStop() {
        lifecycleSubject.onNext(ActivityEvent.STOP);
        super.onStop();
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        lifecycleSubject.onNext(ActivityEvent.DESTROY);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
