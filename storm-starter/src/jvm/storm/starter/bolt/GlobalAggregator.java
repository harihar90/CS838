package storm.starter.bolt;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import storm.starter.util.TopologyConstants;
import storm.starter.util.TupleHelpers;

public class GlobalAggregator extends BaseRichBolt {

	Map<Long, Map<String, Long>> wordCountByGen = new HashMap<Long, Map<String, Long>>();
	Map<Long, Path> fileMap = new HashMap<Long, Path>();

	String fileName;

	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		fileName = (String) conf.get(TopologyConstants.FILE_NAME_STR);
		;

	}

	@Override
	public void execute(Tuple input) {
		if (TupleHelpers.isTickTuple(input)) {
			for (Map.Entry<Long, Map<String, Long>> genEntry : wordCountByGen.entrySet()) {
				Long gen = genEntry.getKey();
				OutputStream file;
				try {
					file = new BufferedOutputStream(Files.newOutputStream(fileMap.get(gen), CREATE, TRUNCATE_EXISTING));
					Map<String, Long> wordCount = sortByComparator(genEntry.getValue());
					Set<String> words = wordCount.keySet();
					for (String word : words) {
						file.write((word + "\n").getBytes());
					}
					file.flush();
					file.close();
					
				} catch (IOException e) {
					System.err.println(e);
				}
				
			}
			if(wordCountByGen.size()>3){
				ArrayList<Long> generations= new ArrayList<Long>(wordCountByGen.keySet());
				Collections.sort(generations);
				for(int i=0;i<generations.size()-5;i++){
					wordCountByGen.remove(generations.get(i));
				}
				
			}
		} else {
			Long generation = (Long) input.getValue(0);
			String word = (String) input.getValue(1);
			if (!wordCountByGen.containsKey(generation)) {
				wordCountByGen.put(generation, new HashMap<String, Long>());

				Path p = Paths.get(fileName + generation);

				fileMap.put(generation, p);

			}
			if (!wordCountByGen.get(generation).containsKey(word))
				wordCountByGen.get(generation).put(word, 1L);
			else
				wordCountByGen.get(generation).put(word, wordCountByGen.get(generation).get(word) + 1);
		}

	}

	private static Map<String, Long> sortByComparator(Map<String, Long> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Long>> list = new LinkedList<Map.Entry<String, Long>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
			public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
		for (Iterator<Map.Entry<String, Long>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Long> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<String, Object>();
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, TopologyConstants.GLOBAL_AGG_EMIT_TIME);
		;
		return conf;
	}

}
