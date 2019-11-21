package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Banner {
    private String id;

    @JsonProperty("fileName")
    private String bannerPath;

    @JsonProperty("thumbnail")
    private String thumbnailPath;

    @JsonProperty("keyType")
    private String bannerType;

    @JsonProperty("subKey")
    private String bannerType2;

    @JsonProperty("languageId")//TODO, this is an int now
    private String language;

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

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public String getBannerType() {
        return bannerType;
    }

    public String getBannerType2() {
        return bannerType2;
    }

    public String getLanguage() {
        return language;
    }

    public String getSeason() {
        //If the banner type is "season" or "seasonwide" then the subkey is the season
        return (bannerType != null && bannerType.startsWith("season")) ? bannerType2 : null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
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

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "Banner [id=" + id + ", bannerPath=" + bannerPath + ", thumbnailPath=" + thumbnailPath + ", bannerType="
                + bannerType + ", bannerType2=" + bannerType2 + ", language=" + language + "]";
    }
}
