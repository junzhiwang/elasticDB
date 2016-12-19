package com.bittiger.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bittiger.client.ClientEmulator;
import com.bittiger.client.Utilities;

public class Executor extends Thread {

	Controller controller = null;
	EventQueue eventQueue = null;
	private ClientEmulator c;

	public Executor(ClientEmulator c) {
		this.c = c;
		this.controller = c.getController();
		this.eventQueue = c.getController().eventQueue;
	}

	private static transient final Logger LOG = LoggerFactory
			.getLogger(Executor.class);

	@Override
	public void run() {
		LOG.info("Executor starts......");
		while (true) {
			ActionType actionType = eventQueue.peek();
			long currTime = System.currentTimeMillis();
			if (currTime > c.getStartTime() + c.getTpcw().warmup
					+ c.getTpcw().mi) {
				return;
			}
			try {
				LOG.info(actionType + " request received");
				if (actionType == ActionType.AvailNotEnoughAddServer
						|| actionType == ActionType.BadPerformanceAddServer) {
					if (controller.candidateQueue.size() == 0) {
						LOG.info("CandidateQueue size is 0, skip adding server");
					} else {
						Server target = controller.candidateQueue.remove(0);
						Server source = controller.readQueue
								.get(controller.readQueue.size() - 1);
						Server master = controller.writeQueue;
						// make sure source ! = master
						if(source.equals(master)){
							LOG.error("source should not be equal to master");
							continue;
						}
						scaleOut(source.getIp(), target.getIp(), master.getIp());
						controller.addServer(target);
						LOG.info("kick in " + target.getIp() + " done ");
					}
				} else if (actionType == ActionType.GoodPerformanceRemoveServer) {
					if (controller.readQueue.size() == Utilities.minimumSlave) {
						LOG.info("Read queue size is " + Utilities.minimumSlave
								+ ", skip scale in");
					} else {
						Server server = controller.removeServer();
						scaleIn(server.getIp());
						LOG.info("Kick out server" + server.getIp() + " done ");
					}
				}
				LOG.info(actionType + " request done");
				eventQueue.get();
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}
	}

	public boolean scaleOut(String source, String target, String master)
			throws InterruptedException, IOException {
		// ssh root@source
		// "/home/ubuntu/elasticDB/scripts/scaleOut.sh source target master"
		// sb.append("ssh root@" + source
		// +" \"/home/ubuntu/elasticDB/scripts/scaleOut.sh " + source +" " +
		// target +" " + master +"\"");
		// LOG.info(sb.toString());
		ProcessBuilder pb = new ProcessBuilder("/bin/bash",
				"script/callScaleOut.sh", source, target, master);
		Process p = pb.start();
		LOG.info("Kick in " + target + " from " + source);
		BufferedReader is = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String line;
		while ((line = is.readLine()) != null)
			LOG.info(line);
		p.waitFor();
		return true;
	}

	public boolean scaleIn(String target) throws InterruptedException,
			IOException {
		ProcessBuilder pb = new ProcessBuilder("/bin/bash",
				"script/callScaleIn.sh", target);
		Process p = pb.start();
		LOG.info("Kick out " + target);
		BufferedReader is = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String line;
		while ((line = is.readLine()) != null)
			LOG.info(line);
		p.waitFor();
		return true;
	}
}
