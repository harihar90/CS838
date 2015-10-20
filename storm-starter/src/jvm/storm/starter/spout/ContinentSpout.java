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

public class ContinentSpout extends BaseRichSpout {

	SpoutOutputCollector collector;
	List<String> continents = Arrays.asList("NA");
	

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;

	}

	@Override
	public void nextTuple() {
		collector.emit(
				new Values(new Date().getTime(),new Sampler<String>().knuthSample(continents, TopologyConstants.CONTINENT_SAMPLESIZE)));
		Utils.sleep(TopologyConstants.INTERVAL);
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("generation");
		fieldList.add("continents");
		/*
		 * for (int i = 0; i < TopologyConstants.CONTINENT_SAMPLESIZE; i++)
		 * fieldList.add("continent" + i);
		 */	
		declarer.declare(new Fields(fieldList));

	}

}