package db.thetvdb;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.Episode;
import data.Image;
import data.Image.Type;
import data.Series;
import db.DatabaseInitializationException;
import db.DatabaseProcessingException;
import db.SeriesCache;
import db.TvInfoSource;
import util.io.UriUtils;


public class TheTvDbDatabase implements TvInfoSource {
    private static final String BASE_URL = "https://api.thetvdb.com";
    private static final String API_KEY = "3D94331EFB6E696C";
    private static final String BANNER_URL = "https://artworks.thetvdb.com/banners/";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper om;

    private String lang;
    private String token;

    final private SeriesCache cache;

    public TheTvDbDatabase(String lang) {
        this.lang = lang;
        cache = new SeriesCache("thetvdb");
    }

    public TheTvDbDatabase(String lang, File cacheDir) {
        this.lang = lang;
        cache = new SeriesCache("thetvdb", cacheDir);
    }

    @Override
    public void clearCaches() {
        cache.clear();
    }

    @Override
    public void initialize() throws DatabaseInitializationException {
        om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        cache.initialize();
        login();
    }

    private void login() {
        ObjectNode login = om.createObjectNode()
                .put("apikey", API_KEY);

        try {
            HttpPost post = new HttpPost(BASE_URL + "/login");
            post.setEntity(new StringEntity(login.toString(), ContentType.APPLICATION_JSON));

            try(CloseableHttpResponse resp = HttpClients.createDefault().execute(post)) {
                if(UriUtils.isSuccess(resp)) {
                    JsonNode root = om.readTree(resp.getEntity().getContent());
                    token = root.get("token").asText();
                } else {
                    log.error("TVdb returned a login failure: {} - {}",
                            resp.getStatusLine().getStatusCode(), resp.getStatusLine().getReasonPhrase());
                }
            }
        } catch(IOException e) {
            log.error("Failure to login to TVdb", e);
        }
    }

    private List<Image> getBannerInfo(Series series) throws DatabaseProcessingException {
        //TODO this should be a config value
        List<String> keys = Arrays.asList(
                "season",
                "seasonwide",
                "poster",
                "fanart",
                "series/graphical");

        List<Image> banners = new ArrayList<>();

        for(String k : keys) {
            String[] split = k.split("/", 2);
            String key = split[0];
            String subkey = split.length > 1 ? split[1] : null;

            banners.addAll(getBannerInfo(series, key, subkey));
        }

        return banners;
    }

    private List<Image> getBannerInfo(Series series, String key, String subkey) throws DatabaseProcessingException {
        URI uri;
        try {

            URIBuilder builder = new URIBuilder(BASE_URL)
                    .setPathSegments("series", series.getId(), "images", "query")
                    .addParameter("keyType", key);
            if(subkey != null) {
                builder.addParameter("subKey", subkey);
            }
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new DatabaseProcessingException("Failed to construct Banner URI", e);
        }


        try(CloseableHttpResponse resp = get(uri)) {
            List<Image> retVal = new ArrayList<>();
            List<TvdbBanner> list = getJsonData(resp, new TypeReference<List<TvdbBanner>>() {});
            for(TvdbBanner b : list) {
                Image.Type type = getBannerType(b);
                if(type != null) {
                    Image image = new Image();
                    image.setId(b.getId());
                    image.setUrl(BANNER_URL + b.getBannerPath());
                    image.setType(type);
                    if(b.getBannerType().startsWith("season")) {
                        image.setSeason(b.getBannerType2());
                    }
                    image.setName(Image.makeName(b.getBannerPath()));
                    retVal.add(image);
                }
            }

            return retVal;
        } catch(IOException e) {
            throw new DatabaseProcessingException("Failed to retrieve Banner information", e);
        }
    }

    private Type getBannerType(TvdbBanner b) {
        if(b.getBannerType().equalsIgnoreCase("season") ||
                b.getBannerType().equalsIgnoreCase("seasonwide") ||
                b.getBannerType().equalsIgnoreCase("poster")) {
            return Image.Type.Poster;
        } else if(b.getBannerType().equalsIgnoreCase("fanart")) {
            return Image.Type.Background;
        } else if(b.getBannerType().equalsIgnoreCase("series") &&
                b.getBannerType2().equalsIgnoreCase("graphical")) {
            return Image.Type.Banner;
        } else {
            log.debug("Skipping banner because it is of undesired type. Banner: {} Type: {} Type2: {}",
                    b.getBannerName(), b.getBannerType(), b.getBannerType2());
            return null;
        }
    }

    @Override
    public Episode lookup(String series, int season, int episode) throws DatabaseProcessingException {
        String seriesId = getSeriesId(series);
        Episode e = null;
        if(seriesId == null) {
            System.err.println("Lookup failed because series id couldn't be found.  Series Name: " + series);
        } else {
            Series s = lookup(seriesId);
            s.setImages(getBannerInfo(s));
            s.setName(series);
            e = getEpisodeInfo(seriesId, season, episode);
            e.setSeries(s);
        }

        return e;
    }

