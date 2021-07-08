package me.liaoheng.wallpaper.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.liaoheng.common.adapter.holder.BaseRecyclerViewHolder;
import com.github.liaoheng.common.adapter.model.Group;
import com.github.liaoheng.common.util.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.liaoheng.wallpaper.R;
import me.liaoheng.wallpaper.util.BaseGroupRecyclerAdapter;
import me.liaoheng.wallpaper.util.BingWallpaperUtils;

/**
 * @author liaoheng
 * @date 2021-07-01 10:32
 */
public class TranslatorActivity extends BaseActivity {
    class TranslatorAdapter extends BaseGroupRecyclerAdapter<Translator> {

        public TranslatorAdapter(Context context, List<Group<Translator>> list) {
            super(context, list);
        }

        @Override
        public BaseRecyclerViewHolder<Group<Translator>> onCreateGroupHeaderViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(getContext());
            textView.setTextSize(DisplayUtils.sp2px(getContext(), 18));
            int border = DisplayUtils.dp2px(getContext(), 3);
            textView.setPadding(border, border, border, border);
            return new TranslatorLanguageViewHolder(textView);
        }

        @Override
        public BaseRecyclerViewHolder<Group<Translator>> onCreateGroupFooterViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public BaseRecyclerViewHolder<Group<Translator>> onCreateGroupContentViewHolder(ViewGroup parent,
                int viewType) {
            return new TranslatorViewHolder(inflate(R.layout.view_translator_list_item, parent));
        }
    }

    class TranslatorLanguageViewHolder extends BaseRecyclerViewHolder<Group<Translator>> {

        public TranslatorLanguageViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onHandle(@Nullable Group<Translator> item, int position) {
            if (item == null) {
                return;
            }
            ((TextView) itemView).setText(item.getText());
        }
    }

    class TranslatorViewHolder extends BaseRecyclerViewHolder<Group<Translator>> {

        @BindView(R.id.translator_list_name)
        TextView name;

        public TranslatorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onHandle(@Nullable Group<Translator> item, int position) {
            if (item == null || item.getContent() == null) {
                return;
            }
            itemView.setOnClickListener(v -> BingWallpaperUtils.openBrowser(getContext(), item.getContent().url));
            name.setText(item.getContent().name);
        }
    }

    static class Translator {
        public Translator(String name, String url) {
            this.name = name;
            this.url = url;
        }

        String name;
        String url;
    }

    @BindView(R.id.translator_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.translator_desc)
    TextView desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);
        ButterKnife.bind(this);
        String text = "if you want to help translation the app, "
                + "please click: https://crowdin.com/project/starth-bing-wallpaper";
        desc.setText(Html.fromHtml(text));

        List<Group<Translator>> translators = new ArrayList<>();

        translators.add(new Group<>(Group.GroupType.HEADER, "Polski(Polish)"));
        addTranslator(translators, "@dekar16", "https://crowdin.com/profile/dekar16");

        translators.add(new Group<>(Group.GroupType.HEADER, "Русский(Russian)"));
        addTranslator(translators, "@tullev(Lev Tulubjev)", "https://crowdin.com/profile/tullev");
        addTranslator(translators, "@FanHamMer(Oleg Popenkov)", "https://crowdin.com/profile/FanHamMer");

        translators.add(new Group<>(Group.GroupType.HEADER, "Čeština(Czech)"));
        addTranslator(translators, "@foreteller", "https://crowdin.com/profile/foreteller");

        translators.add(new Group<>(Group.GroupType.HEADER, "Slovenčina(Slovak)"));
        addTranslator(translators, "@foreteller", "https://crowdin.com/profile/foreteller");

        translators.add(new Group<>(Group.GroupType.HEADER, "Deutsch(German)"));
        addTranslator(translators, "@Bergradler", "https://crowdin.com/profile/Bergradler");

        translators.add(new Group<>(Group.GroupType.HEADER, "Nederlands(Dutch)"));
        addTranslator(translators, "@5qx9Pe7Lvj8Fn7zg(Jasper)", "https://crowdin.com/profile/5qx9Pe7Lvj8Fn7zg");

        translators.add(new Group<>(Group.GroupType.HEADER, "Français(French)"));
        addTranslator(translators, "@Faux-ami(Nicolas)", "https://crowdin.com/profile/Faux-ami");

        translators.add(new Group<>(Group.GroupType.HEADER, "български(Bulgarian)"));
        addTranslator(translators, "@trifon71(Trifon Ribnishki)", "https://crowdin.com/profile/trifon71");

        translators.add(new Group<>(Group.GroupType.HEADER, "日本語(Japanese)"));
        addTranslator(translators, "@Rintan", "https://crowdin.com/profile/rintan");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new TranslatorAdapter(this, translators));

    }

    private void addTranslator(List<Group<Translator>> translators, String name, String url) {
        translators.add(new Group<>(new Translator(name, url)));
    }

}
