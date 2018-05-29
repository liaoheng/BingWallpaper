package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.flyco.systembar.SystemBarHelper;

import net.yslibrary.licenseadapter.LicenseAdapter;
import net.yslibrary.licenseadapter.LicenseEntry;
import net.yslibrary.licenseadapter.Licenses;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;

/**
 * @author liaoheng
 * @version 2017-12-27 22:39
 */
public class LicenseActivity extends BaseActivity {
    @BindView(R.id.license_recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);
        SystemBarHelper
                .tintStatusBar(this, ContextCompat.getColor(this, R.color.colorPrimaryDark), 0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        List<LicenseEntry> licenses = new ArrayList<>();
        licenses.add(Licenses.noContent("Android SDK", "Google Inc.",
                "https://developer.android.com/sdk/terms.html"));
        licenses.add(Licenses.fromGitHubApacheV2("JakeWharton/ButterKnife"));
        licenses.add(Licenses.fromGitHubApacheV2("Google/Gson"));
        licenses.add(Licenses.fromGitHubApacheV2("apache/commons-io"));
        licenses.add(Licenses.fromGitHubApacheV2("dlew/joda-time-android"));
        licenses.add(Licenses.fromGitHubApacheV2("square/okhttp"));
        licenses.add(Licenses.fromGitHubApacheV2("square/retrofit"));
        licenses.add(Licenses.fromGitHubApacheV2("bumptech/glide"));
        licenses.add(Licenses.fromGitHubApacheV2("reactivex/rxjava"));
        licenses.add(Licenses.fromGitHubApacheV2("reactivex/rxandroid"));
        licenses.add(Licenses.fromGitHubApacheV2("trello/rxlifecycle"));
        licenses.add(Licenses.fromGitHubApacheV2("orhanobut/logger"));
        licenses.add(Licenses.fromGitHub("DreaminginCodeZH/SystemUiHelper", Licenses.LICENSE_APACHE_V2));
        licenses.add(
                Licenses.fromGitHubApacheV2("ferrannp/material-preferences"));
        licenses.add(Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter"));
        licenses.add(Licenses.fromGitHubMIT("H07000223/FlycoSystemBar"));
        licenses.add(Licenses.fromGitHubApacheV2("Clans/FloatingActionButton"));
        licenses.add(Licenses.fromGitHubApacheV2("grandcentrix/tray"));

        LicenseAdapter adapter = new LicenseAdapter(licenses);
        mRecyclerView.setAdapter(adapter);

        Licenses.load(licenses);
    }
}
