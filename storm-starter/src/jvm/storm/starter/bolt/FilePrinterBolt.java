package storm.starter.bolt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import static java.nio.file.StandardOpenOption.*;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import storm.starter.util.TopologyConstants;
import twitter4j.Status;

public class FilePrinterBolt extends BaseRichBolt {
	OutputCollector _collector;
	OutputStream file;

	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		String fileName = (String) conf.get(TopologyConstants.FILE_NAME_STR)+"_"+context.getThisTaskId();;
		Path p = Paths.get(fileName);
		try {
			file = new BufferedOutputStream(Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING));	
			_collector = collector;
		} catch (IOException x) {
			System.err.println(x);
		}

	}

	@Override
	public void execute(Tuple tuple) {
		if (file != null) {
			try {
				//file.write((((Status)tuple.getValue(0)).getText().replace('\n', ' ')+"\n").getBytes());
				file.write((((Status)tuple.getValue(0)).toString().replace('\n', ' ')+"\n").getBytes());
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("word"));
	}

	@Override
	public void cleanup() {
		try {
			if (file != null)
				file.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
