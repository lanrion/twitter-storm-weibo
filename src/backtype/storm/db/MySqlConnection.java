package backtype.storm.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MySqlConnection {
	private MySqlConnection() {
		
	}
//	static {
//		PropertyConfigurator.configure("/resources/log4j.properties");
//	}
	public static Logger logger = Logger.getLogger(MySqlConnection.class);
	
	private static ComboPooledDataSource ds = null;  
	
//    static {
//        try {
//        	Properties prop = new Properties();
//        	InputStream in = new FileInputStream("resources/c3p0.properties");
//        	prop.load(in);
////        	prop.load(MySqlConnection.class.getResourceAsStream("/resources/c3p0.properties"));
//        	System.out.println(prop.getProperty("c3p0.jdbcUrl"));
//            // Logger log = Logger.getLogger("com.mchange"); // 日志
//              // log.setLevel(Level.WARNING);
//            ds = new ComboPooledDataSource();
//            // 设置JDBC的Driver类
//              ds.setDriverClass(prop.getProperty("c3p0.driverClass"));  // 参数由 Config 类根据配置文件读取
//              // 设置JDBC的URL
//            ds.setJdbcUrl(prop.getProperty("c3p0.jdbcUrl"));
//            // 设置数据库的登录用户名
//              ds.setUser(prop.getProperty("c3p0.user"));
//            // 设置数据库的登录用户密码
//              ds.setPassword(prop.getProperty("c3p0.password"));
//            // 设置连接池的最大连接数
//              ds.setMaxPoolSize(Integer.parseInt(prop.getProperty("c3p0.maxPoolSize")));
//            // 设置连接池的最小连接数
//              ds.setMinPoolSize(Integer.parseInt(prop.getProperty("c3p0.minPoolSize")));
//              
//              ds.setTestConnectionOnCheckin(true);
////              ds.setIdleConnectionTestPeriod(Integer.parseInt(prop.getProperty("")));
////              ds.setMaxIdleTime(Integer.parseInt(prop.getProperty("")));
//              
//        } catch (Exception e) {
//        	logger.info(e);
//        }
//	}
	
	static {
		logger.info("start init c3p0!!" );
		ds = new ComboPooledDataSource();
	}

	public static synchronized Connection getConnection() {
		Connection con = null;
		long cu1;
		try {
			logger.info("start connection c3p0 mysql");
			cu1 = System.currentTimeMillis();
			con = ds.getConnection();
			logger.info("the time is :" + (cu1 - System.currentTimeMillis()));
			logger.info("success connection c3p0 mysql");
			
		} catch (SQLException e1) {
			logger.info(e1);
		}
		return con;
	}
}
