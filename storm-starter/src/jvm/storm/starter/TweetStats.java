/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package storm.starter;

import java.util.Arrays;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import storm.starter.bolt.ContinentFilter;
import storm.starter.bolt.GlobalAggregator;
import storm.starter.bolt.HashTagFilter;
import storm.starter.bolt.PartialAggregator;
import storm.starter.bolt.StopWordFilter;
import storm.starter.bolt.WordSplitter;
import storm.starter.spout.ContinentSpout;
import storm.starter.spout.HashTagSpout;
import storm.starter.spout.TwitterSampleSpout;
import storm.starter.util.TopologyConstants;

public class TweetStats {        
	


	private static void hashStrings(String[] hashTags) {
		for(int i=0;i<hashTags.length;i++){
			hashTags[i]="#"+hashTags[i];
		}
	}

	public static void main(String[] args) {
        String consumerKey = args[0]; 
        String consumerSecret = args[1]; 
        String accessToken = args[2]; 
        String accessTokenSecret = args[3];
        String fileName = args[4];
        //arg 5 should have a dummy keyword that does not return any result - necessitated by the restriction on Twitter's API which requires a keyword to be present if we want 
        //only geo-tagged tweets.
        String[] arguments = args.clone();
        String[] hashTags = Arrays.copyOfRange(arguments, 6, arguments.length);
        hashStrings(hashTags);
        TopologyBuilder builder = new TopologyBuilder();
        
        builder.setSpout(TopologyConstants.TWEET_STREAM, new TwitterSampleSpout(consumerKey, consumerSecret,
                                accessToken, accessTokenSecret,new String[]{args[5]},true));
        builder.setSpout(TopologyConstants.HASHTAG_SPOUT, new HashTagSpout(hashTags));
        builder.setSpout(TopologyConstants.CONTINENT_SPOUT, new ContinentSpout());
        builder.setBolt(TopologyConstants.CONTINENT_FILTER, new ContinentFilter())
                .allGrouping(TopologyConstants.CONTINENT_SPOUT).shuffleGrouping(TopologyConstants.TWEET_STREAM);
        builder.setBolt(TopologyConstants.HASHTAG_FILTER, new HashTagFilter())
        .allGrouping(TopologyConstants.HASHTAG_SPOUT).shuffleGrouping(TopologyConstants.CONTINENT_FILTER);        
         builder.setBolt(TopologyConstants.WORD_SPLITTER, new WordSplitter()).shuffleGrouping(TopologyConstants.HASHTAG_FILTER);
         builder.setBolt(TopologyConstants.STOP_WORD_FILTER, new StopWordFilter()).shuffleGrouping(TopologyConstants.WORD_SPLITTER);
         builder.setBolt(TopologyConstants.PARTIAL_AGGREGATOR, new PartialAggregator()).fieldsGrouping(TopologyConstants.STOP_WORD_FILTER, new Fields("word"));
         builder.setBolt(TopologyConstants.GLOBAL_AGGREGATOR, new GlobalAggregator()).globalGrouping(TopologyConstants.PARTIAL_AGGREGATOR);
        Config conf = new Config();
        conf.put(TopologyConstants.FILE_NAME_STR, fileName);
        
        LocalCluster cluster = new LocalCluster();
        
        cluster.submitTopology("test", conf, builder.createTopology());
        
        Utils.sleep(60000);
        cluster.shutdown();
        System.exit(0);
    }
}
