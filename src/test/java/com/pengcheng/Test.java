package com.pengcheng;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

import com.bittiger.querypool.QueryMetaData;

/**
 * Created by IntelliJ IDEA. User: pxiong Date: Jun 14, 2010 Time: 7:46:54 PM To
 * change this template use File | Settings | File Templates.
 */
public class Test {
	public static final String userName = "root";
	public static final String password = "123456";
	public static final String server = "192.168.56.102";
	public static final String url = "jdbc:mysql://" + server + "/tpcw";

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, ParseException,
			SQLException {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		DriverManager.setLoginTimeout(5);
		Connection con = DriverManager.getConnection(url, userName, password);
		con.setAutoCommit(true);
		Statement s = con.createStatement();
		for (int r = 1; r <= 13; r++) {
			String classname = "com.pengcheng.querypool.bq" + r;
			QueryMetaData query = (QueryMetaData) Class.forName(classname)
					.newInstance();
			String command = query.getQueryStr();
			long starttime = System.currentTimeMillis();
			ResultSet rs = s.executeQuery(command);
			long endtime = System.currentTimeMillis();
			rs.close();
			System.out.println(r + "," + (endtime - starttime) + "," + command);
		}
		for (int w = 1; w <= 5; w++) {
			String classname = "com.pengcheng.querypool.wq" + w;
			QueryMetaData query = (QueryMetaData) Class.forName(classname)
					.newInstance();
			String command = query.getQueryStr();
			long starttime = System.currentTimeMillis();
			s.executeUpdate(command);
			long endtime = System.currentTimeMillis();
			System.out.println(w + "," + (endtime - starttime) + "," + command);
		}
		s.close();
		con.close();

	}
}
