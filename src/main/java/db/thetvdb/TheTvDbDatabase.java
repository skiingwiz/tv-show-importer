package db.thetvdb;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import config.GlobalConfig;
import data.Banner;
import data.Episode;
import data.Series;
import db.DatabaseInitializationException;
import db.DatabaseProcessingException;


public class TheTvDbDatabase {
    private static final String BASE_URL = "https://api.thetvdb.com";
    private static final String API_KEY = "3D94331EFB6E696C";
    private static final String SERIES_ID_FILE = "series_ids.dat";
    private static final String BANNER_URL = "https://artworks.thetvdb.com/banners/";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper om;

    private Properties seriesIds;
    private String lang;
    private File cacheDir;
    private String token;

    public TheTvDbDatabase(String lang) {
        this.lang = lang;
        String cacheName = GlobalConfig.get().getString(GlobalConfig.CACHE_DIR);
        cacheDir = new File(cacheName);
    }

    public TheTvDbDatabase(String lang, File cacheDir) {
        this.lang = lang;
        this.cacheDir = cacheDir;
    }

    public void clearCaches() {
        if(cacheDir.exists()) {
            for(File f : cacheDir.listFiles()) {
                FileUtils.deleteQuietly(f);
            }
        }
    }

    public void initialize() throws DatabaseInitializationException {
        om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if(!cacheDir.exists()) {
            if(!cacheDir.mkdirs()) {
                throw new DatabaseInitializationException("Faile to create cache directory: " + cacheDir);
            }
        }

        login();
    }

    private void login() {
        ObjectNode login = om.createObjectNode()
                .put("apikey", API_KEY);

        try {
            HttpPost post = new HttpPost(BASE_URL + "/login");
            post.setEntity(new StringEntity(login.toString(), ContentType.APPLICATION_JSON));

            try(CloseableHttpResponse resp = HttpClients.createDefault().execute(post)) {
                if(isSuccess(resp)) {
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

    public List<Banner> getBannerInfo(Series series) throws DatabaseProcessingException {
        //TODO this should be a config value
        List<String> keys = Arrays.asList(
                "season",
                "seasonwide",
                "poster",
                "fanart",
                "series/graphical");

        List<Banner> banners = new ArrayList<>();

        for(String k : keys) {
            String[] split = k.split("/", 2);
            String key = split[0];
            String subkey = split.length > 1 ? split[1] : null;

            banners.addAll(getBannerInfo(series, key, subkey));
        }

        return banners;
    }

    private List<Banner> getBannerInfo(Series series, String key, String subkey) throws DatabaseProcessingException {
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
            return getJsonData(resp, new TypeReference<List<Banner>>() {});
        } catch(IOException e) {
            throw new DatabaseProcessingException("Failed to retrieve Banner information", e);
        }
    }


    public void downloadBanner(Banner banner, String dir) throws DatabaseProcessingException {
        String urlPath = BANNER_URL + banner.getBannerPath();
        URL bannerUrl;
        try {
            bannerUrl = new URL(urlPath);
        } catch (MalformedURLException e) {
            throw new DatabaseProcessingException("Could not retrieve series information.  Bad Url.  URL:" + urlPath.toString(), e);
        }

        File dirF = new File(dir);
        if(!dirF.exists() && !dirF.mkdirs()) {
            System.err.println("Unable to make directory for storing fanart.  Directory: " + dir);
        }

        StringBuffer filename = new StringBuffer(dir);
        if(!dir.endsWith(File.separator)) {
            filename.append(File.separator);
        }

        filename.append(banner.getBannerName());
        File fileD = new File(filename.toString());
        if(fileD.exists()) {
            log.debug("Skipping fanart file {} because it already exists.", fileD.getPath());
        } else {
            String imageFormat = GlobalConfig.get().getString(GlobalConfig.IMAGE_FORMAT);

            try {
                BufferedImage im = ImageIO.read(bannerUrl);
                ImageIO.write(im, imageFormat, new File(filename.toString()));
            } catch (IOException ioe) {
                throw new DatabaseProcessingException("Failed to download banner.  Error: " + ioe.getMessage(), ioe);
            }
        }
    }

    public Episode lookup(String series, int season, int episode) throws DatabaseProcessingException {
        String seriesId = getSeriesId(series);
        Episode e = null;
        if(seriesId == null) {
            System.err.println("Lookup failed because series id couldn't be found.  Series Name: " + series);
        } else {
            Series s = lookup(seriesId);
            s.setName(series);
            e = getEpisodeInfo(seriesId, season, episode);
            e.setSeries(s);
        }

        return e;
    }

    public Series lookup(String seriesId) throws DatabaseProcessingException {
        File seriesIdFile = new File(cacheDir,  seriesId + ".json");

        if(!seriesIdFile.exists()) {
            try(CloseableHttpResponse resp = get(BASE_URL + "/series/" + seriesId)) {
                JsonNode root = om.readTree(resp.getEntity().getContent());
                if(root.has("data")) {
                    JsonNode data = root.get("data");
                    om.writeValue(seriesIdFile, data);
                } else {
                    //TODO error
                }
            } catch(IOException e) {
                //TODO error
                e.printStackTrace();
            }
        }

        Series series;
        try {
            series = om.readValue(seriesIdFile, Series.class);
            series.setOriginalName(series.getName());
        } catch (IOException e) {
            log.error("Failed to parse series info JSON file {}", seriesIdFile, e);
            throw new DatabaseProcessingException("Failed to parse series file", e);
        }

        return series;
    }

    private static boolean isSuccess(CloseableHttpResponse resp) {
        int code = resp.getStatusLine().getStatusCode();
        return code >= 200 && code < 300;
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
        boolean writeUpdates = true;
        File seriesIdFile = new File(cacheDir, SERIES_ID_FILE);

        if(seriesIds == null) {
            seriesIds = new Properties();
            if(seriesIdFile.exists()) {
                log.debug("Loading Series ID cache from ", seriesIdFile.getAbsolutePath());

                try (InputStream in = FileUtils.openInputStream(seriesIdFile)){
                    seriesIds.load(in);
                } catch (IOException e) {
                    writeUpdates = false;
                    log.error("Could not load Series ID file {}.  Updates will not be written to disk.",
                            seriesIdFile.getAbsolutePath(), e);
                }

            } else {
                log.warn("Series ID cache file not found at {}", seriesIdFile.getAbsolutePath());
            }
        }

        String id = seriesIds.getProperty(seriesName);
        if(id == null) {
            id = lookupSeriesId(seriesName);
            if(id != null) {
                seriesIds.put(seriesName, id);

                if(writeUpdates) {
                    try {
                        seriesIds.store(new BufferedWriter(new FileWriter(seriesIdFile)), "");
                    } catch (IOException e) {
                        log.error("Could not save series id file {}.  Updates will not be written to disk.",
                                seriesIdFile.getAbsolutePath(), e);
                    }
                }
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
        if(isSuccess(resp)) {
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
