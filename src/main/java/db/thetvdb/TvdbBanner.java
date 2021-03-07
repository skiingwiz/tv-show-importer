package db.thetvdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TvdbBanner {
    private String id;

    @JsonProperty("fileName")
    private String bannerPath;

    @JsonProperty("keyType")
    private String bannerType;

    @JsonProperty("subKey")
    private String bannerType2;

    @JsonProperty("languageId")//TODO, this is an int now
    private String language;

    private String bannerName;

    public String getId() {
        return id;
    }

    public String getBannerPath() {
        return bannerPath;
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

    public String getBannerName() {
        return bannerName;
    }
}
