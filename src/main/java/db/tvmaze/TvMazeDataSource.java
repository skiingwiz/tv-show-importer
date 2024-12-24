package db.tvmaze;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.Episode;
import data.Image;
import data.Series;
import db.DatabaseInitializationException;
import db.DatabaseProcessingException;
import db.SeriesCache;
import db.TvInfoSource;
import util.io.UriUtils;

public class TvMazeDataSource implements TvInfoSource {

    private static final String BASE_URL = "http://api.tvmaze.com";

    private final Logger log = LoggerFactory.getLogger(getClass());


    private final SeriesCache cache;
    private ObjectMapper om = new ObjectMapper();

    public TvMazeDataSource() {
        cache = new SeriesCache("tvmaze");
    }

    protected TvMazeDataSource(File cacheDir) throws DatabaseInitializationException {
        cache = new SeriesCache("tvmaze", cacheDir);
    }

    @Override
    public void clearCaches() {
        cache.clear();
    }

    @Override
    public Episode lookup(String seriesName, int seasonNum, int episodeNum) throws DatabaseProcessingException {
        Series series = getSeries(seriesName);
        series.setOriginalName(seriesName); //TODO why do I have this around?
        URI uri;
        try {
            uri = new URIBuilder(BASE_URL)
                .setPathSegments("shows", series.getId(), "episodebynumber")
                .addParameter("season", Integer.toString(seasonNum))
                .addParameter("number", Integer.toString(episodeNum))
                .build();
        } catch(URISyntaxException e) {
            //TODO error handling
            return null;
        }

        JsonNode json = getJson(uri);
        if(json.isNull()) {
            log.info("No episode returned for search {} {}x{}", seriesName, seasonNum, episodeNum);
            return null;
        }

        Episode e = new Episode();
        e.setSeries(series);
        e.setSeasonNum(seasonNum);
        e.setEpisodeNum(episodeNum);
        e.setEpisodeTitle(json.get("name").asText());
        //e.setRating();   //TODO Available?
        e.setDescription(stripHtml(json.get("summary").asText()));
        //e.setWriters(null);   TODO available?
        //e.setGuestStars(null); TODO  available?
        e.setFirstAirDate(makeDate(json.get("airstamp").asText()));

        return e;
    }

    private Series getSeries(String seriesName) throws DatabaseProcessingException {
        String id = cache.getSeriesId(seriesName);
        if(id != null) {
            Series s = cache.getSeries(id);
            if(s != null) {
                return s;
            }

        }

        return lookupSeries(seriesName);
    }

    private Series lookupSeries(String seriesName) throws DatabaseProcessingException {
        URI uri;
        try {
            uri = new URIBuilder(BASE_URL)
                .setPathSegments("singlesearch", "shows")
                .addParameter("q", seriesName)
                .addParameter("embed[]", "images")
                .addParameter("embed[]", "cast")
                .build();
        } catch(URISyntaxException e) {
            //TODO error handling
            return null;
        }

        JsonNode json = getJson(uri);

        if(json.isNull()) {
            log.info("No series returned for search {}", seriesName);
            return null;
        }

        Collection<Image> images = packImages(json.get("_embedded").get("images"));
        Series series = new Series();
        series.setName(json.get("name").asText());
        series.setImages(images);
        series.setDescription(stripHtml(json.get("summary").asText()));
        series.setFirstAirDate(json.get("premiered").asText());
        series.setId(json.get("id").asText());
        series.setLanguage(json.get("language").asText());
        series.setNetwork(json.get("network").get("name").asText());
        series.setGenre(packGenre(json.get("genres")));
        series.setStarRating(json.get("rating").get("average").asText());
        series.setActors(packActors(json.get("_embedded").get("cast")));

        series.setIds(packIds(json.get("externals")));

        cache.storeSeries(series);
        return series;
    }

    private Map<String, String> packIds(JsonNode json) {
        Map<String, String> retVal = new HashMap<>();

        for(Iterator<String> iter = json.fieldNames(); iter.hasNext(); ) {
            String key = iter.next();
            retVal.put(key, json.get(key).asText());
        }

        return retVal;
    }

    private String[] packActors(JsonNode json) {
        String[] retVal = null;
        if(json.isArray()) {
            int size = json.size();
            retVal = new String[size];
            for(int i = 0; i < size; i++) {
                JsonNode cast = json.get(i);
                retVal[i] = //Use String.join when we get away from the Java7 requirement
                        cast.get("person").get("name").asText()
                        + ":"
                        + cast.get("character").get("name").asText();

            }
        }
        return retVal;
    }

    private String[] packGenre(JsonNode json) {
        String[] retVal = null;
        if(json.isArray()) {
            int size = json.size();
            retVal = new String[size];
            for(int i = 0; i < size; i++) {
                retVal[i] = json.get(i).asText();
            }
        }

        return retVal;
    }

    private String stripHtml(String string) {
        return string.replaceAll("<[^>]*>", "");
    }

    private JsonNode getJson(URI uri) throws DatabaseProcessingException {
        try {
            CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet(uri));
            if(!UriUtils.isSuccess(resp)) {
                log.error("Received unsuccessful HTTP response {} - {}",
                        resp.getStatusLine().getStatusCode(),
                        resp.getStatusLine().getReasonPhrase());
                throw new DatabaseProcessingException("Received unsuccessful HTTP response");
            }
            return om.readTree(resp.getEntity().getContent());
        } catch (IOException e) {
            throw new DatabaseProcessingException("Can't read JSON response", e);
        }
    }

    private Collection<Image> packImages(JsonNode json) {
        Collection<Image> banners = new ArrayList<>();
        for(int i = 0; i < json.size(); i++) {
            JsonNode image = json.get(i);
            if("banner".equals(image.get("type").asText())) {
                Image b = new Image();
                b.setId(image.get("id").asText());

                Image.Type type = Image.Type.Other;
                if(image.get("type").isTextual()) {
                    String typeStr = image.get("type").asText();
                    switch(typeStr) {
                    case "poster":
                        type = Image.Type.Poster;
                        break;
                    case "background":
                        type = Image.Type.Background;
                        break;
                    case "banner":
                        type = Image.Type.Banner;
                        break;
                    default:
                        log.warn("Found unexpected image type {}", typeStr);
                        type = Image.Type.Other;
                    }
                }
                b.setType(type);
                b.setUrl(image.get("resolutions").get("original").get("url").asText());
                b.setMain(image.get("main").asBoolean());
                b.setName(Image.makeName(b.getUrl()));
                banners.add(b);
            }
        }

        return banners;
    }

    private Date makeDate(String str) throws DatabaseProcessingException {
        //      "2013-06-25T02:00:00+00:00"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            throw new DatabaseProcessingException("Could not parse given date: " + str, e);
        }
    }

    @Override
    public void initialize() throws DatabaseInitializationException {
        // TODO Auto-generated method stub

    }
}
