package data;

import java.io.File;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Episode {
//TODO what about multipart episodes?
	protected Series series;

	@JsonProperty("airedSeason")
	protected Integer seasonNum;

	@JsonProperty("airedEpisodeNumber")
	protected Integer episodeNum;

	@JsonProperty("episodeName")
	protected String episodeTitle;

	@JsonProperty("siteRating")
	private String rating;

	@JsonProperty("overview")
	private String description;

	@JsonProperty("writers")
	private String[] writers;

	@JsonProperty("guestStars")
	private String[] guestStars;

	@JsonProperty("firstAired")
	private Date firstAirDate;

	@JsonIgnore
	private String originalFile;

	public Series getSeries() {
		return series;
	}
	public Integer getSeasonNum() {
		return seasonNum;
	}
	public void setSeasonNum(int seasonNum) {
		this.seasonNum = seasonNum;
	}
	public Integer getEpisodeNum() {
		return episodeNum;
	}
	public void setEpisodeNum(int episodeNum) {
		this.episodeNum = episodeNum;
	}
	public String getEpisodeTitle() {
		return episodeTitle;
	}
	public void setEpisodeTitle(String episodeTitle) {
		this.episodeTitle = episodeTitle;
	}

	public String[] getWriters() {
		return writers;
	}
	public void setWriters(String[] writer) {
		this.writers = writer;
	}

	public String[] getGuestStars() {
		return guestStars;
	}

	public void setGuestStars(String[] guestStars) {
		this.guestStars = guestStars;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Show=").append(series == null ? "No Series" : series.getName()).append(",");
		sb.append("Season=").append(seasonNum).append(",");
		sb.append("Episode=").append(episodeNum);

		return sb.toString();
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getFirstAirDate() {
		return firstAirDate;
	}

	public void setFirstAirDate(Date date) {
		firstAirDate = date;
	}
	public void setSeries(Series s) {
		this.series = s;
	}
	public void setOriginalFile(File file) {
		originalFile = file.getAbsolutePath();
	}
	public String getOriginalFile() {
		return originalFile;
	}
}
