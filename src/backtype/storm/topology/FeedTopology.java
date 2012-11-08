package backtype.storm.topology;

//backtype.storm.spout.FeedTopology
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.db.Configs;
import backtype.storm.db.MongoManager;
import backtype.storm.db.MySqlConnection;
import backtype.storm.scheme.StringScheme;
import backtype.storm.spout.KestrelThriftSpouts;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class FeedTopology {
	public static Logger logger = Logger.getLogger(FeedTopology.class);
	public static class SaveFeeds extends BaseRichBolt {

		private static final long serialVersionUID = -2270861508148163214L;
		OutputCollector _collector;

		@Override
		public void prepare(Map map, TopologyContext tc, OutputCollector collector) {
			_collector = collector;
		}

		@Override
		public void execute(Tuple tuple) {
			ObjectId tweet_id = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			Connection 	con = null;
			logger.info("method execute is starting~");
			
			try {
				tweet_id = new ObjectId(tuple.getString(0));
				logger.info("Tweet_id is :" + tweet_id);
				MongoManager.getInstance().init(Integer.parseInt(Configs.getProp("mongodb.poolsize")));
				logger.info("connection mongodb successfully: "+ tweet_id);
				DB db = MongoManager.getInstance().getDB();
				logger.info("connection mongodb successfully: " + tweet_id);
				DBCollection tweets = db.getCollection("tweets");

				BasicDBObject query = new BasicDBObject();
				query.put("_id", tweet_id);
				DBCursor cursor = tweets.find(query);

				if (!cursor.hasNext()) {
					_collector.emit(tuple, new Values("tweet_feed_deleted!! " + tweet_id));
					_collector.ack(tuple);// 标记处理完。
					logger.info("tweet_feed_deleted: " + tweet_id);
					return;
				}

				String sender_id = null;
				Object created_at = null;
				if (cursor.hasNext()) {
					DBObject dbo = cursor.next();
					sender_id = dbo.get("sender_id").toString();
					created_at = dbo.get("created_at");
				}

				// 初始化mysql
				con = MySqlConnection.getConnection();
				List<Integer> followed_ids = new ArrayList<Integer>();

				pstmt = con.prepareStatement(Configs.getProp("tweet.sql"));
				pstmt.setString(1, sender_id);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					followed_ids.add(rs.getInt("user_id"));
				}

				if (followed_ids.size() == 0) {
					_collector.emit(tuple,	new Values("no_feeds: " + tweet_id));
					_collector.ack(tuple);// 标记处理完。
					logger.info("no_tweet_feeds: " + tweet_id);
					return;
				}

				DBCollection feed = db.getCollection("feeds");
				Iterator<Integer> iter = followed_ids.iterator();
				while (iter.hasNext()) {
					int receiver_id = (Integer) iter.next();
					BasicDBObject doc = new BasicDBObject();
					doc.put("favorite", false);
					doc.put("receiver_id", receiver_id);
					doc.put("tweet_id", tweet_id);
					doc.put("sender_id", sender_id);
					doc.put("created_at", created_at);
					feed.save(doc);
				}

				_collector.emit(tuple, new Values(tweet_id));
				_collector.ack(tuple);
				logger.info("tweet_feed_done: " + tweet_id);

			} catch (Exception e1) {
				logger.info(" Tweet execue errors："+ e1);
				_collector.emit(tuple, new Values(tweet_id));
				_collector.fail(tuple);
				logger.info("tweet_feed_failly: " + tweet_id);
			} finally {
				try{
				 logger.info("start close mysql ");	
					rs.close();
					pstmt.close();
					con.close();
				 logger.info("close successfully");	
				}catch(Exception se){
				 logger.info("close mysql error:" + se);
				}
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("tweet"));
		}
	}

	public static void main(String[] args) {
		try {
			TopologyBuilder builder = new TopologyBuilder();
			KestrelThriftSpouts spout = new KestrelThriftSpouts(Configs.getKestrels(), 2229, "tweet_id", new StringScheme());
			builder.setSpout("tweet", spout, 10).setDebug(true);
			builder.setBolt("tweet_feed", new SaveFeeds(), 20).shuffleGrouping("tweet");
			
			 Config conf = new Config();
			 conf.setDebug(true);
			 conf.setMaxSpoutPending(10);
			 conf.setNumWorkers(5);
//			 conf.setMaxTaskParallelism(30);
			 conf.setMessageTimeoutSecs(120);
			 conf.setNumAckers(1);
			 logger.info("submiting bolt....");
			 StormSubmitter.submitTopology("TweetFeed", conf, builder.createTopology());
			 Thread.sleep(60000);
			 logger.info("submit success!");
			 
			
		} catch (Exception se) {
			logger.info("main方法："+ se);
		}

//		LocalCluster cluster = new LocalCluster();
//		Config conf = new Config();
//		conf.setDebug(true);
//		cluster.submitTopology("test", conf, builder.createTopology());
//		Utils.sleep(10000);
//		cluster.killTopology("test");
//		cluster.shutdown();
	}
}