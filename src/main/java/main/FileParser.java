package main;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.Episode;
import data.Series;

public class FileParser {
    private static final String SEASON = "season";
    private static final String EPISODE = "episode";
    private static final String SHOW = "show";

    private static class PathPattern {
        private Pattern pattern;
        private Collection<String> groups;

        public PathPattern(String pattern, String... groups) {
            this.pattern = Pattern.compile(pattern);
            this.groups = Arrays.asList(groups);
        }

        public Matcher matcher(CharSequence string) {
            return pattern.matcher(string);
        }

        public boolean hasGroup(String name) {
            return groups.contains(name);
        }

        public Pattern getPattern() {
            return pattern;
        }
    }

    private static final PathPattern[] FILE_NAME_PATTERNS = new PathPattern[] {
        new PathPattern("(?<show>.*?)\\s*-\\s*(?<season>[0-9]?[0-9])(?<episode>[0-9][0-9]).*?", SHOW, SEASON, EPISODE),
        new PathPattern("(?<show>.*?)\\s*-\\s*[Ss](?<season>[0-9]?[0-9])[Ee](?<episode>[0-9][0-9]).*?", SHOW, SEASON, EPISODE),

        new PathPattern("(?i)season *(?<season>[0-9]{1,2})", SEASON),
        new PathPattern("(?i)episode *(?<episode>[0-9]{1,2})", SEASON),

        new PathPattern(".*[Ss](?<season>[0-9]?[0-9])[Xx _]?[Ee](?<episode>[0-9]?[0-9]).*", SEASON, EPISODE),
        new PathPattern(".*?(?<season>[0-9]?[0-9])[Xx](?<episode>[0-9]?[0-9]).*?", SEASON, EPISODE),
        new PathPattern(".*?(?<season>[0-9]?[0-9])(?<episode>[0-9][0-9]).*?", SEASON, EPISODE),
        };

    /**
     *
     * NB: The file passed in does <b>NOT</b> have to exist.
     *
     * @param file
     * @return
     */
    public static Episode parse(File file) {
        Episode retVal = new Episode();

        File curPath = file;
        boolean match = true;
        while(curPath != null && match) {
            match = false;

            for(PathPattern p : FILE_NAME_PATTERNS) {
                Matcher m = p.matcher(curPath.getName());
                if(m.matches()) {
                    match = true;

                    checkSeasonNum(retVal, p, m);
                    checkEpisodeNum(retVal, p, m);
                    checkShowName(retVal, p, m);
                    break;
                }
            }

            if(match) {
                curPath = curPath.getParentFile();
            }
        }

        //Check for show, special case where show name is the remaining folder
        if(retVal.getSeries() == null && curPath != null) {
            Series s = new Series();
            s.setName(curPath.getName());
            retVal.setSeries(s);
        }

        return retVal;
    }

    private static void checkSeasonNum(Episode result, PathPattern pattern, Matcher matcher) {
        Integer intVal = checkGroup(pattern, matcher, SEASON, result.getSeasonNum());
        if(intVal != null) {
            result.setSeasonNum(intVal);
        }
    }

    private static void checkEpisodeNum(Episode result, PathPattern pattern, Matcher matcher) {
        Integer intVal = checkGroup(pattern, matcher, EPISODE, result.getEpisodeNum());
        if(intVal != null) {
            result.setEpisodeNum(intVal);
        }
    }

    private static void checkShowName(Episode result, PathPattern pattern, Matcher matcher) {
        if(pattern.hasGroup(SHOW)) {
             String name = matcher.group(SHOW);
             Series series = result.getSeries();
             if(series == null) {
                 series = new Series();
                 result.setSeries(series);
             } else {
                 if(!name.equals(series.getName())) {
                     throw new InconsistentDataException("Show Name", name, series.getName());
                 }
             }

             series.setName(name);
        }
    }

    private static Integer checkGroup(PathPattern p, Matcher m, String groupName, Integer curValue) {
        Integer retVal = null;
        if(p.hasGroup(groupName)) {
            String val = m.group(groupName);
            retVal = Integer.valueOf(val);
            if(curValue != null) {
                if(retVal != curValue) {
                    throw new InconsistentDataException(groupName, retVal, curValue);
                }
            }
        }

        return retVal;
    }
}
