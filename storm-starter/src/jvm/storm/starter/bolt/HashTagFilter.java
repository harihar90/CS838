package storm.starter.bolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import storm.starter.util.TopologyConstants;
import twitter4j.Status;

public class HashTagFilter extends BaseRichBolt {
	OutputCollector _collector;
	Queue<Tuple> tweetQueue;

	Map<Long, Long> timeStampMap = new HashMap<Long, Long>();
	Queue<List<String>> hashTagQueue = new LinkedList<List<String>>();
	Map<Long, List<String>> hashTagMap = new HashMap<Long, List<String>>();

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this._collector = collector;
		tweetQueue = new LinkedList<Tuple>();
	}

	@Override
	public void execute(Tuple input) {
		if (input.getSourceComponent().equals(TopologyConstants.HASHTAG_SPOUT)) {
			List<String> hashTags = new ArrayList<String>();

			for (Object value : input.getValues()) {
				hashTags.add((String) value);
			}
			hashTagQueue.add(hashTags);

			int len = tweetQueue.size();
			for (int i = 0; i < len; i++) {
				Tuple previous = tweetQueue.poll();
				filterTuple(previous);
			}

		}
		if (input.getSourceComponent().equals(TopologyConstants.TWEET_STREAM)) {
			filterTuple(input);
		}

	}

	private void filterTuple(Tuple input) {
		if (!hashTagMap.containsKey((Long) input.getValue(0))) {
			if (hashTagQueue.isEmpty())
				tweetQueue.add(input);
			else {
				hashTagMap.put((Long) input.getValue(0), hashTagQueue.poll());
				process(input, hashTagMap.get((Long) input.getValue(0)));

			}
		} else
			process(input, hashTagMap.get((Long) input.getValue(0)));
	}

	private void process(Tuple input, List<String> hashTags) {
		Status tweet = (Status) input.getValue(1);
		for (String tag : hashTags) {
			if (tweet.getText().contains(tag))
				_collector.emit(new Values(input.getValues()));
		}

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("generation");
		fieldList.add("tweet");
		/*
		 * for (int i = 0; i < sampleSize; i++) fieldList.add("hashTag" + i);
		 */
		declarer.declare(new Fields(fieldList));

	}

}
