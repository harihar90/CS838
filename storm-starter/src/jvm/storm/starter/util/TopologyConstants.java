package storm.starter.util;

public class TopologyConstants {
public static final long INTERVAL = 30000L;
public static final int HASHTAG_SAMPLESIZE = 5;
public static final int CONTINENT_SAMPLESIZE = 1;
public static final String HASHTAG_SPOUT = "HASHTAG_SPOUT";
public static final String TWEET_STREAM = "TWEET_STREAM";
public static final String CONTINENT_SPOUT = "CONTINENT_SPOUT";
public static final long PARTIAL_AGG_EMIT_TIME = 10; //seconds
public static final long GLOBAL_AGG_EMIT_TIME = 120;
public static final String CONTINENT_FILTER = "CONTINENT_FILTER";
public static final String HASHTAG_FILTER = "HASHTAG_FILTER";
public static final String WORD_SPLITTER = "WORD_SPLITTER";
public static final String STOP_WORD_FILTER = "STOP_WORD_FILTER";
public static final String PARTIAL_AGGREGATOR = "PARTIAL_AGGREGATOR";
public static final String GLOBAL_AGGREGATOR = "GLOBAL_AGGREGATOR";
public static String FILE_NAME_STR="output_file";

}
