package main;

import config.GlobalConfig;
import data.Episode;
import db.TvInfoSource;
import db.TvInfoSourceFactory;
import util.TextFileWriter;

public class StringRun {

    public static void main(String[] args) throws Exception{

        GlobalConfig.parse(args);

        TvInfoSource source = TvInfoSourceFactory.getInstance(null);
        Episode e = source.lookup("The Big Bang Theory", 7, 3);

        StringBuilder sb = new StringBuilder();
        TextFileWriter out = new TextFileWriter(sb);
        new PropertiesFileWriter().writeFile(out, e);

        //Rated

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(sb);
    }

}
