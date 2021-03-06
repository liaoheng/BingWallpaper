package me.liaoheng.wallpaper.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.liaoheng.common.adapter.base.BaseRecyclerAdapter;
import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;
import me.liaoheng.wallpaper.util.GlideApp;

/**
 * @author liaoheng
 * @version 2017-12-27 22:39
 */
public class LicenseActivity extends BaseActivity {

    class LicenseAdapter extends BaseRecyclerAdapter<License, LicenseViewHolder> {

        public LicenseAdapter(Context context, List<License> list) {
            super(context, list);
        }

        @NonNull
        @Override
        public LicenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new LicenseViewHolder(inflate(R.layout.view_license_list_item, parent));
        }

        @Override
        public void onBindViewHolderItem(@NonNull LicenseViewHolder holder, @Nullable License item, int position) {
            holder.onHandle(item, position);
        }
    }

    class LicenseViewHolder extends BaseRecyclerViewHolder<License> {

        @BindView(R.id.view_license_list_item_name)
        TextView name;
        @BindView(R.id.view_license_list_item_author)
        TextView author;
        @BindView(R.id.view_license_list_item_text)
        TextView text;

        public LicenseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onHandle(@Nullable License item, int position) {
            if (item == null) {
                return;
            }
            itemView.setOnClickListener(v -> BingWallpaperUtils.openBrowser(getContext(), item.url));
            name.setText(item.name);
            author.setText(item.author);
            text.setText(item.text);
        }
    }

    static class License {
        public License(String name, String author, String url) {
            this.name = name;
            this.author = author;
            this.url = url;
        }

        public License(String name, String author, String url, String text) {
            this.name = name;
            this.author = author;
            this.url = url;
            this.text = text;
        }

        String name;
        String author;
        String url;
        String text = "Apache License 2.0";
    }

    @BindView(R.id.license_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.license_text)
    TextView license;
    @BindView(R.id.license_image)
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        setTitle(R.string.open_source_license);
        ButterKnife.bind(this);
        String gpl= "<p> This program is free software: you can redistribute it and or modify"
                + "it under the terms of the GNU General Public License as published by"
                + "the Free Software Foundation, either version 3 of the License, or"
                + "(at your option) any later version.</p>"
                + "<p> This program is distributed in the hope that it will be useful,"
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of"
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
                + "GNU General Public License for more details.</p>"
                + "<p> You should have received a copy of the GNU General Public License"
                + "along with this program.  If not, see https://www.gnu.org/licenses.</p>";
        license.setText(Html.fromHtml(gpl));
        GlideApp.with(this).load("https://www.gnu.org/graphics/gplv3-127x51.png").into(image);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
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
                "https://github.com/getsentry/sentry-java","MIT"));

        mRecyclerView.setAdapter(new LicenseAdapter(this, licenses));
    }
}
