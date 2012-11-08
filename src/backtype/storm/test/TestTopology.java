package backtype.storm.test;


import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.scheme.StringScheme;
import backtype.storm.spout.KestrelThriftSpouts;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;


public class TestTopology {
	public static Logger logger = Logger.getLogger(TestTopology.class);
    public static class FailEveryOther extends BaseRichBolt {
        
        OutputCollector _collector;
//        int i=0;
        
        @Override
        public void prepare(Map map, TopologyContext tc, OutputCollector collector) {
            _collector = collector;
        }

        @Override
        public void execute(Tuple tuple) {
//            i++;
//            if(i%2==0) {
//            	_collector.emit(tuple, new Values(tuple.getString(0) + " fail!!!"));
//            	logger.info("TestTopology begin failing...");
//                _collector.fail(tuple);
//                logger.info("TestTopology end failing...");
//            } else {
//            	_collector.emit(tuple, new Values(tuple.getString(0) + " ack!!!"));
        		_collector.emit(tuple, new Values(tuple.getString(0) + " ack!!!"));
            	logger.info("TestTopology begin acking...");
                _collector.ack(tuple);
                logger.info("TestTopology end acking...");
//            }
        }
        
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        	declarer.declare(new Fields("spout"));
        }
    }
    
    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        KestrelThriftSpouts spout = new KestrelThriftSpouts("125.64.96.77", 2229, "tweet_id", new StringScheme());
        builder.setSpout("spout", spout, 8);
        builder.setBolt("bolt", new FailEveryOther(), 12).shuffleGrouping("spout");
        
        Config conf = new Config();
        conf.setDebug(true);
        conf.setNumWorkers(3);
        conf.setMaxSpoutPending(5);
        
        if(args!=null && args.length > 0) {
        	conf.setNumAckers(1);
//        	conf.setMessageTimeoutSecs(120);
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        } else {
          LocalCluster cluster = new LocalCluster();
          cluster.submitTopology("test", conf, builder.createTopology());
        }
        
//        LocalCluster cluster = new LocalCluster();
//        Config conf = new Config();
//        cluster.submitTopology("test", conf, builder.createTopology());
        
        Thread.sleep(6000);
    }
}