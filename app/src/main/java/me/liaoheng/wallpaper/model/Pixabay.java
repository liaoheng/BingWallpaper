package me.liaoheng.wallpaper.model;

import java.util.List;

/**
 * @author liaoheng
 * @version 2019-01-10 11:17
 */
public class Pixabay {
    private long totalHits;
    private long total;
    private List<PixabayImage> hits;

    public long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<PixabayImage> getHits() {
        return hits;
    }

    public void setHits(List<PixabayImage> hits) {
        this.hits = hits;
    }
}
