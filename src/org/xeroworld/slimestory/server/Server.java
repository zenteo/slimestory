package org.xeroworld.slimestory.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.xeroworld.slimestory.net.Connection;
import org.xeroworld.slimestory.net.PacketHandler;
import org.xeroworld.slimestory.net.PacketManager;
import org.xeroworld.slimestory.net.packet.MessagePacket;
import org.xeroworld.slimestory.net.packet.Packet;

public abstract class Server implements Runnable, PacketHandler {
	private PacketManager packetManager;
	private ServerSocket socket;
	private Thread acceptThread;
	private boolean running;
	private ArrayList<Connection> connections;
	private int tickDelay = 0;

	public abstract void tick(Connection connection, double deltaTime);
	public abstract void handleConnection(Connection connection);
	public abstract void handleDisconnection(Connection connection);
	
	public Server(int port) {
		try {
			socket = new ServerSocket(port);
			connections = new ArrayList<Connection>();
			packetManager = new PacketManager();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Connection> getConnections() {
		return connections;
	}

	public void setConnections(ArrayList<Connection> connections) {
		this.connections = connections;
	}
	
	public PacketManager getPacketManager() {
		return packetManager;
	}

	public void setPacketManager(PacketManager packetManager) {
		this.packetManager = packetManager;
	}
	
	public void mainloop() {
		long lastTick = System.nanoTime();
		while (running) {
			long now = System.nanoTime();
			double deltaTime = (now-lastTick) / 1.0E9;
			lastTick = now;
			try {
				for (int i = connections.size()-1; i >= 0; i--) {
					Connection c = connections.get(i);
					c.tick(deltaTime);
					tick(c, deltaTime);
					if (c.getStatus() != Connection.STATUS_CONNECTED) {
						connections.remove(i);
						System.out.println("We lost an connection.");
						handleDisconnection(c);
					}
				}
				Thread.sleep(tickDelay);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handleClient(Socket client) {
		Connection c = new Connection(client, packetManager);
		c.addPacketHandler(this);
		connections.add(c);
		System.out.println("We got an connection.");
		handleConnection(c);
	}
	
	@Override
	public void run() {
		while (running) {
			try {
				Socket client = socket.accept();
				handleClient(client);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void start() {
		running = true;
		if (acceptThread == null) {
			acceptThread = new Thread(this);
			acceptThread.start();
		}
		System.out.println("SlimeStory server started.");
		mainloop();
	}
	
	public void stop() {
		running = false;
		acceptThread = null;
	}
}
