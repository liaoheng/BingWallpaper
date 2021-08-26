package me.liaoheng.wallpaper.ui;

import android.os.Bundle;
import android.text.Html;

import com.github.liaoheng.common.adapter.model.Group;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.liaoheng.wallpaper.adapter.TranslatorAdapter;
import me.liaoheng.wallpaper.databinding.ActivityTranslatorBinding;
import me.liaoheng.wallpaper.model.Translator;

/**
 * @author liaoheng
 * @date 2021-07-01 10:32
 */
public class TranslatorActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTranslatorBinding binding = ActivityTranslatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String text = "if you want to help translation the app, "
                + "please click: https://crowdin.com/project/starth-bing-wallpaper";
        binding.translatorDesc.setText(Html.fromHtml(text));

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

        translators.add(new Group<>(Group.GroupType.HEADER, "Italiano(Italian)"));
        addTranslator(translators, "@afe", "https://crowdin.com/profile/afe");
        addTranslator(translators, "@enrico-sorio(Enrico Sorio)", "https://crowdin.com/profile/enrico-sorio");

        translators.add(new Group<>(Group.GroupType.HEADER, "Español(Spanish)"));
        addTranslator(translators, "@OCReactive", "https://crowdin.com/profile/ocreactive");

        binding.translatorRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.translatorRecyclerView.setHasFixedSize(true);
        binding.translatorRecyclerView.setAdapter(new TranslatorAdapter(this, translators));
    }

    private void addTranslator(List<Group<Translator>> translators, String name, String url) {
        translators.add(new Group<>(new Translator(name, url)));
    }

}
