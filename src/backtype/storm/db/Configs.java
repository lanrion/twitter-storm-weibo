package backtype.storm.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Configs {
	
	//cnfig.properties
	public static String getProp(String propName) throws IOException {
		Properties prop = new Properties();
//    	InputStream in = new FileInputStream("resources/config.properties");
//    	prop.load(in);
    	prop.load(Configs.class.getResourceAsStream("/resources/config.properties"));
    	return prop.getProperty(propName);
	}
	
	public static List<String> getKestrels() throws IOException {
		String url = Configs.getProp("kestrel.servers");
		ArrayList<String> kestrels = new ArrayList<String>();
		for (String s: url.split(",")) {
			kestrels.add(new String(s));
		}
		return kestrels;
	}
	
	
	public static void main(String args []) throws IOException {
		
		System.out.println("Mongodb的Server1:" + Configs.getProp("tweet.sql"));
		Iterator itor = Configs.getKestrels().iterator();
		while(itor.hasNext()) {
			System.out.println(itor.next());
			
		}
	}

}
