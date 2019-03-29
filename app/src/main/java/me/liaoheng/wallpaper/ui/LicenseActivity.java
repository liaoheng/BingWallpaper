package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import net.yslibrary.licenseadapter.Library;
import net.yslibrary.licenseadapter.LicenseAdapter;
import net.yslibrary.licenseadapter.Licenses;

import java.util.ArrayList;
import java.util.List;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        List<Library> licenses = new ArrayList<>();
        licenses.add(Licenses.noContent("Android SDK", "Google Inc.",
                "https://developer.android.com/sdk/terms.html"));
        licenses.add(Licenses.fromGitHubApacheV2("JakeWharton/ButterKnife"));
        licenses.add(Licenses.fromGitHubApacheV2("Google/Gson"));
        licenses.add(Licenses.fromGitHubApacheV2("apache/commons-io"));
        licenses.add(Licenses.fromGitHubApacheV2("dlew/joda-time-android"));
        licenses.add(Licenses.fromGitHubApacheV2("square/okhttp"));
        licenses.add(Licenses.fromGitHubApacheV2("square/retrofit"));
        licenses.add(Licenses.fromGitHubBSD("bumptech/glide"));
        licenses.add(Licenses.fromGitHubApacheV2("ReactiveX/RxAndroid", "2.x/" + Licenses.FILE_AUTO));
        licenses.add(Licenses.fromGitHubApacheV2("ReactiveX/RxJava", "2.x/" + Licenses.FILE_AUTO));
        licenses.add(Licenses.fromGitHubApacheV2("trello/rxlifecycle"));
        licenses.add(Licenses.fromGitHubApacheV2("orhanobut/logger"));
        licenses.add(Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter"));
        licenses.add(Licenses.fromGitHubApacheV2("Clans/FloatingActionButton"));
        licenses.add(Licenses.fromGitHubApacheV2("grandcentrix/tray"));
        licenses.add(Licenses.fromGitHubApacheV2("apl-devs/AppIntro"));

        mRecyclerView.setAdapter(new LicenseAdapter(licenses));
    }
}
