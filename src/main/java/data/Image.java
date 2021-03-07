package data;

public class Image {
    public enum Type {
        Banner,
        Background,
        Poster,
        Other
    }

    private String id;
    private Type type;
    private String language;
    private String url;
    private boolean main;
    private String season;
    private String name;

    public static String makeName(String url) {
        int pos = url.lastIndexOf("/");
        return url.substring(pos == -1 ? 0 : pos);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

    public String getSeason() {
        return season;
    }

    public String getUrl() {
        return url;
    }

    public boolean isMain() {
        return main;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    @Override
    public String toString() {
        return "Banner [id=" + id + ", type=" + type + ", language=" + language + ", url=" + url + ", main=" + main
                + ", season=" + season + ", name=" + name + "]";
    }


}
