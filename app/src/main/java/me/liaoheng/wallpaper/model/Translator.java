package me.liaoheng.wallpaper.model;

/**
 * @author liaoheng
 * @date 2021-07-22 13:41
 */
public class Translator {
    public Translator(String name, String url) {
        this.name = name;
        this.url = url;
    }

    String name;
    String url;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
