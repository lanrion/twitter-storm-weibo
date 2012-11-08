package backtype.storm.db;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * Describe class DBManager here. example:
 * 初始化：DBManager.getInstance().init("74.208.78.5",27017,200);
 * 之后，每次通过下面的代码获取数据库对象 DBManager.getInstance().getDB();
 */
public class MongoManager {

//	public static final String DB_NAME = "weibo_development";
	private MongoManager() {
	
	}
//	static {
//		PropertyConfigurator.configure("/resources/log4j.properties");
//		PropertyConfigurator.
//	}
	
	public static Logger logger = Logger.getLogger(MongoManager.class);

	public static MongoManager getInstance() {
		return InnerHolder.INSTANCE;
	}

	private static class InnerHolder {
		static final MongoManager INSTANCE = new MongoManager();
	}

	public DB getDB() throws IOException {
		return mongo.getDB(Configs.getProp("mongodb.name"));
	}

	private Mongo mongo;

	@SuppressWarnings("deprecation")
	public void init(int poolSize) {
		try{
			String url = Configs.getProp("mongodb.servers");
			ArrayList<ServerAddress> addr = new ArrayList<ServerAddress>();
			for (String s: url.split(",")) {
			    addr.add(new ServerAddress(s));
			}
			logger.info("mongodb IP:"+ addr);
			System.setProperty("MONGO.POOLSIZE", String.valueOf(poolSize));
			if (mongo == null) {
				mongo = new Mongo(addr);
				MongoOptions options = mongo.getMongoOptions();
				options.autoConnectRetry = true;
				options.connectionsPerHost = poolSize;
			}
			mongo.slaveOk();
			
		}catch(Exception se) {
			logger.info("connection mongodb Exception:"+ se);
		}

	}
	
//	public void inits()throws IOException {
//		System.setProperty("MONGO.POOLSIZE", "2000");
//		if (mongo == null){
//			mongo = new Mongo("125.64.96.70:27017");
//			MongoOptions options = mongo.getMongoOptions();
//			options.autoConnectRetry = true;
//			options.connectionsPerHost = 200;
//		}
//	}
	
	
	public static void main(String args[]) throws IOException {
//		String url = Configs.getProp("mongodb.servers");
//		ArrayList<ServerAddress> addr = new ArrayList<ServerAddress>();
//		for (String s: url.split(",")) {
//		    addr.add(new ServerAddress(s));
//		}
//		Iterator ito = addr.iterator();
//		while(ito.hasNext()) {
//			System.out.println(ito.next());
//		}
//		Mongo mongoSafe = new Mongo(new MongoURI("mongodb://127.0.0.1:31000,127.0.0.1:31001,127.0.0.1:31002"));
//		System.out.println(mongoSafe.getAllAddress());
		System.out.println("开始连接：");
		MongoManager.getInstance().init(Integer.parseInt(Configs.getProp("mongodb.poolsize")));

		DB db = MongoManager.getInstance().getDB();

		DBCollection tweets = db.getCollection("tweets");

		DBObject cursor = tweets.findOne();

		System.out.println("开始打印：");
		String sender_id = cursor.get("sender_id").toString();
		System.out.println("sender_id:" + sender_id);
		
		
	}
	
	
}