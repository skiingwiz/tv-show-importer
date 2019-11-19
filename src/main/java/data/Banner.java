package data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Banner {
    private String id;

    @JsonProperty("fileName")
    private String bannerPath;

    @JsonIgnore
    private String vignettePath;

    @JsonProperty("thumbnail")
    private String thumbnailPath;

    @JsonProperty("keyType")
    private String bannerType;

    @JsonProperty("subKey")
    private String bannerType2;

    @JsonIgnore
    private String colors;

    @JsonProperty("languageId")//TODO, this is an int now
    private String language;

    @JsonIgnore
    private String season;

    public String getId() {
        return id;
    }

    public String getBannerName() {
        int pos = bannerPath.lastIndexOf("/");
        return bannerPath.substring(pos == -1 ? 0 : pos);
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public String getVignettePath() {
        return vignettePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getBannerType() {
        return bannerType;
    }

    public String getBannerType2() {
        return bannerType2;
    }

    public String getColors() {
        return colors;
    }

    public String getLanguage() {
        return language;
    }

    public String getSeason() {
        return season;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    public void setVignettePath(String vignettePath) {
        this.vignettePath = vignettePath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public void setBannerType(String bannerType) {
        this.bannerType = bannerType;
    }

    public void setBannerType2(String bannerType2) {
        this.bannerType2 = bannerType2;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}
