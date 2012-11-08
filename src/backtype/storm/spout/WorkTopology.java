package backtype.storm.spout;

//backtype.storm.spout.WorkTopology
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.db.Configs;
import backtype.storm.db.MongoManager;
import backtype.storm.db.MySqlConnection;
import backtype.storm.scheme.StringScheme;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/*
 * 1，根据worktweetID找到该tweet所属的组id
 * 2，再找到所有该组的用户，去除发送者ID
 * 3，再添加到Wkfeed
 * 工作组微博： work_wktweets
 * 工作组feed: work_wkfeeds
 * 工作组：work_groupcpies
 */

public class WorkTopology {
	public static class SaveWorks extends BaseRichBolt {
		private static final long serialVersionUID = -3254049946802887239L;
		
		OutputCollector _collector;

		@Override
		public void prepare(Map map, TopologyContext tc,
				OutputCollector collector) {
					_collector = collector;
		}

		@Override
		public void execute(Tuple tuple) {
			ObjectId tweet_id = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			Connection 	con = null;
		 
			try {
				tweet_id = new ObjectId(tuple.getString(0));
				MongoManager.getInstance().init(Integer.parseInt(Configs.getProp("mongodb.poolsize")));

				DB db = MongoManager.getInstance().getDB();;
				DBCollection work_wktweets = db.getCollection("work_wktweets");
				BasicDBObject query = new BasicDBObject();
				query.put("_id", tweet_id);
				DBCursor cursor = work_wktweets.find(query);

				if (!cursor.hasNext()) {
					_collector.emit(tuple, new Values("work_tweet_delete: "	+ tweet_id));
					_collector.ack(tuple);// 标记处理完。
					return;
				}

				String sender_id = null;
				Object created_at = null;
				String group_id = null;
				Object istop = null;
				if (cursor.hasNext()) {
					DBObject dbo = cursor.next();
					sender_id = dbo.get("sender_id").toString();
					created_at = dbo.get("created_at");
					group_id = dbo.get("group_id").toString();
					istop = dbo.get("istop");
				}
				
				List<Integer> worker_ids = new ArrayList<Integer>();
				con = MySqlConnection.getConnection();
				pstmt = con.prepareStatement(Configs.getProp("work.sql"));
				pstmt.setString(1, group_id);
				pstmt.setString(2, sender_id);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					 worker_ids.add(rs.getInt("user_id"));
				}

				if (worker_ids.size() == 0) {
					_collector.emit(tuple, new Values("no_workfeeds: "	+ tweet_id));
					_collector.ack(tuple);
					return;
				}

				DBCollection feed = db.getCollection("work_wkfeeds");
				Iterator<Integer> iter = worker_ids.iterator();
				while (iter.hasNext()) {
					int receiver_id = (Integer) iter.next();
					BasicDBObject doc = new BasicDBObject();
					doc.put("receiver_id", receiver_id);
					doc.put("tweet_id", tweet_id);
					doc.put("sender_id", sender_id);
					doc.put("created_at", created_at);
					doc.put("group_id", group_id);
					doc.put("istop", istop);
					doc.put("isread", false);
					feed.save(doc);
				}
				_collector.emit(tuple, new Values("work_feed_done: " + tweet_id));
				_collector.ack(tuple);

			} catch (Exception e) {
				_collector.emit(tuple, new Values("work_feed_failly: " + tweet_id));
				_collector.fail(tuple);
				e.printStackTrace();
			} finally {
				try{
					rs.close();
					pstmt.close();
					con.close();
				}catch(Exception se){
					se.printStackTrace();
				}
			}
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("work"));
		}
	}

	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();
		KestrelThriftSpout spout = new KestrelThriftSpout(Configs.getKestrels(), 2229, "work_id", new StringScheme());
		builder.setSpout("work", spout, 5);
		builder.setBolt("feed", new SaveWorks(), 5).shuffleGrouping("work");

		Config conf = new Config();
		conf.setDebug(true);
		conf.setMaxTaskParallelism(3);
		conf.setNumWorkers(20);
		conf.setMaxSpoutPending(5000);
		StormSubmitter.submitTopology("WorkFeed", conf,	builder.createTopology());
		Thread.sleep(30000);

		// LocalCluster cluster = new LocalCluster();
		// Config conf = new Config();
		// conf.setDebug(true);
		// cluster.submitTopology("test", conf, builder.createTopology());
		// Utils.sleep(10000);
		// cluster.killTopology("test");
		// cluster.shutdown();
	}
}