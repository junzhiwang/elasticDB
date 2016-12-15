package com.pengcheng;

import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: pxiong
 * Date: Jun 16, 2010
 * Time: 2:54:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class testReplica {
    public static void main(String[] args) throws InterruptedException {
        try {
            String server = "vader-1-vm5";
            String userName = "alfred";
            String password = "slave";
            String url = "jdbc:mysql://" + server + "/tpcw";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            java.sql.Connection con = (java.sql.Connection) DriverManager.getConnection(url, userName, password);
            con.setAutoCommit(true);
            Statement s = con.createStatement();
            s.executeUpdate("insert into address values" +
                    "(280001,'a','a','a','a','a',1)");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Thread.sleep(1000);

        try {
            String server = "vader-3-vm5";
            String userName = "alfred";
            String password = "slave";
            String url = "jdbc:mysql://" + server + "/tpcw";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            java.sql.Connection con = (java.sql.Connection) DriverManager.getConnection(url, userName, password);
            con.setAutoCommit(true);
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("select * from address where addr_id=280001");
            while (rs.next()) {
            String r = rs.getString(1);
            System.out.println("s = " + r);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Thread.sleep(1000);
        try {
            String server = "vader-1-vm5";
            String userName = "alfred";
            String password = "slave";
            String url = "jdbc:mysql://" + server + "/tpcw";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            java.sql.Connection con = (java.sql.Connection) DriverManager.getConnection(url, userName, password);
            con.setAutoCommit(true);
            Statement s = con.createStatement();
            s.executeUpdate("delete from address where addr_id=280001");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Thread.sleep(1000);
        try {
            String server = "vader-3-vm5";
            String userName = "alfred";
            String password = "slave";
            String url = "jdbc:mysql://" + server + "/tpcw";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            java.sql.Connection con = (java.sql.Connection) DriverManager.getConnection(url, userName, password);
            con.setAutoCommit(true);
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("select * from address where addr_id=280001");
            while (rs.next()) {
            String r = rs.getString(1);
            System.out.println("s = " + r);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }



    }
}
