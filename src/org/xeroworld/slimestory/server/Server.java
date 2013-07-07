package org.xeroworld.slimestory.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
	private ServerSocket socket;
	private Thread acceptThread;
	private boolean running;
	
	public Server(int port) {
		try {
			socket = new ServerSocket(port);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void mainloop() {
		while (running) {
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handleClient(Socket client) {
		
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
