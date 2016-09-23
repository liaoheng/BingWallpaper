package me.liaoheng.bingwallpaper.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.liaoheng.common.util.AppUtils;
import com.github.liaoheng.common.util.L;
import com.github.liaoheng.common.util.SystemException;
import com.github.liaoheng.common.util.UIUtils;
import java.util.ArrayList;
import java.util.List;
import me.liaoheng.bingwallpaper.R;
import net.yslibrary.licenseadapter.LicenseAdapter;
import net.yslibrary.licenseadapter.LicenseEntry;
import net.yslibrary.licenseadapter.Licenses;

/**
 * About
 * @author liaoheng
 * @version 2016-06-25 23:24
 */
public class AboutActivity extends BaseActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();

    public static void start(Context context) {
        UIUtils.startActivity(context, AboutActivity.class);
    }

    @BindView(R.id.about_version)       TextView     version;
    @BindView(R.id.about_recycler_view) RecyclerView mRecyclerView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        try {
            version.setText(AppUtils.getVersionInfo(getApplication()).versionName);
        } catch (SystemException e) {
            L.getToast().e(TAG, getApplicationContext(), e);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        List<LicenseEntry> licenses = new ArrayList<>();

        licenses.add(Licenses.noContent("Android SDK", "Google Inc.",
                "https://developer.android.com/sdk/terms.html"));

        licenses.add(Licenses.fromGitHub("square/okhttp", Licenses.FILE_TXT));
        licenses.add(Licenses.fromGitHub("square/retrofit", Licenses.FILE_TXT));
        licenses.add(Licenses.fromGitHub("bumptech/glide", Licenses.FILE_NO_EXTENSION));
        licenses.add(Licenses.fromGitHub("reactivex/rxjava", Licenses.FILE_NO_EXTENSION));
        licenses.add(Licenses.fromGitHub("reactivex/rxandroid", Licenses.FILE_NO_EXTENSION));
        licenses.add(Licenses.fromGitHub("trello/rxlifecycle", Licenses.FILE_NO_EXTENSION));
        licenses.add(Licenses.fromGitHub("JakeWharton/butterknife", Licenses.FILE_TXT));
        licenses.add(Licenses.fromGitHub("orhanobut/logger", Licenses.FILE_NO_EXTENSION));
        licenses.add(
                Licenses.fromGitHub("DreaminginCodeZH/SystemUiHelper", Licenses.LICENSE_APACHE_V2));
        licenses.add(Licenses.fromGitHub("jonfinerty/Once", Licenses.FILE_TXT));
        licenses.add(
                Licenses.fromGitHub("ferrannp/material-preferences", Licenses.FILE_NO_EXTENSION));
        licenses.add(Licenses.fromGitHub("yshrsmz/LicenseAdapter", Licenses.FILE_NO_EXTENSION));

        licenses.add(Licenses.fromGitHub("FasterXML/jackson", Licenses.LICENSE_APACHE_V2));
        licenses.add(Licenses.fromGitHub("apache/commons-io", Licenses.FILE_TXT));
        licenses.add(Licenses.fromGitHub("dlew/joda-time-android", Licenses.FILE_NO_EXTENSION));

        LicenseAdapter adapter = new LicenseAdapter(licenses);
        mRecyclerView.setAdapter(adapter);

        Licenses.load(licenses);
    }

    @OnClick(R.id.about_icon) void open() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/liaoheng/BingWallpaper"));
        startActivity(intent);
    }
}
