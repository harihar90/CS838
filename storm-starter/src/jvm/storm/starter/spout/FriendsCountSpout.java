package storm.starter.spout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import storm.starter.util.Sampler;
import storm.starter.util.TopologyConstants;

public class FriendsCountSpout extends BaseRichSpout {
	List<Integer> friendsCount;
	SpoutOutputCollector _collector;
	private int sampleSize;
	

	public FriendsCountSpout(Integer[] friendsCount) {
		super();
		this.friendsCount = Arrays.asList(friendsCount);
		this.sampleSize = TopologyConstants.FRIENDSCOUNT_SAMPLESIZE;

	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		_collector = collector;
	}

	@Override
	public void nextTuple() {
		_collector.emit(new Values(new Date().getTime(), new Sampler<Integer>().knuthSample(friendsCount, sampleSize)));
		Utils.sleep(TopologyConstants.INTERVAL);
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

		List<String> fieldList = new ArrayList<String>();
		fieldList.add("generation");
		fieldList.add("friendsCount");
		/*
		 * for (int i = 0; i < sampleSize; i++) fieldList.add("hashTag" + i);
		 */	
		declarer.declare(new Fields(fieldList));

	}

}
