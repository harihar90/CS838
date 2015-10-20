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

public class ContinentFilter extends BaseRichBolt {
	
	OutputCollector _collector;
	Queue<Tuple> tweetQueue;
	List<String> continents;
	Long generation;
	String mapping="AD,EU|AE,AS|AF,AS|AG,NA|AI,NA|AL,EU|AM,AS|AN,NA|AO,AF|AP,AS|AQ,AN|AR,SA|AS,OC|AT,EU|AU,OC|AW,NA|AX,EU|AZ,AS|BA,EU|BB,NA|BD,AS|BE,EU|BF,AF|BG,EU|BH,AS|BI,AF|BJ,AF|BL,NA|BM,NA|BN,AS|BO,SA|BR,SA|BS,NA|BT,AS|BV,AN|BW,AF|BY,EU|BZ,NA|CA,NA|CC,AS|CD,AF|CF,AF|CG,AF|CH,EU|CI,AF|CK,OC|CL,SA|CM,AF|CN,AS|CO,SA|CR,NA|CU,NA|CV,AF|CX,AS|CY,AS|CZ,EU|DE,EU|DJ,AF|DK,EU|DM,NA|DO,NA|DZ,AF|EC,SA|EE,EU|EG,AF|EH,AF|ER,AF|ES,EU|ET,AF|EU,EU|FI,EU|FJ,OC|FK,SA|FM,OC|FO,EU|FR,EU|FX,EU|GA,AF|GB,EU|GD,NA|GE,AS|GF,SA|GG,EU|GH,AF|GI,EU|GL,NA|GM,AF|GN,AF|GP,NA|GQ,AF|GR,EU|GS,AN|GT,NA|GU,OC|GW,AF|GY,SA|HK,AS|HM,AN|HN,NA|HR,EU|HT,NA|HU,EU|ID,AS|IE,EU|IL,AS|IM,EU|IN,AS|IO,AS|IQ,AS|IR,AS|IS,EU|IT,EU|JE,EU|JM,NA|JO,AS|JP,AS|KE,AF|KG,AS|KH,AS|KI,OC|KM,AF|KN,NA|KP,AS|KR,AS|KW,AS|KY,NA|KZ,AS|LA,AS|LB,AS|LC,NA|LI,EU|LK,AS|LR,AF|LS,AF|LT,EU|LU,EU|LV,EU|LY,AF|MA,AF|MC,EU|MD,EU|ME,EU|MF,NA|MG,AF|MH,OC|MK,EU|ML,AF|MM,AS|MN,AS|MO,AS|MP,OC|MQ,NA|MR,AF|MS,NA|MT,EU|MU,AF|MV,AS|MW,AF|MX,NA|MY,AS|MZ,AF|NA,AF|NC,OC|NE,AF|NF,OC|NG,AF|NI,NA|NL,EU|NO,EU|NP,AS|NR,OC|NU,OC|NZ,OC|O1,--|OM,AS|PA,NA|PE,SA|PF,OC|PG,OC|PH,AS|PK,AS|PL,EU|PM,NA|PN,OC|PR,NA|PS,AS|PT,EU|PW,OC|PY,SA|QA,AS|RE,AF|RO,EU|RS,EU|RU,EU|RW,AF|SA,AS|SB,OC|SC,AF|SD,AF|SE,EU|SG,AS|SH,AF|SI,EU|SJ,EU|SK,EU|SL,AF|SM,EU|SN,AF|SO,AF|SR,SA|ST,AF|SV,NA|SY,AS|SZ,AF|TC,NA|TD,AF|TF,AN|TG,AF|TH,AS|TJ,AS|TK,OC|TL,AS|TM,AS|TN,AF|TO,OC|TR,EU|TT,NA|TV,OC|TW,AS|TZ,AF|UA,EU|UG,AF|UM,OC|US,NA|UY,SA|UZ,AS|VA,EU|VC,NA|VE,SA|VG,NA|VI,NA|VN,AS|VU,OC|WF,OC|WS,OC|YE,AS|YT,AF|ZA,AF|ZM,AF|ZW,AF";	
	Map<String,String> countryMap= new HashMap<String,String>();
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		String[] maps=mapping.split("\\|");
		for(String map:maps){
			String[] countryToContinent=map.split(",");
			countryMap.put(countryToContinent[0], countryToContinent[1]);
		}
		
		this._collector = collector;
		tweetQueue=new LinkedList<Tuple>();
	}

	@Override
	public void execute(Tuple input) {
		if (input.getSourceComponent().equals(TopologyConstants.CONTINENT_SPOUT)) {
			if(continents==null){
				continents= new ArrayList<String>();
			}
			continents.clear();
			generation=(Long)input.getValue(0);
			for (Object value : input.getValues().subList(1,input.getValues().size())) {
				for (String word : (List<String>) value)
					continents.add(word);
			}
			
			while(!tweetQueue.isEmpty()){
				process(tweetQueue.poll());
			}
			
		}
		if(input.getSourceComponent().equals(TopologyConstants.TWEET_STREAM)){
			if(continents==null)
				tweetQueue.add(input);
			else
				process(input);
		}

	}

	private void process(Tuple input) {
		Status tweet = (Status) input.getValue(0);
		if(tweet!=null && tweet.getPlace()!=null)
			if(continents.contains(countryMap.get(tweet.getPlace().getCountryCode())))
				_collector.emit(new Values(generation,input.getValue(0)));
		
		
		
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
