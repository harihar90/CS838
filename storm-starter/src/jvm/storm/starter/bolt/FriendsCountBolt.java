package storm.starter.bolt;

import java.util.ArrayList;
import java.util.HashMap;
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

public class FriendsCountBolt extends BaseRichBolt {
	
	OutputCollector _collector;
	Queue<Tuple> tweetQueue;
	List<Integer> friendsCount;
	Long generation;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this._collector = collector;
		tweetQueue=new LinkedList<Tuple>();
	}

	@Override
	public void execute(Tuple input) {
		if (input.getSourceComponent().equals(TopologyConstants.FRIENDSCOUNT_SPOUT)) {
			if(friendsCount== null){
				friendsCount= new ArrayList<Integer>();
			}
			friendsCount.clear();
			generation=(Long)input.getValue(0);
			for (Object value : input.getValues().subList(1,input.getValues().size())) {
				for (Integer count : (List<Integer>) value)
					friendsCount.add(count);
			}
			
			while(!tweetQueue.isEmpty()){
				process(tweetQueue.poll());
			}
			
		}
		if(input.getSourceComponent().equals(TopologyConstants.TWEET_STREAM)){
			System.out.println("Expected Count " + friendsCount);
			System.out.println("Actual Count " + ((Status)input.getValue(0)).getUser().getFriendsCount());
			if(friendsCount==null)
				tweetQueue.add(input);
			else
				process(input);
		}

	}

	private void process(Tuple input) {
		Status tweet = (Status) input.getValue(0);
		int friends = 0;
		boolean accept = false;
		if(tweet!=null && tweet.getUser() != null)
		{
			friends = tweet.getUser().getFriendsCount();
			for(Integer i : friendsCount)
			{
				if(friends < i)
				{
					accept = true;
					break;
				}
			}
		}
		if(accept)
		{
			_collector.emit(new Values(generation,input.getValue(0)));
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
