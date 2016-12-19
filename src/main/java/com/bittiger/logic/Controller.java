package com.bittiger.logic;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bittiger.client.ClientEmulator;
import com.bittiger.client.Utilities;
import com.bittiger.logic.rules.AvailabilityNotEnoughRule;
import com.bittiger.logic.rules.ScaleInRule;
import com.bittiger.logic.rules.ScaleOutRule;

import java.sql.Connection;

public class Controller extends Thread {
	List<Server> readQueue = new ArrayList<Server>();
	Server writeQueue = null;
	List<Server> candidateQueue = new ArrayList<Server>();
	int nextReadServer = 0;
	Map<Server, Integer> serverCount = new HashMap<Server, Integer>();
	private ClientEmulator c;
	private static transient final Logger LOG = LoggerFactory
			.getLogger(Controller.class);
	EventQueue eventQueue = null;

	public Controller(ClientEmulator ce) {
		this.c = ce;
		writeQueue = new Server(ce.getTpcw().writeQueue[0]);
		for (int i = 0; i < ce.getTpcw().readQueue.length; i++) {
			readQueue.add(new Server(ce.getTpcw().readQueue[i]));
		}
		for (int i = 0; i < ce.getTpcw().candidateQueue.length; i++) {
			candidateQueue.add(new Server(ce.getTpcw().candidateQueue[i]));
		}
		this.eventQueue = new EventQueue();
	}

	public synchronized Pair<Server, Connection> getNextWriteConnection() {
		Server server = null;
		Connection connection = null;
		int tryTime = 0;
		while (connection == null && tryTime++ < Utilities.retryTimes) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				// DriverManager.setLoginTimeout(5);
				server = writeQueue;
				connection = (Connection) DriverManager.getConnection(
						Utilities.getUrl(server), c.getTpcw().username,
						c.getTpcw().password);
				connection.setAutoCommit(true);
			} catch (Exception e) {
				LOG.error(e.toString());
			}
		}
		return new ImmutablePair<Server, Connection>(server, connection);
	}

	public synchronized Pair<Server, Connection> getNextReadConnection() {
		Server server = null;
		Connection connection = null;
		while (connection == null) {
			nextReadServer = (nextReadServer + 1) % readQueue.size();
			server = readQueue.get(nextReadServer);
			int tryTime = 0;
			while (connection == null && tryTime++ < Utilities.retryTimes) {
				try {
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					connection = (Connection) DriverManager.getConnection(
							Utilities.getUrl(server), c.getTpcw().username,
							c.getTpcw().password);
					connection.setAutoCommit(true);
				} catch (Exception e) {
					LOG.error(e.toString());
				}
			}
			if (connection == null) {
				LOG.error(server.getIp() + " is down. ");
				readQueue.remove(server);
				/**
				 * Create a rules engine and register the business rule
				 */
				RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine()
						.build();
				AvailabilityNotEnoughRule availabilityRule = new AvailabilityNotEnoughRule();
				availabilityRule.setInput(this);
				rulesEngine.registerRule(availabilityRule);
				rulesEngine.fireRules();
			} else {
				if (!serverCount.containsKey(server)) {
					serverCount.put(server, 1);
				} else {
					serverCount.put(server, serverCount.get(server) + 1);
				}
				return new ImmutablePair<Server, Connection>(server, connection);
			}
		}
		return null;
	}

	public synchronized void returnBack(Server server) {
		serverCount.put(server, serverCount.get(server) - 1);
		notifyAll();
	}

	public synchronized void addServer(Server server) {
		readQueue.add(server);
	}

	public synchronized Server removeServer() {
		Server server = readQueue.remove(readQueue.size() - 1);
		while (serverCount.get(server) > 0) {
			LOG.info("wait for requests to " + server.getIp() + " to finish");
			try {
				wait();
			} catch (InterruptedException e) {
				LOG.error(e.getMessage());
			}
		}
		candidateQueue.add(server);
		return server;
	}

	public void run() {
		LOG.info("Controller starts......");
		while (true) {
			long currTime = System.currentTimeMillis();
			if (currTime > c.getStartTime() + c.getTpcw().warmup
					+ c.getTpcw().mi + c.getTpcw().warmdown) {
				this.eventQueue.put(ActionType.NoOp);
				return;
			}
			try {
				Thread.sleep(c.getTpcw().interval);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String perf = c.getMonitor().readPerformance();
			/**
			 * Create a rules engine and register the business rule
			 */
			RulesEngine rulesEngine = RulesEngineBuilder.aNewRulesEngine()
					.build();
			ScaleOutRule scaleOutRule = new ScaleOutRule();
			scaleOutRule.setInput(this, perf);
			ScaleInRule scaleInRule = new ScaleInRule();
			scaleInRule.setInput(this, perf);
			rulesEngine.registerRule(scaleOutRule);
			rulesEngine.registerRule(scaleInRule);
			rulesEngine.fireRules();
		}
	}

	public synchronized List<Server> getReadQueue() {
		return readQueue;
	}

	public synchronized EventQueue getEventQueue() {
		return eventQueue;
	}

}
