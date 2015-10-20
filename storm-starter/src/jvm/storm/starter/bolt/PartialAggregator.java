package storm.starter.bolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import storm.starter.util.TopologyConstants;
import storm.starter.util.TupleHelpers;

public class PartialAggregator extends BaseRichBolt {
	OutputCollector _collector;
	Map<Long, Map<String, Long>> wordCountByGen = new HashMap<Long, Map<String, Long>>();

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this._collector = collector;

	}

	@Override
	public void execute(Tuple input) {
		if (TupleHelpers.isTickTuple(input)) {
			for (Map.Entry<Long, Map<String, Long>> genEntry : wordCountByGen.entrySet()) {
				Long gen = genEntry.getKey();

				for (Map.Entry<String, Long> countEntry : genEntry.getValue().entrySet()) {
					String word = countEntry.getKey();
					Long count = countEntry.getValue();
					_collector.emit(new Values(gen, word, count));
				}
				
			}
			wordCountByGen.clear();
		} else {
			Long generation = (Long) input.getValue(0);
			String word = (String) input.getValue(1);
			if (!wordCountByGen.containsKey(generation))
				wordCountByGen.put(generation, new HashMap<String, Long>());
			if (!wordCountByGen.get(generation).containsKey(word))
				wordCountByGen.get(generation).put(word, 1L);
			else
				wordCountByGen.get(generation).put(word, wordCountByGen.get(generation).get(word) + 1);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("generation");
		fieldList.add("word");
		fieldList.add("count");
		/*
		 * for (int i = 0; i < sampleSize; i++) fieldList.add("hashTag" + i);
		 */
		declarer.declare(new Fields(fieldList));

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, TopologyConstants.PARTIAL_AGG_EMIT_TIME);
		;
		return conf;
	}

}
