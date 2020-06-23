package me.liaoheng.wallpaper.model;

/**
 * @author liaoheng
 * @version 2019-01-10 11:17
 */
public class PixabayImage {

    private String largeImageURL;
    private int webformatHeight;
    private int webformatWidth;
    private int imageWidth;
    private int id;
    private int user_id;
    private String pageURL;
    private int imageHeight;
    private String webformatURL;
    private String type;
    private int previewHeight;
    private String tags;
    private String user;
    private int imageSize;
    private int previewWidth;
    private String userImageURL;
    private String previewURL;

    public Wallpaper to() {
        return new Wallpaper(null, getLargeImageURL(), getPreviewURL(),
                "Photo by " + getUser() + " on Pixabay", getPageURL(), null);
    }

    public String getLargeImageURL() { return largeImageURL;}

    public void setLargeImageURL(String largeImageURL) { this.largeImageURL = largeImageURL;}

    public int getWebformatHeight() { return webformatHeight;}

    public void setWebformatHeight(int webformatHeight) { this.webformatHeight = webformatHeight;}

    public int getWebformatWidth() { return webformatWidth;}

    public void setWebformatWidth(int webformatWidth) { this.webformatWidth = webformatWidth;}

    public int getImageWidth() { return imageWidth;}

    public void setImageWidth(int imageWidth) { this.imageWidth = imageWidth;}

    public int getId() { return id;}

    public void setId(int id) { this.id = id;}

    public int getUser_id() { return user_id;}

    public void setUser_id(int user_id) { this.user_id = user_id;}

    public String getPageURL() { return pageURL;}

    public void setPageURL(String pageURL) { this.pageURL = pageURL;}

    public int getImageHeight() { return imageHeight;}

    public void setImageHeight(int imageHeight) { this.imageHeight = imageHeight;}

    public String getWebformatURL() { return webformatURL;}

    public void setWebformatURL(String webformatURL) { this.webformatURL = webformatURL;}

    public String getType() { return type;}

    public void setType(String type) { this.type = type;}

    public int getPreviewHeight() { return previewHeight;}

    public void setPreviewHeight(int previewHeight) { this.previewHeight = previewHeight;}

    public String getTags() { return tags;}

    public void setTags(String tags) { this.tags = tags;}

    public String getUser() { return user;}

    public void setUser(String user) { this.user = user;}

    public int getImageSize() { return imageSize;}

    public void setImageSize(int imageSize) { this.imageSize = imageSize;}

    public int getPreviewWidth() { return previewWidth;}

    public void setPreviewWidth(int previewWidth) { this.previewWidth = previewWidth;}

    public String getUserImageURL() { return userImageURL;}

    public void setUserImageURL(String userImageURL) { this.userImageURL = userImageURL;}

    public String getPreviewURL() { return previewURL;}

    public void setPreviewURL(String previewURL) { this.previewURL = previewURL;}
}
