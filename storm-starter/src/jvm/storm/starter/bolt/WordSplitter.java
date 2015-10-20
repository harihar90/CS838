package storm.starter.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import twitter4j.Status;

public class WordSplitter extends BaseRichBolt{

	OutputCollector _collector;
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this._collector=collector;
		
	}

	@Override
	public void execute(Tuple input) {
		Status tweet = (Status)input.getValue(1);
		String[] words = tweet.getText().split("\\s+");
		for(String word:words){
			_collector.emit(new Values(input.getValue(0),word));
		}
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("generation");
		fieldList.add("word");
		/*
		 * for (int i = 0; i < sampleSize; i++) fieldList.add("hashTag" + i);
		 */	
		declarer.declare(new Fields(fieldList));
	}

}
