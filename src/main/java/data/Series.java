package data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Series {
    private String id;

    private Map<String, String> ids = new HashMap<>();

    @JsonIgnore
    private String language;

    @JsonProperty("seriesName")
    private String name;

    @JsonIgnore
    private String originalName;

    @JsonProperty("overview")
    private String description;

    @JsonProperty("firstAired")
    private String firstAirDate;

    @JsonProperty("siteRating")
    private String starRating;

    private String network;

    private String[] genre;

    @JsonProperty("rating")
    private String contentRating;

    @JsonIgnore
    private String[] actors;

    @JsonIgnore
    private Collection<Image> images;

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getDescription() {
        return description;
    }

    public String getFirstAirDate() {
        return firstAirDate;
    }

    public Collection<Image> getImages() {
        return images;
    }

    public Map<String, String> getIds() {
        return ids;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFirstAirDate(String firstAirDate) {
        this.firstAirDate = firstAirDate;
    }

    public void setActors(String[] actors) {
        this.actors = actors;
    }

    public void setContentRating(String rating) {
        this.contentRating = rating;
    }

    public void setGenre(String[] genre) {
        this.genre = genre;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public void setStarRating(String rating) {
        this.starRating = rating;
    }

    public String getStarRating() {
        return starRating;
    }

    public String getNetwork() {
        return network;
    }

    public String[] getGenre() {
        return genre;
    }

    public String getContentRating() {
        return contentRating;
    }

    public String[] getActors() {
        return actors;
    }

    public void setImages(Collection<Image> images) {
        this.images = images;
    }

    public void setIds(Map<String, String> ids) {
        this.ids = ids;
    }

    public void addId(String name, String id) {
        ids.put(name, id);
    }
}
