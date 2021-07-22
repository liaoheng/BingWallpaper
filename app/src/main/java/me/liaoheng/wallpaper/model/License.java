package me.liaoheng.wallpaper.model;

/**
 * @author liaoheng
 * @date 2021-07-22 13:38
 */
public class License {
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

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }
}
