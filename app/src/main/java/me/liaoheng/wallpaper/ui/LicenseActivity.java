package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import android.text.Html;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.adapter.LicenseAdapter;
import me.liaoheng.wallpaper.databinding.ActivityLicenseBinding;
import me.liaoheng.wallpaper.model.License;
import me.liaoheng.wallpaper.util.GlideApp;

/**
 * @author liaoheng
 * @version 2017-12-27 22:39
 */
public class LicenseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLicenseBinding binding = ActivityLicenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.open_source_license);
        String gpl = "<p> This program is free software: you can redistribute it and or modify"
                + "it under the terms of the GNU General Public License as published by"
                + "the Free Software Foundation, either version 3 of the License, or"
                + "(at your option) any later version.</p>"
                + "<p> This program is distributed in the hope that it will be useful,"
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
                + "GNU General Public License for more details.</p>"
                + "<p> You should have received a copy of the GNU General Public License"
                + "along with this program.  If not, see https://www.gnu.org/licenses.</p>";
        binding.licenseText.setText(Html.fromHtml(gpl));
        GlideApp.with(this).load("https://www.gnu.org/graphics/gplv3-127x51.png").into(binding.licenseImage);

        List<License> licenses = new ArrayList<>();
        licenses.add(new License("Gson", "Google Inc.",
                "https://github.com/google/gson"));
        licenses.add(new License("Guava", "Google Inc.",
                "https://github.com/google/guava"));
        licenses.add(new License("OkHttp", "Square Inc.",
                "https://github.com/square/okhttp"));
        licenses.add(new License("Retrofit", "Square Inc.",
                "https://github.com/square/retrofit"));
        licenses.add(new License("Butter Knife", "JakeWharton",
                "https://github.com/JakeWharton/butterknife"));
        licenses.add(new License("Glide", "bumptech",
                "https://github.com/bumptech/glide", "BSD, part MIT and Apache 2.0"));
        licenses.add(new License("RxJava", "ReactiveX",
                "https://github.com/ReactiveX/RxJava"));
        licenses.add(new License("RxAndroid", "ReactiveX",
                "https://github.com/ReactiveX/RxAndroid"));
        licenses.add(new License("RxLifecycle", "Trello",
                "https://github.com/trello/rxlifecycle"));
        licenses.add(new License("Logger", "Orhan Obut",
                "https://github.com/orhanobut/logger"));
        licenses.add(new License("FloatingActionButton", "Dmytro Tarianyk",
                "https://github.com/Clans/FloatingActionButton"));
        licenses.add(new License("Tray", "grandcentrix GmbH",
                "https://github.com/grandcentrix/tray"));
        licenses.add(new License("AppIntro", "AppIntro",
                "https://github.com/apl-devs/AppIntro"));
        licenses.add(new License("RootBeer", "scottyab",
                "https://github.com/scottyab/rootbeer"));
        licenses.add(new License("joda-time-android", "dlew",
                "https://github.com/dlew/joda-time-android"));
        licenses.add(new License("sentry-java", "getsentry",
                "https://github.com/getsentry/sentry-java", "MIT"));
        licenses.add(new License("subsampling-scale-image-view", "davemorrissey",
                "https://github.com/davemorrissey/subsampling-scale-image-view"));
        licenses.add(new License("Conscrypt", "Google Inc.",
                "https://github.com/google/conscrypt"));

        binding.licenseRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.licenseRecyclerView.setHasFixedSize(true);
        binding.licenseRecyclerView.setAdapter(new LicenseAdapter(this, licenses));
    }
}
