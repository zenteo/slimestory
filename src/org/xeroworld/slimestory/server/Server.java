package org.xeroworld.slimestory.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.xeroworld.slimestory.net.Connection;
import org.xeroworld.slimestory.net.PacketManager;

public class Server implements Runnable {
	private PacketManager packetManager;
	private ServerSocket socket;
	private Thread acceptThread;
	private boolean running;
	private ArrayList<Connection> clients;
	
	public Server(int port) {
		try {
			socket = new ServerSocket(port);
			clients = new ArrayList<Connection>();
			packetManager = new PacketManager();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void mainloop() {
		long lastTick = System.nanoTime();
		while (running) {
			long now = System.nanoTime();
			double deltaTime = (now-lastTick) / 1.0E9;
			lastTick = now;
			try {
				for (int i = clients.size()-1; i >= 0; i--) {
					Connection c = clients.get(i);
					c.tick(deltaTime);
					if ((c.getStatus()&Connection.STATUS_CONNECTED) != 0) {
						clients.remove(i);
						System.out.println("We lost an connection.");
					}
				}
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handleClient(Socket client) {
		clients.add(new Connection(client, packetManager));
		System.out.println("We got an connection.");
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
	
	public static void main(String[] args) {
		Server server = new Server(5340);
		server.start();
	}
}