    public Series lookup(String seriesId) throws DatabaseProcessingException {
        Series series = cache.getSeries(seriesId);
        if(series == null) {
            try(CloseableHttpResponse resp = get(BASE_URL + "/series/" + seriesId)) {
                JsonNode root = om.readTree(resp.getEntity().getContent());
                if(root.has("data")) {
                    JsonNode data = root.get("data");
                    series = om.convertValue(data, Series.class);
                    series.setOriginalName(series.getName());
                    series.addId("tvdb", series.getId());
                    cache.storeSeries(series);
                } else {
                    //TODO error
                }
            } catch(IOException e) {
                //TODO error
                e.printStackTrace();
            }
        }

        return series;
    }

    private Episode getEpisodeInfo(String seriesId, int seasonNum, int episodeNum) throws DatabaseProcessingException {
        URI uri;
        try {
            uri = new URIBuilder(BASE_URL)
                .setPathSegments("series", seriesId, "episodes", "query")
                .addParameter("airedSeason", Integer.toString(seasonNum))
                .addParameter("airedEpisode", Integer.toString(episodeNum))
                .build();
        } catch (URISyntaxException e) {
            log.error("Failed to construct episode lookup URI", e);
            throw new DatabaseProcessingException("Failed to construct episode lookup URI", e);
        }

        try(CloseableHttpResponse resp = get(uri)) {
            Episode[] episodeList = getJsonData(resp, Episode[].class);
            switch(episodeList.length) {
            case 0:
                log.info("No episode returned for search {}: {}x{}", seriesId, seasonNum, episodeNum);
                return null;
                //break;
            default:
                log.warn("More than one series returned for search {}: {}x{}", seriesId, seasonNum, episodeNum);
                //fall through
            case 1:
                return episodeList[0];
            }
        } catch(IOException e) {
            log.error("Failure trying to retrieve episode information for {} - {}x{}",
                    seriesId, seasonNum, episodeNum, e);
            throw new DatabaseProcessingException("Failue trying to retrieve episode information", e);
        }
    }

    private String getSeriesId(String seriesName) {
        String id = cache.getSeriesId(seriesName);
        if(id == null) {
            id = lookupSeriesId(seriesName);
            if(id != null) {
                cache.storeSeriesId(seriesName, id);
            }
        }

        return id;
    }

    private String lookupSeriesId(String seriesName) {
        URI uri;
        try {
            uri = new URIBuilder(BASE_URL)
                    .setPathSegments("search", "series")
                    .addParameter("name", seriesName)
                    .build();
        } catch (URISyntaxException e) {
            log.error("Failed to construct series lookup URI", e);
            //TODO check if this is an acceptable return
            return null;
        }

        try(CloseableHttpResponse resp = get(uri)) {
            Series[] seriesList = getJsonData(resp, Series[].class);

            switch(seriesList.length) {
            case 0:
                log.info("No series returned for search {}", seriesName);
                return null;
                //break;
            default:
                for(Series s : seriesList) {
                    if(seriesName.equals(s.getName())) {
                        return s.getId();
                    }
                }

                log.warn("More than one series returned for search {}.  Using first result.", seriesName);
                //Fall through
            case 1:
                return seriesList[0].getId();
            }
        } catch(IOException e) {
            log.error("Failure trying to retrieve series information for {}", seriesName, e);
            return null;
        }
    }

    private <T> T getJsonData(CloseableHttpResponse resp, Class<T> c) throws IOException {
        JsonNode data = getJsonData(resp);
        return om.readerFor(c).readValue(data);
    }

    private <T> T getJsonData(CloseableHttpResponse resp, TypeReference<T> tr) throws IOException {
        JsonNode data = getJsonData(resp);
        return om.readerFor(tr).readValue(data);
    }

    private JsonNode getJsonData(CloseableHttpResponse resp) throws IOException {
        JsonNode root = om.readTree(resp.getEntity().getContent());
        if(root.has("data")) {
            return root.get("data");
        }

        log.error("HTTP Response did not contain the expected 'data' element");
        return null;
    }

    private CloseableHttpResponse get(URI uri) throws IOException {
        return get(new HttpGet(uri));
    }

    private CloseableHttpResponse get(String uri) throws IOException {
        return get(new HttpGet(uri));
    }

    private CloseableHttpResponse get(HttpGet get) throws IOException {
        checkToken();
        get.addHeader("Authorization", "Bearer " + token);
        get.addHeader("Accept-Language", lang);

        CloseableHttpResponse resp = HttpClients.createDefault().execute(get);
        if(UriUtils.isSuccess(resp)) {
            return resp;
        } else {
            log.error("Received unsuccessful HTTP response {} - {}",
                    resp.getStatusLine().getStatusCode(),
                    resp.getStatusLine().getReasonPhrase());
            throw new IOException("Received unsuccessful HTTP response");
        }
    }

    private void checkToken() {
        if(token == null) {
            throw new IllegalStateException("No authentication token.  Login before invoking actions");
        }
    }
}
