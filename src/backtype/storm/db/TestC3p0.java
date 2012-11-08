package backtype.storm.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import backtype.storm.topology.FeedTopology;


public class TestC3p0 {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public TestC3p0() {
		
	}
	
	static {
		PropertyConfigurator.configure("resources/log4j.properties");
	}
	
	public static void main(String[] args) throws IOException, SQLException {
//		PropertyConfigurator.configure("resources/log4j.properties");
		Logger logger = Logger.getLogger(TestC3p0.class.getName());
	    String sql = "SELECT followships.followed_id from followships";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = MySqlConnection.getConnection();
		logger.info("aaa");
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				System.out.println("粉丝IDS：" + rs.getInt("followed_id"));
			}
			rs.close();
			pstmt.close();

		} catch (Exception e) {
			logger.info(e);
			System.out.println(e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
