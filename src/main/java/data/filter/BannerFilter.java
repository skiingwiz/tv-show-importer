package data.filter;

import data.Image;

public class BannerFilter implements Filter<Image> {
    private String lang;

    public BannerFilter(String lang) {
        this.lang = lang;
    }
    @Override
    public boolean test(Image item) {
        return item.getLanguage().equalsIgnoreCase(lang);
    }

}
